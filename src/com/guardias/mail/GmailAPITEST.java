package com.guardias.mail;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.gmail.Gmail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class GmailAPITEST {
    /** Application name. */
    private static final String APPLICATION_NAME ="Gmail API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File( System.getProperty("user.home"), ".credentials/gmail-java-quickstart");;

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
     * at ~/.credentials/gmail-java-quickstart
     */
    private static final List<String> SCOPES =
        Arrays.asList(GmailScopes.GMAIL_LABELS);

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
    	File initialFileP12 = new File("c:\\Guardias-718fedead1ea.json");
        InputStream in = new FileInputStream(initialFileP12);
        /* InputStream in =
            GmailAPITEST.class.getResourceAsStream(); 
        
        Collection<String> SCOPES 
        = Collections.unmodifiableCollection(
                Arrays.asList(
                        new String[]{
                                GmailScopes.GMAIL_COMPOSE,
                                GmailScopes.GMAIL_SEND,
                                GmailScopes.MAIL_GOOGLE_COM,
                                GmailScopes.
                                
                        }));
        
        */
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        
        
     // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        

        //credential.refreshToken();
        
        
        
        
       
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Gmail client service.
     * @return an authorized Gmail client service
     * @throws IOException
     * @throws GeneralSecurityException 
     */
    public static Gmail getGmailService() throws IOException, GeneralSecurityException {
        Credential credential = authorize();
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException, AddressException, MessagingException, javax.mail.MessagingException {
        // Build a new authorized API client service.
        Gmail service = getGmailService();

        //Message message = createMessageWithEmail("dnevado@gmail.com");
        //message = service.users().messages().send("refundable.tech@gmail.com", message).execute();

                
        MimeMessage mimeMessage = createEmail("refundable.tech@gmail.com", "dnevado@gmail.com", "Testing", "hey");
        sendMessage(service, "me", mimeMessage);
        
        

        
        // Print the labels in the user's account.
        String user = "refundable.tech@gmail.com";
        ListLabelsResponse listResponse =
            service.users().labels().list(user).execute();
        List<Label> labels = listResponse.getLabels();
        if (labels.size() == 0) {
            System.out.println("No labels found.");
        } else {
            System.out.println("Labels:");
            for (Label label : labels) {
                System.out.printf("- %s\n", label.getName());
            }
        }
    }
    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to email address of the receiver
     * @param from email address of the sender, the mailbox account
     * @param subject subject of the email
     * @param bodyText body text of the email
     * @return the MimeMessage to be used to send email
     * @throws MessagingException
     * @throws javax.mail.MessagingException 
     * @throws AddressException 
     */
    public static MimeMessage createEmail(String to,
                                          String from,
                                          String subject,
                                          String bodyText)
            throws MessagingException, AddressException, javax.mail.MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    /**
       * Send an email from the user's mailbox to its recipient.
       *
       * @param service Authorized Gmail API instance.
       * @param userId User's email address. The special value "me"
       * can be used to indicate the authenticated user.
       * @param email Email to be sent.
       * @throws MessagingException
       * @throws IOException
     * @throws javax.mail.MessagingException 
       */
      public static void sendMessage(Gmail service, String userId, MimeMessage email)
          throws MessagingException, IOException, javax.mail.MessagingException {
        Message message = createMessageWithEmail(email);
        System.out.println("userId = " + userId);
        message = service.users().messages().send(userId, message).execute();

        System.out.println("Message id: " + message.getId());
        System.out.println(message.toPrettyString());
      }

      /**
       * Create a Message from an email
       *
       * @param email Email to be set to raw of message
       * @return Message containing base64url encoded email.
       * @throws IOException
       * @throws MessagingException
     * @throws javax.mail.MessagingException 
       */
      public static Message createMessageWithEmail(MimeMessage email)
          throws MessagingException, IOException, javax.mail.MessagingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        email.writeTo(baos);
        String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
      }

}