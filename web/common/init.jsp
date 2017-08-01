<%@ page language="java" contentType="text/html; charset=UTF-8"     pageEncoding="UTF-8"%> 
<%@page import="com.guardias.database.MedicoDBImpl"%>
    
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.text.SimpleDateFormat"%> 
<%@page import="java.util.*"%>
 
 <script src='<%=request.getContextPath()%>/js/guardias.js?timestamp=3131236574567567'></script>
 <script src='<%=request.getContextPath()%>/js/form/validator.js'></script>
 <script src='<%=request.getContextPath()%>/js/jquery-ui.multidatespicker.js'></script>
 <script>var _RESIDENTE ='<%= Util.eTipo.RESIDENTE.toString().toLowerCase()%>';</script>
 <script>var _SIMULADO ='<%= Util.eSubtipoResidente.SIMULADO.toString()%>';</script>
 
 <%
  String UserLogged = (String) request.getSession().getAttribute("User");

  Medico MedicoLogged = MedicoDBImpl.getMedicoByEmail(UserLogged, new Long(-1));
  
  request.setAttribute("MedicoLogged", MedicoLogged);
  
 
 %>
 
  <div id="editarmedico"  title="Datos del Médico"></div>
  
  <script>  var obj = {};
	var _CANCELADA= '<%=Util.eEstadoCambiosGuardias.CANCELADA.toString()%>';
	var _APROBADA= '<%=Util.eEstadoCambiosGuardias.APROBADA.toString()%>';
	var _PRESENCIA = '<%=Util.eTipoGuardia.PRESENCIA.toString().toLowerCase()%>';
	var _LOCALIZADA= '<%=Util.eTipoGuardia.LOCALIZADA.toString().toLowerCase()%>';
	var _REFUERZO= '<%=Util.eTipoGuardia.REFUERZO.toString().toLowerCase()%>';
	var _ADJUNTO= '<%=Util.eTipo.ADJUNTO.toString().toLowerCase()%>';
	var _RESIDENTE= '<%=Util.eTipo.RESIDENTE.toString().toLowerCase()%>';
	var _SIMULADO= '<%=Util.eSubtipoResidente.SIMULADO.toString().toLowerCase()%>';		
	var _REQUEST_CONTEXT ='<%=request.getContextPath()%>/';
	var _REQUEST_URI ="<%=request.getRequestURI()%>";
	var _USER_LOGGED ='<%=MedicoLogged.getID()%>';		
	var _IS_A = $.parseJSON('<%=MedicoLogged.isAdministrator()%>');

</script>
  
 