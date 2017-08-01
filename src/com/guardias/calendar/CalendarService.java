package com.guardias.calendar;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.TimeZone;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Setting;
import com.guardias.Util;
import com.guardias.database.ConfigurationDBImpl;





/**
 * @author Yaniv Inbar
 */
public class CalendarService {

  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private String APPLICATION_NAME = "ServiceCalendar";

  /** E-mail address of the service account. */
  private String SERVICE_ACCOUNT_EMAIL = "guardiasapi@guardias-155520.iam.gserviceaccount.com";
  
  private String SERVICE_ACCOUNT_FILETOJSON_AUTH = "c:\\Guardias-718fedead1ea.json";

  /** Global instance of the HTTP transport. */
  private HttpTransport httpTransport;

  /** Global instance of the JSON factory. */
  private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();


  public  Calendar configure() {
    try {
    	
      SERVICE_ACCOUNT_EMAIL	= ConfigurationDBImpl.GetConfiguration(Util.getoCONST_GOOGLE_SERVICE_ACCOUNT(),new Long(-1)).getValue();
      SERVICE_ACCOUNT_FILETOJSON_AUTH =  ConfigurationDBImpl.GetConfiguration(Util.getoCONST_CALENDARIO_FICHERO_P12_RUTA(),new Long(-1)).getValue();
    	
      httpTransport = new NetHttpTransport();
	// check for valid setup
	  if (SERVICE_ACCOUNT_EMAIL.startsWith("Enter ")) {
     	  System.err.println(SERVICE_ACCOUNT_EMAIL);
	      System.exit(1);
	}
	
	
	File initialFile = new File(SERVICE_ACCOUNT_FILETOJSON_AUTH);
    InputStream targetStream = new FileInputStream(initialFile);


    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    GoogleCredential credential = new GoogleCredential.Builder()
		    .setTransport(httpTransport)
		    .setJsonFactory(JSON_FACTORY)
		    .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
		    .setServiceAccountScopes(Collections.singleton(CalendarScopes.CALENDAR))
		    .setServiceAccountPrivateKeyFromP12File(new File(SERVICE_ACCOUNT_FILETOJSON_AUTH))		    		    
		    .build();
		
	
		
    Calendar   client3 = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    
    return client3;
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
    return null;
  }


public  String getApplicationName() {
	return APPLICATION_NAME;
}


public String getServiceAccountEmail() {
	return SERVICE_ACCOUNT_EMAIL;
}


public String getServiceAccountFiletojsonAuth() {
	return SERVICE_ACCOUNT_FILETOJSON_AUTH;
}


public String getAPPLICATION_NAME() {
	return APPLICATION_NAME;
}


public void setAPPLICATION_NAME(String aPPLICATION_NAME) {
	APPLICATION_NAME = aPPLICATION_NAME;
}


public String getSERVICE_ACCOUNT_EMAIL() {
	return SERVICE_ACCOUNT_EMAIL;
}


public void setSERVICE_ACCOUNT_EMAIL(String sERVICE_ACCOUNT_EMAIL) {
	SERVICE_ACCOUNT_EMAIL = sERVICE_ACCOUNT_EMAIL;
}


public String getSERVICE_ACCOUNT_FILETOJSON_AUTH() {
	return SERVICE_ACCOUNT_FILETOJSON_AUTH;
}


public void setSERVICE_ACCOUNT_FILETOJSON_AUTH(String sERVICE_ACCOUNT_FILETOJSON_AUTH) {
	SERVICE_ACCOUNT_FILETOJSON_AUTH = sERVICE_ACCOUNT_FILETOJSON_AUTH;
}


}  