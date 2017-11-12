<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@page import="com.guardias.*"%>
<%@page import="com.guardias.servicios.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.*"%>


<jsp:useBean id="MedicoLogged" class="com.guardias.Medico"
	scope="session" />


<%

	/* LLEGA CUANDO AÑADES MIEMBROS, LE METO COMO ADMINISTRADOR DEL SERVICIO */

	/* VERIFICAMOS QUE REALMENTE PERTENEZCA AL SERVICIO */
	
	boolean bPertenece =false;

	Long NewParamService = new Long(-1);
	if (request.getParameter("serviceid")!=null)
		NewParamService =  Long.parseLong(request.getParameter("serviceid"));

	List<Guardias_Servicios> lItems =  Guardias_ServiciosDBImpl.getListGuardias_ServiciosOfUser(MedicoLogged.getID().intValue()); 

	for (Guardias_Servicios _oServicioUser : lItems)
	{
		if (_oServicioUser.getIdServicio().equals(NewParamService))
		{
			bPertenece = true;
			break;
		}
	}
	
	
	if (bPertenece)
	{
		
	
		Medico UserLogged = (Medico) request.getSession().getAttribute("MedicoLogged");
		UserLogged.setServicioId(NewParamService);
		UserLogged.setAdministrator(true);
		request.getSession().setAttribute("MedicoLogged", UserLogged);
	
	
	}

	/* RCOOKIE RECORDATORIO 
	Cookie cookie = new Cookie("ServicioId",UserLogged.getServicioId().toString());
	
	//cookie.setHttpOnly(true);
	cookie.setMaxAge(60 * 60 * 24 * 365);
	
	//cookie.setMaxAge(0);
	response.addCookie(cookie);*/
%>




