<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.Medico"%>    
<%@page import="com.guardias.cambios.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="com.guardias.Util"%>

<%@page import="java.util.*"%>
<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>

<script>

 $( document ).ready(function() {			
	 
	 var Table = $("#cambios").DataTable( {	    responsive: true } );
	 
	    
}); 

</script>




	
<table id="cambios" class="table responsive table-bordered table-hover table-striped"  style="width: 100%;"  role="grid">
<thead>
    <tr>    	   
    	<th></th>     
        <th>De</th>               
        <th>A</th>
        <th>Tipo</th>
        <th>Destinatario</th>
        <th>Solicitud</th>
        <th>Aprobación</th>        
        <th>Estado</th>        
        <th></th>
    </tr>
</thead>
<tbody>
<%

	boolean bIsAdministrator = MedicoLogged.isAdministrator();
	
	List<CambiosGuardias> lItems = null;
	
    // DISTINGUIR ADMINISTRADOR O NO
    CambiosGuardias _oCambio = new CambiosGuardias(); 
    _oCambio.setIdServicio(MedicoLogged.getServicioId());
     if (!bIsAdministrator)
    	lItems = CambiosGuardiasDBImpl.getCambioGuardiasByMedicoSolicitante(MedicoLogged.getID().intValue());
    else
    	lItems = CambiosGuardiasDBImpl.getCambiosGuardia(_oCambio);

    if (lItems!=null && !lItems.isEmpty())
    {
    	
    
	for (int j=0;j<lItems.size();j++)
	{
		
		CambiosGuardias oCambio= lItems.get(j);
		Medico Solicitante = MedicoDBImpl.getMedicos(oCambio.getIdMedicoSolicitante(),MedicoLogged.getServicioId()).get(0);
		
		String _MedicoDestinatario = "";
		Medico Destinatario =null;
		if (oCambio.getIdMedicoDestino()!=null)
		{
			Destinatario = MedicoDBImpl.getMedicos(oCambio.getIdMedicoDestino(),MedicoLogged.getServicioId()).get(0);
			_MedicoDestinatario = Destinatario.getNombre().concat(" ").concat(Destinatario.getApellidos()); 
		}
		
		
		
		
		
		
	%>
    <tr class="ui-state-default"  id="<%=oCambio.getIdCambio()%>">
    <td><%=Solicitante.getNombre() + " " + Solicitante.getApellidos() %></td> 
    <td><%=oCambio.getFechaIniCambio()%></td>    
    <td><%=oCambio.getFechaFinCambio()%></td>
    <td id="<%=oCambio.getIdCambio()%>_tipocambio"><%=(oCambio.getTipoCambio()!=null ? oCambio.getTipoCambio().toString() : "")%></td>
    <td id="<%=oCambio.getIdCambio()%>_destinatario"><%=_MedicoDestinatario%></td>
    <td><%=oCambio.getFechaCreacion()%></td>
    <%
     String Aprobacion= oCambio.getFechaAprobacion()!=null ? oCambio.getFechaAprobacion() : "";     
     %>       
    <td id="<%=oCambio.getIdCambio()%>_aprobacion"><%=Aprobacion%></td>
    <td id="<%=oCambio.getIdCambio()%>_estado"><%=oCambio.getEstado()%></td>
        
    <td id="<%=oCambio.getIdCambio()%>_action" class="dropdown">
    	  <% if (bIsAdministrator && oCambio.getEstado().equals(Util.eEstadoCambiosGuardias.PENDIENTE.toString())) { %>	
    	  <button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">Confirmar
		  <span class="caret"></span></button>
		  <ul class="dropdown-menu">
		    <li><a onclick =_state('<%=Util.eEstadoCambiosGuardias.APROBADA%>',<%=oCambio.getIdCambio()%>,'<%=oCambio.getTipoCambio()%>','<%=oCambio.getIdMedicoDestino()%>') href="#">APROBAR</a></li>		    		   
		    <li><a onclick =_state('<%=Util.eEstadoCambiosGuardias.CANCELADA%>',<%=oCambio.getIdCambio()%>,'<%=oCambio.getTipoCambio()%>','<%=oCambio.getIdMedicoDestino()%>') href="#">CANCELAR</a></li>
		  </ul>
		  <% } %> 
	</td>    
    </tr>
		
	<% }
    }
	%>	
	</tbody>
</table>

<div id=confirmar_cambio></div>	