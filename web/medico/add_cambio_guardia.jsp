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
DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");
oCambioGuardias.setFechaCreacion(_format.format(cHOY.getTime()));
oCambioGuardias.setIdCambio(new Long(CambiosGuardiasDBImpl.getMaxIDCambiosGuardiasID()));

if (bIsAdministrator)
	oCambioGuardias.setEstado(Util.eEstadoCambiosGuardias.APROBADA.toString());


List<Guardias> lGuardiaOrigen  = GuardiasDBImpl.getGuardiasMedicoFecha(oCambioGuardias.getIdMedicoSolicitante(), oCambioGuardias.getFechaIniCambio());
List<Guardias> lGuardiaDestino = GuardiasDBImpl.getGuardiasMedicoFecha(oCambioGuardias.getIdMedicoDestino(), oCambioGuardias.getFechaFinCambio());;
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

if (oCambioGuardias.getTipoCambio().equals(Util.eTipoCambiosGuardias.CAMBIO.toString()))
{
	/* BUSCAMOS SI LOS DESTINOS, TIENEN GUARDIAS EN ORIGEN */
	/* OJO QUE PUEDE SER QUE NO EXISTA ESA TIPO DE GUARDIA , P.E DE UNA LOCALIZADA A UNA PRESENCIA */
	//List<Guardias> lGuardiaDestino = GuardiasDBImpl.getGuardiasFechaTipo( _format.format(_cFIN.getTime()), GuardiaOrigen.getTipo());

	//QUE NO ESTE DE GUARDIA EL GENERADOR  EN EL DESTINO
	  
	lGuardiasSolicitanteEnDestino = GuardiasDBImpl.getGuardiasMedicoFecha(oCambioGuardias.getIdMedicoSolicitante(), oCambioGuardias.getFechaFinCambio());
	lGuardiasDestinatarioEnOrigen = GuardiasDBImpl.getGuardiasMedicoFecha(oCambioGuardias.getIdMedicoDestino(), oCambioGuardias.getFechaIniCambio());
	
	if (lGuardiasSolicitanteEnDestino!=null && lGuardiasSolicitanteEnDestino.size()>0)
	{	
			sError = RB.getString("cambio_guardias.existe_guardia_existente_medico"); 
	}

	if (lGuardiasDestinatarioEnOrigen!=null && lGuardiasDestinatarioEnOrigen.size()>0)
	{	
			sError = RB.getString("cambio_guardias.existe_guardia_existente_medico");
			
	}
	
	
}

if (oCambioGuardias.getTipoCambio().equals(Util.eTipoCambiosGuardias.CESION.toString()))
{

	// TIENE ALGUNOS DE LOS DESTINATARIOS GUARDIAS EN ORIGEN ??
	//QUE NO ESTE DE GUARDIA EL DESTINATORIO   EN EL ORIGEN
	//lGuardiasDestinatarioEnOrigen = GuardiasDBImpl.getGuardiasMedicoFecha(GuardiaDestino.getIdMedico(), _format.format(_cINICIO.getTime()));
	lGuardiasDestinatarioEnOrigen = GuardiasDBImpl.getGuardiasMedicoFecha(oCambioGuardias.getIdMedicoDestino(), oCambioGuardias.getFechaIniCambio());

}

if (GuardiaOrigen==null ||  GuardiaDestino==null || !sError.equals(""))
	out.println("NOOK." + sError);
else
{
	
	/* OJO, SI ES EL ADMINISTRADOR QUIEN LA ESTA GENERANDO EL ALTA, SE AUTOCONFIRMA */
	
	CambiosGuardiasDBImpl.AddCambioGuardias(oCambioGuardias);	
	// enviamos email  administrador e usuarios 
	List<Medico> lAdministradores =  MedicoDBImpl.getMedicosAdministradores();
	String[] Emails;
	List<String> lEmails = new ArrayList<String>();	
	for (Medico oMedico : lAdministradores)
	{
		lEmails.add(oMedico.getEmail());
	}
	Medico oSolicitante = MedicoDBImpl.getMedicos(oCambioGuardias.getIdMedicoSolicitante()).get(0);
	Medico oDestino = MedicoDBImpl.getMedicos(oCambioGuardias.getIdMedicoDestino()).get(0);
	
	lEmails.add(oSolicitante.getEmail());
	lEmails.add(oDestino.getEmail());
	
	MailingUtil.SendScheduleChangeNotification(lEmails.parallelStream().toArray(String[]::new), oCambioGuardias, false);
	
	Gson gsonOUTPUT = new GsonBuilder().create();
	String jsonInString = gsonOUTPUT.toJson(oCambioGuardias,CambiosGuardias.class);
	 
	
	out.println(jsonInString);
	
	//out.println(RB.getString("general.success") + "<br>ID:[" + oCambioGuardias.getIdCambio() + "]");
	
}



%>