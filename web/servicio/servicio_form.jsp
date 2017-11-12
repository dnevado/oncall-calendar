<%@page import="guardias.security.SecurityUtil"%>
<%@page import="com.guardias.mail.MailingUtil"%>
<%@page import="com.guardias.database.MedicoDBImpl"%>
<%@page import="com.guardias.servicios.Guardias_Servicios"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="com.guardias.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.text.SimpleDateFormat"%> 
<%@page import="java.util.*"%>

<%@page import="com.guardias.Vacaciones_Medicos"%>

<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>


<%

	ResourceBundle RB = ResourceBundle.getBundle(Util.PROPERTIES_FILE, Locale.getDefault());

	Integer _ID = new Integer(-1);	

	String Nuevo = "N";
	if (request.getParameter("id")!=null && request.getParameter("id").equals("-1"))
		Nuevo = "S";

	
	/* String _Path = request.getServletContext().getRealPath("/") + "//medicos.xml";	
	ProcesarMedicos oUtilMedicos = new ProcesarMedicos();
	*/
	boolean saving = false;
	String  sError = "OK";
	boolean newServicio = false;
	if (request.getParameter("guardar") !=null)
			saving = true;
 
	if (request.getParameter("nuevo") !=null && request.getParameter("nuevo").equals("S"))
		newServicio=true;
	
	String nombre = "";
	
	Guardias_Servicios _Guardias_Servicios = new Guardias_Servicios();
	if (saving) 
	{
		
			_Guardias_Servicios = new Guardias_Servicios();
			
			nombre = request.getParameter("nombre");
			
			
			if (newServicio)
			{				
				Guardias_Servicios oServicioMX  = Guardias_ServiciosDBImpl.getMaxIDGuardias_ServiciosID();
				 oServicioMX.setIdServicio(oServicioMX.getIdServicio()+1);
				_Guardias_Servicios.setIdServicio( oServicioMX.getIdServicio());
				
				/* EXISTE???? */
				List<Guardias_Servicios> lServicioExiste = Guardias_ServiciosDBImpl.getGuardias_ServiciosByName(_Guardias_Servicios); 
				if (lServicioExiste!=null  && !lServicioExiste.isEmpty())
				{
					
					sError = RB.getString("group.exists");
				}
				
			}
			else
				_Guardias_Servicios  = Guardias_ServiciosDBImpl.getGuardias_ServiciosById(new Integer(request.getParameter("id")));
									
			
			String Descripcion = request.getParameter("descripcion");
			String CodigoInterno = request.getParameter("codigointerno");
			Long activo = request.getParameter("activo")!=null && request.getParameter("activo").toString().equals("S") ? new Long(1) :  new Long(0);
			Long visible = request.getParameter("visible")!=null && request.getParameter("visible").toString().equals("S") ? new Long(1) :  new Long(0);
			
			
			
			
		
			_Guardias_Servicios.setActivo(activo);
			_Guardias_Servicios.setNombre(nombre);
			_Guardias_Servicios.setDescripcion(Descripcion);
			_Guardias_Servicios.setVisible(visible);			
			_Guardias_Servicios.setCodigoInterno(CodigoInterno);
			_Guardias_Servicios.setIdMedicoOwner(MedicoLogged.getID());
			

			if (sError.equals("OK"))
			{
				
			
			if (newServicio)	
			{
			
				 Guardias_ServiciosDBImpl.AddGuardias_Servicios(_Guardias_Servicios);	
				 Long OldServicio  = MedicoLogged.getServicioId();
				 boolean OldAdministrator  = MedicoLogged.isAdministrator()		;		 
				 MedicoLogged.setServicioId(_Guardias_Servicios.getIdServicio());
				 MedicoLogged.setAdministrator(true);
				 MedicoLogged.setActivoServicio(new Long(1));
				 MedicoDBImpl.AddMedicoAlServicio(MedicoLogged);
				 MedicoLogged.setServicioId(OldServicio);
				 MedicoLogged.setAdministrator(OldAdministrator);
				 
				 /* CREAMOS UN SIMULADO */
				 Medico ThisMedico = MedicoDBImpl.getUltimoIDMedico();
				 
				 Medico _Simulado = new Medico();
				 _Simulado.setActivo(true);
				 _Simulado.setAdministrator(false);
				 _Simulado.setConfirmado(true);
				 _Simulado.setApellidos(Util.SIMULADO_APELLIDOS);
				 _Simulado.setNombre(Util.SIMULADO_NOMBRE);
				 _Simulado.setTipo(Util.eTipo.RESIDENTE);
				 _Simulado.setSubTipoResidente(Util.eSubtipoResidente.SIMULADO);
				 _Simulado.setEmail(String.valueOf(System.currentTimeMillis()) +   Util.SIMULADO_EMAIL);
				 _Simulado.setID(ThisMedico.getID());
				 _Simulado.setServicioId(_Guardias_Servicios.getIdServicio());
				 _Simulado.setActivoServicio(new Long(1));
					
				 MedicoDBImpl.AddMedico(_Simulado);
				 //MedicoDBImpl.AddMedicoAlServicio(_Simulado);
				 
				 /* GENERAMOS LA CONFIGURACION POR DEFECTO BASE, SI NO EXISTE UNA PREVIA DEL USUARIO POR SER LA PRIMERA VEZ, DEBE  EXISTIR UNA BASE  */
				 Guardias_Servicios oExisteServicio = Guardias_ServiciosDBImpl.getGuardias_ServiciosById(OldServicio.intValue());
				 if (oExisteServicio==null)  // cogemos un servicio aleatorio que exista
				 {
					 Guardias_Servicios oServicio = Guardias_ServiciosDBImpl.getMaxIDGuardias_ServiciosID();
					 OldServicio = oServicio.getIdServicio();					 
				 }
				 /* verificamos que exista */
				 Configuracion oConfiguracion =  ConfigurationDBImpl.GetConfiguration(Util.getoCONST_AJUSTE_A_ESTOS_MESES_VACACIONES(),OldServicio);
				 if (oConfiguracion==null  || oConfiguracion.getKey()==null )
					 OldServicio =new Long(1); // DEBE EXISTE!!!
				 
				 ConfigurationDBImpl.InicializarConfiguration(_Guardias_Servicios.getIdServicio(), OldServicio);
				 
				 
				 
			}
			else
				Guardias_ServiciosDBImpl.UpdateCambioGuardias(_Guardias_Servicios);
			} // FIN DE NO HAY ERROR 
				 		 		
	}
	else	
	{
	
	

	_ID = new Integer(-1);
	if (request.getParameter("id") !=null)
		_ID = Integer.parseInt(request.getParameter("id"));
	
	
	if (_ID.equals(new Integer(-1))==false)
	{
		
		// VERIFICAMOS SI ES EL OWNER 
		_Guardias_Servicios  = Guardias_ServiciosDBImpl.getGuardias_ServiciosById(_ID);
		if (!MedicoLogged.getID().equals(_Guardias_Servicios.getIdMedicoOwner()))
			response.sendRedirect(request.getContextPath() + "/inicio.jsp");
				
		
		
	}
	else
		_Guardias_Servicios = new Guardias_Servicios();
	
	}
		
	//Medico _oMedico = oUtilMedicos.LeerMedico(_Path,_ID.toString());
	
%>




<style>

/* Color of invalid field */
.has-error .control-label,
.has-error .help-block,
.has-error .form-control-feedback {
    color: #a94442;
}
</style>


	

<script>

$(document).ready(function() 
{
	$('#fmedico').validator();
	$('#fmedico').removeAttr('novalidate');

});



</script>

	<!-- /.row -->
    <div class="row">
    	<div>
	        <div class="panel panel-default">	
    	     	<div class="panel-heading">
        	     	<%= RB.getString("servicios.detalle")%>
         		</div>
				 <div class="panel-body">
				<form  id=fmedico method=post role="form" data-toggle="validator" enctype="application/x-www-form-urlencoded" accept-charset="ISO-8859-1">                                     						
                        <!-- SUCCESS  -->
                        <div  id=success  class="alert alert-success" style="display:none">
                        <div class="panel-body">
			               <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
			               <span class="glyphicon glyphicon-ok"></span> <strong>Los datos han sido guardados satisfactoriamente</strong>
			                <hr class="message-inner-separator">
			                <p class="f18px"><%=RB.getString("registration.shareit")%></p>
									<jsp:include page="/common/rrss.jsp" />
									<script>
							        			$(".rrss .name").html("<%=RB.getString("group.registration")%> <%=nombre%> <%=RB.getString("group.registration2")%>");
							        			$(".rrss .enlace").attr("href","<%=RB.getString("registration.url")%>");

										mostrarRRSS('rrss', 'enlace', 'name');
									</script>                            
                        </div>                                               
                    	</div> 
						<!--  SUCCESS -->            
						
						<div  id=error  class="alert alert-danger" style="display:none">                        
                        <div class="panel-body">
				                <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
				                <span class="glyphicon glyphicon-hand-right"></span> <strong>Aviso</strong>
				                <hr class="message-inner-separator">
				                <p><%= sError%></p> 
                        </div>                                               
                    	</div> 
                        
						<div class="form-group">
						<label  class="control-label"><%= RB.getString("servicios.codigointerno")%></label>
						<input   required maxlength="50" class="ui-textfield form-control" type="text" name="codigointerno" id="codigointerno"  value='<%=_Guardias_Servicios.getCodigoInterno()%>'/>						
						</div>						
						<div class="form-group">
						<label  class="control-label" ><%= RB.getString("servicios.nombre")%></label>
						<input  required  maxlength="150" required class="ui-textfield form-control" type="text" name="nombre" id="nombre"  value='<%=_Guardias_Servicios.getNombre() !=null ? _Guardias_Servicios.getNombre()  : ""%>'/>
						</div>
						<div class="form-group">
						<label  class="control-label" for="descripcion"><%= RB.getString("servicios.descripcion")%></label>
						 <textarea class="form-control" maxlength="500" rows="5" required class="ui-textfield form-control" type="text" name="descripcion"  id="descripcion" ><%=_Guardias_Servicios.getDescripcion() !=null ? _Guardias_Servicios.getDescripcion()  : ""%></textarea>
						</div>						
																
						<% 			
						String _selected ="";
						if (_Guardias_Servicios.getActivo().equals(new Long(1)))
							_selected="checked";
						
						%>
						<div class="form-group">
							<div class="checkbox checkbox-primary">
         	               <input  <%=_selected%> type="checkbox" name="activo"  id ="activo" value='S'/>
                           <label for="activo">
                           			<%= RB.getString("servicios.activo")%> <span class="glyphicon glyphicon-question-sign" title="<%= RB.getString("servicios.descripcionactivo")%>">
                        			</span></label>
           					</div>							
						</div>	
						<% 			
						 _selected ="";
						if (_Guardias_Servicios.getVisible().equals(new Long(1)))
							_selected="checked";
						
						%>
						<div class="form-group">
							<div class="checkbox checkbox-primary">
	         	               <input  <%=_selected%> type="checkbox" name="visible"  id ="visible" value='S'/>
	                           <label for="visible">
	                           			<%= RB.getString("servicios.visible")%> <span class="glyphicon glyphicon-question-sign" title="<%= RB.getString("servicios.descripcionvisible")%>"></span>
	                           	</label>
           					</div>							
						</div>
																							
						<input type="hidden" name="guardar"  value='1'/>
						<input type="hidden" name="id" id="id" value='<%=_Guardias_Servicios.getIdServicio()%>'/>	
						<input type="hidden" name="pid" id="pid" value='<%=_ID%>'/>
						<input type="hidden" name="nuevo" id="nuevo" value='<%=Nuevo%>'/>
						
						<div class="form-group pull-right">
    							<button type="submit"   class="btn  btn-primary"><%= RB.getString("servicios.guardar")%></button>
    							<a href="<%=request.getContextPath()%>/servicios.jsp"  class="btn  btn-primary"><%= RB.getString("servicios.cancelar")%></a>
  						</div>				
						</form> 
		</div>
	</div>
	</div>
</div>

<%    
if (saving) {
	if (sError.equals("OK"))
	{%>
		<script>
		$('#success').show();
		</script>
<%  }  
	else
	{%>
		<script>
		$('#error').show();
		</script>
<%  }
}
%>