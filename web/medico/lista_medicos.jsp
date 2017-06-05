<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.*"%>

<script>

var _DoctorTableList;



function fn_callOrdenDoctorList() {
	//Return a helper with preserved width of cells	
	var fixHelper = function(e, ui) {  
		  ui.children().each(function() {  
		    $(this).width($(this).width());  
		  });  
		  return ui;  
		};
	
	$( "#doctor_list tbody" ).sortable({
	    axis: 'y',
	    helper: fixHelper,
	    update: function (event, ui) {
	        var data = $("#doctor_list tbody").sortable('serialize');
	    //    alert(data);
	        alert("Elemento reordenado satisfactoriamente");
	        
	        // POST to server using $.post or $.ajax
	        $.ajax({
	            data: data,
	            type: 'POST',
	            url: '<%=request.getContextPath()%>/medico/medico_orden.jsp',
	        });
	        
	        
	    }
	});
	
	$( "#doctor_list tbody" ).disableSelection();
} 



 $( document ).ready(function() {			
	 fn_callOrdenDoctorList();
	 _DoctorTableList=$("#doctor_list").DataTable( {
		    responsive: true
	 } );
}); 

</script>

 <div class="row">
<div class="col-lg-12 form-group">
        <button onclick="EditarMedico(-1)" type="button" class="pull-right btn btn-outline btn-warning">Nuevo</button>                    
</div>
</div>	
	
<table id="doctor_list" class="table responsive table-bordered table-hover table-striped"  style="width: 100%;"  role="grid">
<thead>
    <tr>
    	<th>#</th>
        <th>Ident.</th>
        <th>Nombre Apellidos</th>               
        <th>Email</th>
        <th>Máx Guardias</th>
        <th>Confirmado</th>
        <th></th>
        <th>Activo</th>
        <th></th>
    </tr>
</thead>
<tbody>
<%
	
	List<Medico> lItems = new ArrayList<Medico>();
	
	String _Path = request.getServletContext().getRealPath("/") + "//medicos.xml";
	
//	System.out.println(_Path);
	
	

	ProcesarMedicos oUtilMedicos = new ProcesarMedicos();
	
	
	//lItems = oUtilMedicos.LeerMedicos(_Path,true);
	lItems =  MedicoDBImpl.getMedicos();
	
	//lItems = 
	
	for (int j=0;j<lItems.size();j++)
	{
		
		Medico oMedico = lItems.get(j);		
	%>
	
	   <!--  <a href="#" class="list-group-item">
			                                    <i class="fa fa-money fa-fw"></i> Payment Received
			                                    <span class="pull-right text-muted small"><em>Yesterday</em>
			                                    </span>
			                                </a> -->
	
	
    <tr class="ui-state-default"  id="id_<%=oMedico.getID()%>_medico">
    <td><%=oMedico.getOrden()%></td>    
    <td><%=oMedico.getIDMEDICO()%></td>
    <td><%=oMedico.getNombre()%> <%=oMedico.getApellidos()%></td>
    <td><%=oMedico.getEmail()%></td>
    <td><%=oMedico.getMax_NUM_Guardias()%></td>
    <td><%=oMedico.isConfirmado() ? 'S' : 'N'%></td>
    <td><%=oMedico.getTipo()%></td>
    <td><%=oMedico.isActivo() ? 'S' : 'N'%></td>
    <td><a class="ui-widget ui-corner-all" href="javascript:EditarMedico(<%=oMedico.getID()%>)">Editar</a></td>   
    </tr>
		
	<% }
	%>	
	</tbody>
</table>
	<!--  <div id="editarmedico"  title="Datos del Médico"></div>-->