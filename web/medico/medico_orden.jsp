<%@ page language="java" contentType="text/html; charset=UTF-8"     pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.*"%>        
    
    
<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>

    
<% 	


	/*String _Path = request.getServletContext().getRealPath("/") + "//medicos.xml";	
	ProcesarMedicos oUtilMedicos = new ProcesarMedicos();
	
   */
	Enumeration<String> parameterNames = request.getParameterNames();
	
	int j=1;
	while (parameterNames.hasMoreElements()) {
	
	  	String paramName = parameterNames.nextElement();	
	
	    String paramValues = request.getParameter(paramName);
	    
	    //out.println (paramName);
	    
	    String ID_MEDICO= paramName.replace("id_", "").replace("[]","");
	    
	    //out.println (ID_MEDICO);
	    
	    List<Medico> lMedico = MedicoDBImpl.getMedicos(Long.parseLong(ID_MEDICO), MedicoLogged.getServicioId());
	
	    Medico _oMedico = lMedico.get(0);
	    
	    _oMedico.setOrden(new Long(j));
	    
	    MedicoDBImpl.UpdateMedico(Long.parseLong(ID_MEDICO), _oMedico, false);	    
	    
	    //oUtilMedicos.GrabarMedico(_Path, ID_MEDICO, _oMedico);
	    
	    j++;
	
	}
	
	//lItems = oUtilMedicos.LeerMedicos(_Path);
	 
	
/*	for (int j=0;j<lItems.size();j++)
	{
		Medico oMedico = lItems.get(j);
		
		
		
	}
*/
		
	%>
	
