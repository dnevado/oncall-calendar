package com.guardias.mail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.message.callback.PrivateKeyCallback.Request;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.guardias.Medico;
import com.guardias.Util;
import com.guardias.cambios.CambiosGuardias;
import com.guardias.database.MedicoDBImpl;
import com.sun.istack.internal.Pool;

import oracle.jrockit.jfr.StringConstantPool;
import freemarker.template.*;
import guardias.security.SecurityUtil;

public class MailingUtil {

	
	public static void SendScheduleChangeNotification(String[] UserTo, CambiosGuardias _DataChangeRequest, boolean _Confirmation) throws IOException, TemplateException
	{
		 Configuration cfg = new Configuration();

         // Where do we load the templates from:
         cfg.setClassForTemplateLoading(MailingUtil.class, "/");

         // Some other recommended s	ettings:
        // cfg.setIncompatibleImprovements(new Version(2, 3, 20));
         cfg.setDefaultEncoding("UTF-8");
         //cfg.setLocale(Locale.);
         cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

         // 2. Proccess template(s)
         //
         // You will do this for several times in typical applications.

         // 2.1. Prepare the template input:

         Map<String, Object> input = new HashMap<String, Object>();
         
         
         List<Medico> _lSolicitante = MedicoDBImpl.getMedicos(_DataChangeRequest.getIdMedicoSolicitante());
         Medico _Solicitante = _lSolicitante.get(0);
         
         List<Medico> _lDestino = MedicoDBImpl.getMedicos(_DataChangeRequest.getIdMedicoDestino());
         Medico _Destino = _lDestino.get(0);
         
         
         input.put("NOMBREAPELLIDOSSOLICITANTE",_Solicitante.getNombre() + " " + _Solicitante.getApellidos());
         input.put("DIASOLICITUD",_DataChangeRequest.getFechaIniCambio());
         input.put("DIADESTINO",_DataChangeRequest.getFechaFinCambio());
         input.put("NOMBREAPELLIDOSDESTINATARIO",_Destino.getNombre() + " " + _Destino.getApellidos());         
         input.put("ESTADO",_DataChangeRequest.getEstado());
         input.put("TIPO",_DataChangeRequest.getTipoCambio());
         input.put("COMPANY","medONCALLS");
         
         // 2.2. Get the template
         Template template = cfg.getTemplate("com/guardias/mail/schedule_daychange.ftl");
         if (_Confirmation)         
        	  template = cfg.getTemplate("com/guardias/mail/schedule_daychange_confirmation.ftl");

         // 2.3. Generate the output

         // Write output to the console
        /*  Writer consoleWriter = new OutputStreamWriter(System.out);
         template.process(input, consoleWriter);
*/
         // For the sake of example, also write output into a file:
         Writer StringWriter = new StringWriter();
         
         try {
                 template.process(input, StringWriter);
                 
                 Util.sendFromGMail(UserTo, Util._SCHEDULE_CHANGE_SUBJECT.toString(), StringWriter.toString(), "", "");
                  
                 
         } finally {
        	 	StringWriter.close();
         }

	}
	
	public static void SendWelcomeRegistration(Medico UserTo, Medico UserFrom, HttpServletRequest GuardiasRequest) throws IOException, TemplateException
	{
		 Configuration cfg = new Configuration();

         // Where do we load the templates from:
         cfg.setClassForTemplateLoading(MailingUtil.class, "/");

         // Some other recommended s	ettings:
        // cfg.setIncompatibleImprovements(new Version(2, 3, 20));
         cfg.setDefaultEncoding("UTF-8");
         //cfg.setLocale(Locale.);
         cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

         // 2. Proccess template(s)
         //
         // You will do this for several times in typical applications.

         // 2.1. Prepare the template input:

         Map<String, Object> input = new HashMap<String, Object>();
         
         String TokenEncripted= SecurityUtil.EncriptarTokenEmail(UserTo.getEmail());
         String _ConfirmationPage = "http" + (GuardiasRequest.getProtocol().toLowerCase().contains("https") ? "s://" : "://") +  GuardiasRequest.getServerName()  +  (GuardiasRequest.getServerPort()!=80 ? ":"  + GuardiasRequest.getServerPort() : "")  +  GuardiasRequest.getContextPath() + "/guest/confirmacion.jsp?auth=" + TokenEncripted; 

         input.put("USER",UserTo.getNombre() + " " + UserTo.getApellidos());
         input.put("COMPANY","medONCALLS");
         input.put("INVITACION",UserFrom.getNombre() + " " + UserFrom.getApellidos());
         input.put("ENLACEINVITACION", _ConfirmationPage);
         input.put("LOGINUSER",UserTo.getEmail());
         input.put("LOGINPASSWORD",UserTo.getPassWord());
         //input.put("USER","MEDONCALLS");

         // 2.2. Get the template

         Template template = cfg.getTemplate("com/guardias/mail/welcome.ftl");

         // 2.3. Generate the output

         // Write output to the console
        /*  Writer consoleWriter = new OutputStreamWriter(System.out);
         template.process(input, consoleWriter);
*/
         // For the sake of example, also write output into a file:
         Writer StringWriter = new StringWriter();
         
         String[] _to= {UserTo.getEmail()};
         
         try {
                 template.process(input, StringWriter);
                 
                 Util.sendFromGMail(_to, Util._WELCOME_REGISTRATION_SUBJECT, StringWriter.toString(), "", "");
                 
                 
         } finally {
        	 	StringWriter.close();
         }

	}
	
	public static void SendSuscriptionRegistration(String UserTo) throws IOException, TemplateException
	{
		 Configuration cfg = new Configuration();

         // Where do we load the templates from:
         cfg.setClassForTemplateLoading(MailingUtil.class, "/");

         // Some other recommended s	ettings:
        // cfg.setIncompatibleImprovements(new Version(2, 3, 20));
         cfg.setDefaultEncoding("UTF-8");
         //cfg.setLocale(Locale.);
         cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

         // 2. Proccess template(s)
         //
         // You will do this for several times in typical applications.

         // 2.1. Prepare the template input:

         Map<String, Object> input = new HashMap<String, Object>();
          
         input.put("COMPANY","medONCALLS");         
         input.put("ENLACEINVITACION", "http://demo.medoncalls.com/inicio.jsp");
         input.put("LOGINUSER","demo@demo.com");
         input.put("LOGINPASSWORD","102030");
         //input.put("USER","MEDONCALLS");

         // 2.2. Get the template

         Template template = cfg.getTemplate("com/guardias/mail/suscription.ftl");

         // 2.3. Generate the output

         // Write output to the console
        /*  Writer consoleWriter = new OutputStreamWriter(System.out);
         template.process(input, consoleWriter);
*/
         // For the sake of example, also write output into a file:
         Writer StringWriter = new StringWriter();
         
         String[] _to= {UserTo};
         
         try {
                 template.process(input, StringWriter);
                 
                 Util.sendFromGMail(_to, Util._WELCOME_REGISTRATION_SUBJECT, StringWriter.toString(), "", "");
                 
                 
         } finally {
        	 	StringWriter.close();
         }

	}
	
	
}
