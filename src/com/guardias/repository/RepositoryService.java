package com.guardias.repository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.guardias.Util;
import com.guardias.database.ConfigurationDBImpl;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.drive.Drive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;




/**
 * @author Yaniv Inbar
 */
public class RepositoryService {

  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static String APPLICATION_NAME = "Drive API";

  /** E-mail address of the service account. */
  private static String SERVICE_ACCOUNT_EMAIL = "guardiasapi@guardias-155520.iam.gserviceaccount.com";
  
  private  static String SERVICE_ACCOUNT_FILETOJSON_AUTH = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_CALENDARIO_FICHERO_P12_RUTA(),new Long(-1)).getValue();;

  /** Global instance of the HTTP transport. */  
  /** Global instance of the JSON factory. */  

  /** Directory to store user credentials for this application. */
  private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("java.io.tmpdir"));

  /** Global instance of the {@link FileDataStoreFactory}. */
  private static FileDataStoreFactory DATA_STORE_FACTORY;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY =
      JacksonFactory.getDefaultInstance();

  /** Global instance of the HTTP transport. */
  private static HttpTransport HTTP_TRANSPORT;

  /** Global instance of the scopes required by this quickstart.
   *
   * If modifying these scopes, delete your previously saved credentials
   * at ~/.credentials/drive-java-quickstart
   */
  
  static {
      try {
          HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
          DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
      } catch (Throwable t) {
          t.printStackTrace();
          System.exit(1);
      }
  }

  /**
   * Creates an authorized Credential object.
   * @return an authorized Credential object.
   * @throws IOException
 * @throws GeneralSecurityException 
   */
  
  
  public static Credential authorize() throws IOException, GeneralSecurityException {
      // Load client secrets.
	  
	  //SERVICE_ACCOUNT_EMAIL	= ConfigurationDBImpl.GetConfiguration(Util.getoCONST_GOOGLE_SERVICE_ACCOUNT()).getValue();
	  //SERVICE_ACCOUNT_FILETOJSON_AUTH =  ConfigurationDBImpl.GetConfiguration(Util.getoCONST_CALENDARIO_FICHERO_P12_RUTA()).getValue();
    	
      HTTP_TRANSPORT = new NetHttpTransport();
	//
/* 	 File initialFile = new File(SERVICE_ACCOUNT_FILETOJSON_AUTH);
	 InputStream targetStream = new FileInputStream(initialFile);
	 
      GoogleClientSecrets clientSecrets =
          GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(targetStream));

   */   
      List<String> SCOPES =
    	        Arrays.asList(DriveScopes.DRIVE_FILE);
      
      
      List<String>scops = new <String>ArrayList();
      scops.add("https://www.googleapis.com/auth/drive");
      
      
      GoogleCredential credential = new GoogleCredential.Builder()
  		    .setTransport(HTTP_TRANSPORT)
  		    .setJsonFactory(JSON_FACTORY)
  		    .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
  		    .setServiceAccountScopes(SCOPES)
  		    .setServiceAccountPrivateKeyFromP12File(new File(SERVICE_ACCOUNT_FILETOJSON_AUTH))		    		    
  		    .build();
  		
      System.out.println(
              "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
      return credential;
  }
  
  public  static  Drive  configure() throws IOException, GeneralSecurityException {
	   
    Credential credential = authorize();
    return new Drive.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();	    
 }




public String getSERVICE_ACCOUNT_FILETOJSON_AUTH() {
	return SERVICE_ACCOUNT_FILETOJSON_AUTH;
}


public void setSERVICE_ACCOUNT_FILETOJSON_AUTH(String sERVICE_ACCOUNT_FILETOJSON_AUTH) {
	SERVICE_ACCOUNT_FILETOJSON_AUTH = sERVICE_ACCOUNT_FILETOJSON_AUTH;
}

public static void main(String[] args) throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    Drive service = configure();

    // Print the names and IDs for up to 10 files.
    
    com.google.api.services.drive.model.File  body = new com.google.api.services.drive.model.File();
    body.setName("Guardias.db");
    body.setDescription("BackupDia");
    body.setMimeType("application/x-sqlite3");

    
    java.io.File fileContent = new java.io.File("D:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\BBDD_sqllite\\guardias.db");
    FileContent mediaContent = new FileContent("application/x-sqlite3", fileContent);
    
    com.google.api.services.drive.model.File file2 = service.files().create(body, mediaContent).execute();
    System.out.println("File ID: " + file2.getId());
    
    
    Permission newPermission = new Permission();
    
    newPermission.setEmailAddress("refundable.tech@gmail.com");
        
    newPermission.setType("user");
    newPermission.setRole("writer");
    try {
    	newPermission = service.permissions().create(file2.getId(), newPermission).execute();
    	/* newPermission.setType("user");
        newPermission.setRole("owner"); */
    } catch (IOException e) {
      System.out.println("An error occurred: " + e);
    }
    //return null;
  


    
    
    
    FileList result = service.files().list()
         .setPageSize(10)
         .setFields("nextPageToken, files(id, name)")
         .execute();    
    List<com.google.api.services.drive.model.File> files = result.getFiles();
    if (files == null || files.size() == 0) {
        System.out.println("No files found.");
    } else {
        System.out.println("Files:");
        for (com.google.api.services.drive.model.File file : files) {
            System.out.printf("%s (%s)\n", file.getName(), file.getId());
        }
    }
}

}  