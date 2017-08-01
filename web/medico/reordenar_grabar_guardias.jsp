<%@ page language="java" contentType="text/html; charset=UTF-8"     pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.*"%>        
<%@page import="com.google.gson.*"%>

<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>


    
<% 	
	
	Long IDMedico = Long.parseLong(request.getParameter("lastidmedico"));
    
    List<Medico> lMedico = MedicoDBImpl.getMedicos(IDMedico,MedicoLogged.getServicioId());
    
   // Long TOTAL = MedicoDBImpl.getUltimoOrden(Util.eTipo.ADJUNTO.toString());
    
    
    
    List<Medico> lMedicos = MedicoDBImpl.getMedicos(new Long(-1),MedicoLogged.getServicioId());
    List<Medico> _lAdjuntos  =ProcesarMedicos.getAdjuntos(lMedicos, false);
    
    
    Medico oMedico = lMedico.get(0);
   
    MedicoDBImpl.OrdenarSecuenciasMedicoUltimaGuardia(oMedico.getOrden(),new Long(_lAdjuntos.size()), Util.eTipo.ADJUNTO.toString());
	
%>
	
