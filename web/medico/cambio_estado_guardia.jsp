<%@ page language="java" contentType="text/html; charset=UTF-8"     pageEncoding="UTF-8"%>

<%@page import="com.guardias.cambios.*"%>    
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.calendar.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.*"%>

<%@page import="com.guardias.database.GuardiasDBImpl"%>
<%@page import="com.guardias.database.MedicoDBImpl"%>
<%@page import="com.guardias.database.ConfigurationDBImpl"%>

<%@page import="java.util.*"%>
<%@page import="com.guardias.mail.*"%>


<%@page import="java.util.Calendar"%>
<%@page import="java.text.*"%>
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.*"%>
<%@page import="java.util.*"%>
<%@page import="java.util.Map.*"%>

<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>

        
    
<% 	



/* 1. VERIFICAR SI ES UN ADMINISTADOR O MEDICO PARA REVISARLA O APROBARLA
2. CONDICIONES INICIALES / FINALES DE DIA
3. INSERTAR EN TABLA
4. VERIFICACION DE QUE SEA UNA GUARDIA YA EXISTENTE EN BBDD
5. VERIFICAR SI ESTA HABILITADA EN CONFIGURACION EL CAMBIO DE GUARDIAS
6. Y SI NECESITA VALIDARSE POR ALGUIEN 	
7. NO DEJAR EN FECHAS PASADAS
8. QUE PASA PARA MOVER DOS DIAS DE DOS MESES DISTINTOS

9. PENDIENTE, COMO HACE EL ADMINISTRADOR CAMBIAR UN SOLO MEDICO DE GUARDIA, QUIZA , DRAG & DROP  Y DIALOG ELIGIENDO EL MEDICO DEL DIA
10. PENDIENTE FALTA QUE SOLO PUEDAS HACER DRAG DROP EN TUS DIAS, A NO SER QUE SEA ADM, PENDTE
*/

boolean bError  = false;
String  sError  = "";
Long  IDCambio = new Long(0);
String   Estado = "";
Medico _medico = null;




DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");

boolean bIsAdministrator = MedicoLogged.isAdministrator();


/* TEMPORAL */
_medico = MedicoDBImpl.getMedicos(MedicoLogged.getID(),MedicoLogged.getServicioId()).get(0) ;


Long DestinationMedico = Long.parseLong(request.getParameter("MedicoDestination"));
Util.eTipoCambiosGuardias _ChangeType =Util.eTipoCambiosGuardias.valueOf(request.getParameter("type"));



//data: {id: idchange, state:newstate, authmedicoid:authMedicoId, type:Type}, 

Medico oDestinationMedico = MedicoDBImpl.getMedicos(DestinationMedico,MedicoLogged.getServicioId()).get(0);

if(request.getParameter("id")!=null)
	IDCambio = new Long(request.getParameter("id"));
if(request.getParameter("state")!=null)
	Estado = request.getParameter("state");

Util.eEstadoCambiosGuardias _State = Util.eEstadoCambiosGuardias.valueOf(Estado);

CambiosGuardias oCambioG = null;
 	
oCambioG = CambiosGuardiasDBImpl.getCambioGuardiasById(IDCambio.intValue()); 
 	
oCambioG.setEstado(_State.toString());
 	 
oCambioG.setFechaAprobacion(_format.format(new Date()));
 	
oCambioG.setUsuarioAprobacion(_medico.getID());

oCambioG.setIdMedicoDestino(DestinationMedico.equals(new Long(-1)) ? null : DestinationMedico);

oCambioG.setTipoCambio(_ChangeType.toString());

 	
CambiosGuardiasDBImpl.UpdateCambioGuardias(oCambioG);   

/* OJO CON LOS EVENTOS DE GOOGLE CALENDAR  SI HAY CAMBIO, DEBE VERIFICARSE SI HAY EVENTO DE GOOGLE CALENDAR */


/* 1. COGEMOS GUARDIA SOLICITANTE */
/* 1. COGEMOS GUARDIA DESTINATARIO  */
/* CAMBIAMOS FECHAS */

 /* CUANDO ACABE, ENVIAMOS MAIL , SI ESTA CONFIGURADO ASI */
String _CalendarioGoogle = ConfigurationDBImpl.GetConfiguration(Util.getoCALENDARIO_GMAIL(),MedicoLogged.getServicioId()).getValue();
String _CalendarioMinutosRecordatorio = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_CALENDARIO_MINUTOS_RECORDATORIO(),MedicoLogged.getServicioId()).getValue();

CalendarEventUtil _calendarUtil = null;
if (_CalendarioGoogle.equals("S"))
{
		_calendarUtil = new CalendarEventUtil();
	 	_calendarUtil.InitCalendarService();
}


if (oCambioG.getEstado().equals(Util.eEstadoCambiosGuardias.APROBADA.toString()))
{

	List<Guardias> _lGuardiaSolicitante =   GuardiasDBImpl.getGuardiasMedicoFecha(oCambioG.getIdMedicoSolicitante(), oCambioG.getFechaIniCambio(),MedicoLogged.getServicioId());
	Guardias _GuardiaSolicitante = null;
	Guardias _GuardiaDestino = null;
	if (_lGuardiaSolicitante!=null && _lGuardiaSolicitante.size()>0)
	{
		_GuardiaSolicitante = _lGuardiaSolicitante.get(0);
		List<Guardias> _lGuardiaDestino =   GuardiasDBImpl.getGuardiasMedicoFecha(DestinationMedico, oCambioG.getFechaFinCambio(),MedicoLogged.getServicioId());
		if (_lGuardiaDestino!=null && _lGuardiaDestino.size()>0)
		{
			_GuardiaDestino  = _lGuardiaDestino.get(0);
			
			/* AL solicitantnte , HACEMOS LO MISMO QUE AL SOLICITANTE SI LLEVA GOOGLE CALENDAR */
			Medico _oMSolicitante = null;
    		_oMSolicitante = MedicoDBImpl.getMedicos(_GuardiaSolicitante.getIdMedico(),MedicoLogged.getServicioId()).get(0);
    		
    		/* AL DESTINATARIO, HACEMOS LO MISMO QUE AL SOLICITANTE SI LLEVA GOOGLE CALENDAR 
    		Medico _oMDestino = null;
			_oMDestino = MedicoDBImpl.getMedicos(_GuardiaDestino.getIdMedico(),MedicoLogged.getServicioId()).get(0);
			*/ 
			
			/* BORRO CALENDARIO DE GOOGLE SOLICITANTE  */
			if (_GuardiaSolicitante.getIdEventoGCalendar()!=null && !_GuardiaSolicitante.getIdEventoGCalendar().equals(""))
			{
				_calendarUtil.DeleteEventByEventId(_GuardiaSolicitante.getIdEventoGCalendar());
			}
			
			boolean _bCambioTipoResidente = false; // SI SE CAMBIO A UN R4 / R5, HAY QUE DEJARLA LA DEL ADJUNTO COMO LOCALIZADA O EN SU DEFECTO COMO REFUERZO
		
			if (_oMSolicitante.getTipo().toString().equals(Util.eTipo.RESIDENTE.toString()))
			{
				// SOLICITANTE R4 R5 Y DESTINO DISTINTO 
				if ((_oMSolicitante.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R4.toString())  || _oMSolicitante.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R5.toString()))
						&& 	!oDestinationMedico.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R4.toString()) &&  !oDestinationMedico.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R5.toString()))
				{	
					_bCambioTipoResidente =true;
				}
				// DESTINO  R4 R5 Y SOLICITANTE DISTINTO
				if ((oDestinationMedico.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R4.toString())  || oDestinationMedico.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R5.toString()))
						&& 	!_oMSolicitante.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R4.toString()) &&  !_oMSolicitante.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R5.toString()))
				{	
					_bCambioTipoResidente =true;
				}
							
			}
					
			/* SI EL SOLICITANDO ESTA CAMBIANDO  LA GUARDIA ????, NO CESION */
		    if (_ChangeType.equals(Util.eTipoCambiosGuardias.CAMBIO))
		    {
		    	
		    	Long EsFestivoOrigen = _GuardiaSolicitante.isEsFestivo();
		    	String  TipoGuardiaOrigen = _GuardiaSolicitante.getTipo();
		    	
		    	
		    
				/* ALTERO LAS FECHAS Y CALENDAR ID */
				_GuardiaSolicitante.setDiaGuardia(oCambioG.getFechaFinCambio());
				_GuardiaSolicitante.setEsFestivo(_GuardiaDestino.isEsFestivo());
				_GuardiaSolicitante.setIdEventoGCalendar(null);
				_GuardiaSolicitante.setTipo(_GuardiaDestino.getTipo());
				
				/* AÑADIMOS NUEVO CALENDARIO */
				if (_CalendarioGoogle.equals("S"))
				{
				
					com.google.api.services.calendar.model.Event _CreatedEvent = null;
					
					
					Calendar _Guardia = Calendar.getInstance();
					_Guardia.setTimeInMillis(_format.parse(oCambioG.getFechaFinCambio()).getTime());
					
					com.google.api.services.calendar.model.Event _Evento = _calendarUtil.CreateEvent(Util._TITULO_EVENTO.replace("{TIPO}", _GuardiaSolicitante.getTipo()), 
						    					Util._DESCRIPCION_EVENTO.replace("{TIPO}", _GuardiaSolicitante.getTipo()).replace("{FECHA}", _GuardiaSolicitante.getDiaGuardia()),
						    					Util._COLOR_EVENTO, _Guardia, _Guardia);
					
					Long _Minutos = Long.parseLong(_CalendarioMinutosRecordatorio);    		
	        		if (!_Minutos.equals(new Long(0)))  // minutos recordatorio si
	        			_Evento = _calendarUtil.SetRemainder(_Evento, _Minutos);
	    			// enviamos 
	    			List<String> _lEmails = new ArrayList<String>();
	    			_lEmails.add(_oMSolicitante.getEmail());
					 _CreatedEvent =_calendarUtil.SendEvent(_Evento, _lEmails);
					 _lEmails.clear();
					 
					 _GuardiaSolicitante.setIdEventoGCalendar(_CreatedEvent.getId());
	
				}
				
				GuardiasDBImpl.UpdateGuardiasFechayCalId(_GuardiaSolicitante.getIdEventoGCalendar(), _GuardiaSolicitante.getDiaGuardia(), _GuardiaSolicitante.getID(), _GuardiaSolicitante.isEsFestivo(),_GuardiaSolicitante.getTipo());
				if (_bCambioTipoResidente)
				{
				 
					Util.eTipoGuardia _NewTipoGuardia = Util.eTipoGuardia.REFUERZO; // R4 / R5 
					Util.eTipoGuardia _OldTipoGuardia = Util.eTipoGuardia.LOCALIZADA; // R4 / R5
					if (_oMSolicitante.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R4.toString())  || _oMSolicitante.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R5.toString()))
					{
						_NewTipoGuardia = Util.eTipoGuardia.LOCALIZADA; // R4 / R5 
						_OldTipoGuardia = Util.eTipoGuardia.REFUERZO; // R4 / R5 
					}
					
					/*  GURDIAS DE LOS REFUERZOS O LOCALIZADAS SE CAMBIAN */
					List<Guardias>  _lGuardias =  GuardiasDBImpl.getGuardiasFechaTipo(_GuardiaSolicitante.getDiaGuardia(), _OldTipoGuardia.toString(), MedicoLogged.getServicioId());
					for (Guardias oGToChange : _lGuardias)
					{
						oGToChange.setTipo(_NewTipoGuardia.toString().toLowerCase());
						GuardiasDBImpl.UpdateGuardiasFechayCalId(oGToChange.getIdEventoGCalendar(), oGToChange.getDiaGuardia(), oGToChange.getID(), oGToChange.isEsFestivo(),oGToChange.getTipo());

					}
					
					
					
				}
				
				/* AL DESTINATARIO, HACEMOS LO MISMO QUE AL SOLICITANTE SI LLEVA GOOGLE CALENDAR */
				
				/* BORRO CALENDARIO DE GOOGLE DESTINO   */
				if (_GuardiaDestino.getIdEventoGCalendar()!=null && !_GuardiaDestino.getIdEventoGCalendar().equals(""))
				{
					_calendarUtil.DeleteEventByEventId(_GuardiaDestino.getIdEventoGCalendar());
				}
				
				/* ALTERO LAS FECHAS Y CALENDAR ID */				
				
				_GuardiaDestino.setDiaGuardia(oCambioG.getFechaIniCambio());
				_GuardiaDestino.setEsFestivo(EsFestivoOrigen);
				_GuardiaDestino.setIdEventoGCalendar(null);
				_GuardiaDestino.setTipo( TipoGuardiaOrigen);
				
				/* AÑADIMOS NUEVO CALENDARIO */
				if (_CalendarioGoogle.equals("S"))
				{
				
					com.google.api.services.calendar.model.Event _CreatedEvent = null;
					
					
					Calendar _Guardia = Calendar.getInstance();
					_Guardia.setTimeInMillis(_format.parse(oCambioG.getFechaIniCambio()).getTime());
					
					com.google.api.services.calendar.model.Event _Evento = _calendarUtil.CreateEvent(Util._TITULO_EVENTO.replace("{TIPO}", _GuardiaDestino.getTipo()), 
						    					Util._DESCRIPCION_EVENTO.replace("{TIPO}", _GuardiaDestino.getTipo()).replace("{FECHA}", _GuardiaDestino.getDiaGuardia()),
						    					Util._COLOR_EVENTO, _Guardia, _Guardia);
					
					Long _Minutos = Long.parseLong(_CalendarioMinutosRecordatorio);    		
	        		if (!_Minutos.equals(new Long(0)))  // minutos recordatorio si
	        			_Evento = _calendarUtil.SetRemainder(_Evento, _Minutos);
	    			// enviamos 
	    			List<String> _lEmails = new ArrayList<String>();
	    			_lEmails.add(oDestinationMedico.getEmail());
					 _CreatedEvent =_calendarUtil.SendEvent(_Evento, _lEmails);
					 
					 
					 _GuardiaDestino.setIdEventoGCalendar(_CreatedEvent.getId());

				}
				
				
				GuardiasDBImpl.UpdateGuardiasFechayCalId(_GuardiaDestino.getIdEventoGCalendar(), _GuardiaDestino.getDiaGuardia(), _GuardiaDestino.getID(),_GuardiaDestino.isEsFestivo(),_GuardiaDestino.getTipo());
				if (_bCambioTipoResidente)
				{
				 
					Util.eTipoGuardia _NewTipoGuardia = Util.eTipoGuardia.REFUERZO; // R4 / R5 
					Util.eTipoGuardia _OldTipoGuardia = Util.eTipoGuardia.LOCALIZADA; // R4 / R5
					if (oDestinationMedico.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R4.toString())  || oDestinationMedico.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R5.toString()))
					{
						_NewTipoGuardia = Util.eTipoGuardia.LOCALIZADA; // R4 / R5 
						_OldTipoGuardia = Util.eTipoGuardia.REFUERZO; // R4 / R5 
					}
					
					/*  GURDIAS DE LOS REFUERZOS O LOCALIZADAS SE CAMBIAN */
					List<Guardias>  _lGuardias =  GuardiasDBImpl.getGuardiasFechaTipo(_GuardiaDestino.getDiaGuardia(), _OldTipoGuardia.toString(), MedicoLogged.getServicioId());
					for (Guardias oGToChange : _lGuardias)
					{
						oGToChange.setTipo(_NewTipoGuardia.toString().toLowerCase());
						GuardiasDBImpl.UpdateGuardiasFechayCalId(oGToChange.getIdEventoGCalendar(), oGToChange.getDiaGuardia(), oGToChange.getID(), oGToChange.isEsFestivo(),oGToChange.getTipo());

					}
					
					
					
				}
			
			} // FIN DE SI ES UN CAMBIO DE GUARDIA
		    else // es una cesion, le borro la guardia del dia. 
		    {
			    
		    	GuardiasDBImpl.DeleteGuardia(_GuardiaSolicitante.getIdMedico(), _GuardiaSolicitante.getDiaGuardia(),MedicoLogged.getServicioId());
		    	
		    	// falta crear una para el origen puesto que se ha eliminado.
		    	
		    	Guardias oGuardiaCesion= new Guardias();		    	
		    	
		    	//oGuardiaCesion.setIdCambio(new Long(CambiosGuardiasDBImpl.getMaxIDCambiosGuardiasID()));
		    	oGuardiaCesion.setDiaGuardia(_GuardiaSolicitante.getDiaGuardia());
		    	oGuardiaCesion.setEsFestivo(_GuardiaSolicitante.isEsFestivo());
		    	oGuardiaCesion.setIdMedico(DestinationMedico);
		    	oGuardiaCesion.setTipo(_GuardiaSolicitante.getTipo());
		    	oGuardiaCesion.setIdEventoGCalendar(null);
		    	oGuardiaCesion.setIdServicio(MedicoLogged.getServicioId());  	  	
		    	
		    	if (_CalendarioGoogle.equals("S"))
				{
				
					com.google.api.services.calendar.model.Event _CreatedEvent = null;
					
					
					Calendar _Guardia = Calendar.getInstance();
					_Guardia.setTimeInMillis(_format.parse(_GuardiaSolicitante.getDiaGuardia()).getTime());
					
					com.google.api.services.calendar.model.Event _Evento = _calendarUtil.CreateEvent(Util._TITULO_EVENTO.replace("{TIPO}", oGuardiaCesion.getTipo()), 
						    					Util._DESCRIPCION_EVENTO.replace("{TIPO}", oGuardiaCesion.getTipo()).replace("{FECHA}", oGuardiaCesion.getDiaGuardia()),
						    					Util._COLOR_EVENTO, _Guardia, _Guardia);
					
					Long _Minutos = Long.parseLong(_CalendarioMinutosRecordatorio);    		
	        		if (!_Minutos.equals(new Long(0)))  // minutos recordatorio si
	        			_Evento = _calendarUtil.SetRemainder(_Evento, _Minutos);
	    			// enviamos 
	    			List<String> _lEmails = new ArrayList<String>();
	    			_lEmails.add(oDestinationMedico.getEmail());
					 _CreatedEvent =_calendarUtil.SendEvent(_Evento, _lEmails);
					 
					 
					 oGuardiaCesion.setIdEventoGCalendar(_CreatedEvent.getId());

				}
		    	
		    	//oGuardiaCesion.setIdEventoGCalendar(idEventoGCalendar)
		    	
		    	GuardiasDBImpl.AddGuardia(oGuardiaCesion);
		    	
		    	if (_bCambioTipoResidente)
				{
				 
					Util.eTipoGuardia _NewTipoGuardia = Util.eTipoGuardia.REFUERZO; // R4 / R5 
					Util.eTipoGuardia _OldTipoGuardia = Util.eTipoGuardia.LOCALIZADA; // R4 / R5
					if (oDestinationMedico.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R4.toString())  || oDestinationMedico.getSubTipoResidente().toString().equals(Util.eSubtipoResidente.R5.toString()))
					{
						_NewTipoGuardia = Util.eTipoGuardia.LOCALIZADA; // R4 / R5 
						_OldTipoGuardia = Util.eTipoGuardia.REFUERZO; // R4 / R5 
					}
					
					/*  GURDIAS DE LOS REFUERZOS O LOCALIZADAS SE CAMBIAN */
					List<Guardias>  _lGuardias =  GuardiasDBImpl.getGuardiasFechaTipo(oGuardiaCesion.getDiaGuardia(), _OldTipoGuardia.toString(), MedicoLogged.getServicioId());
					for (Guardias oGToChange : _lGuardias)
					{
						oGToChange.setTipo(_NewTipoGuardia.toString().toLowerCase());
						GuardiasDBImpl.UpdateGuardiasFechayCalId(oGToChange.getIdEventoGCalendar(), oGToChange.getDiaGuardia(), oGToChange.getID(), oGToChange.isEsFestivo(),oGToChange.getTipo());

					}
					
					
					
				}
		    	
		    	
		    	
		    }
			
			
		
		
			
		}
	
	}
	
	
	
}

/* 	FIN DE ACTUALIZACION DE ESTADOS Y MOVIMIENTO DE DIA */
// enviamos email  administrador e usuarios 
List<Medico> lAdministradores =  MedicoDBImpl.getMedicosAdministradores(MedicoLogged.getServicioId());
String[] Emails;
List<String> lEmails = new ArrayList<String>();	
for (Medico oMedico : lAdministradores)
{
	lEmails.add(oMedico.getEmail());
}
Medico oSolicitante = MedicoDBImpl.getMedicos(oCambioG.getIdMedicoSolicitante(),MedicoLogged.getServicioId()).get(0);
Medico oDestino=null;
if (oCambioG.getIdMedicoDestino()!=null)
{
	oDestino = MedicoDBImpl.getMedicos(oCambioG.getIdMedicoDestino(),MedicoLogged.getServicioId()).get(0);
	lEmails.add(oDestino.getEmail());
}
lEmails.add(oSolicitante.getEmail());


MailingUtil.SendScheduleChangeNotification(lEmails.parallelStream().toArray(String[]::new), oCambioG, true);
if (oDestino!=null)
	out.println(oDestinationMedico.getNombre() + " " + oDestinationMedico.getApellidos());
	

	










	

	
%>
	
