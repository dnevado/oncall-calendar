package com.guardias.repository;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.google.api.client.http.FileContent;
import com.google.api.client.util.DateTime;



import com.google.api.services.calendar.model.AclRule.Scope;
import com.sun.xml.internal.ws.message.EmptyMessageImpl;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.Calendars;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.guardias.Util;
import com.guardias.database.ConfigurationDBImpl;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;


import com.google.api.services.calendar.model.Colors;
import com.google.api.services.calendar.model.ColorDefinition;


public class RepositoryUtil {

	Drive service =null;
	
	String _EMAIL_GOOGLE_ACCOUNT = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_CALENDARIO_EMAIL_OWNER(),new Long(-1)).getValue();
	
	public RepositoryUtil  RepositoryUtil() throws IOException, GeneralSecurityException {
		
		RepositoryUtil c = new RepositoryUtil();
		 if (service==null)
	        {	
	        	service = new RepositoryService().configure();
	        }
		 return c;
	        
	}
	
	/* GUARDIAS.DB*/
	/* BackupDia */
	/* application/x-sqlite3 */
	
	public void   CreateBackupDB(String Titulo, String Descripcion,String MimeType) throws IOException, GeneralSecurityException {
		
		if (service==null)
        {	        	
			service = RepositoryService.configure();
        }
	
		com.google.api.services.drive.model.File  body = new com.google.api.services.drive.model.File();
	    body.setName(Titulo);
	    body.setDescription(Descripcion);
	    body.setMimeType(MimeType);

	    
	    java.io.File fileContent = new java.io.File(ConfigurationDBImpl.GetConfiguration(Util.getoCONST_BBDD_PATH(),new Long(-1)).getValue());
	    FileContent mediaContent = new FileContent(MimeType, fileContent);
	    
	    com.google.api.services.drive.model.File file2 = service.files().create(body, mediaContent).execute();
	    //System.out.println("File ID: " + file2.getId());
	    
	    
	    Permission newPermission = new Permission();
	    
	    newPermission.setEmailAddress(_EMAIL_GOOGLE_ACCOUNT);
	        
	    newPermission.setType("user");
	    newPermission.setRole("writer");
	    try {
	    	newPermission = service.permissions().create(file2.getId(), newPermission).execute();
	    	/* newPermission.setType("user");
	        newPermission.setRole("owner"); */
	    } catch (IOException e) {
	      System.out.println("An error occurred: " + e);
	    }
		
		
        
	}
	
	 

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
     
    }
}  