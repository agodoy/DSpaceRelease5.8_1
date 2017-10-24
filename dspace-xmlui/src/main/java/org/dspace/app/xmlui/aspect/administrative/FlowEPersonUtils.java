/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.administrative;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.commons.lang.StringUtils;
import org.dspace.app.xmlui.utils.AuthenticationUtil;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.AccountManager;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.EPersonDeletionException;

/**
 * Utility methods to processes actions on EPeople. These methods are used
 * exclusively from the administrative flow scripts.
 *
 * @author scott phillips
 */
public class FlowEPersonUtils {

	/** Language Strings */
	private static final Message T_add_eperson_success_notice =
		new Message("default","xmlui.administrative.FlowEPersonUtils.add_eperson_success_notice");

	private static final Message T_edit_eperson_success_notice =
		new Message("default","xmlui.administrative.FlowEPersonUtils.edit_eperson_success_notice");

	private static final Message T_reset_password_success_notice =
		new Message("default","xmlui.administrative.FlowEPersonUtils.reset_password_success_notice");
	
	private static final Message t_delete_eperson_success_notice =
		new Message("default","xmlui.administrative.FlowEPersonUtils.delete_eperson_success_notice");
	
	private static final Message t_delete_eperson_failed_notice =
		new Message("default","xmlui.administrative.FlowEPersonUtils.delete_eperson_failed_notice");
	
	/**
	 * Add a new eperson. This method will check that the email address, 
	 * first name, and last name are non empty. Also a check is performed 
	 * to see if the requested email address is already in use by another
	 * user.
	 * 
	 * @param context The current DSpace context
	 * @param request The HTTP request parameters
	 * @param objectModel Cocoon's object model
	 * @return A process result's object.
	 */
	public static FlowResult processAddEPerson(Context context, Request request, Map objectModel) throws SQLException, AuthorizeException 
	{
		FlowResult result = new FlowResult();
		result.setContinue(false); // default to no continue
		
		// Get all our request parameters
		String email = request.getParameter("email_address").trim();
		String first = request.getParameter("first_name").trim();
		String last  = request.getParameter("last_name").trim();
		String phone = request.getParameter("phone").trim();
        String sexo = request.getParameter("sexo").trim();
        String edad = request.getParameter("edad").trim();
        String educacion = request.getParameter("educacion").trim();
        String ocupacion = request.getParameter("ocupacion").trim();
        String otraocupacion = request.getParameter("otraocupacion").trim();
        String organizacion = request.getParameter("organizacion").trim();
        String tipoorganizacion = request.getParameter("tipoorganizacion").trim();
        String otrotipoorganizacion = request.getParameter("otrotipoorganizacion").trim();
        String actividadorganizacion = request.getParameter("actividadorganizacion").trim();
        String otraactividadorganizacion = request.getParameter("otraactividadorganizacion").trim();
        String pais = request.getParameter("pais").trim();
        String estado = request.getParameter("estado").trim();
        String usoinformacion = request.getParameter("usoinformacion").trim();
        String otrousoinformacion = request.getParameter("otrousoinformacion").trim();

        boolean login = (request.getParameter("can_log_in") != null) ? true : false;
		boolean certificate = (request.getParameter("certificate") != null) ? true : false;
		
		// If we have errors, the form needs to be resubmitted to fix those problems
	    if (StringUtils.isEmpty(email))
        {
            result.addError("email_address");
        }
		if (StringUtils.isEmpty(first))
        {
            result.addError("first_name");
        }
		if (StringUtils.isEmpty(last))
        {
            result.addError("last_name");
        }
	    
	    
		// Check if the email address is all ready being used.	        		
    	EPerson potentialDupicate = EPerson.findByEmail(context,email);
    	if (potentialDupicate != null)
    	{
    		// special error that the front end knows about.
    		result.addError("eperson_email_key");
    	}
		
	    // No errors, so we try to create the EPerson from the data provided
	    if (result.getErrors() == null)
	    {
    		EPerson newPerson = AuthenticationUtil.createNewEperson(objectModel,email);
    		
    		newPerson.setFirstName(first);
            newPerson.setLastName(last);
            newPerson.setSexo(sexo);
            newPerson.setEdad(edad);
            newPerson.setEducacion(educacion);
            newPerson.setOcupacion(ocupacion);
            newPerson.setOtraocupacion(otraocupacion);
            newPerson.setOrganizacion(organizacion);
            newPerson.setTipoorganizacion(tipoorganizacion);
            newPerson.setOtrotipoorganizacion(otrotipoorganizacion);
            newPerson.setActividadorganizacion(actividadorganizacion);
            newPerson.setOtraactividadorganizacion(otraactividadorganizacion);
            newPerson.setPais(pais);
            newPerson.setEstado(estado);
            newPerson.setUsoinformacion(usoinformacion);
            newPerson.setOtrousoinformacion(otrousoinformacion);
            newPerson.setMetadata("phone", phone);
            newPerson.setCanLogIn(login);
            newPerson.setRequireCertificate(certificate);
            newPerson.setSelfRegistered(false);
            
            newPerson.update();
            context.commit();
            // success
            result.setContinue(true);
            result.setOutcome(true);
            result.setMessage(T_add_eperson_success_notice);
            result.setParameter("epersonID", newPerson.getID());
	    }
    	
    	return result;
	}


	/**
	 * Edit an eperson's metadata, the email address, first name, and last name are all
	 * required. The user's email address can be updated but it must remain unique, if
	 * the email address already exists then the an error is produced.
	 * 
	 * @param context The current DSpace context
	 * @param request The HTTP request parameters
	 * @param ObjectModel Cocoon's object model
	 * @param epersonID The unique id of the eperson being edited.
	 * @return A process result's object.
	 */
	public static FlowResult processEditEPerson(Context context,
            Request request, Map ObjectModel, int epersonID)
            throws SQLException, AuthorizeException 
	{

		FlowResult result = new FlowResult();
		result.setContinue(false); // default to failure
		
		// Get all our request parameters
		String email = request.getParameter("email_address");
		String first = request.getParameter("first_name");
		String last  = request.getParameter("last_name");
		String phone = request.getParameter("phone");
        String sexo = request.getParameter("sexo");
        String edad = request.getParameter("edad");
        String educacion = request.getParameter("educacion");
        String ocupacion = request.getParameter("ocupacion");
        String otraocupacion = request.getParameter("otraocupacion");
        String organizacion = request.getParameter("organizacion");
        String tipoorganizacion = request.getParameter("tipoorganizacion");
        String otrotipoorganizacion = request.getParameter("otrotipoorganizacion");
        String actividadorganizacion = request.getParameter("actividadorganizacion");
        String otraactividadorganizacion = request.getParameter("otraactividadorganizacion");
        String pais = request.getParameter("pais");
        String estado = request.getParameter("estado");
        String usoinformacion = request.getParameter("usoinformacion");
        String otrousoinformacion = request.getParameter("otrousoinformacion");

        boolean login = (request.getParameter("can_log_in") != null) ? true : false;
		boolean certificate = (request.getParameter("certificate") != null) ? true : false;
		
		
		// If we have errors, the form needs to be resubmitted to fix those problems
	    if (StringUtils.isEmpty(email))
        {
            result.addError("email_address");
        }
		if (StringUtils.isEmpty(first))
        {
            result.addError("first_name");
        }
		if (StringUtils.isEmpty(last))
        {
            result.addError("last_name");
        }
		
		
	    // No errors, so we edit the EPerson with the data provided
		if (result.getErrors() == null)
	    {
    		// Grab the person in question 
    		EPerson personModified = EPerson.find(context, epersonID);
        	
    		// Make sure the email address we are changing to is unique
        	String originalEmail = personModified.getEmail();
            if (originalEmail == null || !originalEmail.equals(email))
        	{	
        		EPerson potentialDupicate = EPerson.findByEmail(context,email);
        		
        		if (potentialDupicate == null) 
        		{
        			personModified.setEmail(email);
        		} 
        		else if (potentialDupicate.equals(personModified)) 
        		{	       
        			// set a special field in error so that the transformer can display a pretty error.
        			result.addError("eperson_email_key");
        			return result;
        		}
        	}
        	String originalFirstName = personModified.getFirstName();
            if (originalFirstName == null || !originalFirstName.equals(first)) {
        		personModified.setFirstName(first);
        	}
        	String originalLastName = personModified.getLastName();
            if (originalLastName == null || !originalLastName.equals(last)) {
        		personModified.setLastName(last);
        	}
            String originalSexo = personModified.getSexo();
            if( originalSexo == null || !originalSexo.equals(sexo) ){
                personModified.setMetadata("sexo",sexo);
            }
            String originalEdad = personModified.getEdad();
            if( originalEdad == null || !originalEdad.equals(edad) ){
                personModified.setMetadata("edad",edad);
            }
            String originalEducacion = personModified.getEducacion();
            if( originalEducacion == null || !originalEducacion.equals(educacion) ){
                personModified.setMetadata("educacion",educacion);
            }
            String originalOcupacion = personModified.getOcupacion();
            if( originalOcupacion == null || !originalOcupacion.equals(ocupacion) ){
                personModified.setMetadata("ocupacion",ocupacion);
            }
            String originalOtraocupacion = personModified.getOtraocupacion();
            if( originalOtraocupacion == null || !originalOtraocupacion.equals(otraocupacion) ){
                personModified.setMetadata("otraocupacion",otraocupacion);
            }
            String originalOrganizacion = personModified.getOrganizacion();
            if( originalOrganizacion == null || !originalOrganizacion.equals(organizacion) ){
                personModified.setMetadata("organizacion",organizacion);
            }
            String originalTipoorganizacion = personModified.getTipoorganizacion();
            if( originalTipoorganizacion == null || !originalTipoorganizacion.equals(tipoorganizacion) ){
                personModified.setMetadata("tipoorganizacion",tipoorganizacion);
            }
            String originalOtrotipoorganizacion = personModified.getOtrotipoorganizacion();
            if( originalOtrotipoorganizacion == null || !originalOtrotipoorganizacion.equals(otrotipoorganizacion) ){
                personModified.setMetadata("otrotipoorganizacion",otrotipoorganizacion);
            }
            String originalActividadorganizacion = personModified.getActividadorganizacion();
            if( originalActividadorganizacion == null || !originalActividadorganizacion.equals(actividadorganizacion) ){
                personModified.setMetadata("actividadorganizacion",actividadorganizacion);
            }
            String originalOtraactividadorganizacion = personModified.getOtraactividadorganizacion();
            if( originalOtraactividadorganizacion == null || !originalOtraactividadorganizacion.equals(otraactividadorganizacion) ){
                personModified.setMetadata("otraactividadorganizacion",otraactividadorganizacion);
            }
            String originalPais = personModified.getPais();
            if( originalPais == null || !originalPais.equals(pais) ){
                personModified.setMetadata("pais",pais);
            }
            String originalEstado = personModified.getEstado();
            if( originalEstado == null || !originalEstado.equals(estado) ){
                personModified.setMetadata("estado",estado);
            }
            String originalUsoinformacion = personModified.getUsoinformacion();
            if( originalUsoinformacion == null || !originalUsoinformacion.equals(usoinformacion) ){
                personModified.setMetadata("usoinformacion",usoinformacion);
            }
            String originalOtrousoinformacion = personModified.getOtrousoinformacion();
            if( originalOtrousoinformacion == null || !originalOtrousoinformacion.equals(otrousoinformacion) ){
                personModified.setMetadata("otrousoinformacion",otrousoinformacion);
            }

        	String originalPhone = personModified.getMetadata("phone");
            if (originalPhone == null || !originalPhone.equals(phone)) {
        		personModified.setMetadata("phone", phone);
        	}
        	personModified.setCanLogIn(login);
        	personModified.setRequireCertificate(certificate);
        	
        	
        	personModified.update();
        	context.commit();
        	
        	result.setContinue(true);
        	result.setOutcome(true);
        	// FIXME: rename this message
        	result.setMessage(T_edit_eperson_success_notice);
	    }
		
	    // Everything was fine
	    return result;
	}
	
	/**
	 * Send the user a forgot password email message. The message will 
	 * contain a token that the user can use to login and pick a new password.
	 * 
	 * @param context The current DSpace context
	 * @param epersonID The unique id of the eperson being edited.
	 * @return A process result's object.
	 */
	public static FlowResult processResetPassword(Context context, int epersonID) throws IOException, MessagingException, SQLException, AuthorizeException 
	{	
		EPerson eperson = EPerson.find(context, epersonID);
		
		// Note, this may throw an error is the email is bad.
		AccountManager.sendForgotPasswordInfo(context,eperson.getEmail());
	
		FlowResult result = new FlowResult();
		result.setContinue(true);
    	result.setOutcome(true);
    	result.setMessage(T_reset_password_success_notice);
    	return result;
	}
	
	
	/**
	 * Log this user in as another user. If the operation failed then the flow result
	 * will be set to failure with it's message set correctly. Note that after logging out
	 * the user may not have sufficient privileges to continue.
	 * 
	 * @param context The current DSpace context.
	 * @param objectModel Object model to obtain the HTTP request from.
	 * @param epersonID The epersonID of the person to login as.
	 * @return The flow result.
	 */
	public static FlowResult processLoginAs(Context context, Map objectModel, int epersonID) throws SQLException
	{
		FlowResult result = new FlowResult();
		result.setContinue(true);
		result.setOutcome(true);
		
		final HttpServletRequest request = (HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
        
		EPerson eperson = EPerson.find(context,epersonID);

		try {
			AuthenticationUtil.loginAs(context, request, eperson);
		} 
		catch (AuthorizeException ae)
		{
			// give the exception error as a notice.
			result.setOutcome(false);
			result.setMessage(new Message(null,ae.getMessage()));
		}
		
		return result;
	}
	
	/**
	 * Delete the epeople specified by the epeopleIDs parameter. This assumes that the
	 * deletion has been confirmed.
	 * 
	 * @param context The current DSpace context
	 * @param epeopleIDs The unique id of the eperson being edited.
	 * @return A process result's object.
	 */
	public static FlowResult processDeleteEPeople(Context context, String[] epeopleIDs) throws NumberFormatException, SQLException, AuthorizeException, EPersonDeletionException
	{
		FlowResult result = new FlowResult();
		
		List<String> unableList = new ArrayList<String>();
    	for (String id : epeopleIDs) 
    	{
    		EPerson personDeleted = EPerson.find(context, Integer.valueOf(id));
    		try {
				personDeleted.delete();
    		} 
    		catch (EPersonDeletionException epde)
    		{
    			String firstName = personDeleted.getFirstName();
    			String lastName = personDeleted.getLastName();
    			String email = personDeleted.getEmail();
    			unableList.add(firstName + " " + lastName + " ("+email+")");
    		}
	    }
    	
    	if (unableList.size() > 0)
    	{
    		result.setOutcome(false);
    		result.setMessage(t_delete_eperson_failed_notice);
    			
    		String characters = null;
    		for(String unable : unableList )
    		{
    			if (characters == null)
                {
                    characters = unable;
                }
    			else
                {
                    characters += ", " + unable;
                }
    		}
    		
    		result.setCharacters(characters);
    	}
    	else
    	{
    		result.setOutcome(true);
    		result.setMessage(t_delete_eperson_success_notice);
    	}
 
    	
    	return result;
	}
}
