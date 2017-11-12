<%@page import="com.guardias.servicios.Guardias_Servicios"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.*"%>
<%@page import="java.text.*"%>

<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>


<script>

var _DoctorTableList;

function _go(Url)
{
	$(location).attr('href',Url);
}


 $( document ).ready(function() {			
	 
	 _DoctorTableList=$("#service_list").DataTable( {
		    responsive: true
	 } );
}); 

</script>

 <div class="row">
<div class="col-lg-12 form-group">
        <button onclick="_go('<%=request.getContextPath()%>/servicio.jsp?id=-1')" type="button" class="pull-right btn btn-outline btn-warning">Nuevo</button>                    
</div>
</div>	
	
<table id="service_list" class="table responsive table-bordered table-hover table-striped"  style="width: 100%;"  role="grid">
<thead>
    <tr>    	
        <th>Código</th>
        <th>Titulo</th>               
        <th>Descripción</th>
        <th title=" Activo">Activo</th>
        <th title=" Visible">Visible</th>
        <th> </th>              
    </tr>
</thead>
<tbody>
<%
	
	List<Guardias_Servicios> lItems = new ArrayList<Guardias_Servicios>();
	
		
	//lItems = oUtilMedicos.LeerMedicos(_Path,true);
	lItems =  Guardias_ServiciosDBImpl.getGuardias_ServiciosByOwner(MedicoLogged.getID().intValue()); 
	
	
	
	
	for (int j=0;j<lItems.size();j++)
	{
		
		Guardias_Servicios oGuardias_Servicios = lItems.get(j);
		
	
		
	%>
	
	
	
	
    <tr  class="ui-state-default">
    <td><%=oGuardias_Servicios.getCodigoInterno()%></td>    
    <td><a href='<%=request.getContextPath()%>/servicio.jsp?id=<%=oGuardias_Servicios.getIdServicio() %>'><%=oGuardias_Servicios.getNombre()%></a></td>
    <td><%=oGuardias_Servicios.getDescripcion()%></td>        
    <td><%=oGuardias_Servicios.getActivo().equals(new Long(1)) ? 'S' : 'N'%></td>
    <td><%=oGuardias_Servicios.getVisible().equals(new Long(1)) ? 'S' : 'N'%></td>    
    <td id="<%=oGuardias_Servicios.getIdServicio()%>_action" class="dropdown">
    	  <button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">Opciones
		  <span class="caret"></span></button>
		  <ul class="dropdown-menu">
		    <li><a href="servicio.jsp?id=<%=oGuardias_Servicios.getIdServicio() %>">Editar</a></li>		    		   
		    <!--  li><a onclick ="">Borrar</a></li>-->
		    <li><a href="javascript:_changeServicio('<%=oGuardias_Servicios.getIdServicio()%>',false);EditarMedico(-1)">Agregar Miembro</a></li>
		  </ul>
		   
	</td>    
    </tr>
	
	<% }
	%>	
	</tbody>
</table>
	<!--  <div id="editarmedico"  title="Datos del Médico"></div>-->