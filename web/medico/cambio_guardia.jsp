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
Long  Medico = new Long(-1);
Long  Duracion = new Long(0);


String  DIA_FINAL = "";


Date dINICIO;
Date dFIN;

Calendar _cINICIO;
Calendar _cFIN;

boolean bIsAdministrator = MedicoLogged.isAdministrator();

boolean _ExistenOrigenYDestino =true;


/* TEMPORAL*/ 
Medico = MedicoLogged.getID();

DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");
String ActivoCambioGuardias=  ConfigurationDBImpl.GetConfiguration(Util.getoCONST_ACTIVAR_CAMBIO_GUARDIAS(),MedicoLogged.getServicioId()).getValue();

String bValidadobyAdmin=   ConfigurationDBImpl.GetConfiguration(Util.getoCONST_VALIDAR_CAMBIOS_GUARDIAS_BY_ADM(),MedicoLogged.getServicioId()).getValue();

if(request.getParameter("start")!=null)
	DIA_FINAL = request.getParameter("start");
if(request.getParameter("duration")!=null)
	Duracion = new Long(request.getParameter("duration"));



dINICIO = _format.parse(DIA_FINAL);
dFIN =  _format.parse(DIA_FINAL);

_cINICIO = Calendar.getInstance();
_cINICIO.setTimeInMillis(dINICIO.getTime());

_cFIN =  Calendar.getInstance();
_cFIN.setTimeInMillis(dFIN.getTime());

_cINICIO.add(Calendar.DATE, -Duracion.intValue());

Calendar cHOY = Calendar.getInstance();

List<Guardias> lGuardiaOrigen  = null;
if (!bIsAdministrator)
 	lGuardiaOrigen  = GuardiasDBImpl.getGuardiasMedicoFecha(Medico, _format.format(_cINICIO.getTime()),MedicoLogged.getServicioId());
else
	 lGuardiaOrigen  = GuardiasDBImpl.getGuardiasPorFecha(_format.format(_cINICIO.getTime()),MedicoLogged.getServicioId());

Guardias GuardiaOrigen  = null;
List<Guardias> lGuardiasDestinatarioEnOrigen =null;
if (lGuardiaOrigen!=null && lGuardiaOrigen.size()>0)
{
	

	GuardiaOrigen  = lGuardiaOrigen.get(0);
	/* BUSCAMOS SI LOS DESTINOS, TIENEN GUARDIAS EN ORIGEN */
	/* OJO QUE PUEDE SER QUE NO EXISTA ESA TIPO DE GUARDIA , P.E DE UNA LOCALIZADA A UNA PRESENCIA */
	//List<Guardias> lGuardiaDestino = GuardiasDBImpl.getGuardiasFechaTipo( _format.format(_cFIN.getTime()), GuardiaOrigen.getTipo());
	List<Guardias> lGuardiaDestino = GuardiasDBImpl.getGuardiasPorFecha( _format.format(_cFIN.getTime()),MedicoLogged.getServicioId());
	Guardias GuardiaDestino  = lGuardiaDestino.get(0);
	
	// TIENE ALGUNOS DE LOS DESTINATARIOS GUARDIAS EN ORIGEN ??
	//QUE NO ESTE DE GUARDIA EL DESTINATORIO   EN EL ORIGEN
	//lGuardiasDestinatarioEnOrigen = GuardiasDBImpl.getGuardiasMedicoFecha(GuardiaDestino.getIdMedico(), _format.format(_cINICIO.getTime()));
	if (!bIsAdministrator)
		lGuardiasDestinatarioEnOrigen = GuardiasDBImpl.getGuardiasMedicoFecha(Medico, _format.format(_cINICIO.getTime()),MedicoLogged.getServicioId());
	
	
}
else
	_ExistenOrigenYDestino = false;



// QUE NO ESTE DE GUARDIA EL GENERADOR  EN EL DESTINO
List<Guardias> lGuardiasMedicoDestino = GuardiasDBImpl.getGuardiasMedicoFecha(Medico, _format.format(_cFIN.getTime()),MedicoLogged.getServicioId());

//QUE HAYA GUARDIA GUARDADA EN ESE DIA POR ALGUIEN
List<Guardias> lGuardiasFecha = GuardiasDBImpl.getGuardiasPorFecha(_format.format(_cFIN.getTime()),MedicoLogged.getServicioId());
//VACACIONES POR EL MEDICO 
List<Vacaciones_Medicos> lVacaciones = VacacionesDBImpl.getVacacionesMedicos(Medico, _format.format(_cFIN.getTime()));
// FECHAS PASADAS NO 
boolean bCambioFuturo = cHOY.before(_cINICIO);
// CAMBIO EN ADELANTE,
// LO QUITO, LAS CESIONES PUEDEN SER ANTERIORES 
boolean bFinPosterior = true; //Duracion.intValue()>=1;
// si no existe una solicitud previa 

ResourceBundle RB = ResourceBundle.getBundle(Util.PROPERTIES_FILE, Locale.getDefault());

if (!ActivoCambioGuardias.equals("S")  || DIA_FINAL.equals("") || Duracion.equals(new Long(0)) || Medico.equals(new Long(0)) )
{
	bError=true;
	sError = RB.getString("cambio_guardias.sistema_no_permite");
}

if (!bIsAdministrator && (lGuardiaOrigen==null || lGuardiaOrigen.size()==0))
{
	bError=true;
	sError = RB.getString("cambio_guardias.guardia_activa");
}
/* 
if (lGuardiasDestinatarioEnOrigen!=null && lGuardiasDestinatarioEnOrigen.size()>0)
{
		bError=true;
		sError = "No es posible solicitar este día. Ya posees una guardia configurada para ese dia";
}

if (lGuardiasMedicoDestino!=null && lGuardiasMedicoDestino.size()>0)
{
		bError=true;
		sError = "No es posible solicitar este día. Ya posees una guardia configurada";
}*/
if (lGuardiasFecha==null || lGuardiasFecha.size()==0 )
{
		bError=true;
		sError = RB.getString("cambio_guardias.guardia_enbbdd");
}
if (!bIsAdministrator && lVacaciones!=null &&  lVacaciones.size()>0)
{
		bError=true;
		sError = RB.getString("cambio_guardias.existe_vacaciones"); 
}
if (!bCambioFuturo || !bFinPosterior)
{
		bError=true;
		sError = RB.getString("cambio_guardias.cambios_futuros");
}




if (!bError)
{
	CambiosGuardias oCambioG = new CambiosGuardias();
	
	oCambioG.setIdCambio(new Long(CambiosGuardiasDBImpl.getMaxIDCambiosGuardiasID()));
	
	oCambioG.setEstado(Util.eEstadoCambiosGuardias.PENDIENTE.toString());
	oCambioG.setFechaCreacion(_format.format(cHOY.getTime()));
	oCambioG.setFechaIniCambio(_format.format(_cINICIO.getTime()));
	oCambioG.setFechaFinCambio(_format.format(_cFIN.getTime()));
	if (!bIsAdministrator)
		oCambioG.setIdMedicoSolicitante(Medico);
	
	List<CambiosGuardias> lCambioGuardiasMedicoFecha = CambiosGuardiasDBImpl.getCambioGuardiasByMedicoSolicitanteYFechaYEstado(oCambioG);	
	if (lCambioGuardiasMedicoFecha!=null &&  lCambioGuardiasMedicoFecha.size()>0)
	{
			bError=true;
			sError = RB.getString("cambio_guardias.solicitud_previa");
			out.println("NOOK."+ sError);
	}
	else
	{
		//CambiosGuardiasDBImpl.AddCambioGuardias(oCambioG);
  		 String _EventsJSON = "";
		_EventsJSON += "{\"medico\": \"" + (bIsAdministrator  ? new Long(-1):  Medico) + "";
		_EventsJSON +="\",";
		_EventsJSON += "\"start\": \"" + _format.format(_cINICIO.getTime()) + "\",";
		_EventsJSON += "\"end\": \"" + _format.format(_cFIN.getTime())   + "\"";		 
		_EventsJSON += "}";
		_EventsJSON += "";
		//_EventsJSON=_EventsJSON.replace("},]", "}]");		
		
		
		out.println(_EventsJSON);	
	}
	
	
	
}
else
	out.println("NOOK."+ sError);	












	

	
%>
	
