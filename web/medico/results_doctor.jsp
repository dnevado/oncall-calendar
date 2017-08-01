<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.*"%>

<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>


<% 
	List<Medico> lItems = new ArrayList<Medico>();
	
	String _Path = request.getServletContext().getRealPath("/") + "//medicos.xml";
	
//	System.out.println(_Path);
	
	ProcesarMedicos oUtilMedicos = new ProcesarMedicos();
	
	
	lItems = MedicoDBImpl.getMedicos(new Long(-1),MedicoLogged.getServicioId());
	List<Medico> lResidentes = oUtilMedicos.getResidentes(lItems);
	List<Medico> lAdjuntos = oUtilMedicos.getAdjuntos(lItems, true);
	

	 %>
	
<!--  METEMOS DOS TABLAS, ADJUNTOS Y RESIDENTES  -->		
<table class="table responsive table-bordered table-hover table-striped"  style="width: 100%;"  role="grid">
<thead>
    <tr role="row">
        <th></th>
        <th>Nombre Apellidos</th>
        <th>Presencias</th>
        <th>Localizadas</th>
        <th>Refuerzos</th>
        <th>Presencias F.</th>
        <th>Localizadas F.</th>
        <th>Refuerzos F.</th>                
        <th>Total</th>
        <th>Simulados</th>
    </tr>
</thead>
<tbody>
<%
	
		
	//lItems = 
	
	for (int j=0;j<lAdjuntos.size();j++)
	{
		
		Medico oMedico = lAdjuntos.get(j);
		//if (!oMedico.getTipo().equals(Util.eTipo.ADJUNTO)) continue;
	%>
	
    <tr role="row" id="<%=oMedico.getID()%>">
    <td class="datad"></td>
    <td><a href="javascript:EditarMedico(<%=oMedico.getID()%>)"><%=oMedico.getNombre()%> <%=oMedico.getApellidos()%></a> </td>
    <td class="mespresencia"></td>
    <td class="meslocalizada"></td>
    <td class="mesrefuerzo"></td>
    <td class="mespresenciaf"></td>
    <td class="meslocalizadaf"></td>
    <td class="mesrefuerzof"></td>
    <td class="mestotaladjunto"></td>
    <td class="messimulados_adjunto"></td>
    </tr>
		
	<% }
	%>	
	<tbody>
</table>
<!--  METEMOS DOS TABLAS, ADJUNTOS Y RESIDENTES  -->	
<table class="table responsive table-bordered table-hover table-striped">
<thead>
    <tr>
        <th></th>
        <th>Nombre Apellidos</th>
        <th>Guardias Diario</th>
        <th>Festivos</th>                       
        <th>Total</th>
    </tr>
</thead>
<tbody>
<%
	
		
	//lItems = 
	
	for (int j=0;j<lResidentes.size();j++)
	{
		
		Medico oMedico = lResidentes.get(j);		
	%>
	
    <tr id="<%=oMedico.getID()%>">
    <td class="datad"></td>
    <td><a href="javascript:EditarMedico(<%=oMedico.getID()%>)"><%=oMedico.getNombre()%> <%=oMedico.getApellidos()%></a></td>
    <td class="mesdiario"></td>
    <td class="mesfestivos"></td>    
    <td class="mestotalresidente"></td>
    </tr>
		
	<% }
	%>	
	<tbody>
</table>
<script>$('tresultsd').DataTable( {   responsive: true} );</script>
