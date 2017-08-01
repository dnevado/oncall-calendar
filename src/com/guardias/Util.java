package com.guardias;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.guardias.database.ConfigurationDBImpl;
import java.util.*;


import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

public class Util {
		
	
		public static final String _WELCOME_REGISTRATION_SUBJECT = "Bienvenido a MEDONCALLS";
		public static final String _SCHEDULE_CHANGE_SUBJECT = "Solicitud de cambio de guardia MEDONCALLS";
		
		public static final String _JOIN_REQUEST_SUBJECT = "Solicitud de unirse a tu servicio MEDONCALLS";
		
		public static final String _PASSWORD_REQUEST_SUBJECT = "Cambio de contraseña MEDONCALLS";
	
		public static final String _COLOR_PRESENCIA = "";
		public static final String _COLOR_LOCALIZADA = ""; 
		public static final String _COLOR_REFUERZO = "";
		public static final String _COLOR_RESIDENTE = "";
		
		public static final String _FORMATO_FECHA = "yyyy-MM-dd";
		
		public static final String _CLAVE_ENCRIPTACION = "medoncalls022017";
	

		public  static final String _TITULO_EVENTO ="medONCALLS  {TIPO}";
		public  static final String _DESCRIPCION_EVENTO ="medONCALLS {TIPO} {FECHA}";
		public  static final String _COLOR_EVENTO ="10";
	
	
		public final static long SECOND_MILLIS = 1000;
	    public final static long MINUTE_MILLIS = SECOND_MILLIS*60;
	    public final static long DAYS_MILLIS = MINUTE_MILLIS*60*24;
	    
	    public final static String  MIME_TYPE_SQLLITE = "application/x-sqlite3";
	    
	    public final static String  SIMULADO_NOMBRE = "SIMULADO";
	    public final static String  SIMULADO_APELLIDOS = "RELLENO";
	    public final static String  SIMULADO_EMAIL = "simulado@medoncalls.com";
	    
	    
	    
	    private final static String  RUTA_DATA_XML ="";
	    
	    public  enum eTipoCambiosGuardias{CAMBIO,CESION,NOAPLICA} ;
	    public  enum eEstadoCambiosGuardias{PENDIENTE,APROBADA,CANCELADA, RECHAZADA} ;
	    public  enum eTipoDia{DIARIO,FESTIVO} ;
	    public  enum eTipo{RESIDENTE,ADJUNTO} ;
	    public  enum eSubtipoResidente {R1,R2,R3,R4,R5,SIMULADO,ROTANTE} ;
	    
	    /* ESTO NOS SIRVE PARA DISTRIBUIR ENTRE EL NUMERO DE SEMANAS COMPLETAS */
	    /* PUEDE SER UN PROBLEMA QUE HAYA SEMANAS DE MENOS  DE 7 DIAS Y QUE SE DISTRIBUYAN UNIFORMEMENTE POR ESAS TB LOS RESIDENTES */ 
	    public final static int CALC_NUM_SEMANAS_MES= 5; 
	    
	    
	    public  enum eTipoGuardia {PRESENCIA,LOCALIZADA,REFUERZO,SIMULADO} ;  // PONEMOS UN CASO ESPECIAL PARA LAS GUARDOAS DE ADJUNTOS SIN RESIDENTES (SIMULADO)
	    private  static final String CALENDARIO_EMAIL_OWNER ="refundable.tech@gmail.com";
	    
	    private  static  String _EMAIL_GOOGLE_ACCOUNT_FROM ="wearyours.noreply@gmail.com";
	    private  static String _EMAIL_GOOGLE_ACCOUNT_PASSWORD ="";
	    private static String EMAIL_GOOGLE_ACCOUNT_HOST= "smtp.gmail.com";
	    
	    private static String BBDD_SQLLITE_PATH= "D:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\BBDD_sqllite\\guardias.db";
	    
	    public static String MYIBATIS_CONFIG_FILE= "com/guardias/persistence";
	    
	    
	    public static String MAIL_SUBJECT = "Guardias del mes ";
	    public static String MAIL_BODY = "A continuación se incluye el calendario de guardias de ";
	    
	    public static String PROPERTIES_FILE = "medoncalls";
	    
	    public static String ENTORNO_PREFIJO_BACKUPS = "des"; // pro
	    
	    
	    
	    private  static Long MAX_NUMERO_DIAS_SEGUIDOS_ADJUNTOS_VALUE = new Long(2);  // EXCEPTO PRESENCIA - PRESENCIA
	    private static Long MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTES_VALUE = new Long(1); // EXCEPTO PRESENCIA - PRESENCIA
	    
	    private static Long DIFERENCIA_MAX_SIMULADOS_POR_ADJUNTO_MES_VALUE = new Long(1); // DIFERENCIA ASUMIBLE DE SIMULADOS ENTRE ADJUNTOS, PARA QUE NO SEA MUY 
	    
	    private static Long MAXIMAS_ITERACIONES_PERMITIDAS_VALUE = new Long(1); // DIFERENCIA ASUMIBLE DE SIMULADOS ENTRE ADJUNTOS, PARA QUE NO SEA MUY
	    
	    
	    
	    /*
	    private static String   PUBLICAR_CALENDARIO_GMAIL_VALUE = ""; // S/N
	    private static String  GOOGLE_SERVICE_ACCOUNT_VALUE = "GOOGLE_SERVICE_ACCOUNT";
	    private static String  CALENDARIO_FICHERO_P12_RUTA_VALUE = "CALENDARIO_FICHERO_P12_RUTA";
	    private static Long   CALENDARIO_MINUTOS_RECORDATORIO_VALUE = new Long(0);
	    
	    */
	    // MECANICA LA ASIGNACION DE RESIDENTES SIMULADOS NI PREVISORA
	    
	    
	    
 		public static String getoCONST_USAR_SECUENCIA_EN_PRESENCIA() {
 			return oCONST_USAR_SECUENCIA_EN_PRESENCIA;
 		}

	    
	    public static String getoCONST_EXISTE_POOLDAY() {
			return oCONST_EXISTE_POOLDAY;
		}


		public static void setoCONST_EXISTE_POOLDAY(String oCONST_EXISTE_POOLDAY) {
			Util.oCONST_EXISTE_POOLDAY = oCONST_EXISTE_POOLDAY;
		}


		private static String oCONST_NUMERO_DIAS_SEGUIDOS_ADJUNTOS = "CONST_MAX_NUMERO_DIAS_SEGUIDOS_ADJUNTOS";
	    private static String oCONST_MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS = "CONST_MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTES";
		private static String oCONST_DIFERENCIA_MAX_SIMULADOS_POR_ADJUNTO_MES = "CONST_DIFERENCIA_MAX_SIMULADOS_POR_ADJUNTO_MES";
	    private static String oCONST_MAXIMAS_ITERACIONES_PERMITIDAS = "CONST_MAXIMAS_ITERACIONES_PERMITIDAS";
	    private static String oCONST_CALENDARIO_GMAIL = "PUBLICAR_CALENDARIO_GMAIL";	    
	    private static String oCONST_GOOGLE_SERVICE_ACCOUNT = "GOOGLE_SERVICE_ACCOUNT";
	    private static String oCONST_CALENDARIO_FICHERO_P12_RUTA = "CALENDARIO_FICHERO_P12_RUTA";
	    private static String oCONST_CALENDARIO_MINUTOS_RECORDATORIO = "CALENDARIO_MINUTOS_RECORDATORIO";  // 0 cero si no se quieren.
	    private static String oCONST__SERVICE_CALENDAR = "ServiceCalendar";  // 0 cero si no se quieren.
	    private static String oCONST_BBDD_PATH = "BBDD_SQLLITE_PATH";  // 0 cero si no se quieren.
	    private static String oCONST_MAIL_FROM = "_EMAIL_GOOGLE_ACCOUNT_FROM";  // 0 cero si no se quieren.
	    private static String oCONST_MAIL_FROM_PASSWORD = "_EMAIL_GOOGLE_ACCOUNT_PASSWORD";  // 0 cero si no se quieren.
	    private static String oCONST_CALENDARIO_EMAIL_OWNER = "CALENDARIO_EMAIL_OWNER";  // 0 cero si no se quieren.
	    private static String oCONST_ENTORNO_PREFIJO_BACKUPS = "ENTORNO_PREFIJO_BACKUPS";  // 0 cero si no se quieren.
	    private static String oCONST_ACTIVAR_CAMBIO_GUARDIAS = "ACTIVAR_CAMBIO_GUARDIAS";  // 0 cero si no se quieren.
	    private static String oCONST_VALIDAR_CAMBIOS_GUARDIAS_BY_ADM = "VALIDAR_CAMBIOS_GUARDIAS_BY_ADM";  // 0 cero si no se quieren.
	    private static String oCONST_EXISTE_POOLDAY = "EXISTE_POOLDAY";  // 0 cero si no se quieren.
	    
	    
	    private static String oCONST_USAR_SECUENCIA_EN_PRESENCIA= "USAR_SECUENCIA_EN_PRESENCIA";  // 0 cero si no se quieren.
	    
	 

		private static String oCONST_NUMERO_PRESENCIAS = "NUMERO_PRESENCIAS";  // 0 cero si no se quieren.
	    private static String oCONST_NUMERO_REFUERZOS_LOCALIZADAS = "NUMERO_REFUERZOS_LOCALIZADAS";  // 0 cero si no se quieren.	    
	    private static String oCONST_NUMERO_RESIDENTES = "NUMERO_RESIDENTES";  // 0 cero si no se quieren.
	    private static String oCONST_MAX_DIF_MAYOR_MENOR_ADJUNTO_GUARDIAS_PRE_LOC_REF = "MAX_DIF_MAYOR_MENOR_ADJUNTO_GUARDIAS_PRE_LOC_REF";  // 0 cero si no se quieren.
	    private static String oCONST_MAX_TOTAL_PRESENCIA_REFUERZO_FESTIVAS = "CONST_MAX_TOTAL_PRESENCIA_REFUERZO_FESTIVAS";  // 0 cero si no se quieren.
	    
	    
	    
	    
	    /*  DURANTE ESTOS MESES, LA ASIGNACION NO ES ALEATORIA, SI NO, AJUSTADA A LAS VACACIONES DE CADA PERSONA PRIMERA, 
	     * SIN TENER EN CUENTA LOS RESIDENTES SEMANA QUE EXISTAN */
	     
	    public static String getoCONST_MAX_TOTAL_PRESENCIA_REFUERZO_FESTIVAS() {
			return oCONST_MAX_TOTAL_PRESENCIA_REFUERZO_FESTIVAS;
		}


		public static void setoCONST_MAX_TOTAL_PRESENCIA_REFUERZO_FESTIVAS(
				String oCONST_MAX_TOTAL_PRESENCIA_REFUERZO_FESTIVAS) {
			Util.oCONST_MAX_TOTAL_PRESENCIA_REFUERZO_FESTIVAS = oCONST_MAX_TOTAL_PRESENCIA_REFUERZO_FESTIVAS;
		}


		public static String getoCONST_MAX_DIF_MAYOR_MENOR_ADJUNTO_GUARDIAS_PRE_LOC_REF() {
			return oCONST_MAX_DIF_MAYOR_MENOR_ADJUNTO_GUARDIAS_PRE_LOC_REF;
		}


		public static void setoCONST_MAX_DIF_MAYOR_MENOR_ADJUNTO_GUARDIAS_PRE_LOC_REF(
				String oCONST_MAX_DIF_MAYOR_MENOR_ADJUNTO_GUARDIAS_PRE_LOC_REF) {
			Util.oCONST_MAX_DIF_MAYOR_MENOR_ADJUNTO_GUARDIAS_PRE_LOC_REF = oCONST_MAX_DIF_MAYOR_MENOR_ADJUNTO_GUARDIAS_PRE_LOC_REF;
		}


		private static String oCONST_AJUSTE_A_ESTOS_MESES_VACACIONES = "AJUSTE_A_ESTOS_MESES_VACACIONES";  // 0 cero si no se quieren.
	    
	    
	    public static String getoCONST_NUMERO_PRESENCIAS() {
			return oCONST_NUMERO_PRESENCIAS;
		}


		public static String getoCONST_NUMERO_REFUERZOS_LOCALIZADAS() {
			return oCONST_NUMERO_REFUERZOS_LOCALIZADAS;
		}

	    
	    public static String getoCONST_AJUSTE_A_ESTOS_MESES_VACACIONES() {
			return oCONST_AJUSTE_A_ESTOS_MESES_VACACIONES;
		}


		public static void setoCONST_AJUSTE_A_ESTOS_MESES_VACACIONES(String oCONST_AJUSTE_A_ESTOS_MESES_VACACIONES) {
			Util.oCONST_AJUSTE_A_ESTOS_MESES_VACACIONES = oCONST_AJUSTE_A_ESTOS_MESES_VACACIONES;
		}


		public static String getoCONST_NUMERO_RESIDENTES() {
			return oCONST_NUMERO_RESIDENTES;
		}


		public static String getoCONST_MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS() {
			return oCONST_MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS;
		}

	 
	public static String getoCONST_ACTIVAR_CAMBIO_GUARDIAS() {
			return oCONST_ACTIVAR_CAMBIO_GUARDIAS;
		}


		public static void setoCONST_ACTIVAR_CAMBIO_GUARDIAS(String oCONST_ACTIVAR_CAMBIO_GUARDIAS) {
			Util.oCONST_ACTIVAR_CAMBIO_GUARDIAS = oCONST_ACTIVAR_CAMBIO_GUARDIAS;
		}


		public static String getoCONST_VALIDAR_CAMBIOS_GUARDIAS_BY_ADM() {
			return oCONST_VALIDAR_CAMBIOS_GUARDIAS_BY_ADM;
		}


		public static void setoCONST_VALIDAR_CAMBIOS_GUARDIAS_BY_ADM(String oCONST_VALIDAR_CAMBIOS_GUARDIAS_BY_ADM) {
			Util.oCONST_VALIDAR_CAMBIOS_GUARDIAS_BY_ADM = oCONST_VALIDAR_CAMBIOS_GUARDIAS_BY_ADM;
		}


	public static String getoCONST_ENTORNO_PREFIJO_BACKUPS() {
			return oCONST_ENTORNO_PREFIJO_BACKUPS;
		}


	public static String getoCONST_CALENDARIO_EMAIL_OWNER() {
			return oCONST_CALENDARIO_EMAIL_OWNER;
		}

	public static String getoCONST_BBDD_PATH() {
			return oCONST_BBDD_PATH;
		}

		public static String getoCONST_MAIL_FROM() {
			return oCONST_MAIL_FROM;
		}

		public static String getoCONST_MAIL_FROM_PASSWORD() {
			return oCONST_MAIL_FROM_PASSWORD;
		}
	public static int daysDiff( Date earlierDate, Date laterDate )
	    {
	        if( earlierDate == null || laterDate == null ) return 0;
	        
	        return (int)((laterDate.getTime()/DAYS_MILLIS) - (earlierDate.getTime()/DAYS_MILLIS));
	    }
	
 	


	

	public static void sendFromGMail(String[] to, String subject, String body, String PathToFile, String FileName ) throws IOException {
        Properties props = System.getProperties();
       
        
        _EMAIL_GOOGLE_ACCOUNT_FROM = ConfigurationDBImpl.GetConfiguration(oCONST_MAIL_FROM, new Long(-1)).getValue();
        _EMAIL_GOOGLE_ACCOUNT_PASSWORD = ConfigurationDBImpl.GetConfiguration(oCONST_MAIL_FROM_PASSWORD,new Long(-1)).getValue();
        
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", EMAIL_GOOGLE_ACCOUNT_HOST);
        props.put("mail.smtp.user", _EMAIL_GOOGLE_ACCOUNT_FROM);
        props.put("mail.smtp.password", _EMAIL_GOOGLE_ACCOUNT_PASSWORD);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(_EMAIL_GOOGLE_ACCOUNT_FROM));
            
            
            
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.BCC, toAddress[i]);
            }
            
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(_EMAIL_GOOGLE_ACCOUNT_FROM));

            BodyPart messageBodyPart = new MimeBodyPart();
            
            
            

            Multipart multipart = new MimeMultipart();

            // Set text message part          

            // Part two is attachment
         /*    messageBodyPart = new MimeBodyPart();         
            DataSource source = new FileDataSource(PathToFile);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(PathToFile);
            multipart.addBodyPart(messageBodyPart); */                     
            
            /*MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText("Calendario.xls");

            MimeBodyPart attachmentBodyPart= new MimeBodyPart();
            DataSource source = new FileDataSource(PathToFile); // ex : "C:\\test.pdf"
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName("2017-01-01.xls"); // ex : "test.pdf"

            multipart.addBodyPart(textBodyPart);  // add the text part
            multipart.addBodyPart(attachmentBodyPart); // add the attachement part
            */
            
         // Create the message body part
            

            // Fill the message
            //messageBodyPart.setText(body);
            messageBodyPart.setContent(body, "text/html");
            		
            
            // Create a multipart message for attachment
           
            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            
            // Second part is attachment
            
            if (!PathToFile.equals(""))
            {
	            messageBodyPart = new MimeBodyPart();            
	            DataSource source = new FileDataSource(PathToFile);
	            messageBodyPart.setDataHandler(new DataHandler(source));
	            messageBodyPart.setFileName(FileName);
	            multipart.addBodyPart(messageBodyPart);
            }
            
            /* MimeBodyPart attachPart = new MimeBodyPart();
            String attachFile = PathToFile;
            attachPart.attachFile(attachFile);
            multipart.addBodyPart(attachPart);
            
            attachPart.setContent(message, "text/html");
            */
            // Send the complete message parts
            message.setContent(multipart);

            message.setSubject(subject);
          //  message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(EMAIL_GOOGLE_ACCOUNT_HOST, _EMAIL_GOOGLE_ACCOUNT_FROM, _EMAIL_GOOGLE_ACCOUNT_PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            
            System.out.println("Sending mail " + subject );
            
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }
	 
	

	public static String getoCONST_NUMERO_DIAS_SEGUIDOS_ADJUNTOS() {
		return oCONST_NUMERO_DIAS_SEGUIDOS_ADJUNTOS;
	}


/* 	public static String getoCONST_MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS() {
		return oCONST_MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS;
	}
*/



	public static String getoCONST_DIFERENCIA_MAX_SIMULADOS_POR_ADJUNTO_MES() {
		return oCONST_DIFERENCIA_MAX_SIMULADOS_POR_ADJUNTO_MES;
	}



	public static String getoCONST_MAXIMAS_ITERACIONES_PERMITIDAS() {
		return oCONST_MAXIMAS_ITERACIONES_PERMITIDAS;
	}



	public static String getoCALENDARIO_GMAIL() {
		return oCONST_CALENDARIO_GMAIL;
	}




	public static String getoCONST_GOOGLE_SERVICE_ACCOUNT() {
		return oCONST_GOOGLE_SERVICE_ACCOUNT;
	}


	public static String getoCONST_CALENDARIO_FICHERO_P12_RUTA() {
		return oCONST_CALENDARIO_FICHERO_P12_RUTA;
	}



	public static String getoCONST_CALENDARIO_MINUTOS_RECORDATORIO() {
		return oCONST_CALENDARIO_MINUTOS_RECORDATORIO;
	}



	public static String getoCONST__SERVICE_CALENDAR() {
		return oCONST__SERVICE_CALENDAR;
	}

	
	/* DIAS 1 DE CADA MES VACACIONAL */
	public static List<java.util.Calendar> ListaMesesVacaciones(Long IdServicio) 
	{
		List<java.util.Calendar> ListaMesesVacaciones= new ArrayList<java.util.Calendar>();
		List<String> lMESES_VACACIONES= new ArrayList<String>();
		String _MesesVacacionesSinAleatorio = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_AJUSTE_A_ESTOS_MESES_VACACIONES(),IdServicio).getValue();
		if (!_MesesVacacionesSinAleatorio.equals(""))
		{
			lMESES_VACACIONES = Arrays.asList(_MesesVacacionesSinAleatorio.split("\\|"));
			for (String Mes : lMESES_VACACIONES)
			{
				java.util.Calendar _cMes = java.util.Calendar.getInstance();
				_cMes.set(java.util.Calendar.MONTH,Integer.valueOf(Mes)-1);
				_cMes.set(java.util.Calendar.DATE,1);
				ListaMesesVacaciones.add(_cMes);
			}
			 
		}
		return ListaMesesVacaciones;
	}
	

	public static boolean EsMesVacaciones(java.util.Calendar cFecha, Long IdServicio) 
	{
		boolean EsMes = false;
		List<String> lMESES_VACACIONES= new ArrayList<String>();
		String _MesesVacacionesSinAleatorio = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_AJUSTE_A_ESTOS_MESES_VACACIONES(),IdServicio).getValue();
		if (!_MesesVacacionesSinAleatorio.equals(""))
		{
			lMESES_VACACIONES = Arrays.asList(_MesesVacacionesSinAleatorio.split("\\|"));
			EsMes =lMESES_VACACIONES.contains(String.valueOf(cFecha.get(java.util.Calendar.MONTH)+1));  // los guardo de 1 a 12, calendar de 0 a 11 
		}
		return EsMes;
	}
	
	
	public static String MonthText(int iIndex) {
        
		String retValue="";
		switch (iIndex)
		{
		case 1:
			   retValue= "Enero";
		break;
		case 2:retValue= "Febrero";
		break;
		case 3:retValue= "Marzo";
		break;
		case 4:retValue= "Abril";
		break;
		case 5:retValue= "Mayo";
		break;
		case 6: retValue= "Junio";
		break;
		case 7:retValue= "Julio";
		break;
		case 8:retValue= "Agosto";
		break;
		case 9:retValue= "Septiembre";
		break;
		case 10:retValue= "Octubre";
		break;
		case 11:retValue= "Noviembre";
		break;
		case 12:retValue= "Diciembre";
		break;
        }		
		return retValue;
    }


	public static int whichDayOfWeek(java.util.Calendar calendar) {
        if (calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.MONDAY) {
            return 1;
        } else if (calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.TUESDAY) {
            return 2;
        } else if (calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.WEDNESDAY) {
            return 3;
        } else if (calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.THURSDAY) {
            return 4;
        } else if (calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY) {
            return 5;
        } else if (calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SATURDAY) {
            return 6;
        } else if (calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY) {
            return 7;
        } else {
            return -1;
        }
    }	


	public static void main(String[] args) {
		
	 //Properties props = new Properties();
	 
	 
	  Properties props = System.getProperties();
      
     
	 
     props.put("mail.smtp.host", "smtp.gmail.com");
     props.put("mail.smtp.socketFactory.port", "465");
     props.put("mail.smtp.socketFactory.class",
             "javax.net.ssl.SSLSocketFactory");
     props.put("mail.smtp.auth", "true");
     props.put("mail.smtp.port", "465");

     Session session = Session.getDefaultInstance(props,
         new javax.mail.Authenticator() {
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication("wearyours.noreply@gmail.com","nevadodM1");
             }
         });

     try {

         Message message = new MimeMessage(session);
         message.setFrom(new InternetAddress("wearyours.noreply@gmail.com"));
         message.setRecipients(Message.RecipientType.TO,
                 InternetAddress.parse("dnevado@gmail.com"));
         message.setSubject("Testing Subject");
         message.setText("Dear User," +
                 "\n\n No spam to my email, please!");

         Transport.send(message);

         System.out.println("Done");

     } catch (MessagingException e) {
    	 e.printStackTrace(); 
         throw new RuntimeException(e);
     }
 }
	
}
