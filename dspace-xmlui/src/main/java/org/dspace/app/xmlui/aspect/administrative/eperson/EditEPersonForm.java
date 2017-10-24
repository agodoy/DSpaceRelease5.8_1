/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.administrative.eperson;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.core.ConfigurationManager;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

/**
 * Edit an existing EPerson, display all the eperson's metadata 
 * along with two special options two reset the eperson's 
 * password and delete this user. 
 *
 * @author Alexey Maslov
 */
public class EditEPersonForm extends AbstractDSpaceTransformer   
{
	/** Language Strings */
	private static final Message T_dspace_home =
		message("xmlui.general.dspace_home");
	
	private static final Message T_submit_save =
		message("xmlui.general.save");
	
	private static final Message T_submit_cancel =
		message("xmlui.general.cancel");
	
	private static final Message T_title =
		message("xmlui.administrative.eperson.EditEPersonForm.title");
	
	private static final Message T_eperson_trail =
		message("xmlui.administrative.eperson.general.epeople_trail");
	
	private static final Message T_trail =
		message("xmlui.administrative.eperson.EditEPersonForm.trail");
	
	private static final Message T_head1 =
		message("xmlui.administrative.eperson.EditEPersonForm.head1");
	
	private static final Message T_email_taken =
		message("xmlui.administrative.eperson.EditEPersonForm.email_taken");
	
	private static final Message T_head2 =
		message("xmlui.administrative.eperson.EditEPersonForm.head2");
	
	private static final Message T_error_email_unique =
		message("xmlui.administrative.eperson.EditEPersonForm.error_email_unique");
	
	private static final Message T_error_email =
		message("xmlui.administrative.eperson.EditEPersonForm.error_email");
	
	private static final Message T_error_fname =
		message("xmlui.administrative.eperson.EditEPersonForm.error_fname");
	
	private static final Message T_error_lname =
		message("xmlui.administrative.eperson.EditEPersonForm.error_lname");
	
	private static final Message T_req_certs =
		message("xmlui.administrative.eperson.EditEPersonForm.req_certs");
	
	private static final Message T_can_log_in =
		message("xmlui.administrative.eperson.EditEPersonForm.can_log_in");
	
	private static final Message T_submit_reset_password =
		message("xmlui.administrative.eperson.EditEPersonForm.submit_reset_password");
	
	private static final Message T_special_help =
		message("xmlui.administrative.eperson.EditEPersonForm.special_help");
	
	private static final Message T_submit_delete =
		message("xmlui.administrative.eperson.EditEPersonForm.submit_delete");
	
	private static final Message T_submit_login_as =
		message("xmlui.administrative.eperson.EditEPersonForm.submit_login_as");
	
	private static final Message T_delete_constraint =
		message("xmlui.administrative.eperson.EditEPersonForm.delete_constraint");

	private static final Message T_constraint_last_conjunction =
		message("xmlui.administrative.eperson.EditEPersonForm.delete_constraint.last_conjunction");
	
	private static final Message T_constraint_item =
		message("xmlui.administrative.eperson.EditEPersonForm.delete_constraint.item");
	
	private static final Message T_constraint_workflowitem =
		message("xmlui.administrative.eperson.EditEPersonForm.delete_constraint.workflowitem");
	
	private static final Message T_constraint_tasklistitem =
		message("xmlui.administrative.eperson.EditEPersonForm.delete_constraint.tasklistitem");

	private static final Message T_constraint_unknown = 
		message("xmlui.administrative.eperson.EditEPersonForm.delete_constraint.unknown");
	
	private static final Message T_member_head =
		message("xmlui.administrative.eperson.EditEPersonForm.member_head");
	
	private static final Message T_indirect_member = 
		message("xmlui.administrative.eperson.EditEPersonForm.indirect_member");
	
	private static final Message T_member_none =
		message("xmlui.administrative.eperson.EditEPersonForm.member_none");
	
	/** Language string used: */
	
    private static final Message T_email_address = 
    	message("xmlui.EPerson.EditProfile.email_address");
    
    private static final Message T_first_name = 
    	message("xmlui.EPerson.EditProfile.first_name");
    
    private static final Message T_last_name = 
    	message("xmlui.EPerson.EditProfile.last_name");
    
    private static final Message T_telephone =
    	message("xmlui.EPerson.EditProfile.telephone");
    
    
    

    	
	public void addPageMeta(PageMeta pageMeta) throws WingException
    {
        pageMeta.addMetadata("title").addContent(T_title);
        pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
        pageMeta.addTrailLink(contextPath + "/admin/epeople",T_eperson_trail);
        pageMeta.addTrail().addContent(T_trail);
    }
	
	
	public void addBody(Body body) throws WingException, SQLException, AuthorizeException 
	{
		// Get all our parameters
		boolean admin = AuthorizeManager.isAdmin(context);
		
		Request request = ObjectModelHelper.getRequest(objectModel);
		
		// Get our parameters;
		int epersonID = parameters.getParameterAsInteger("epersonID",-1);
		String errorString = parameters.getParameter("errors",null);
		ArrayList<String> errors = new ArrayList<String>();
		if (errorString != null)
		{
			for (String error : errorString.split(","))
            {
                errors.add(error);
            }
		}
		
		// Grab the person in question 
		EPerson eperson = EPerson.find(context, epersonID);
		
		if (eperson == null)
        {
            throw new UIException("Unable to find eperson for id:" + epersonID);
        }
		
		String emailValue = eperson.getEmail();
		String firstValue = eperson.getFirstName();
		String lastValue  = eperson.getLastName();
		String phoneValue = eperson.getMetadata("phone");
        String sexoValue = eperson.getMetadata("sexo");
        String edadValue= eperson.getMetadata("edad");
        String educacionValue= eperson.getMetadata("educacion");
        String ocupacionValue= eperson.getMetadata("ocupacion");
        String otraocupacionValue= eperson.getMetadata("otraocupacion");
        String organizacionValue= eperson.getMetadata("organizacion");
        String tipoorganizacionValue= eperson.getMetadata("tipoorganizacion");
        String otrotipoorganizacionValue= eperson.getMetadata("otrotipoorganizacion");
        String actividadorganizacionValue= eperson.getMetadata("actividadorganizacion");
        String otraactividadorganizacionValue= eperson.getMetadata("otraactividadorganizacion");
        String paisValue= eperson.getMetadata("pais");
        String estadoValue= eperson.getMetadata("estado");
        String usoinformacionValue= eperson.getMetadata("usoinformacion");
        String otrousoinformacionValue= eperson.getMetadata("otrousoinformacion");

        boolean canLogInValue = eperson.canLogIn();
		boolean certificatValue = eperson.getRequireCertificate();
		java.util.List<String> deleteConstraints = eperson.getDeleteConstraints();
		
		if (request.getParameter("email_address") != null)
        {
            emailValue = request.getParameter("email_address");
        }
		if (request.getParameter("first_name") != null)
        {
            firstValue = request.getParameter("first_name");
        }
		if (request.getParameter("last_name") != null)
        {
            lastValue = request.getParameter("last_name");
        }
        if (request.getParameter("sexo") != null){
            sexoValue = request.getParameter("sexo");
        }
        if (request.getParameter("edad") != null){
            edadValue = request.getParameter("edad");
        }
        if (request.getParameter("educacion") != null){
            educacionValue = request.getParameter("educacion");
        }
        if (request.getParameter("ocupacion") != null){
            ocupacionValue = request.getParameter("ocupacion");
        }
        if (request.getParameter("otraocupacion") != null){
            otraocupacionValue = request.getParameter("otraocupacion");
        }
        if (request.getParameter("organizacion") != null){
            organizacionValue = request.getParameter("organizacion");
        }
        if (request.getParameter("tipoorganizacion") != null){
            tipoorganizacionValue = request.getParameter("tipoorganizacion");
        }
        if (request.getParameter("otrotipoorganizacion") != null){
            otrotipoorganizacionValue = request.getParameter("otrotipoorganizacion");
        }
        if (request.getParameter("actividadorganizacion") != null){
            actividadorganizacionValue = request.getParameter("actividadorganizacion");
        }
        if (request.getParameter("otraactividadorganizacion") != null){
            otraactividadorganizacionValue = request.getParameter("otraactividadorganizacion");
        }
        if (request.getParameter("pais") != null){
            paisValue = request.getParameter("pais");
        }
        if (request.getParameter("estado") != null){
            estadoValue = request.getParameter("estado");
        }
        if (request.getParameter("usoinformacion") != null){
            usoinformacionValue = request.getParameter("usoinformacion");
        }
        if (request.getParameter("otrousoinformacion") != null){
            otrousoinformacionValue = request.getParameter("otrousoinformacion");
        }


		if (request.getParameter("phone") != null)
        {
            phoneValue = request.getParameter("phone");
        }
		
		
		
		// DIVISION: eperson-edit
	    Division edit = body.addInteractiveDivision("eperson-edit",contextPath+"/admin/epeople",Division.METHOD_POST,"primary administrative eperson");
	    edit.setHead(T_head1);
	    
	    
	    if (errors.contains("eperson_email_key")) {
	    	Para problem = edit.addPara();
	    	problem.addHighlight("bold").addContent(T_email_taken);
	    }
        
	    
        List identity = edit.addList("form",List.TYPE_FORM);
        identity.setHead(T_head2.parameterize(eperson.getFullName()));       
        
        if (admin)
        {
	        Text email = identity.addItem().addText("email_address");
	        email.setRequired();
	        email.setLabel(T_email_address);
	        email.setValue(emailValue);
	        if (errors.contains("eperson_email_key"))
            {
                email.addError(T_error_email_unique);
            }
	        else if (errors.contains("email_address"))
            {
                email.addError(T_error_email);
            }
        }
        else
        {
        	identity.addLabel(T_email_address);
        	identity.addItem(emailValue);
        }
        
        if (admin)
        {
	        Text firstName = identity.addItem().addText("first_name");
	        firstName.setRequired();
	        firstName.setLabel(T_first_name);
	        firstName.setValue(firstValue);
	        if (errors.contains("first_name"))
            {
                firstName.addError(T_error_fname);
            }
        }
        else
        {
        	identity.addLabel(T_first_name);
        	identity.addItem(firstValue);
        }
        
        if (admin)
        {
	        Text lastName = identity.addItem().addText("last_name");
	        lastName.setRequired();
	        lastName.setLabel(T_last_name);
	        lastName.setValue(lastValue);
	        if (errors.contains("last_name"))
            {
                lastName.addError(T_error_lname);
            }
        }
        else
        {
        	identity.addLabel(T_last_name);
        	identity.addItem(lastValue);
        }

        if(admin){
            org.dspace.app.xmlui.wing.element.Select sexo = identity.addItem().addSelect("sexo");
            sexo.setLabel(message("sexo"));
            sexo.addOption("--","");
            sexo.addOption("Hombre","Hombre");
            sexo.addOption("Mujer","Mujer");
            sexo.setOptionSelected(sexoValue);

            //edad
            Text edad = identity.addItem().addText("edad");
            edad.setLabel(message("Edad"));
            edad.setValue(edadValue);

            //educacion
            org.dspace.app.xmlui.wing.element.Select educacion = identity.addItem().addSelect("educacion");
            educacion.setLabel(message("educacion"));
            educacion.addOption("--","");
            educacion.addOption("Primaria","Primaria");
            educacion.addOption("Secundaria","Secundaria");
            educacion.addOption(new String("T"+"\u00E9"+"cnica/ Vocacional"),new String("T"+"\u00E9"+"cnica/ Vocacional"));
            educacion.addOption("Medio Superior","Medio Superior");
            educacion.addOption("Licenciatura","Licenciatura");
            educacion.addOption(new String("Maestr"+"\u00ED"+"a"),new String("Maestr"+"\u00ED"+"a"));
            educacion.addOption("Doctorado","Doctorado");
            educacion.setOptionSelected(educacionValue);

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
            ocupacion.setOptionSelected(ocupacionValue);

            //otraocupacion
            Text otraocupacion = identity.addItem().addText("otraocupacion");
            otraocupacion.setLabel(message("otraocupacion"));
            otraocupacion.setValue(otraocupacionValue);
            otraocupacion.setHelp(message("helpotraocupacion"));

            identity.addLabel(message("informacionorganizacion"));

            //organizacion
            Text organizacion = identity.addItem().addText("organizacion");
            organizacion.setLabel(message("organizacion"));
            organizacion.setValue(organizacionValue);
            organizacion.setRequired(true);

            //tipoorganizacion
            Select tipoorganizacion = identity.addItem().addSelect("tipoorganizacion");
            tipoorganizacion.setLabel(message("tipoorganizacion"));
            tipoorganizacion.addOption("--","");
            tipoorganizacion.addOption(new String("P"+"Ãº"+"blica"),new String("P"+"Ãº"+"blica"));
            tipoorganizacion.addOption("Privada","Privada");       tipoorganizacion.addOption("Social","Social");
            tipoorganizacion.setOptionSelected(tipoorganizacionValue);

            //otrotipoorganizacion
            Text otrotipoorganizacion = identity.addItem().addText("otrotipoorganizacion");
            otrotipoorganizacion.setLabel(message("otrotipoorganizacion"));
            otrotipoorganizacion.setValue(otrotipoorganizacionValue);
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
            actividadorganizacion.setOptionSelected(actividadorganizacionValue);

            //otraactividadorganizacion
            Text otraactividadorganizacion = identity.addItem().addText("otraactividadorganizacion");
            otraactividadorganizacion.setLabel(message("otraactividadorganizacion"));
            otraactividadorganizacion.setValue(otraactividadorganizacionValue);
            otraactividadorganizacion.setHelp(message("helpotraactividadorganizacion"));

            //pais
            Text pais = identity.addItem().addText("pais");
            pais.setLabel(message("pais"));
            pais.setValue(paisValue);

            //estado
            Text estado = identity.addItem().addText("estado");
            estado.setLabel(message("estado"));
            estado.setValue(estadoValue);

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
            usoinformacion.setOptionSelected(usoinformacionValue);

            //otrousoinformacion
            Text otrousoinformacion = identity.addItem().addText("otrousoinformacion");
            otrousoinformacion.setLabel(message("otrousoinformacion"));
            otrousoinformacion.setValue(otrousoinformacionValue);
            otrousoinformacion.setHelp(message("helpotrousoinformacion"));

        }else{
            identity.addLabel(message("sexo"));             identity.addItem(sexoValue);
            identity.addLabel(message("edad"));            identity.addItem(edadValue);
            identity.addLabel(message("educacion"));       identity.addItem(educacionValue);
            identity.addLabel(message("ocupacion"));       identity.addItem(ocupacionValue);
            identity.addLabel(message("otraocupacion"));   identity.addItem(otraocupacionValue);
            identity.addLabel(message("organizacion"));     identity.addItem(organizacionValue);
            identity.addLabel(message("tipoorganizacion")); identity.addItem(tipoorganizacionValue);
            identity.addLabel(message("otrotipoorganizacion")); identity.addItem(otrotipoorganizacionValue);
            identity.addLabel(message("actividadorganizacion")); identity.addItem(actividadorganizacionValue);
            identity.addLabel(message("otraactividadorganizacion")); identity.addItem(otraactividadorganizacionValue);
            identity.addLabel(message("pais"));                     identity.addItem(paisValue);
            identity.addLabel(message("estado"));                   identity.addItem(estadoValue);
            identity.addLabel(message("usoinformacion"));           identity.addItem(usoinformacionValue);
            identity.addLabel(message("otrousoinformacion"));       identity.addItem(otrousoinformacionValue);

        }

        
        if (admin)
        {
	        Text phone = identity.addItem().addText("phone");
	        phone.setLabel(T_telephone);
	        phone.setValue(phoneValue);
        }
        else
        {
        	identity.addLabel(T_telephone);
        	identity.addItem(phoneValue);
        }
        
        if (admin)
        {	
        	// Administrative options:
	        CheckBox canLogInField = identity.addItem().addCheckBox("can_log_in");
	        canLogInField.setLabel(T_can_log_in);
	        canLogInField.addOption(canLogInValue, "true");
	        
	        CheckBox certificateField = identity.addItem().addCheckBox("certificate");
	        certificateField.setLabel(T_req_certs);
	        certificateField.addOption(certificatValue,"true");
	        
	        
        	// Buttons to reset, delete or login as
	        identity.addItem().addHighlight("italic").addContent(T_special_help);
	        Item special = identity.addItem();
	        special.addButton("submit_reset_password").setValue(T_submit_reset_password);
	        
	        Button submitDelete = special.addButton("submit_delete");
	        submitDelete.setValue(T_submit_delete);
	       
	        Button submitLoginAs = special.addButton("submit_login_as");
	        submitLoginAs.setValue(T_submit_login_as);
	        if (!ConfigurationManager.getBooleanProperty("xmlui.user.assumelogin", false))
            {
                submitLoginAs.setDisabled();
            }
	       
	        if (deleteConstraints != null && deleteConstraints.size() > 0)
	        {
	        	submitDelete.setDisabled();
	        	
	        	Highlight hi = identity.addItem("eperson-delete-constraint","eperson-delete-constraint").addHighlight("error");
	        	hi.addContent(T_delete_constraint);
	        	hi.addContent(" ");
	        	
	        	for (String constraint : deleteConstraints)
        		{
	        		int idx = deleteConstraints.indexOf(constraint);
	        		if (idx > 0 && idx == deleteConstraints.size() -1 )
	        		{
	        			hi.addContent(", ");
	        			hi.addContent(T_constraint_last_conjunction);
	        			hi.addContent(" ");
	        		}
	        		else if (idx > 0)
                    {
                        hi.addContent(", ");
                    }
	        		
	        		if ("item".equals(constraint))
                    {
                        hi.addContent(T_constraint_item);
                    }
	        		else if ("workflowitem".equals(constraint))
                    {
                        hi.addContent(T_constraint_workflowitem);
                    }
	        		else if ("tasklistitem".equals(constraint))
                    {
                        hi.addContent(T_constraint_tasklistitem);
                    }
	        		else
                    {
                        hi.addContent(T_constraint_unknown);
                    }
        			
        		}
	        	hi.addContent(".");
	        }
        }
        
        
        Item buttons = identity.addItem();
        if (admin)
        {
            buttons.addButton("submit_save").setValue(T_submit_save);
        }
        buttons.addButton("submit_cancel").setValue(T_submit_cancel);
        
        
        
        if (admin)
        {
	        List member = edit.addList("eperson-member-of");
	        member.setHead(T_member_head);

	        Group[] groups = Group.allMemberGroups(context, eperson);
	        for (Group group : groups)
	        {
	        	String url = contextPath + "/admin/groups?administrative-continue="+knot.getId()+"&submit_edit_group&groupID="+group.getID();
	        	
	        	Item item = member.addItem();
	        	item.addXref(url,group.getName());
	        	
	        	// Check if this membership is via another group or not, if so then add a note.
        		Group via = findViaGroup(eperson, group);
        		if (via != null)
                {
                    item.addHighlight("fade").addContent(T_indirect_member.parameterize(via.getName()));
                }
	        }
	        
	        if (groups.length <= 0)
            {
                member.addItem().addHighlight("italic").addContent(T_member_none);
            }
        }
        
	    edit.addHidden("administrative-continue").setValue(knot.getId());
	}
	
	
	/**
	 * Determine if the given eperson is a direct member of this group if
	 * they are not the return the group that membership is implied
	 * through (the via group!). This will only find one possible relation 
	 * path, there may be multiple.
	 * 
	 * 
	 * @param eperson The source group to search from
	 * @param group The target group to search for.
	 * @return The group this member is related through or null if none found.
	 */
	private Group findViaGroup(EPerson eperson, Group group) throws SQLException
	{
		// First check if this eperson is a direct member of the group.
		for (EPerson direct : group.getMembers())
		{
			if (direct.getID() == eperson.getID())
            {
                // Direct membership
                return null;
            }
		}
			
		// Otherwise check what group this eperson is a member through
		Group[] targets = group.getMemberGroups();
		
		Group[] groups = Group.allMemberGroups(context, eperson);
		for (Group member : groups)
		{
			for (Group target : targets)
			{
				if (member.getID() == target.getID())
                {
                    return member;
                }
			}
		}
		
		// This should never happen, but let's just say we couldn't find the relationship.
		return null;
	}
	
}
