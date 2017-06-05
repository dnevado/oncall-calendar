<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.Medico"%>
<%@page import="com.guardias.database.*"%> 
<%@page import="java.util.*"%>
<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>


<!DOCTYPE html>
<html lang="es">

<head>

<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">

<title>Distribución Guardias Médicos Inicio </title>
<link href='<%=request.getContextPath()%>/css/bootstrap.min.css' rel='stylesheet' />
<link href='<%=request.getContextPath()%>/css/jquery-ui.css' rel='stylesheet' />
<!-- <link href="<%=request.getContextPath()%>/vendor/datatables-plugins/dataTables.bootstrap.css" rel="stylesheet"> -->
<link href="<%=request.getContextPath()%>/vendor/datatables-responsive/dataTables.responsive.css" rel="stylesheet">
<link href='<%=request.getContextPath()%>/css/fullcalendar.css' rel='stylesheet' />
<link href='<%=request.getContextPath()%>/css/custom.css?er4544423423423' rel='stylesheet'/> 
<link href="<%=request.getContextPath()%>/vendor/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">
<link href="<%=request.getContextPath()%>/dist/css/sb-admin-2.css" rel="stylesheet">	
<link href="<%=request.getContextPath()%>/css/introjs.css" rel="stylesheet">
<link href="<%=request.getContextPath()%>/css/introjs-modern.css" rel="stylesheet">
<script src='<%=request.getContextPath()%>/js/lib/moment.min.js'></script>
<script src='<%=request.getContextPath()%>/js/lib/jquery.min.js'></script>
<script src='<%=request.getContextPath()%>/js/bootstrap.min.js'></script>
<script src='<%=request.getContextPath()%>/js/jquery-ui.js'></script>
<script src='<%=request.getContextPath()%>/js/jquery.ui.es.js'></script>
<script src='<%=request.getContextPath()%>/js/fullcalendar.min.js'></script>
<script src='<%=request.getContextPath()%>/js/locale-all.js'></script>
<script src="<%=request.getContextPath()%>/vendor/datatables/js/jquery.dataTables.patched.es.js"></script>
<!--  <script src="<%=request.getContextPath()%>/vendor/datatables-plugins/dataTables.bootstrap.min.js"></script>-->
<script src="<%=request.getContextPath()%>/vendor/datatables-responsive/dataTables.responsive.js"></script>
<script src='<%=request.getContextPath()%>/js/guardias.js?timestamp=3131236574567567'></script>
<script src='<%=request.getContextPath()%>/js/interact.min.js'></script>
<script src='<%=request.getContextPath()%>/js/ga.js'></script>
<script src='<%=request.getContextPath()%>/js/intro.js'></script>



</head>
<script>  var obj = {};
		var _CANCELADA= '<%=Util.eEstadoCambiosGuardias.CANCELADA.toString()%>';
		var _APROBADA= '<%=Util.eEstadoCambiosGuardias.APROBADA.toString()%>';
		var _PRESENCIA = '<%=Util.eTipoGuardia.PRESENCIA.toString().toLowerCase()%>';
		var _LOCALIZADA= '<%=Util.eTipoGuardia.LOCALIZADA.toString().toLowerCase()%>';
		var _REFUERZO= '<%=Util.eTipoGuardia.REFUERZO.toString().toLowerCase()%>';
		var _ADJUNTO= '<%=Util.eTipo.ADJUNTO.toString().toLowerCase()%>';
		var _RESIDENTE= '<%=Util.eTipo.RESIDENTE.toString().toLowerCase()%>';
		var _SIMULADO= '<%=Util.eSubtipoResidente.SIMULADO.toString().toLowerCase()%>';		
		var _REQUEST_CONTEXT ='<%=request.getContextPath()%>/';
		var _REQUEST_URI ="<%=request.getRequestURI()%>";
		var _USER_LOGGED ='<%=MedicoLogged.getID()%>';		
		var _IS_A = $.parseJSON('<%=MedicoLogged.isAdministrator()%>');

</script>

<%

/* por defecto de la base de datos si hay */
String _CalendarioGoogle = ConfigurationDBImpl.GetConfiguration(Util.getoCALENDARIO_GMAIL()).getValue();	
String _PageToFill="get_guardias.jsp";
String _SelectedMonth = "";
boolean _bCalculating = false;

if (request.getParameter("fecha")!=null) 
{		
		_PageToFill="fill_agenda.jsp";
		_SelectedMonth = request.getParameter("fecha");
}
if (request.getParameter("start_calc")!=null)  // se rellena con los huecos  
{
	_PageToFill="calc_agenda.jsp";
	_SelectedMonth = request.getParameter("start_calc");
	_bCalculating = true;
	
	if (request.getParameter("festivo1")!=null)
	{%>
		<script>
					obj["start_calc"] = '<%=request.getParameter("start_calc")%>';	
					obj["end_calc"] = '<%=request.getParameter("end_calc")%>';
	   </script>
<%
		for (int j=1;j<=31;j++)
		{
			String _param = "poolday" + j;
			String _param1 = "festivo" + j;
			//out.println (request.getParameter(_param1));
			if (request.getParameter(_param1)!=null)
			{
%>
				<script>
					obj["poolday<%=j%>"] = '<%=request.getParameter(_param)%>';	
					obj["festivo<%=j%>"] = '<%=request.getParameter(_param1)%>';
				</script>
<%
			}
		}
	}
}	
%>
<script>


var _Start;
var _Duration;
var cancelDrag = false;


function _DropEvent(event, delta, revertFunc){
	
	if (cancelDrag)
	{
		
		// si viene del drop de eventos internos, la tengo, de externos, no
		try
		 {
			revertFunc();
		 }
		catch(e){}
		
		return false;
	}
	
	
 	console.log(event.title + " was dropped on " + event.start.format());
		
     $("#modalCambioGuardias").modal("show");
     
     _Start =  event.start.format();
     _Duration =  delta.days();
     /* QUITAMOS LAS LLAMADAS PREVIAS */
     $('#btCancelChange').off('click');
     $('#btCancelChange').unbind("click");	        
     
     $('#btAcceptChange').off('click');
     $('#btAcceptChange').unbind("click");
     
     
     $('#btCancelChange').on('click',function(evt){
     	$("#modalCambioGuardias").modal ("hide");
     	try
		 {
			revertFunc();
		 }
		catch(e){}
 		return false;
     });
     
     $('#btAcceptChange').on('click',function(evt){
     	
     	
     	$("#modalCambioGuardias").modal ("hide");
     	    try
     	    {
    			revertFunc();
    			// borramos de la zona de drag 
    			$('#external-events .fc-event').each(function() {
    				$(this).remove();
    			});
    		}
    		catch(e){}
			$.ajax({
			data: 'start=' + _Start + '&duration=' + _Duration,
		    //contentType: "application/json",
		    dataType: "json",
           type: 'POST',
           url: '<%=request.getContextPath()%>/medico/cambio_guardia.jsp',
	        complete: function(data) {
	        	 	
	        	/* 	dataRESULT = JSON.stringify(data.responseText);
	        		console.log(JSON.stringify(dataRESULT.medico)); */
	        		if (data.responseText.indexOf("NOOK")>=0)
	        		{
	        			$('#loadedbody span').html(data.responseText);
	  	        		$('#loadedtitle').html("Información");
		        	  	$('#loaded').modal('show');
	        		}	  	        			
	        		else
	        		{
	        			var _oCAMBIOJSON = $.parseJSON(data.responseText);
	        	  	    var oCambio = new Object ();
	        	  		oCambio.idmedico = _oCAMBIOJSON.medico;
	        	  		oCambio.inicio = _oCAMBIOJSON.start;
	        	  		oCambio.end = _oCAMBIOJSON.end;
	        	  		 
	        			_newChange (oCambio.idmedico,oCambio.inicio , oCambio.end );
	        			
	        		}
	        	
	        		// segundo paso, llamas por ajax al formulario 
	        		 
	        	
	        	//	alert(data.responseText);
	        		/*$('#loadedbody span').html(data.responseText);
	        		$('#loadedtitle').html("Información");
	        	  	$('#loaded').modal('show');*/
	        	  	
	          },
	        fail: function(data) {
	        	
	        	  alert("Error" + data);
	          }
       	});
			
     });
	
}

function _DragExternalEvents(event, delta, revertFunc)
{
	
$('#external-events .fc-event').each(function() {

    // store data so the calendar knows to render an event upon drop
    $(this).data('event', {
        title: $.trim($(this).val()), // use the element's text as the event title
        stick: true // maintain when user navigates (see docs on the renderEvent method)
    });

    // make the event draggable using jQuery UI
    $(this).draggable({
        zIndex: 999,
        revert: true,      // will cause the event to go back to its
        revertDuration: 0  //  original position after the drag
    });

});
}


/* METEMOS UN 5% DE MARGEN */

var isEventOverDiv = function(x, y) {

    var external_events = $( '#external-events' );
    //var position = external_events.getBoundingClientRect();
   
    var offset = external_events.offset();
    offset.right = external_events.width() + offset.left;
    offset.bottom = external_events.height() + offset.top;
	
    var AbsX = x + document.body.scrollLeft;
    var AbsY = y + document.body.scrollTop;
    
    
    // Compare
    if (AbsX >= (offset.left - offset.left*0.15)
        && AbsY >= (offset.top - offset.top*0.15)
        && AbsX <= (offset.right + offset.right*0.15)
        && AbsY <= (offset.bottom + offset.bottom*0.15)) { return true; }
    return false;

}


function allowDrop(ev) {
	console.log("allowDrop");
    //ev.preventDefault();
    console.log("allowDrop2");
}

function drag(ev) {
	console.log("drag");
   // ev.dataTransfer.setData("text", ev.target.id);
    console.log("drag2");
}

function drop(ev) {
	console.log("dropev");
    //ev.preventDefault();
    //var data = ev.dataTransfer.getData("text");
    //ev.target.appendChild(document.getElementById(data));
    console.log("dropev2");
}


var _dateExternalEvent="";
	
$(document).ready(function() {
			

		//jQuery.event.props.push('dataTransfer');

		$.event.addProp('dataTransfer');
	
		 var _defaultDate= new Date();
		 
		 
		
		<% if (!_SelectedMonth.equals("")) { %>
			
		   _defaultDate = '<%=_SelectedMonth%>';
		
		<% } %>
		
		
		$('#calendar').fullCalendar({
			header: {
			        left: 'prev,next today',
	                center: 'title',
	                right: 'year,month,basicWeek,basicDay'
			},
			defaultDate: _defaultDate,
			fixedWeekCount : false, // solo semanas del mes
			aspectRatio: 2,
			navLinks: true, // can click day/week names to navigate views
			height:600,
			droppable: true,		
			 dragRevertDuration: 0,
			editable: true,			 
			eventLimit: true, // allow "more" link when too many events
			color: "yellow",   // an option!
		    textColor: "black", // an option!
		    selectHelper : true,
		    locale: 'es',
			events:   {
				url: '<%=_PageToFill%>',
				//method : 'post',
				data : obj,
				error: function() {
					$('#script-warning').show();
				}
			},
			eventReceive  : function( event ) 
			{ 
				//console.log("eventReceive");
				$("#calendar").fullCalendar('removeEvents', event._id); //replace 123 with reference to a real ID

			}, 
			drop : function( date, jsEvent, ui, resourceId ) { 

				// event.preventDefault();
			   // var data = event.dataTransfer.getData("text");
			    console.log("drop");
			 	var _Date1= moment(date.format('YYYY-MM-DD'));
			 	var _Date2= moment(jsEvent.target.innerHTML);
				
			 	var objEvent = new Object();				
				 objEvent.title= jsEvent.target.innerHTML;  //
				 objEvent.start= _Date1;
				 
				 var objDelta = new Object();
				 objDelta.days =  function () {  return _Date1.diff(_Date2,'days') };
				 
				 
				 _DropEvent(objEvent,objDelta, function () {});
			 },
			eventDragStop: function( event, jsEvent, ui, view ) {
	            
	        	console.log("eventDragStop");
	        	
	        	$("#external-events-listing").removeClass("borderdrop");
                $("#external-events-listing").addClass("sinborde");
	        	
	            if(isEventOverDiv(jsEvent.clientX, jsEvent.clientY)) {
	               // $('#calendar').fullCalendar('removeEvents', event._id);
	              // <div id="div1" ondrop="drop(event)" ondragover="allowDrop(event)"></div>

	                var el = $( "<div  draggable='true'  id='ExEvent' ondragstart='drag(event)'  class='fc-event'>" ).appendTo( '#external-events-listing' ).text( event.start.format());
	                //el.
	                el.draggable({
	                  zIndex: 999,
	                  revert: true, 
	                  revertDuration: 0 
	                });
	                el.data('event', { title: event.title, id :event.id, stick: false});                
	            }
	        },
			loading: function (bool) {
				if (bool)
					    
					    $('#loading').modal('show');
					  else 
					    $('#loading').modal('hide');
			 } ,
		    eventAfterAllRender: function (view) {
		    	_FillDataDays();
		    	_OrdenarGuardias();
		    },
		    eventRender: function (event, element) {
		    	element.find('.fc-title').html(event.title); // si encuentra input?
		    	if (event.title.indexOf("input")>=0)
		    		element.addClass('poolday');
		    },
		 /*   eventRender: function (event, element) {
		    	event.editable = false;
		    },*/
		      
		    eventDragStart: function (event, jsEvent, ui, view) {
	                console.log(event);
	                cancelDrag =   !_IsMedicoONCall(_USER_LOGGED,event.title) && !_IS_A;
	                $("#external-events-listing").addClass("borderdrop");
	                $("#external-events-listing").removeClass("sinborde");
	              
	            	  
	               // var dragged = [ui.helper[0], event];
	            },    
		    eventDrop: function(event, delta, revertFunc) {
				
		    	_DropEvent(event,delta,revertFunc);
		    	
		    }	
			    
			    
			    
		});
		
	});
</script>
<body>
    <div id="wrapper">
       <jsp:include page="common/init.jsp"/>
       <jsp:include page="common/navigation.jsp"/>
    </div>
    <!-- /#wrapper -->
    <div id="page-wrapper">
    <div class="row">
                <div class="col-lg-12">
                    <h1 class="page-header">Calendario </h1>
                </div>
                 <div class="panel-heading">                 		 
                          <div class="row">
                               <div class="col-lg-10">	                               		
                               		<p>(* Recuerda generar el primer mes correcto. No se permite almacenar guardias pasadas.)</p>								        	   	                                                                     
                                   <div id='calendar'></div>
                                   <!--  hay resultados -->
                                   <%  if (request.getParameter("fecha")==null && MedicoLogged.isAdministrator())  {  // se rellena con los huecos o viene de BBDD                                	   
                                   %>                                                                                                          		
                                   <div class="panel panel-default">
				                        <div class="panel-heading">
				                            <i class="fa fa-bell fa-fw"></i> Resultados  Guardias
				                        </div>
				                        <!-- /.panel-heading -->
				                        <div class="panel-body">
	                            			<div class="row">
	                                			<div class="col-lg-11"> 
	                                    			<div id="doctor_list" class="table-responsive">		                                          
				                            				<jsp:include page="medico/results_doctor.jsp"/>                            				                          	    	
	                                    			</div>
	                                    	</div>
	                                    	<!--  table-responsive -->
	                                		</div>
				                        </div>                            
			                        </div>
			                        <!-- /.panel-body -->			                  
			                    	<% } %>                       
                             </div>
                             
                               
                               <div class="col-lg-2">
                               		 
				                 
                               
                               		<div class="panel panel-yellow colorsguardias"><div class="panel-heading">Presencia</div></div> 
                               		<div class="panel panel-green colorsguardias"><div class="panel-heading">Localizada</div></div>                               		
                               		<div class="panel panel-primary colorsguardias"><div class="panel-heading">Refuerzo</div></div>
                               		<div class="panel panel-red colorsguardias"><div class="panel-heading">Residente</div></div>
                               		<%	
	                               		if (MedicoLogged.isAdministrator()) {
	                               	%>		
                                                                                                    
                                    <div class="panel panel-green">
				                        <div class="panel-heading">				                        
				                            <i class="fa fa-calendar-minus-o"></i> Configurar Mes
				                        </div>
				                        <div class="panel-body">				                            
				                            <form  id=ff method=post ><!--  onsubmit="return  fn_FillMonth()" -->
												<input  type="hidden" name="fecha" id=fecha value='2016-09-10'/>
												<div  hidden="true" id="dialog-confirm" title="Datos previos">
													  	<p>Esto perderá los cálculos de pantalla que no estén correctamente almacenados.¿Deseas continuar?</p>
												</div>
												<a href="javascript:void(0)" onclick="fn_FillMonth(<%=_bCalculating%>)">
												<div class="panel-footer">
					                                <span class="pull-left">Planificar</span>
					                                <span class="pull-right"><i class="glyphicon glyphicon-check"></i></span>
					                                <div class="clearfix"></div>
					                            </div>
												</a>
												<!-- <input  class="ui-button ui-widget ui-corner-all" type="submit" value="Resetear Mes"/> -->
											</form>
										</div>
									</div>	
									<% if (request.getParameter("fecha")!=null) /* hay calculo previo o hay datos previos cargados  */ { %>  
									<div class="panel panel-red">
				                        <div class="panel-heading">
				                            <i class="fa fa-bar-chart-o fa-fw"></i> Guardias
				                        </div>
				                        <div class="panel-body">				                            
				                                <form name="fSave" id=fSave method ="POST">			
				                                <a  href="javascript:void(0)" onclick="fn_Save()">																					
												<div class="panel-footer">
					                                <span class="pull-left">Calcular</span>
					                                <span class="pull-right"><i class="glyphicon glyphicon-send"></i></span>
					                                <div class="clearfix"></div>
					                            </div>
												</a>
												<!-- <input class="ui-button ui-widget ui-corner-all" type="button" onclick="fn_Save()" value="Calcular Guardias "/> -->
											</form>
										</div>
									</div>
									<% } %>
									<% if (request.getParameter("start_calc")!=null) /* hay calculo previo o hay datos previos cargados  */ { %>
									<div class="panel panel-primary">
				                        <div class="panel-heading">
				                            <i class="fa fa-bar-chart-o fa-fw"></i> Resultados
				                        </div>
				                        <div class="panel-body">				                            
											<form  id=fSaveDatabase method=post>
												<input  type="hidden" name="fecha" id=fecha value='2016-09-10'/>
												<a href="javascript:void(0)"  onclick="fSaveDatabaseOrExcel('save')">
												<div class="panel-footer">
					                                <span class="pull-left">Guardar</span>
					                                <span class="pull-right"><i class="glyphicon glyphicon-floppy-saved"></i></span>
					                                <div class="clearfix"></div>
					                            </div>
												</a>
												<a href="#doctor_list">
												<div class="panel-footer">
					                                <span class="pull-left">Revisar</span>
					                                <span class="pull-right"><i class="glyphicon glyphicon-exclamation-sign"></i></span>
					                                <div class="clearfix"></div>
					                            </div>
												</a>
													<!-- <input  class="ui-button ui-widget ui-corner-all" type="submit" value="Guardar"/> -->
											</form>
										</div>
									</div>
									<% } %>
									<% if (!_bCalculating) { %>
									<div id="toExcel" class="panel panel-yellow">
				                        <div class="panel-heading">
				                            <i class="fa fa-bar-chart-o fa-fw"></i> Excel
				                        </div>
				                        <div class="panel-body">				                            
				                            <form accept-charset="UTF-8" target="frame1" id=ff3 method=post action="toExcel.jsp" enctype="application/x-www-form-urlencoded">
												<!-- <input  type="hidden" name="fechaExcel" id=fechaExcel value='2016-09-10'/>-->
												<input  type="hidden" name="guardias" id=guardias value='2016-09-10'/>
												<input  type="hidden" name="MesGuardia" id=MesGuardia value='2016-09-10'/>
												<!--   <a href="javascript:fn_Excel()">-->
												<a href="javascript:fSaveDatabaseOrExcel('excel')">												 
												<div class="panel-footer">
					                                <span class="pull-left">Descargar</span>					                                
					                                <span class="pull-right glyphicon glyphicon-download"></span>
					                                <div class="clearfix"></div>
					                            </div>
												</a>
												<input type="hidden" id="filecontent" name="filecontent">												
												<!-- <input  class="ui-button ui-widget ui-corner-all" onclick="fn_Excel()" type="button" value="Descargar Excel"/> -->
												
											</form>											
												<a href="javascript:fSaveDatabaseOrExcel('excelmail')">
												<!--  <a href="javascript:fn_ExcelMail()"> -->
												<div class="panel-footer">
					                                <span class="pull-left">Enviar</span>
					                                <span class="pull-right glyphicon glyphicon-envelope"></span>
					                                <div class="clearfix"></div>
					                            </div>
												</a>
											   <input  type="hidden" name="fechaExcel3" id=fechaExcel3 value='2016-09-10'/>
												<input type="hidden" id="filecontent3" name="filecontent3">												
												<!-- <input  class="ui-button ui-widget ui-corner-all" onclick="fn_Excel()" type="button" value="Descargar Excel"/> -->
												
											
										</div>
									</div>
									<% } %>
									
                    				<% 
                    				}                    				
                    				if (!_bCalculating) { %>
			                 		<jsp:include page="tour/tour.jsp"/>
			                 		
			                 		<div class="panel panel-primary ">
				                        <div class="panel-heading">
				                            <i class="fa fa-hand-o-right" aria-hidden="true"></i>  Arrastra aquí tu cambio
				                            <i class="fa fa-drivers-license-o"></i> 
				                        </div>
				                        <div class="panel-body pad3">	
					                        <div id='external-events' data-step='31'  data-intro='Arrastra aquí los días que desees cambiar entre meses distintos (navegando con la flecha -> y <- del calendario)'>
										          <div id='external-events-listing'>
										            <!-- <div class='fc-event'>My Event 1</div> -->										           
										          </div>										          										       
											</div>
										</div>	
										<a href=javascript:void(0);" onclick="startIntro();">												 
												<div class="panel-footer">
					                                <span class="pull-left">Muéstrame cómo</span>					                                
					                                <i class="pull-right fa fa-eye" aria-hidden="true"></i>
					                                <div class="clearfix"></div>
					                            </div>
										</a>
										
										
									</div><% } %>
			                 	<!-- class="col-xs-2"> -->
                        			
                    				
                               </div>
                           </div>
                  </div>
                <!-- /.col-lg-12 -->
				<!-- Modal -->
				<div class="modal fade" id="loadingmail" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
				  <div class="modal-dialog" role="document">
				    <div class="modal-content">
				      <div class="modal-header">				      
				        <h5 class="modal-title" id="exampleModalLabel">Enviando mail a lista de médicos</h5>				        
				      </div>
				      <div class="modal-body">
				        <span>Por favor, espere..</span>
				      </div>				      
				    </div>
				  </div>
				</div>
				<div class="modal fade" id="loading" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
				  <div class="modal-dialog" role="document">
				    <div class="modal-content">
				      <div class="modal-header">
				      <% 
				      	String _Message="Cargando datos.";  
				      	if (_CalendarioGoogle.equals("S")) 
				      			_Message = _Message.concat("Recuerda que una vez que guardes un mes se procederá a sincronizar el calendario (Google Calendar)");
				      	
				      %>
				        <h5 class="modal-title" id="exampleModalLabel"><%=_Message %></h5>				        
				      </div>
				      <div class="modal-body">
				        <span>Por favor, espere..</span>
				      </div>
				    </div>
				  </div>
				</div>
				<!-- Modal CAMBIO DE GUARDIAS  -->
				<div id="modalCambioGuardias" class="modal fade" role="dialog">
				  <div class="modal-dialog modal-sm">
				
				    <!-- Modal content-->
				    <div class="modal-content">
				      <div class="modal-header">
				        <!-- <button type="button" class="close" data-dismiss="modal">&times;</button> -->
				        <h4 class="modal-title">Información</h4>
				      </div>
				      <div class="modal-body">
				        <p>¿Estás seguro de quieres proceder con este cambio de guardia?</p>
				      </div>
				      <div class="modal-footer">
				        <button id="btAcceptChange" type="button" class="btn btn-primary" data-dismiss="modal">Aceptar</button>
				        <button id="btCancelChange" type="button" class="btn btn-primary" data-dismiss="modal">Cancelar</button>
				      </div>
				    </div>				
				  </div>
				</div>                 			
				<!-- FIN  Modal CAMBIO DE GUARDIAS  -->
				<div class="modal fade" id="loaded" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
				  <div class="modal-dialog" role="document">
				    <div class="modal-content">
				      <div class="modal-header">
				        <h5 class="modal-title" id="loadedtitle"></h5>
				        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
				          <span aria-hidden="true">&times;</span>
				        </button>
				      </div>
				      <div class="modal-body" id="loadedbody">
				        <span>Los datos han sido almacenados y enviados  correctamente</span>
				      </div>
				      <div class="modal-footer">
				        <button type="button" class="btn btn-primary" data-dismiss="modal">Cerrar</button>				      
				    </div>
				  </div>
				</div>
				<!--  CAMBIO DE GUARDIAS --> 
			
				<div style="display:none" id="loaded2" title="Correcto"><p>Datos almacenados satisfactoriamente</p></div>
				
      </div>    
		<div id="frame1"></div>
		<div id=confirmar_cambio></div>
</body>
</html>


