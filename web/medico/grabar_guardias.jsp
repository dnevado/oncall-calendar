<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.guardias.calendar.CalendarEventUtil"%>
<%@page import="com.guardias.calendar.CalendarService"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"     pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="com.guardias.calendar.*"%>
<%@page import="java.util.*"%>
<%@page import="java.text.*"%>        
<%@page import="com.google.gson.*"%>

<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>


    
<% 	


	/*String _Path = request.getServletContext().getRealPath("/") + "//medicos.xml";	
	ProcesarMedicos oUtilMedicos = new ProcesarMedicos();
	
   */
	String GuardiasJSON = request.getParameter("guardias");
    String Mes  = request.getParameter("MesGuardia");  //MesGuardia:2017-05-01
	
    DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");
    
    Date dMes = _format.parse(Mes);
    dMes.setDate(1);
    
    
    /* validacion si el mes anterior y posterior estan rellenos, si no, error */
    
    Date  dAnioNMenos1   = dMes;
    
    Calendar cal1 = Calendar.getInstance();
	cal1.setTime(dMes);
    
    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(dMes);
    
    /* UN AÑO MENOS */ 
    
    cal1.add(Calendar.YEAR, -1);
    cal2.add(Calendar.MONTH, -1);
    cal2.add(Calendar.DATE, -1);
    
    
   // Date  dMesMenos1   = dMes.setDate(dMes.getDate()-1);
        
   
    /* VERIFICO QUE HAYA MES ANTERIOR SIEMPRE Y CUANDO NO SEA LA PRIMERA VEZ */  
    List<Guardias> lGuardiasPrevias= GuardiasDBImpl.getGuardiasEntreFechas(_format.format(cal1.getTime()), _format.format(cal2.getTime()), MedicoLogged.getServicioId());
    
	    
	cal1.setTime(dMes);      
    cal2.setTime(dMes);
    
    /* UN AÑO MAS */ 
    cal1.add(Calendar.MONTH, 1);    
    cal2.add(Calendar.YEAR, 1);
    
    List<Guardias> lGuardiasPosteriores= GuardiasDBImpl.getGuardiasEntreFechas(_format.format(cal1.getTime()), _format.format(cal2.getTime()), MedicoLogged.getServicioId());
    
    cal1.setTime(dMes);      
    cal2.setTime(dMes);
    
    /* MES ANTERIOR */ 
    cal1.add(Calendar.MONTH, -1);        
    cal2.add(Calendar.DATE, -1);
    
    List<Guardias> lGuardiasMesAnterior= GuardiasDBImpl.getGuardiasEntreFechas(_format.format(cal1.getTime()), _format.format(cal2.getTime()), MedicoLogged.getServicioId());
    
    
    

    /* ERROR CUANDO HAY GUARDIAS POSTERIORES O HAY ANTERIORES Y QUE NO SEAN DEL MES ANTERIOR */
    if (lGuardiasPosteriores.size()>0 || (lGuardiasPrevias.size()>0 && lGuardiasMesAnterior.size()==0))
    {
    	out.println("[Error]. No se puede continuar, debe generarse la guardia del mes previo y no existir guardias futuras");
    }
    else
    {
   	
        	
    
    Guardias[] lGuardias ;
    		
    Gson gson = new GsonBuilder().create();
   
    lGuardias = gson.fromJson(GuardiasJSON, Guardias[].class); 

     
    /* CUANDO ACABE, ENVIAMOS MAIL , SI ESTA CONFIGURADO ASI */
	String _CalendarioGoogle = ConfigurationDBImpl.GetConfiguration(Util.getoCALENDARIO_GMAIL(),MedicoLogged.getServicioId()).getValue();
	String _CalendarioMinutosRecordatorio = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_CALENDARIO_MINUTOS_RECORDATORIO(),MedicoLogged.getServicioId()).getValue();
	
	CalendarEventUtil _calendarUtil = null;
	if (_CalendarioGoogle.equals("S"))
	{
			 _calendarUtil = new CalendarEventUtil();
			 _calendarUtil.InitCalendarService();
	}
    
	String EventId= "";
    for (int i=0;i<lGuardias.length;i++)
    {
    
    	
    	Guardias oGuardias = lGuardias[i];
    	Guardias oGuardiaDB = null;
    	/* BORRAMOS TODO EL FUTURO */    
    	List<Guardias> lGuardiasDB = GuardiasDBImpl.getGuardiasPorFecha(oGuardias.getDiaGuardia(), MedicoLogged.getServicioId());
    	if (lGuardiasDB!=null && lGuardiasDB.size()>0)
    		oGuardiaDB = lGuardiasDB.get(0);
    	if (_CalendarioGoogle.equals("S") && oGuardiaDB!=null && oGuardiaDB.getIdEventoGCalendar()!=null)   // hay guardias previas y esta el campo de eventid
    	{
    		if (!EventId.equals(oGuardiaDB.getIdEventoGCalendar()))
    			_calendarUtil.DeleteEventByEventId(oGuardiaDB.getIdEventoGCalendar());
    		
    		if (oGuardias.getIdEventoGCalendar()!=null)    	
    			EventId = oGuardias.getIdEventoGCalendar();

    	}
		GuardiasDBImpl.DeleteGuardia(new Long(-1), oGuardias.getDiaGuardia(), MedicoLogged.getServicioId());
    	
    	
    	
    }
   
    
    /* ENVIAMOS UN EVENTO POR DIA AL CALENDARIO */    
    String _Dia = "";
    Guardias oGuardias = null;
    List<String> _lEmails = new ArrayList<String>();
    
	Calendar _Guardia = Calendar.getInstance();
	
	com.google.api.services.calendar.model.Event _CreatedEvent = null;
    
    for (int i=0;i<lGuardias.length;i++)
    {
    
    	oGuardias = lGuardias[i];
    	
    	if (_CalendarioGoogle.equals("S"))
    	{
    		
    		
    		
    		Medico _oM = null;
    		_oM = MedicoDBImpl.getMedicos(oGuardias.getIdMedico(),MedicoLogged.getServicioId()).get(0);
    		
    		
    		if (!_Dia.equals("") && !oGuardias.getDiaGuardia().equals(_Dia))  // ha cambiado de dia ???
    		{
	    			 Date _dGuardia = _format.parse(_Dia);
	        		_Guardia.setTimeInMillis(_dGuardia.getTime());
    			
	    			com.google.api.services.calendar.model.Event _Evento = _calendarUtil.CreateEvent(Util._TITULO_EVENTO.replace("{TIPO}", oGuardias.getTipo()), 
	    					Util._DESCRIPCION_EVENTO.replace("{TIPO}", oGuardias.getTipo()).replace("{FECHA}", _Dia), Util._COLOR_EVENTO, _Guardia, _Guardia);
	        		Long _Minutos = Long.parseLong(_CalendarioMinutosRecordatorio);    		
	        		if (!_Minutos.equals(new Long(0)))  // minutos recordatorio si
	        			_Evento = _calendarUtil.SetRemainder(_Evento, _Minutos);
        			// enviamos 
    				 _CreatedEvent =_calendarUtil.SendEvent(_Evento, _lEmails);
    				_lEmails.clear();
    		}
    		_lEmails.add(_oM.getEmail());	 	    		
    		
    		if (_CreatedEvent!=null)
        	{
        		// este es la guardia del dia anterior con calid distinto de "";
    			GuardiasDBImpl.UpdateGuardiasCalId(_CreatedEvent.getId(), _Dia); 
        	}
    		_Dia = oGuardias.getDiaGuardia();
    		
    	}
    	
    	oGuardias.setIdServicio(MedicoLogged.getServicioId());
    	GuardiasDBImpl.AddGuardia(oGuardias);
    	// actualimos las anteriores
/*    	if (_CreatedEvent!=null)
    	{
    		// este es la guardia del dia anterior con calid distinto de "";
    		GuardiasDBImpl.UpdateGuardiasCalId(_Dia);
    	}
    	
  */  	
    }
    /* TIENE QUE HACER EL ULTIMO */
    if (_CalendarioGoogle.equals("S"))
	{
    	
    	 Date _dGuardia = _format.parse(oGuardias.getDiaGuardia());
 		_Guardia.setTimeInMillis(_dGuardia.getTime());
    	
    	com.google.api.services.calendar.model.Event _Evento = _calendarUtil.CreateEvent(Util._TITULO_EVENTO.replace("{TIPO}", oGuardias.getTipo()), 
				Util._DESCRIPCION_EVENTO.replace("{TIPO}", oGuardias.getTipo()).replace("{FECHA}", oGuardias.getDiaGuardia()), Util._COLOR_EVENTO, _Guardia, _Guardia);
		Long _Minutos = Long.parseLong(_CalendarioMinutosRecordatorio);    		
		if (!_Minutos.equals(new Long(0)))  // minutos recordatorio si
			_Evento = _calendarUtil.SetRemainder(_Evento, _Minutos);
		// enviamos 		
		// enviamos 
		 _CreatedEvent =_calendarUtil.SendEvent(_Evento, _lEmails);
    	// este es la guardia del dia anterior con calid distinto de "";
    	if (_CreatedEvent!=null)
        	{
        		// este es la guardia del dia anterior con calid distinto de "";
    			GuardiasDBImpl.UpdateGuardiasCalId(_CreatedEvent.getId(), _Dia); 
        	}
    	
	}
	out.println("OK");	
    } //  end of 	if (lMesAnterior.size()==0 || lMesPosterior.size()>0)
		
	
	
	%>
	
 