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

  Medico MedicoLogged = MedicoDBImpl.getMedicoByEmail(UserLogged);
  
  request.setAttribute("MedicoLogged", MedicoLogged);
 
 %>
   
  <div id="editarmedico"  title="Datos del Médico"></div> 