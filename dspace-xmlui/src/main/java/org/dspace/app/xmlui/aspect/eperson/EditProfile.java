/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.eperson;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Button;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Field;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Select;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.content.Collection;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.I18nUtil;
import org.dspace.core.LogManager;
import org.dspace.eperson.Group;
import org.dspace.eperson.Subscribe;
import org.xml.sax.SAXException;

/**
 * Display a form that allows the user to edit their profile.
 * There are two cases in which this can be used: 1) when an
 * existing user is attempting to edit their own profile, and
 * 2) when a new user is registering for the first time.
 *
 * There are several parameters this transformer accepts:
 *
 * email - The email address of the user registering for the first time.
 *
 * registering - A boolean value to indicate whether the user is registering for the first time.
 *
 * retryInformation - A boolean value to indicate whether there was an error with the user's profile.
 *
 * retryPassword - A boolean value to indicate whether there was an error with the user's password.
 *
 * allowSetPassword - A boolean value to indicate whether the user is allowed to set their own password.
 *
 * @author Scott Phillips
 */
public class EditProfile extends AbstractDSpaceTransformer
{
    private static Logger log = Logger.getLogger(EditProfile.class);

    /** Language string used: */
    private static final Message T_title_create =
        message("xmlui.EPerson.EditProfile.title_create");

    private static final Message T_title_update =
        message("xmlui.EPerson.EditProfile.title_update");

    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");

    private static final Message T_trail_new_registration =
        message("xmlui.EPerson.trail_new_registration");
    
    private static final Message T_trail_update =
        message("xmlui.EPerson.EditProfile.trail_update");
    
    private static final Message T_head_create =
        message("xmlui.EPerson.EditProfile.head_create");
    
    private static final Message T_head_update =
        message("xmlui.EPerson.EditProfile.head_update");
    
    private static final Message T_email_address =
        message("xmlui.EPerson.EditProfile.email_address");
    
    private static final Message T_first_name =
        message("xmlui.EPerson.EditProfile.first_name");
    
    private static final Message T_error_required =
        message("xmlui.EPerson.EditProfile.error_required");
    
    private static final Message T_last_name =
        message("xmlui.EPerson.EditProfile.last_name");
    
    private static final Message T_telephone =
        message("xmlui.EPerson.EditProfile.telephone");
    
    private static final Message T_language =
        message("xmlui.EPerson.EditProfile.Language");
    
    private static final Message T_create_password_instructions =
        message("xmlui.EPerson.EditProfile.create_password_instructions");
    
    private static final Message T_update_password_instructions =
        message("xmlui.EPerson.EditProfile.update_password_instructions");
    
    private static final Message T_password =
        message("xmlui.EPerson.EditProfile.password");
    
    private static final Message T_error_invalid_password =
        message("xmlui.EPerson.EditProfile.error_invalid_password");
    
    private static final Message T_confirm_password =
        message("xmlui.EPerson.EditProfile.confirm_password");
    
    private static final Message T_error_unconfirmed_password =
        message("xmlui.EPerson.EditProfile.error_unconfirmed_password");
    
    private static final Message T_submit_update =
        message("xmlui.EPerson.EditProfile.submit_update");
    
    private static final Message T_submit_create =
        message("xmlui.EPerson.EditProfile.submit_create");
    
    private static final Message T_subscriptions =
        message("xmlui.EPerson.EditProfile.subscriptions");

    private static final Message T_subscriptions_help =
        message("xmlui.EPerson.EditProfile.subscriptions_help");

    private static final Message T_email_subscriptions =
        message("xmlui.EPerson.EditProfile.email_subscriptions");

    private static final Message T_select_collection =
        message("xmlui.EPerson.EditProfile.select_collection");
 
    private static final Message T_head_auth =
        message("xmlui.EPerson.EditProfile.head_auth");
    
    private static final Message T_head_identify =
        message("xmlui.EPerson.EditProfile.head_identify");
    
    private static final Message T_head_security =
        message("xmlui.EPerson.EditProfile.head_security");
    
    private static Locale[] supportedLocales = getSupportedLocales();
    static
    {
        Arrays.sort(supportedLocales, new Comparator<Locale>() {
            public int compare(Locale a, Locale b)
            {
                return a.getDisplayName().compareTo(b.getDisplayName());
            }
        });
    }
    
    /** The email address of the user registering for the first time.*/
    private String email;

    /** Determine if the user is registering for the first time */
    private boolean registering;
    
    /** Determine if the user is allowed to set their own password */
    private boolean allowSetPassword;
    
    /** A list of fields in error */
    private java.util.List<String> errors;
    
    public void setup(SourceResolver resolver, Map objectModel, String src,
            Parameters parameters) throws ProcessingException, SAXException,
            IOException
    {
        super.setup(resolver,objectModel,src,parameters);
        
        this.email = parameters.getParameter("email","unknown");
        this.registering = parameters.getParameterAsBoolean("registering",false);
        this.allowSetPassword = parameters.getParameterAsBoolean("allowSetPassword",false);
        
        String errors = parameters.getParameter("errors","");
        if (errors.length() > 0)
        {
            this.errors = Arrays.asList(errors.split(","));
        }
        else
        {
            this.errors = new ArrayList<String>();
        }
        
        // Ensure that the email variable is set.
        if (eperson != null)
        {
            this.email = eperson.getEmail();
        }
    }
       
    public void addPageMeta(PageMeta pageMeta) throws WingException
    {
        // Set the page title
        if (registering)
        {
            pageMeta.addMetadata("title").addContent(T_title_create);
        }
        else
        {
            pageMeta.addMetadata("title").addContent(T_title_update);
        }
        
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        if (registering)
        {
            pageMeta.addTrail().addContent(T_trail_new_registration);
        }
        else
        {
            pageMeta.addTrail().addContent(T_trail_update);
        }
    }
    
    
   public void addBody(Body body) throws WingException, SQLException
   {
       // Log that we are viewing a profile
       log.info(LogManager.getHeader(context, "view_profile", ""));

       Request request = ObjectModelHelper.getRequest(objectModel);
       
       String defaultFirstName="",defaultLastName="",defaultPhone="";
       String defaultLanguage=null;
       String defaultSexo=null;
       String defaultEdad=null;
       String defaultEducacion=null;
       String defaultOcupacion=null;
       String defaultOtraocupacion=null;
       String defaultOrganizacion=null;
       String defaultTipoorganizacion=null;
       String defaultOtrotipoorganizacion=null;
       String defaultActividadorganizacion=null;
       String defaultOtraactividadorganizacion=null;
       String defaultPais=null;
       String defaultEstado=null;
       String defaultUsoinformacion=null;
       String defaultOtrousoinformacion=null;

       if (request.getParameter("submit") != null)
       {
           defaultFirstName = request.getParameter("first_name");
           defaultLastName = request.getParameter("last_name");
           defaultPhone = request.getParameter("phone");
           defaultLanguage = request.getParameter("language");
           defaultSexo = request.getParameter("sexo");
           defaultEdad= request.getParameter("edad");
           defaultEducacion= request.getParameter("educacion");
           defaultOcupacion= request.getParameter("ocupacion");
           defaultOtraocupacion= request.getParameter("otraocupacion");
           defaultOrganizacion= request.getParameter("organizacion");
           defaultTipoorganizacion= request.getParameter("tipoorganizacion");
           defaultOtrotipoorganizacion= request.getParameter("otrotipoorganizacion");
           defaultActividadorganizacion= request.getParameter("actividadorganizacion");
           defaultOtraactividadorganizacion= request.getParameter("otraactividadorganizacion");
           defaultPais= request.getParameter("pais");
           defaultEstado= request.getParameter("estado");
           defaultUsoinformacion= request.getParameter("usoinformacion");
           defaultOtrousoinformacion= request.getParameter("otrousoinformacion");
       }
       else if (eperson != null)
       {
           defaultFirstName = eperson.getFirstName();
           defaultLastName = eperson.getLastName();
           defaultPhone = eperson.getMetadata("phone");
           defaultLanguage = eperson.getLanguage();
           defaultSexo = eperson.getSexo();
           defaultEdad= eperson.getEdad();
           defaultEducacion= eperson.getEducacion();
           defaultOcupacion= eperson.getOcupacion();
           defaultOtraocupacion= eperson.getOtraocupacion();
           defaultOrganizacion= eperson.getOrganizacion();
           defaultTipoorganizacion= eperson.getTipoorganizacion();
           defaultOtrotipoorganizacion= eperson.getOtrotipoorganizacion();
           defaultActividadorganizacion= eperson.getActividadorganizacion();
           defaultOtraactividadorganizacion= eperson.getOtraactividadorganizacion();
           defaultPais= eperson.getPais();
           defaultEstado= eperson.getEstado();
           defaultUsoinformacion= eperson.getUsoinformacion();
           defaultOtrousoinformacion= eperson.getOtrousoinformacion();
       }
       
       String action = contextPath;
       if (registering)
       {
           action += "/register";
       }
       else
       {
           action += "/profile";
       }
       
       
       
       
       Division profile = body.addInteractiveDivision("information",
               action,Division.METHOD_POST,"primary");

       if (registering)
       {
           profile.setHead(T_head_create);
       }
       else
       {
           profile.setHead(T_head_update);
       }
       
       // Add the progress list if we are registering a new user
       if (registering)
       {
           EPersonUtils.registrationProgressList(profile, 2);
       }
       
       
       
       
       
       List form = profile.addList("form",List.TYPE_FORM);
       
       List identity = form.addList("identity",List.TYPE_FORM);
       identity.setHead(T_head_identify);
       
       // Email
       identity.addLabel(T_email_address);
       identity.addItem(email);
       
       // First name
       Text firstName = identity.addItem().addText("first_name");
       firstName.setRequired();
       firstName.setLabel(T_first_name);
       firstName.setValue(defaultFirstName);
       if (errors.contains("first_name"))
       {
           firstName.addError(T_error_required);
       }
       if (!registering && !ConfigurationManager.getBooleanProperty("xmlui.user.editmetadata", true))
       {
           firstName.setDisabled();
       }
       
       // Last name
       Text lastName = identity.addItem().addText("last_name");
       lastName.setRequired();
       lastName.setLabel(T_last_name);
       lastName.setValue(defaultLastName);
       if (errors.contains("last_name"))
       {
           lastName.addError(T_error_required);
       }
       if (!registering &&!ConfigurationManager.getBooleanProperty("xmlui.user.editmetadata", true))
       {
           lastName.setDisabled();
       }

       // sexo
       Select sexo = identity.addItem().addSelect("sexo");
       sexo.setLabel(message("sexo"));
       sexo.addOption("--","");
       sexo.addOption("Hombre","Hombre");
       sexo.addOption("Mujer","Mujer");
       sexo.setOptionSelected(defaultSexo);

       //edad
       Text edad = identity.addItem().addText("edad");
       edad.setLabel(message("Edad"));
       edad.setValue(defaultEdad);

       //educacion
       Select educacion = identity.addItem().addSelect("educacion");
       educacion.setLabel(message("educacion"));
       educacion.addOption("--","");
       educacion.addOption("Primaria","Primaria");
       educacion.addOption("Secundaria","Secundaria");
       educacion.addOption(new String("T"+"\u00E9"+"cnica/ Vocacional"),new String("T"+"\u00E9"+"cnica/ Vocacional"));
       educacion.addOption("Medio Superior","Medio Superior");
       educacion.addOption("Licenciatura","Licenciatura");
       educacion.addOption(new String("Maestr"+"\u00ED"+"a"),new String("Maestr"+"\u00ED"+"a"));
       educacion.addOption("Doctorado","Doctorado");
       educacion.setOptionSelected(defaultEducacion);

       //ocupacion
       Select ocupacion = identity.addItem().addSelect("ocupacion");
       ocupacion.setLabel(message("ocupacion"));
       ocupacion.addOption("--","");
       ocupacion.addOption("Estudiante","Estudiante");        ocupacion.addOption("Profesor","Profesor");
       ocupacion.addOption("Investigador","Investigador");        ocupacion.addOption("Consultor","Consultor");
       ocupacion.addOption("Asesor","Asesor");        ocupacion.addOption("Analista","Analista");
       ocupacion.addOption(new String("Funcionario P"+"\u00FA"+"blico"),new String("Funcionario P"+"\u00FA"+"blico"));
       ocupacion.addOption("Profesionista independiente","Profesionista independiente");
       ocupacion.addOption("Empleado","Empleado");        ocupacion.addOption("Personal administrativo","Personal administrativo");
       ocupacion.setOptionSelected(defaultOcupacion);

       //otraocupacion
       Text otraocupacion = identity.addItem().addText("otraocupacion");
       otraocupacion.setLabel(message("otraocupacion"));
       otraocupacion.setValue(defaultOtraocupacion);
       otraocupacion.setHelp(message("helpotraocupacion"));

       identity.addLabel(message("informacionorganizacion"));

       //organizacion
       Text organizacion = identity.addItem().addText("organizacion");
       organizacion.setLabel(message("organizacion"));
       organizacion.setValue(defaultOrganizacion);
       organizacion.setRequired(true);

       //tipoorganizacion
       Select tipoorganizacion = identity.addItem().addSelect("tipoorganizacion");
       tipoorganizacion.setLabel(message("tipoorganizacion"));
       tipoorganizacion.addOption("--","");
       tipoorganizacion.addOption(new String("P"+"Ãº"+"blica"),new String("P"+"Ãº"+"blica"));
       tipoorganizacion.addOption("Privada","Privada");       tipoorganizacion.addOption("Social","Social");
       tipoorganizacion.setOptionSelected(defaultTipoorganizacion);

       //otrotipoorganizacion
       Text otrotipoorganizacion = identity.addItem().addText("otrotipoorganizacion");
       otrotipoorganizacion.setLabel(message("otrotipoorganizacion"));
       otrotipoorganizacion.setValue(defaultOtrotipoorganizacion);
       otrotipoorganizacion.setHelp(message("helpotrotipoorganizacion"));

       //actividadorganizacion
       Select actividadorganizacion = identity.addItem().addSelect("actividadorganizacion");
       actividadorganizacion.setLabel(message("actividadorganizacion"));
       actividadorganizacion.addOption("--","");
       actividadorganizacion.addOption(new String("Educaci"+"\u00F3"+"n"),new String("Educaci"+"\u00F3"+"n"));
       actividadorganizacion.addOption(new String("Investigaci"+"\u00F3"+"n"),new String("Investigaci"+"\u00F3"+"n"));
       actividadorganizacion.addOption(new String("Consultor"+"\u00ED"+"a"),new String("Consultor"+"\u00ED"+"a"));
       actividadorganizacion.addOption("Comercial","Comercial");
       actividadorganizacion.addOption("Servicios","Servicios");
       actividadorganizacion.addOption("Industrial","Industrial");
       actividadorganizacion.setOptionSelected(defaultActividadorganizacion);

       //otraactividadorganizacion
       Text otraactividadorganizacion = identity.addItem().addText("otraactividadorganizacion");
       otraactividadorganizacion.setLabel(message("otraactividadorganizacion"));
       otraactividadorganizacion.setValue(defaultOtraactividadorganizacion);
       otraactividadorganizacion.setHelp(message("helpotraactividadorganizacion"));

       //pais
       Text pais = identity.addItem().addText("pais");
       pais.setLabel(message("pais"));
       pais.setValue(defaultPais);

       //estado
       Text estado = identity.addItem().addText("estado");
       estado.setLabel(message("estado"));
       estado.setValue(defaultEstado);

       //usoinformacion
       Select usoinformacion = identity.addItem().addSelect("usoinformacion");
       usoinformacion.setLabel(message("usoinformacion"));
       usoinformacion.addOption("--","");
       usoinformacion.addOption(new String("Investigaci"+"\u00F0"+"n"),new String("Investigaci"+"\u00F0"+"n"));
       usoinformacion.addOption("Docencia","Docencia");
       usoinformacion.addOption(new String("Desarrollo de pol"+"\u00ED"+"tica p"+"\u00FA"+"blica"),new String("Desarrollo de pol"+"\u00ED"+"tica p"+"\u00FA"+"blica"));
       usoinformacion.addOption("Desarrollo de planes y programas","Desarrollo de planes y programas");
       usoinformacion.addOption(new String("Art"+"\u00ED"+"culos de difusi"+"\u00F0"+"n/ opini"+"\u00F0"+"n p"+"\u00FA"+"blica."),new String("Art"+"\u00ED"+"culos de difusi"+"\u00F0"+"n/ opini"+"\u00F0"+"n p"+"\u00FA"+"blica."));
       usoinformacion.addOption("Toma de decisiones organizacionales","Toma de decisiones organizacionales");
       usoinformacion.addOption("Estudios de Mercado","Estudios de Mercado");
       usoinformacion.addOption("Trabajo Escolar","Trabajo Escolar");
       usoinformacion.setOptionSelected(defaultUsoinformacion);

       //otrousoinformacion
       Text otrousoinformacion = identity.addItem().addText("otrousoinformacion");
       otrousoinformacion.setLabel(message("otrousoinformacion"));
       otrousoinformacion.setValue(defaultOtrousoinformacion);
       otrousoinformacion.setHelp(message("helpotrousoinformacion"));


       if (!registering && !ConfigurationManager.getBooleanProperty("xmlui.user.editmetadata", true))
       {
           sexo.setDisabled();
           edad.setDisabled();
           educacion.setDisabled();
           ocupacion.setDisabled();
           otraocupacion.setDisabled();
           organizacion.setDisabled();
           tipoorganizacion.setDisabled();
           actividadorganizacion.setDisabled();
           otraactividadorganizacion.setDisabled();
           pais.setDisabled();
           estado.setDisabled();
           usoinformacion.setDisabled();
           otrousoinformacion.setDisabled();
       }
       
       // Phone
       Text phone = identity.addItem().addText("phone");
       phone.setLabel(T_telephone);
       phone.setValue(defaultPhone);
       if (errors.contains("phone"))
       {
           phone.addError(T_error_required);
       }
       if (!registering && !ConfigurationManager.getBooleanProperty("xmlui.user.editmetadata", true))
       {
           phone.setDisabled();
       }
        
       // Language
       Select lang = identity.addItem().addSelect("language");
       lang.setLabel(T_language);
       if (supportedLocales.length > 0)
       {
           for (Locale lc : supportedLocales)
           {
               lang.addOption(lc.toString(), lc.getDisplayName());
           }
       }
       else
       {
           lang.addOption(I18nUtil.DEFAULTLOCALE.toString(), I18nUtil.DEFAULTLOCALE.getDisplayName());
       }
       lang.setOptionSelected((defaultLanguage == null || defaultLanguage.equals("")) ?
                              I18nUtil.DEFAULTLOCALE.toString() : defaultLanguage);
       if (!registering && !ConfigurationManager.getBooleanProperty("xmlui.user.editmetadata", true))
       {
           lang.setDisabled();
       }

       // Subscriptions
       if (!registering)
       {
           List subscribe = form.addList("subscriptions",List.TYPE_FORM);
           subscribe.setHead(T_subscriptions);
           
           subscribe.addItem(T_subscriptions_help);
           
           Collection[] currentList = Subscribe.getSubscriptions(context, context.getCurrentUser());
           Collection[] possibleList = Collection.findAll(context);
           
           Select subscriptions = subscribe.addItem().addSelect("subscriptions");
           subscriptions.setLabel(T_email_subscriptions);
           subscriptions.setHelp("");
           subscriptions.enableAddOperation();
           subscriptions.enableDeleteOperation();
           
           subscriptions.addOption(-1,T_select_collection);
           for (Collection possible : possibleList)
           {
               String name = possible.getMetadata("name");
               if (name.length() > 50)
               {
                   name = name.substring(0, 47) + "...";
               }
               subscriptions.addOption(possible.getID(), name);
           }
                   
           for (Collection collection: currentList)
           {
               subscriptions.addInstance().setOptionSelected(collection.getID());
           }
       }
       
       
       if (allowSetPassword)
       {
           List security = form.addList("security",List.TYPE_FORM);
           security.setHead(T_head_security);
           
           if (registering)
           {
                   security.addItem().addContent(T_create_password_instructions);
           }
           else
           {
                   security.addItem().addContent(T_update_password_instructions);
           }
           
           
           Field password = security.addItem().addPassword("password");
           password.setLabel(T_password);
           if (registering)
           {
               password.setRequired();
           }
           if (errors.contains("password"))
           {
               password.addError(T_error_invalid_password);
           }
           
           Field passwordConfirm = security.addItem().addPassword("password_confirm");
           passwordConfirm.setLabel(T_confirm_password);
           if (registering)
           {
               passwordConfirm.setRequired();
           }
           if (errors.contains("password_confirm"))
           {
               passwordConfirm.addError(T_error_unconfirmed_password);
           }
       }
       
       Button submit = form.addItem().addButton("submit");
       if (registering)
       {
           submit.setValue(T_submit_update);
       }
       else
       {
           submit.setValue(T_submit_create);
       }
       
       profile.addHidden("eperson-continue").setValue(knot.getId());
       
       
       
       if (!registering)
       {
                // Add a list of groups that this user is apart of.
                        Group[] memberships = Group.allMemberGroups(context, context.getCurrentUser());
                
                
                        // Not a member of any groups then don't do anything.
                        if (!(memberships.length > 0))
                        {
                            return;
                        }
                        
                        List list = profile.addList("memberships");
                        list.setHead(T_head_auth);
                        for (Group group: memberships)
                        {
                                list.addItem(group.getName());
                        }
       }
   }

   /**
    * Recycle
    */
    public void recycle()
    {
        this.email = null;
        this.errors = null;
        super.recycle();
    }

    /**
     * get the available Locales for the User Interface as defined in dspace.cfg
     * property xmlui.supported.locales
     * returns an array of Locales or null
     *
     * @return an array of supported Locales or null
     */
    private static Locale[] getSupportedLocales()
    {
        String ll = ConfigurationManager.getProperty("xmlui.supported.locales");
        if (ll != null)
        {
            return I18nUtil.parseLocales(ll);
        }
        else
        {
            Locale result[] = new Locale[1];
            result[0] =  I18nUtil.DEFAULTLOCALE;
            return result;
        }
    }
}
