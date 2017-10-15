<%@page import="guardias.security.SecurityUtil"%>
<%@page import="com.guardias.mail.MailingUtil"%>
<%@page import="com.guardias.database.MedicoDBImpl"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"     pageEncoding="UTF-8"%>
<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>
<%@page import="com.guardias.persistence.*"%>
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.servicios.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.text.SimpleDateFormat"%> 
<%@page import="java.util.*"%>

<%@page import="com.guardias.Vacaciones_Medicos"%>




<%

	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	ResourceBundle RB = ResourceBundle.getBundle(Util.PROPERTIES_FILE, Locale.getDefault());


	String _ReadOnly = ""; 
	String _Disabled = "";
	String _classHidden= ""; 
	if (!MedicoLogged.isAdministrator())
	{
		_ReadOnly = "readonly=\"readonly\"";
		_Disabled = "disabled=disabled";
		_classHidden = "hide";
		 
	}
	
	
	String Nuevo = "N";
	if (request.getParameter("id")!=null && request.getParameter("id").equals("-1"))
		Nuevo = "S";

	
	/* String _Path = request.getServletContext().getRealPath("/") + "//medicos.xml";	
	ProcesarMedicos oUtilMedicos = new ProcesarMedicos();
	*/
	boolean saving = false;
	String  sError = "OK";
	boolean newMedico = false;
	if (request.getParameter("guardar") !=null)
			saving = true;
 
	if (request.getParameter("nuevo") !=null && request.getParameter("nuevo").equals("S"))
		newMedico=true;
	
	if (saving) 
	{
		
		String inputEmail = request.getParameter("inputEmail");
		String  oldEmail= request.getParameter("oldEmail");
		
		Medico ExisteMedico = MedicoDBImpl.getMedicoByEmail(inputEmail,MedicoLogged.getServicioId());
		
		
		
		/* EXISTE MEDICO NO ASIGNADO A NINGUN SERVICIO */
		
		/* cambiado mail  */
		if (ExisteMedico!=null && !inputEmail.equals(oldEmail)) 
		{
			  
			Guardias_Servicios _GS = Guardias_ServiciosDBImpl.getGuardias_ServiciosOfUser(ExisteMedico.getID().intValue());
			
			/* TOTAL : getIdServicio*/
			if (_GS!=null && _GS.getIdServicio()>0)  // y no pertence a ningun servicio
			{
				sError = "NOOK.Por favor, has introducido un mail de usuario que ya pertenece a una servicio . Verifícalo y procede con otro email de contacto.";	
			}
			
			
			
		}
		else
		{
		
			Medico _NuevoMedico = new Medico();
			
			
			
			String ID = request.getParameter("ID");
			String IDMEDICO = request.getParameter("IDMEDICO");
			String nombre = request.getParameter("nombre");
			String apellidos = request.getParameter("apellidos");
			Long servicio = Long.parseLong(request.getParameter("servicio"));
			
			Util.eTipo tipo = Util.eTipo.ADJUNTO; 
			
			
			
			
			
			
			String  Max_Num_Guardias= request.getParameter("max_guardias");
			
			String[] aVacaciones = request.getParameterValues("vacaciones[]");
			
			
			if (request.getParameter("tipo")!=null && request.getParameter("tipo").toString().equals(Util.eTipo.RESIDENTE.toString()))
				tipo = Util.eTipo.RESIDENTE;
			
			Util.eSubtipoResidente stipo     = Util.eSubtipoResidente.R1;		
			if (request.getParameter("residente")!=null && request.getParameter("residente")!=null)
				stipo =Util.eSubtipoResidente.valueOf(request.getParameter("residente"));
			
			//String residente = request.getParameter("residente");
			Boolean guardiassolo = request.getParameter("guardiassolo")!=null && request.getParameter("guardiassolo").toString().equals("S") ? new Boolean(true) :  new Boolean(false);
			String  vacaciones = request.getParameter("vacaciones");	
			Boolean activo = request.getParameter("activo")!=null && request.getParameter("activo").toString().equals("S") ? new Boolean(true) :  new Boolean(false);
			
			if (!newMedico)
				_NuevoMedico = MedicoDBImpl.getMedicoByEmail(oldEmail,MedicoLogged.getServicioId());
			
			
			_NuevoMedico.setActivo(activo);
			_NuevoMedico.setActivoServicio(activo ? new Long(1) : new Long(0));
			_NuevoMedico.setNombre(nombre);
			_NuevoMedico.setApellidos(apellidos);
			_NuevoMedico.setGuardiaSolo(guardiassolo);
			//_NuevoMedico.setID(new Long(ID));
			_NuevoMedico.setIDMEDICO(new Long(IDMEDICO));
			//_NuevoMedico.setlGuardias(lGuardias);
			//_NuevoMedico.setlVacaciones(lVacaciones);
					
			_NuevoMedico.setSubTipoResidente(stipo);
			_NuevoMedico.setTipo(tipo);
			_NuevoMedico.setEmail(inputEmail);
			_NuevoMedico.setMax_NUM_Guardias(Long.valueOf(Max_Num_Guardias));
			_NuevoMedico.setServicioId(servicio);
			
			
			/* USUARIO ADMIN LOGGED */		
			Medico PowerMedico = MedicoDBImpl.getMedicoByEmail((String) request.getSession().getAttribute("User"),MedicoLogged.getServicioId());
			
			/* VERIFICAOS QUE NO HAYA VACACIONES CON GUARDIAS ALMACENADAS */
			if (aVacaciones!=null)
			{
				for (int j=0;j<aVacaciones.length;j++)
				{					
					String DiaVacaciones = aVacaciones[j];
					
					List<Guardias>  lVacacionesDay =  GuardiasDBImpl.getGuardiasMedicoFecha(new Long(ID), DiaVacaciones, MedicoLogged.getServicioId());
					if (lVacacionesDay!=null && !lVacacionesDay.isEmpty())
					{
						Guardias _oGuardia = lVacacionesDay.get(0);	
						sError = "NOOK Existe el día de vacaciones " + _oGuardia.getDiaGuardia() + " con guardia activa";
						break;
					}
					/* 20171015 
					
					QUITAMOS EL AÑADIR VACACACIONES Y BORRAMOS TODAS LAS VACACIONES SI LAS HUBIERA AL FINAL DESDE EL MES EN CUESTION 
					VacacionesDBImpl.AddVacacionesMedico(new Long(ID), DiaVacaciones);
					*/
					
					
				}			
			}
			
			if (!sError.contains("NOOK"))
			{
				if (newMedico)
				{
							
					Medico ThisMedico = MedicoDBImpl.getUltimoIDMedico();
					
					ID=ThisMedico.getID().toString();
					
					_NuevoMedico.setID(Long.parseLong(ID));
					
					_NuevoMedico.setPassWord(SecurityUtil.GeneratePlainRandomPassword());	
					//_NuevoMedico.setServicioId(MedicoLogged.getServicioId());
					/* contraseña plana */
					MailingUtil.SendWelcomeRegistration(_NuevoMedico,PowerMedico, request);
					/* contraseña ENCRIPTADA */
					_NuevoMedico.setPassWord(SecurityUtil.GenerateEncriptedRandomPassword(_NuevoMedico.getPassWord()));			
					MedicoDBImpl.AddMedico(_NuevoMedico);
					
				}
				else
				{
					_NuevoMedico.setID(Long.parseLong(ID));
					boolean bChangedEmail = false;
					if (!_NuevoMedico.getEmail().equals(oldEmail))  // hay cambio de emails
					{
						_NuevoMedico.setPassWord(SecurityUtil.GeneratePlainRandomPassword());
						MailingUtil.SendWelcomeRegistration(_NuevoMedico,PowerMedico,request); 
						_NuevoMedico.setPassWord(SecurityUtil.GenerateEncriptedRandomPassword(_NuevoMedico.getPassWord()));
						_NuevoMedico.setConfirmado(false);
						bChangedEmail = true;
					}
							
					
					MedicoDBImpl.UpdateMedico(Long.parseLong(ID), _NuevoMedico, bChangedEmail);
					
					
				}
				/* VACACIONES */
				/* COGEMOS EL DIA ACTUAL Y BORRAMOS HACIA ADELANTE */
				/* OJO, PARA LAS VACACIONES, AL SER FUTURAS, BORRAMOS CADA REGISTRO PREVIO QUE HUBIERA EN EL FUTURO, DEL MES EN CURSO Y DEL SGTE */
				/* vamos a borrar vacaciones del mes actual */
				
				
				
				Date _Hoy = new Date();
				_Hoy.setDate(1);// desde el dia uno
				
				String  _MesMenorVacaciones = formatter.format(_Hoy); 
				
				if (aVacaciones!=null)
				{
					for (int j=0;j<aVacaciones.length;j++)
					{
									
						String DiaVacaciones = aVacaciones[j];
						if (_MesMenorVacaciones.equals(""))
							_MesMenorVacaciones = DiaVacaciones;
						
						if (_MesMenorVacaciones.compareTo(DiaVacaciones)>0)
							_MesMenorVacaciones = DiaVacaciones;
						
						
					}
				}
				
				/* BORRAMOS DESDE EL MES */
					
				VacacionesDBImpl.DeleteVacacionesMedicoDesde(new Long(ID), _MesMenorVacaciones );
				
				if (aVacaciones!=null)
				{
					for (int j=0;j<aVacaciones.length;j++)
					{					
						String DiaVacaciones = aVacaciones[j];
						
						VacacionesDBImpl.AddVacacionesMedico(new Long(ID), DiaVacaciones);
						
						
					}
				
				}
			}
									 		
			
		}
		out.print(sError);
		
		
				 		 		
	}
	else	
	{
	
	
	
	Long _ID = new Long(-1);
	if (request.getParameter("id") !=null)
		_ID = Long.parseLong(request.getParameter("id"));
	
	List<Medico> _lMedicos =null;
	Medico _oMedico = null;
	List<Vacaciones_Medicos> _lVacaciones = null;
	
	
	
	List<Guardias_Servicios> _lGS = null;
	
	//lItems = oUtilMedicos.LeerMedicos(_Path,true);
	if (!MedicoLogged.isAdministrator())
	{
		_lGS =  Guardias_ServiciosDBImpl.getListGuardias_ServiciosOfUser(MedicoLogged.getID().intValue());		
		
	}		 
	else
		_lGS =  Guardias_ServiciosDBImpl.getGuardias_ServiciosByOwner(MedicoLogged.getID().intValue());
	
	
	if (!_ID.equals(new Long(-1)))
	{
		_lMedicos = MedicoDBImpl.getMedicos(_ID,MedicoLogged.getServicioId());	
		if (_lMedicos!=null && _lMedicos.size()>0)		
			_oMedico = _lMedicos.get(0);
		
		/* 20171015 SOLO SACO LAS DE UN MES ANTERIOR Y UN AÑO ADELANTE, ya que es lo que muestra el picker */
		_lVacaciones = VacacionesDBImpl.getVacacionesMedicos(new Long(_ID), ""); 
		Calendar _VHoy = Calendar.getInstance();
		_VHoy.add(Calendar.MONTH, -1);
		_VHoy.set(Calendar.DATE, 1);
		Calendar _VFuture = Calendar.getInstance();
		_VFuture.add(Calendar.YEAR, 2);
		_lVacaciones = VacacionesDBImpl.getVacacionesMedicosDesdeHasta(new Long(_ID),formatter.format(_VHoy.getTime()), formatter.format(_VFuture.getTime())); 
		
	}
	else
		_oMedico = new Medico();
	
		
	//Medico _oMedico = oUtilMedicos.LeerMedico(_Path,_ID.toString());
	
		
%>
<!DOCTYPE html>
<html lang="es">

<head>

<style>

/* Color of invalid field */
.has-error .control-label,
.has-error .help-block,
.has-error .form-control-feedback {
    color: #a94442;
}
</style>


	
</head>
<body>
<script>

$(document).ready(function() 
{
	$('#fmedico').validator();
	$('#fmedico').removeAttr('novalidate');
	fn_EnableResidente();
	fn_DefaultResidente();
});


var holidays= [];


// NUEVO 
function fn_DefaultResidente(){

	
		// alert($("#residente").val());
		var strSIMULADO = _SIMULADO.toUpperCase();			
		if ($("#residente").val()==strSIMULADO)  // metemos simulados un numero muy grande de guardias
		{
				$("#max_guardias").val(9999);
				$("#max_guardias").prop('disabled', true);			
				$("#activo").prop('disabled', true);
		}
		else
		{
			$("#max_guardias").prop('disabled', false);
	//		$("#max_guardias").val(1);
		}
	

}


function getday(dateText, inst) { 
   
	/* QUITAMOS O PONEMOS CLASES DE SELECCION */	
	$('#vacaciones-usage').multiDatesPicker('removeDates', dateText);	   
}
//disabled: true,
function date_(){
	
	$("#vacaciones").multiDatesPicker( {dateFormat: "yy-mm-dd",  minDate: "-1M", maxDate: "+12M",    		
	    // Primer dia de la semana El lunes
	    firstDay: 1,
	    // Dias Largo en castellano
	    dayNames: [ "Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado" ],
	    // Dias cortos en castellano
	    dayNamesMin: [ "Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sa" ],
	    // Nombres largos de los meses en castellano
	    monthNames: [ "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" ],
	    // Nombres de los meses en formato corto 
	    monthNamesShort: [ "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dec" ]});		
	$('#vacaciones').multiDatesPicker('addDates', holidays);
	
}



function fn_EnableResidente()
{
	
	if ($("#tipo").val()==_RESIDENTE.toUpperCase())
	{
		$("#residente").show();
		$("#labelresidente").show();
		$("#guardiassolo").hide();
		$("#labelguardiassolo").hide();
	}	
	else
	{
		$("#residente").hide();
		$("#labelresidente").hide();
		$("#guardiassolo").show();
		$("#labelguardiassolo").show();
	}    
}


var obj = {};



function _GuardarMedico()
{
	
	//$('#fmedico').validator();
	obj['ID']=$("#id").val();
	obj['IDMEDICO']=$("#idmedico").val();
	obj['nombre']= $("#nombre").val();
	obj['apellidos']=	$("#apellidos").val();
	obj['inputEmail']=	$("#inputEmail").val();
	obj['tipo']=$("#tipo").val();
	//obj['stipo']=$("#stipo").val();
	obj['residente']=$("#residente").val();
	obj['guardiassolo']="N";
	if  ($("#guardiassolo").prop('checked'))
		obj['guardiassolo']= "S";		
	obj['vacaciones']= $("#vacaciones").val();
	//obj['simulado']=$("#simulado").val();
	obj['activo']= "N";
	if  ($("#activo").prop('checked'))
		obj['activo']= "S";
//	alert($("#activo").prop('checked'));
	obj['max_guardias']=	$("#max_guardias").val();	
	obj['nuevo']=	$("#nuevo").val(); 
	obj['servicio']=	$("#servicio").val();
	obj['vacaciones']=	$('#vacaciones').multiDatesPicker('getDates');
	obj['oldEmail']=$("#oldEmail").val();
	//alert(obj['vacaciones']);
	obj['guardar']=	"1";
						
		
	$.ajax({
		  type: "POST",
		  url: '<%=request.getContextPath()%>/medico/detallemedico.jsp',
		  data: obj,
		  async:false,
		  success: function(data) {
			
			  $("#error").hide();
			  
			  if (data.indexOf("NOOK")>=0)
			  {
				  $("#error .panel-body").html(data.replace("NOOK",""));
				  $("#error").show();
			  }
			  else
			  {	
				  $("#success").show();
				  
			  }
			  
		   },
		   error: function(data) {				  				  
			   alert("error");
			   }
		});
	return false;
}

</script>
<div id="wrapper">
	<!-- /.row -->
    <div class="row">
    	<div class="col-lg-12">
	        <div class="panel panel-default">	
    	     	<div class="panel-heading">
        	     	Detalle Médico. Se requiere previa activación por parte del usuario.
         		</div>
				 <div class="panel-body">
				<form  id=fmedico method=post role="form" data-toggle="validator" onsubmit="return _GuardarMedico();">                                     						
                        <!-- SUCCESS  -->
						<div  id=success  class="alert alert-success" style="display:none">
                        <div class="panel-body">
			               <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
			               <span class="glyphicon glyphicon-ok"></span> <strong>Los datos han sido guardados satisfactoriamente</strong>
			                <hr class="message-inner-separator">
			                <p>En caso de ser nuevo miembro o modificación de email de contacto, se enviará notificación por correo electrónico para confirmar.</p>                            
                        </div>                                               
                    	</div> 
						<!--  SUCCESS -->            
						
						<div  id=error  class="alert alert-danger" style="display:none">                        
                        <div class="panel-body">
				                <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
				                <span class="glyphicon glyphicon-hand-right"></span> <strong>Aviso</strong>
				                <hr class="message-inner-separator">
				                <p></p> 
                        </div>                                               
                    	</div> 
						<!--  SUCCESS -->
						<div class="form-group">
						<label  class="control-label">Id Médico:</label>
						<input   required maxlength="6" type="number" class="ui-textfield form-control" type="text" name="idmedico" id="idmedico"  value='<%=_oMedico.getIDMEDICO()%>'/>						
						</div>						
						<div class="form-group">
						<label  class="control-label" >Nombre:</label>
						<input  required  maxlength="25" required class="ui-textfield form-control" type="text" name="nombre" id="nombre"  value='<%=_oMedico.getNombre() !=null ? _oMedico.getNombre()  : ""%>'/>
						</div>
						<div class="form-group">
						<label  class="control-label">Apellidos:</label>
						<input  required   maxlength="25" required class="ui-textfield form-control" type="text" name="apellidos"  id="apellidos" value='<%=_oMedico.getApellidos() !=null ? _oMedico.getApellidos()  : ""%>'/>
						</div>
						<div class="form-group">
						<label  class="control-label">Max Guardias Mes: <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("medico.simulado")%>"></label>
						<input <%=_ReadOnly %> type="number" required maxlength="2" class="ui-textfield form-control"  name="max_guardias"  id="max_guardias" value='<%=_oMedico.getMax_NUM_Guardias()%>'/>
						</div>
						<div class="form-group <%=_classHidden%>">
						<label  class="control-label">Tipo:</label>
						<select <%=_ReadOnly %>  required class="form-control ui-select" name="tipo" id="tipo" onChange="fn_EnableResidente()">
							<option <%=_oMedico.getTipo().equals(Util.eTipo.ADJUNTO) ? "selected" : ""%> value="<%=Util.eTipo.ADJUNTO %>"><%=Util.eTipo.ADJUNTO%></option>
							<option <%=_oMedico.getTipo().equals(Util.eTipo.RESIDENTE) ? "selected" : ""%> value="<%=Util.eTipo.RESIDENTE %>"><%=Util.eTipo.RESIDENTE %></option>
						</select>						
						</div>
						<div class="form-group">
						<label  class="control-label">Servicio:</label>
						<select <%=_ReadOnly %>  required class="form-control ui-select" name="servicio" id="servicio">
						<%
							for (Guardias_Servicios Gs : _lGS)
							{
								
							
						%>
							<option <%=Gs.getIdServicio().equals(MedicoLogged.getServicioId()) ? "selected" : ""%> value="<%=Gs.getIdServicio() %>"><%=Gs.getNombre()%></option>
							
						<% 
							}	
						%>
						</select>						
						</div>
						
						
						<div class="form-group">
						    <label  class="control-label" for="inputEmail" class="control-label">Email</label>
						    <input  required <%=_ReadOnly %> type="email" class="form-control" id="inputEmail" placeholder="Email" value="<%=_oMedico.getEmail()%>" data-error="Por favor, introduzca un mail válido" required>						    
					    </div>																								
						<div class="form-group <%=_classHidden%>" >
						<label  class="control-label" id="labelresidente">Residente:</label>
						<select <%=_ReadOnly %> class="form-control ui-select" name="residente" id="residente" onChange="fn_DefaultResidente()">>
							<option <%=_oMedico.getSubTipoResidente().equals(Util.eSubtipoResidente.R1) ? "selected" : ""%> value="<%=Util.eSubtipoResidente.R1%>"><%=Util.eSubtipoResidente.R1%></option>
							  <option <%=_oMedico.getSubTipoResidente().equals(Util.eSubtipoResidente.R2) ? "selected" : ""%> value="<%=Util.eSubtipoResidente.R2%>"><%=Util.eSubtipoResidente.R2%></option>
							  <option <%=_oMedico.getSubTipoResidente().equals(Util.eSubtipoResidente.R3) ? "selected" : ""%> value="<%=Util.eSubtipoResidente.R3%>"><%=Util.eSubtipoResidente.R3%></option>
							  <option <%=_oMedico.getSubTipoResidente().equals(Util.eSubtipoResidente.R4) ? "selected" : ""%> value="<%=Util.eSubtipoResidente.R4%>"><%=Util.eSubtipoResidente.R4%></option>
							  <option <%=_oMedico.getSubTipoResidente().equals(Util.eSubtipoResidente.R5) ? "selected" : ""%> value="<%=Util.eSubtipoResidente.R5%>"><%=Util.eSubtipoResidente.R5%></option>
							  <option <%=_oMedico.getSubTipoResidente().equals(Util.eSubtipoResidente.ROTANTE) ? "selected" : ""%> value="<%=Util.eSubtipoResidente.ROTANTE%>"><%=Util.eSubtipoResidente.ROTANTE%></option>
							  <option <%=_oMedico.getSubTipoResidente().equals(Util.eSubtipoResidente.SIMULADO) ? "selected" : ""%> value="<%=Util.eSubtipoResidente.SIMULADO%>"><%=Util.eSubtipoResidente.SIMULADO%></option>					  
						</select>						
						</div>												
						<% 			
						String _selected ="";
						if (_oMedico.isGuardiaSolo())
							_selected="checked";
						
						%>
						<div class="form-group">
							<label  class="control-label" id="labelguardiassolo">Guardias Solo:</label>
							<input  <%=_Disabled %> <%=_selected%> type="checkbox" name="guardiassolo"  id ="guardiassolo" value='<%=_oMedico.isGuardiaSolo()%>'/>
						</div>	
						<% _selected=""; 
						if (_oMedico.getActivoServicio().equals(new Long(1)))
							_selected="checked";
						String confirmed=""; 
						if (!_oMedico.isConfirmado())
							confirmed="disabled";
						
						%>
						<div class="form-group">
							<label  class="control-label" >Activo:</label>
							<input  <%=_Disabled %> <%=confirmed%> <%=_selected%> type="checkbox" <%=_selected %> name="activo" id="activo" value='<%=_oMedico.getActivoServicio()%>'/>
						</div>					
						<div class="form-group">
							<label  class="control-label"></label>
							<div id="vacaciones"></div>
							<script>
							<% 
												
							if (_lVacaciones!=null && _lVacaciones.size()>0)
							{
								for (int j=0;j<_lVacaciones.size();j++)
								{															
									Vacaciones_Medicos oVacaciones = _lVacaciones.get(j);
									%>
									holidays[<%=j%>] = '<%=oVacaciones.getDiaVacaciones()%>';
								<%																								
								}
							}
							%>
							</script>
							 <button  <%=_Disabled%>  type="button" onclick="date_()" class="pull-center btn btn-outline btn-success">Vacaciones</button>							 							  							
						</div>						
						
																	
						<input type="hidden" name="guardar"  value='1'/>
						<input type="hidden" name="id" id="id" value='<%=_oMedico.getID()%>'/>	
						<input type="hidden" name="pid" id="pid" value='<%=_ID%>'/>
						<input type="hidden" name="nuevo" id="nuevo" value='<%=Nuevo%>'/>
						<input type="hidden" name="oldEmail" id="oldEmail" value='<%=_oMedico.getEmail()%>'/>
						
						<div class="form-group">
    							<button type="submit"   class="btn btn-block  btn-primary">Guardar</button>
  						</div>				
						</form> 
		</div>
	</div>
	</div>
</div>
</div>
</body>
</html>
<%  }  %>