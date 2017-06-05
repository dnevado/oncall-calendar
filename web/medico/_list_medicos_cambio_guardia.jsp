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

<%@page import="java.util.Calendar"%>
<%@page import="java.text.*"%>
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.*"%>
<%@page import="java.util.*"%>
<%@page import="java.util.Map.*"%>

<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>



        
    
<%

/* PUEDE SER UNA SOLICITUD O UNA CONFIRMACION */
Util.eEstadoCambiosGuardias _oNewState;
Integer _IDCambioGuardia = new Integer(-1);
_oNewState = Util.eEstadoCambiosGuardias.PENDIENTE;

boolean bIsAdministrator = MedicoLogged.isAdministrator();

Long IdSolicitante = new Long(-1);
if (request.getParameter("idsolicitante")!=null)
	IdSolicitante = Long.parseLong(request.getParameter("idsolicitante"));

Util.eTipoCambiosGuardias  _cType = Util.eTipoCambiosGuardias.CAMBIO;
CambiosGuardias _oCambio; 

if (request.getParameter("type")!=null)	
	_cType = Util.eTipoCambiosGuardias.valueOf(request.getParameter("type"));

if (request.getParameter("cambioid")!=null && !request.getParameter("cambioid").equals("-1"))
{
	_oNewState = Util.eEstadoCambiosGuardias.valueOf(request.getParameter("newstate"));
	_IDCambioGuardia = Integer.parseInt(request.getParameter("cambioid"));
	_oCambio = CambiosGuardiasDBImpl.getCambioGuardiasById(_IDCambioGuardia);
	
}
else
{
	_oCambio = new CambiosGuardias();
	_oCambio.setIdMedicoSolicitante(IdSolicitante);
	_oCambio.setFechaIniCambio(request.getParameter("inicio"));
	_oCambio.setFechaFinCambio(request.getParameter("fin"));
	
}


Medico _oMedicoSolicitante = null;
List<Guardias> _lGuardiasDiaOrigen=null;
if (!bIsAdministrator)
	_oMedicoSolicitante = MedicoDBImpl.getMedicos(_oCambio.getIdMedicoSolicitante()).get(0);
else
	_lGuardiasDiaOrigen =  GuardiasDBImpl.getGuardiasEntreFechas(_oCambio.getFechaIniCambio(), _oCambio.getFechaIniCambio());
	
/* MEDICOS POR TIPO QUE HAGAN GUARDIA EN EL DIA FIN */
List<Guardias> _lGuardiasDiaDestino =  GuardiasDBImpl.getGuardiasEntreFechas(_oCambio.getFechaFinCambio(), _oCambio.getFechaFinCambio());

if (_lGuardiasDiaDestino!=null && _lGuardiasDiaDestino.size()>0)
{
%>

<form id="fmedicos" method="post" role="form" data-toggle="validator" onsubmit="return _saveChangeOnCall()"  novalidate="true">                                     						
<!-- SUCCESS  -->
<div id="success" class="alert alert-success" style="display:none">
<div class="panel-body">
    <p>Los datos han sido guardados satisfactoriamente</p>
</div>                                               
</div> 
<!--  SUCCESS -->
<div  id=error  class="alert alert-danger" style="display:none">   
   <div class="panel-body">
       <p>Se ha producido un error. Contacte con el administrador</p>
   </div>                                               
</div>
<div class="form-group">
	<label class="control-label" id="INI">De:</label>
	<input class=" ui-text"   type="text"name="tINI" value="<%=_oCambio.getFechaIniCambio() %>" disabled="disabled" readonly="readonly"> 
	<label class="control-label" id="FIN">A:</label>
	<input  class="ui-text"  type="text"name="tFIN" value="<%=_oCambio.getFechaFinCambio() %>" readonly="readonly" disabled="disabled" > 
</div>             

<% 
if (_lGuardiasDiaOrigen!=null && !_lGuardiasDiaOrigen.isEmpty())
{%>
	<div class="form-group">
	<label class="control-label" id="labelresidente">Médico solicitante:</label>
	<select class="form-control ui-select" name="medicosolicitante" id="medicosolicitante" onchange="fn_ChangeDoctorType()">
	<%	for (Guardias oGuardia : _lGuardiasDiaOrigen)  // quitamos el medico que lo solicito
		{
			
			Medico oMedico = MedicoDBImpl.getMedicos(oGuardia.getIdMedico()).get(0);
			String _ValueMedico = oMedico.getID() + "|" + oMedico.getTipo();
			// activo , del mismo tipo y que no sea el mismo */
			
			String _NombreMedico = oMedico.getNombre().concat(" ").concat(oMedico.getApellidos());							
			%>
			
				<option value="<%=_ValueMedico%>"><%= _NombreMedico%></option>			
			<%
			
		}	%>					 
	</select>						
	</div>
<%	
}
%>

					

																
<div class="form-group">
<label class="control-label" id="type">Tipo:</label>
<select class="form-control ui-select" name="tipocambio" id="tipocambio">
<option <%=(_cType!=null && _cType.equals(Util.eTipoCambiosGuardias.CAMBIO) ? "selected" : "") %> value="<%=Util.eTipoCambiosGuardias.CAMBIO%>"><%=Util.eTipoCambiosGuardias.CAMBIO%></option>
<option <%=(_cType!=null && _cType.equals(Util.eTipoCambiosGuardias.CESION) ? "selected" : "") %> value="<%=Util.eTipoCambiosGuardias.CESION%>"><%=Util.eTipoCambiosGuardias.CESION%></option>
</select>
</div>							
<div class="form-group">
<label class="control-label" id="labelresidente">Elige quién será asignado:</label>
<select class="form-control ui-select" name="medico" id="medico">
<%	for (Guardias oGuardia : _lGuardiasDiaDestino)  // quitamos el medico que lo solicito
	{
		
		Medico oMedico = MedicoDBImpl.getMedicos(oGuardia.getIdMedico()).get(0);
		String _NombreMedico = oMedico.getNombre().concat(" ").concat(oMedico.getApellidos());
		String _ValueMedico = oMedico.getID() + "|" + oMedico.getTipo();
		// activo , del mismo tipo y que no sea el mismo */
		if (bIsAdministrator) {  
			if (oMedico.isActivo()) {%>
			<option value="<%=_ValueMedico%>"><%= _NombreMedico%></option>		
		<%	} 
		} else
			{
				if (!oMedico.getID().equals(_oMedicoSolicitante.getID()) && oMedico.isActivo() && oMedico.getTipo().equals(_oMedicoSolicitante.getTipo()))
				{								
			%>	
				<option <%=(_oCambio.getIdMedicoDestino()!=null && _oCambio.getIdMedicoDestino().equals(oMedico.getID()) ?  "selected" : "") %> value="<%=_ValueMedico%>"><%= _NombreMedico%></option>
						
			<%
				}
			}	
	}	%>					 
</select>						
</div>												
<div class="form-group">
		<input type="hidden"  name="idsolicitante" id="solicitante">
		<input type="hidden"  name="inicio" id="inicio">
		<input type="hidden"  name="fin" id="fin">		
		<button id="enviar" type="submit" class="btn btn-block  btn-primary">Guardar</button>
	</div>				
</form>
<%  } %>

<script>


$(document).ready( function () {fn_ChangeDoctorType() } )
		
/* CUANDO CAMBIE EL SELECTOR DE SOLICITANTE, DESHABILITAMOS LOS QUE NO SEAN DEL MISMO TIPO */
function fn_ChangeDoctorType()
{
	// ID|TIPO
	var _ValueDoctor = $("#medicosolicitante").val();
	var Type = _ValueDoctor.split("\|")[1];
	$("#medico option").each(function( index ) {
		 var_otherType=$( this ).val().split("\|")[1];
		 var _disabled=false;
		 if (var_otherType!=Type) // no coinciden, deshabilito, coinciden, habilito
			 _disabled=true;
		 $( this ).prop('disabled', _disabled);	 
		});
	
}


function _saveChangeOnCall()
{
	//debugger;

	<%
	// nueva 
	if (_IDCambioGuardia.equals(new Integer("-1")))
	{%>
	
	var Cambio = new Object();
	if (<%=IdSolicitante%>!=-1)	
		Cambio.IdMedicoSolicitante = '<%=_oCambio.getIdMedicoSolicitante()%>';
	else
		Cambio.IdMedicoSolicitante = $("#medicosolicitante").val().split("\|")[0]; // ID SOLO 
	Cambio.FechaIniCambio = '<%=_oCambio.getFechaIniCambio()%>';
	Cambio.FechaFinCambio =  '<%=_oCambio.getFechaFinCambio()%>';
	Cambio.IdMedicoDestino = $("#medico").val().split("\|")[0]; // ID SOLO 
	Cambio.Estado = '<%=_oNewState%>';
	Cambio.TipoCambio = $("#tipocambio").val();
	

	var JSONChangeData = JSON.stringify(Cambio);
	//var JSONChangeData= 
	_addChange (JSONChangeData);
	<% }
	else
	{%>
	_changeState ('<%=_oNewState%>', <%=_IDCambioGuardia%>,$("#medico").val().split("\|")[0],'<%=_cType%>');
	<%}
	%>
	
	return false;
}

</script>
	
