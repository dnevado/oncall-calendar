<%@page import="com.guardias.mail.MailingUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"     pageEncoding="UTF-8"%>

<%@page import="com.guardias.cambios.*"%>    
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.Guardias"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.*"%>

<%@page import="com.guardias.database.GuardiasDBImpl"%>
<%@page import="com.guardias.database.MedicoDBImpl"%>
<%@page import="com.guardias.database.ConfigurationDBImpl"%>

<%@page import="java.util.*"%>

<%@page import="java.util.Calendar"%>
<%@page import="java.text.*"%>
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.*"%>
<%@page import="java.util.*"%>
<%@page import="java.util.Map.*"%>


<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.Locale"%>
 <%@page import="com.google.gson.*"%>

<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.Locale"%>
 


<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>

        
    
<% 	


ResourceBundle RB = ResourceBundle.getBundle(Util.PROPERTIES_FILE, Locale.getDefault());

boolean bIsAdministrator = MedicoLogged.isAdministrator();

CambiosGuardias oCambioGuardias;

Gson gson = new GsonBuilder().create();
oCambioGuardias = gson.fromJson(request.getParameter("datachange"), CambiosGuardias.class); 


Calendar cHOY = Calendar.getInstance();
DateFormat _format = new SimpleDateFormat(Util._FORMATO_FECHA);
oCambioGuardias.setFechaCreacion(_format.format(cHOY.getTime()));
oCambioGuardias.setIdCambio(new Long(CambiosGuardiasDBImpl.getMaxIDCambiosGuardiasID()));


oCambioGuardias.setIdServicio(MedicoLogged.getServicioId());

if (bIsAdministrator)
	oCambioGuardias.setEstado(Util.eEstadoCambiosGuardias.APROBADA.toString());


List<Guardias> lGuardiaOrigen  = GuardiasDBImpl.getGuardiasMedicoFecha(oCambioGuardias.getIdMedicoSolicitante(), oCambioGuardias.getFechaIniCambio(),MedicoLogged.getServicioId());
List<Guardias> lGuardiaDestino = GuardiasDBImpl.getGuardiasMedicoFecha(oCambioGuardias.getIdMedicoDestino(), oCambioGuardias.getFechaFinCambio(),MedicoLogged.getServicioId());
Guardias GuardiaOrigen  = null;
Guardias GuardiaDestino  = null;

List<Guardias> lGuardiasDestinatarioEnOrigen =null;
List<Guardias> lGuardiasSolicitanteEnDestino =null;

//que exista la guardia del medico en el origen
if (lGuardiaOrigen!=null && lGuardiaOrigen.size()>0)
		GuardiaOrigen  = lGuardiaOrigen.get(0);
if (lGuardiaDestino!=null && lGuardiaDestino.size()>0)
	GuardiaDestino  = lGuardiaDestino.get(0);

String sError="";

Medico oSolicitante = MedicoDBImpl.getMedicos(oCambioGuardias.getIdMedicoSolicitante(), MedicoLogged.getServicioId()).get(0);
Medico oDestino = MedicoDBImpl.getMedicos(oCambioGuardias.getIdMedicoDestino(),MedicoLogged.getServicioId()).get(0);




if (oCambioGuardias.getTipoCambio().equals(Util.eTipoCambiosGuardias.CAMBIO.toString()))
{
	/* BUSCAMOS SI LOS DESTINOS, TIENEN GUARDIAS EN ORIGEN */
	/* OJO QUE PUEDE SER QUE NO EXISTA ESA TIPO DE GUARDIA , P.E DE UNA LOCALIZADA A UNA PRESENCIA */
	//List<Guardias> lGuardiaDestino = GuardiasDBImpl.getGuardiasFechaTipo( _format.format(_cFIN.getTime()), GuardiaOrigen.getTipo());

	//QUE NO ESTE DE GUARDIA EL GENERADOR  EN EL DESTINO
	  
	lGuardiasSolicitanteEnDestino = GuardiasDBImpl.getGuardiasMedicoFecha(oCambioGuardias.getIdMedicoSolicitante(), oCambioGuardias.getFechaFinCambio(),MedicoLogged.getServicioId());
	lGuardiasDestinatarioEnOrigen = GuardiasDBImpl.getGuardiasMedicoFecha(oCambioGuardias.getIdMedicoDestino(), oCambioGuardias.getFechaIniCambio(),MedicoLogged.getServicioId());
	
	if (lGuardiasSolicitanteEnDestino!=null && lGuardiasSolicitanteEnDestino.size()>0)
	{	
			sError = RB.getString("cambio_guardias.existe_guardia_existente_medico"); 
	}

	if (lGuardiasDestinatarioEnOrigen!=null && lGuardiasDestinatarioEnOrigen.size()>0)
	{	
			sError = RB.getString("cambio_guardias.existe_guardia_existente_medico");
			
	}
	// solicitante que cambia esta el dia fin de vacaciones   
	List<Vacaciones_Medicos> lVacacionesSOLICITANTE = VacacionesDBImpl.getVacacionesMedicos(oCambioGuardias.getIdMedicoSolicitante(), oCambioGuardias.getFechaFinCambio());
	// DESTIONO EN EL ORIGEN DE VACACIONES 
	List<Vacaciones_Medicos> lVacacionesDESTINATARIO = VacacionesDBImpl.getVacacionesMedicos(oCambioGuardias.getIdMedicoDestino(), oCambioGuardias.getFechaIniCambio());

	if (lVacacionesSOLICITANTE!=null && lVacacionesSOLICITANTE.size()>0)
	{	
			//cambio_guardias.existe_vacaciones_medico_fecha=No es posible realizar el cambio. {MEDICO} está de vacaciones el día {FECHA} 
			sError = RB.getString("cambio_guardias.existe_vacaciones_medico_fecha");
	//		WelcomeMessage=Welcome Mr. ${firstName} ${lastName} !!!
			Map<String, String> valuesMap = new HashMap<String, String>();
			sError = sError.replace("{MEDICO}", oSolicitante.getNombre() + " " + oSolicitante.getApellidos());
			sError = sError.replace("{FECHA}", oCambioGuardias.getFechaFinCambio());
		
			
			
			
	}
	if (lVacacionesDESTINATARIO!=null && lVacacionesDESTINATARIO.size()>0)
	{	
			//cambio_guardias.existe_vacaciones_medico_fecha=No es posible realizar el cambio. {MEDICO} está de vacaciones el día {FECHA} 
			sError = RB.getString("cambio_guardias.existe_vacaciones_medico_fecha");
	//		WelcomeMessage=Welcome Mr. ${firstName} ${lastName} !!!
			Map<String, String> valuesMap = new HashMap<String, String>();
			sError = sError.replace("{MEDICO}", oDestino.getNombre() + " " + oDestino.getApellidos());
			sError = sError.replace("{FECHA}", oCambioGuardias.getFechaIniCambio());
		
			
	}
	
	// SIENDO UN CAMBIO, QUE NO DEJE AL QUE LA CEDE TENER GUARDIA EL DIA ANTES O DESPUES DE MANERA PRESENCIA  QUITANDO AL SIMULADO
	Calendar cGuardiaDIASCONSECUTIVOS = Calendar.getInstance();
	
	cGuardiaDIASCONSECUTIVOS.setTime(_format.parse(oCambioGuardias.getFechaFinCambio()));
	// SUMAMOS UN DIA 
	cGuardiaDIASCONSECUTIVOS.add(Calendar.DATE,1);	
	List<Guardias> lGuardiaDIADESPUES = GuardiasDBImpl.getGuardiasMedicoFechaTipo(oSolicitante.getID(), _format.format(cGuardiaDIASCONSECUTIVOS.getTime()), Util.eTipoGuardia.PRESENCIA.toString(),MedicoLogged.getServicioId());
	
	// que sea de presencia el cambio tambien 
	if (!oSolicitante.isResidenteSimulado()  &&  lGuardiaDIADESPUES!=null && lGuardiaDIADESPUES.size()>0 && GuardiaDestino.getTipo().toUpperCase().equals(Util.eTipoGuardia.PRESENCIA.toString()))
	{	
			//cambio_guardias.existe_vacaciones_medico_fecha=No es posible realizar el cambio. {MEDICO} está de vacaciones el día {FECHA} 
			//cambio_guardias.existe_presencia_medico_fecha_antes=No es posible realizar el cambio. {MEDICO} está de presencia el día anterior a la fecha del cambio {FECHA}
			//cambio_guardias.existe_presencia_medico_fecha_despues=No es posible realizar el cambio. {MEDICO} está de presencia el día posterior a la fecha del cambio {FECHA}
			
			sError = RB.getString("cambio_guardias.existe_presencia_medico_fecha_despues");
	//		WelcomeMessage=Welcome Mr. ${firstName} ${lastName} !!!
			Map<String, String> valuesMap = new HashMap<String, String>();
			sError = sError.replace("{MEDICO}", oSolicitante.getNombre() + " " + oSolicitante.getApellidos());
			sError = sError.replace("{FECHA}",_format.format(cGuardiaDIASCONSECUTIVOS.getTime()));
		
			
	}

	
	//cGuardiaDIASCONSECUTIVOS.setTime(_format.parse(oCambioGuardias.getFechaIniCambio()));
	Calendar _cDiaAnterior = Calendar.getInstance();
	_cDiaAnterior.setTimeInMillis(cGuardiaDIASCONSECUTIVOS.getTimeInMillis());
	_cDiaAnterior.setTime(_format.parse(oCambioGuardias.getFechaIniCambio()));
	
	// RESTAMOS  UN DIA 
	cGuardiaDIASCONSECUTIVOS.setTime(_format.parse(oCambioGuardias.getFechaFinCambio()));
	cGuardiaDIASCONSECUTIVOS.add(Calendar.DATE,-1);	
	
	/* NO CONTROLAMOSEL DIA ANTES  SI SON CORRELATIVAS */
	if (cGuardiaDIASCONSECUTIVOS.compareTo(_cDiaAnterior)!=0)
	{
		List<Guardias> lGuardiaDIAANTES = GuardiasDBImpl.getGuardiasMedicoFechaTipo(oSolicitante.getID(), _format.format(cGuardiaDIASCONSECUTIVOS.getTime()), Util.eTipoGuardia.PRESENCIA.toString(),MedicoLogged.getServicioId());
		
		if (!oSolicitante.isResidenteSimulado() && lGuardiaDIAANTES!=null && lGuardiaDIAANTES.size()>0 && GuardiaDestino.getTipo().toUpperCase().equals(Util.eTipoGuardia.PRESENCIA.toString()))
		{	
				//cambio_guardias.existe_vacaciones_medico_fecha=No es posible realizar el cambio. {MEDICO} está de vacaciones el día {FECHA} 
				//cambio_guardias.existe_presencia_medico_fecha_antes=No es posible realizar el cambio. {MEDICO} está de presencia el día anterior a la fecha del cambio {FECHA}
				//cambio_guardias.existe_presencia_medico_fecha_despues=No es posible realizar el cambio. {MEDICO} está de presencia el día posterior a la fecha del cambio {FECHA}
				
				sError = RB.getString("cambio_guardias.existe_presencia_medico_fecha_antes");
		//		WelcomeMessage=Welcome Mr. ${firstName} ${lastName} !!!
				Map<String, String> valuesMap = new HashMap<String, String>();
				sError = sError.replace("{MEDICO}", oSolicitante.getNombre() + " " + oSolicitante.getApellidos());
				sError = sError.replace("{FECHA}", _format.format(cGuardiaDIASCONSECUTIVOS.getTime()));
			
				
		}
	}
	
	
	
	
	
	
	
}

if (oCambioGuardias.getTipoCambio().equals(Util.eTipoCambiosGuardias.CESION.toString()))
{

	// TIENE ALGUNOS DE LOS DESTINATARIOS GUARDIAS EN ORIGEN ??
	//QUE NO ESTE DE GUARDIA EL DESTINATORIO   EN EL ORIGEN
	//lGuardiasDestinatarioEnOrigen = GuardiasDBImpl.getGuardiasMedicoFecha(GuardiaDestino.getIdMedico(), _format.format(_cINICIO.getTime()));
	lGuardiasDestinatarioEnOrigen = GuardiasDBImpl.getGuardiasMedicoFecha(oCambioGuardias.getIdMedicoDestino(), oCambioGuardias.getFechaIniCambio(),MedicoLogged.getServicioId());
	
	// VACACIONES DEL RECEPTOR DE LA GUARDIA EN EL ORIGEN
	List<Vacaciones_Medicos> lVacacionesDESTINATARIO = VacacionesDBImpl.getVacacionesMedicos(oCambioGuardias.getIdMedicoDestino(), oCambioGuardias.getFechaIniCambio());
	
	if (lVacacionesDESTINATARIO!=null && lVacacionesDESTINATARIO.size()>0)
	{	
			//cambio_guardias.existe_vacaciones_medico_fecha=No es posible realizar el cambio. {MEDICO} está de vacaciones el día {FECHA} 
			sError = RB.getString("cambio_guardias.existe_vacaciones_medico_fecha");
	//		WelcomeMessage=Welcome Mr. ${firstName} ${lastName} !!!
			Map<String, String> valuesMap = new HashMap<String, String>();
			sError = sError.replace("{MEDICO}", oDestino.getNombre() + " " + oDestino.getApellidos());
			sError = sError.replace("{FECHA}", oCambioGuardias.getFechaIniCambio());
		
			
	}
	

}


///SIEMPRE VERIFICAMOS , QUE NO DEJE AL QUE LA DISFRUTA  TENER GUARDIA EL DIA ANTES O DESPUES DE MANERA PRESENCIA 
Calendar cGuardiaDIASCONSECUTIVOS = Calendar.getInstance();

cGuardiaDIASCONSECUTIVOS.setTime(_format.parse(oCambioGuardias.getFechaIniCambio()));
// SUMAMOS UN DIA , SIEMPRE QUE NO SEAN CORRELATIVOS 
cGuardiaDIASCONSECUTIVOS.add(Calendar.DATE,1);	

Calendar _cDiaSiguiente = Calendar.getInstance();
_cDiaSiguiente.setTimeInMillis(cGuardiaDIASCONSECUTIVOS.getTimeInMillis());
_cDiaSiguiente.setTime(_format.parse(oCambioGuardias.getFechaFinCambio()));


/* VERIFICAMOS EL DIA SIGUIENTE DE LA FECHA INICIO CUANDO EL INICIO Y EL FIN NO SEA CORRELATIVOS */
if (_cDiaSiguiente.compareTo(cGuardiaDIASCONSECUTIVOS)!=0)
{

	List<Guardias> lGuardiaDIAANTES = GuardiasDBImpl.getGuardiasMedicoFechaTipo(oDestino.getID(), _format.format(cGuardiaDIASCONSECUTIVOS.getTime()), Util.eTipoGuardia.PRESENCIA.toString(),MedicoLogged.getServicioId());

	if (!oSolicitante.isResidenteSimulado() && lGuardiaDIAANTES!=null && lGuardiaDIAANTES.size()>0 && GuardiaOrigen.getTipo().toUpperCase().equals(Util.eTipoGuardia.PRESENCIA.toString()))
	{	
		//cambio_guardias.existe_vacaciones_medico_fecha=No es posible realizar el cambio. {MEDICO} está de vacaciones el día {FECHA} 
		//cambio_guardias.existe_presencia_medico_fecha_antes=No es posible realizar el cambio. {MEDICO} está de presencia el día anterior a la fecha del cambio {FECHA}
		//cambio_guardias.existe_presencia_medico_fecha_despues=No es posible realizar el cambio. {MEDICO} está de presencia el día posterior a la fecha del cambio {FECHA}
		
		/* verificamos que no sea de presencia 
		Guardias _gAntes = lGuardiaDIAANTES.get(0);
		if (_gAntes.getTipo().equals(Util.eTipoGuardia.PRESENCIA) && oCambioGuardias.getTipoCambio().equals(Util.eTipoGuardia.PRESENCIA))  // presencia - presencia, error 
		{
			*/
			sError = RB.getString("cambio_guardias.existe_presencia_medico_fecha_despues");
			Map<String, String> valuesMap = new HashMap<String, String>();
			sError = sError.replace("{MEDICO}", oDestino.getNombre() + " " + oDestino.getApellidos());
			sError = sError.replace("{FECHA}",_format.format(cGuardiaDIASCONSECUTIVOS.getTime()));
		
	//	}
	}
}


// RESTAMOS  UN DIA 
cGuardiaDIASCONSECUTIVOS.setTime(_format.parse(oCambioGuardias.getFechaIniCambio()));
cGuardiaDIASCONSECUTIVOS.add(Calendar.DATE,-1);	
List<Guardias> lGuardiaDIADESPUES = GuardiasDBImpl.getGuardiasMedicoFechaTipo(oDestino.getID(), _format.format(cGuardiaDIASCONSECUTIVOS.getTime()), Util.eTipoGuardia.PRESENCIA.toString(),MedicoLogged.getServicioId());

if (!oSolicitante.isResidenteSimulado()  && lGuardiaDIADESPUES!=null && lGuardiaDIADESPUES.size()>0 && GuardiaOrigen.getTipo().toUpperCase().equals(Util.eTipoGuardia.PRESENCIA.toString()))
{	
	
		/* verificamos que no sea de presencia 
		Guardias _gDIADESPUES = lGuardiaDIADESPUES.get(0);
		if (_gDIADESPUES.getTipo().equals(Util.eTipoGuardia.PRESENCIA) && oCambioGuardias.getTipoCambio().equals(Util.eTipoGuardia.PRESENCIA))  // presencia - presencia, error 
		{*/
			sError = RB.getString("cambio_guardias.existe_presencia_medico_fecha_antes");
	//		WelcomeMessage=Welcome Mr. ${firstName} ${lastName} !!!
			Map<String, String> valuesMap = new HashMap<String, String>();
			sError = sError.replace("{MEDICO}", oDestino.getNombre() + " " + oDestino.getApellidos());
			sError = sError.replace("{FECHA}", _format.format(cGuardiaDIASCONSECUTIVOS.getTime()));
	//	}
	
		
}







if (GuardiaOrigen==null ||  GuardiaDestino==null || !sError.equals(""))
	out.println("NOOK." + sError);
else
{
	
	/* OJO, SI ES EL ADMINISTRADOR QUIEN LA ESTA GENERANDO EL ALTA, SE AUTOCONFIRMA */
	
	CambiosGuardiasDBImpl.AddCambioGuardias(oCambioGuardias);	
	// enviamos email  administrador e usuarios 
	List<Medico> lAdministradores =  MedicoDBImpl.getMedicosAdministradores(MedicoLogged.getServicioId());
	String[] Emails;
	List<String> lEmails = new ArrayList<String>();	
	for (Medico oMedico : lAdministradores)
	{
		lEmails.add(oMedico.getEmail());
	}
	
	lEmails.add(oSolicitante.getEmail());
	lEmails.add(oDestino.getEmail());
	
	MailingUtil.SendScheduleChangeNotification(lEmails.parallelStream().toArray(String[]::new), oCambioGuardias, false);
	
	Gson gsonOUTPUT = new GsonBuilder().create();
	String jsonInString = gsonOUTPUT.toJson(oCambioGuardias,CambiosGuardias.class);
	 
	
	out.println(jsonInString);
	
	//out.println(RB.getString("general.success") + "<br>ID:[" + oCambioGuardias.getIdCambio() + "]");
	
}



%>