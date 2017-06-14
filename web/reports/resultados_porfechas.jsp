<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.GuardiasReportingUtilContainer"%>    
<%@page import="com.guardias.Guardias"%>
<%@page import="com.guardias.Medico"%>    
<%@page import="com.guardias.cambios.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="com.guardias.Util"%>

<%@page import="java.util.*"%>
<%@page import="java.text.*"%>
 

<%

/* PASAMOS A  YYYY-MM-DD
SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
*/
String desde  = request.getParameter("desde");
String hasta  = request.getParameter("hasta");
/*
Date Ddesde = sdf.parse(desde);
Date Dhasta = sdf.parse(hasta);

desde  = sdf2.format(Ddesde);
hasta  = sdf2.format(Dhasta);
*/




%>

<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>


<script>

 $( document ).ready(function() {			
	 var Table = $("#report1").DataTable( {	    responsive: true, pageLength: 200,  order: [[ 1, "asc" ],[ 0, "asc" ]] } );
}); 
</script>
		
<table id="report1" class="table responsive table-bordered table-hover table-striped"  style="width: 100%;"  role="grid">
<thead>
   <tr role="row">
        <th>Nombre Apellidos</th>
        <th>Tipo</th>
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

	List<Guardias> lItems = null;
    List<Medico> lMedicos = null;
    
    boolean bIsAdministrator = MedicoLogged.isAdministrator();
    if (bIsAdministrator )
    {
    	lItems = GuardiasDBImpl.getReportGuardiasEntreFechas(desde, hasta);	
    }
    else    
    	lItems = GuardiasDBImpl.getReportGuardiasEntreFechasMedico(desde, hasta,MedicoLogged.getID());

    GuardiasReportingUtilContainer oReport = new GuardiasReportingUtilContainer();
    
    List<GuardiasReportingUtilContainer> lGuardiasReport = oReport.getTotalByDoctor(lItems);
	
	for (int j=0;j<lGuardiasReport.size();j++)
	{
		
		GuardiasReportingUtilContainer oGuardia = lGuardiasReport.get(j);
		
		Long Total = oGuardia.getPresencias() + oGuardia.getLocalizadas() + oGuardia.getRefuerzos() +
							oGuardia.getPresenciasF() + oGuardia.getLocalizadasF() + oGuardia.getRefuerzosF();
								 
		
		
	%>
	
	<tr role="row">	    
	    <td><%=oGuardia.getApellidos()%> <%=oGuardia.getNombre()%> </td>
	    <td ><%=oGuardia.getTipoMedico()%></td>
	    <td ><span class="badge"><%=oGuardia.getPresencias().equals(new Long(0)) ? "" : oGuardia.getPresencias() %></span></td>
	    <td ><span class="badge"><%=oGuardia.getLocalizadas().equals(new Long(0)) ? "" : oGuardia.getLocalizadas() %></span></td>
	    <td ><span class="badge"><%=oGuardia.getRefuerzos().equals(new Long(0)) ? "" : oGuardia.getRefuerzos() %></span></td>
	    <td ><span class="badge"><%=oGuardia.getPresenciasF().equals(new Long(0)) ? "" : oGuardia.getPresenciasF() %></span></td>
	    <td ><span class="badge"><%=oGuardia.getLocalizadasF().equals(new Long(0)) ? "" : oGuardia.getLocalizadasF() %></span></td>
	    <td ><span class="badge"><%=oGuardia.getRefuerzosF().equals(new Long(0)) ? "" : oGuardia.getRefuerzosF() %></span></td>
	    <td ><span class="badge"><%=Total.equals(new Long(0)) ? "" : Total %></span></td>
	    <td ><span class="badge"><%=oGuardia.getTotalSimulados().equals(new Long(0)) ? "" : oGuardia.getTotalSimulados() %></span></td>
    </tr>
	
	<% }
	%>	
	</tbody>
</table>

	