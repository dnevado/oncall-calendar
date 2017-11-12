<%@ page language="java" contentType="text/html; charset=UTF-8"     pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.servicios.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.*"%>

<%@page import="com.guardias.mail.*"%>

        
    
<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>

    
<% 	

Long  _ServiceIdJoined = Long.parseLong(request.getParameter("serviceid"));

Medico _AdministradorServicio =null;

Guardias_Servicios oServicio = Guardias_ServiciosDBImpl.getGuardias_ServiciosById(_ServiceIdJoined.intValue());

_AdministradorServicio = MedicoDBImpl.getMedicos(oServicio.getIdMedicoOwner(), oServicio.getIdServicio()).get(0);

MailingUtil.SendJoinRequest(_AdministradorServicio,MedicoLogged, request);


Medico _ServiceJoined = new Medico();
_ServiceJoined = MedicoDBImpl.getMedicos(MedicoLogged.getID(), MedicoLogged.getServicioId()).get(0);
_ServiceJoined.setServicioId(_ServiceIdJoined);
_ServiceJoined.setAdministrator(false);
_ServiceJoined.setActivoServicio(new Long(0));

/* FALTA INSERTAR AL USUARIO COMO MIEMBRO NO ACTIVO */
MedicoDBImpl.AddMedicoAlServicio(_ServiceJoined);
		
%>
	
