<%@page import="com.guardias.mail.MailingUtil"%>
<%@page import="org.brickred.socialauth.AuthProvider"%>
<%@page import="org.brickred.socialauth.AuthProviderFactory"%>
<%@page import="guardias.security.SecurityUtil"%>
<%@page import="com.guardias.database.MedicoDBImpl"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.*"%>
<%@page import="java.util.*"%>
<%@page import="java.security.*"%>
<%@page import="org.apache.commons.codec.binary.Hex"%>

<%@page import="org.brickred.*"%>
<%@page import="org.brickred.socialauth.*"%>
<%@page import="org.brickred.socialauth.util.*"%>
 
 

<!DOCTYPE html>
<html lang="es">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge"> 
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Distribuye eficientemente a todo tu servicio médico de manera ágil y organizada.Olvida la rutina de cuadrar turnos manualmente. Con medONcalls  te facilitamos esta tarea ahorrando la gestión y el coste de tiempo para ello.Recordatorios en tu calendario.Turnos equitativos entre los integrantes del servicio para un remuneración homogénea.En un solo click, podrás solicitar cambios en tu turno">
    <meta name="author" content="medONcalls">
    <meta name="keywords" content="agilidad,organización,eficiencia,oncalls,médicos,turnos,cambios de turnos,guardias,médicas,justas,remuneración,calendario,doctores,vacaciones,residente,adjunto,presencia,refuerzo,localizada">

    <title>Login medONcalls.Turnos de guardias ágiles, justas , eficientes y equitativas</title>
	<link href='<%=request.getContextPath()%>/css/bootstrap.min.css' rel='stylesheet' />
	<link href='<%=request.getContextPath()%>/css/jquery-ui.css' rel='stylesheet' />
	<link href='<%=request.getContextPath()%>/css/fullcalendar.css' rel='stylesheet' />
	<link href='<%=request.getContextPath()%>/css/fullcalendar.print.css' rel='stylesheet' media='print' />
    <link href="<%=request.getContextPath()%>/vendor/datatables-plugins/dataTables.bootstrap.css" rel="stylesheet">
    <link rel="icon"   type="image/png"  href="<%=request.getContextPath()%>/public/images/medoncalls4_s_ico.jpg">
    <link href="<%=request.getContextPath()%>/vendor/datatables-responsive/dataTables.responsive.css" rel="stylesheet">
	<link href="<%=request.getContextPath()%>/vendor/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">
	<link href="<%=request.getContextPath()%>/dist/css/sb-admin-2.css" rel="stylesheet">	
	<link href='<%=request.getContextPath()%>/css/custom.css?erddd444' rel='stylesheet'/>
	<link href='<%=request.getContextPath()%>/css/login.css?erddd444' rel='stylesheet'/>
	<script src='<%=request.getContextPath()%>/js/lib/moment.min.js'></script>
	<script src='<%=request.getContextPath()%>/js/lib/jquery.min.js'></script>
	<script src='<%=request.getContextPath()%>/js/bootstrap.min.js'></script>
	<script src='<%=request.getContextPath()%>/js/jquery.ui.js'></script>
	<script src='<%=request.getContextPath()%>/js/jquery.ui.es.js'></script>	
	<script src='<%=request.getContextPath()%>/js/fullcalendar.min.js'></script>
	<script src='<%=request.getContextPath()%>/js/locale-all.js'></script>	
	<script src='<%=request.getContextPath()%>/js/jquery.ui.es.js'></script>	
	<script src="<%=request.getContextPath()%>/vendor/datatables/js/jquery.dataTables.patched.es.js"></script>
	<script src="<%=request.getContextPath()%>/vendor/datatables-plugins/dataTables.bootstrap.min.js"></script>
	<script src="<%=request.getContextPath()%>/vendor/datatables-responsive/dataTables.responsive.js"></script>
	<script src='<%=request.getContextPath()%>/js/form/validator.js'></script>
	<script src='<%=request.getContextPath()%>/js/ga.js'></script>
	

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

</head>

<%


ResourceBundle RB = ResourceBundle.getBundle(Util.PROPERTIES_FILE, Locale.getDefault());

String _eMail ="";
Medico AuthUser=null;
String _sError = "";

if (request.getParameter("hiddenauth")!=null)  // CAMBIO DE CONTRASEÑA 
{
	try {
	_eMail = SecurityUtil.DesEncriptarTokenEmail(request.getParameter("hiddenauth"));
	}
	catch (Exception e) {}
	AuthUser = MedicoDBImpl.getMedicoByEmail(_eMail, new Long(-1));
	
	if (AuthUser!=null)
	{
	
		AuthUser.setPassWord(SecurityUtil.GenerateEncriptedRandomPassword(request.getParameter("password1")));
	
		MedicoDBImpl.UpdateMedico(AuthUser.getID(), AuthUser, false);
		_sError = "Se ha procedido a guardar correctamente los datos de la contraseña. Por favor, dirígete a la página de login";
	}
	else
		_sError = "NOOK.Token inválido o no existe";
	 
	
	
}

if (request.getParameter("auth")!=null)
{ 
	try {
		_eMail = SecurityUtil.DesEncriptarTokenEmail(request.getParameter("hiddenauth"));
		}
	catch (Exception e) {}	 
	AuthUser = MedicoDBImpl.getMedicoByEmail(_eMail, new Long(-1));
	 if (AuthUser==null)
		 _sError = "NOOK.Token inválido o no existe";
	
}
if (request.getParameter("emailInput")!=null)
{
	_eMail = SecurityUtil.EncriptarTokenEmail(request.getParameter("emailInput"));
	AuthUser = MedicoDBImpl.getMedicoByEmail(request.getParameter("emailInput"), new Long(-1));
	if (AuthUser!=null)
		MailingUtil.SendPasswordRecovery(AuthUser,  request);
	
	 _sError = "Se ha enviado un correo electrónico a la cuenta especificada";

	
}



%>

<body class="login">
<!--   <a href="#" data-toggle="modal" data-target="#login-modal">Login</a>-->

<script>
$(document).ready(function() 
{
	$('#password').validator();
	$('#password').removeAttr('novalidate');
	<% if (_sError.contains("NOOK")) {  %>
		$('#error').show();
	<% } %>
	
});

function passwords()
{
	
	if ($('#password1').val() != $('#password2').val()) {
		$('#error').show();
		return false;
	}

	
}

</script>


	


<div class="container">
	
	<div class="row">
			<div class="col-md-12">
            	<nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">			
					 <div class="navbar-header">                
             			<a class="navbar-brand" href="<%=request.getContextPath() %>/inicio.jsp"><img src="/Guardias/public/images/medoncalls4_s.jpg"></a>
            		</div>		            
    		    </nav>
    		 </div>   
    </div>
    <div class="row">
        <div class="row">
            <div class="col-md-4 col-md-offset-4">
                <div class="auth login-panel panel panel-default">
                    <div class="panel-body">
                        <div class="text-center">
                          <h3><i class="fa fa-lock fa-4x"></i></h3>
                          <h2 class="text-center">¿Olvidaste  Contraseña?</h2>
                          <p>Puedes generar una nueva aquí.</p>
                            <div class="panel-body">
                            <div  id=error  class="alert alert-danger" style="display:none">                        
	                        <div class="panel-body">
					                <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
					                <span class="glyphicon glyphicon-hand-right"></span> <strong>Aviso</strong>
					                <hr class="message-inner-separator">
					                <p><%= RB.getString("registration.passwordnotmach")  %></p> 
	                        </div>                                               
	                    	</div> 
                            <!-- SUCCESS  -->
                             <% if (request.getParameter("emailInput")!=null || request.getParameter("hiddenauth")!=null) { %>                         
	                        <div  id=success  class="alert alert-success">
	                        <div class="panel-body">
				               <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
				               <span class="glyphicon glyphicon-ok"></span> <strong><%=_sError %></strong>				                                     
	                        </div>                                               
	                    	</div> 
                            <% } %>                               
                              <form class="form" id="password" method="post" onsubmit="return passwords()">
                                <fieldset>
                                  <div class="form-group">
                                  <% if (request.getParameter("auth")==null) { %>
                                   <div class="input-group">
                                      <span class="input-group-addon"><i class="glyphicon glyphicon-envelope color-blue"></i></span>                                      
                                      <input name="emailInput"  id="emailInput" placeholder="email" class="form-control" type="email"  required>
                                    </div>
                                  <%  } 
                                  else { %>
                                  	 <div class="input-group">
	                                  		<div class="input-group-addon">
												<span class="glyphicon glyphicon-lock"></span> 
										   </div>                        
			                               <input id="password1" name=password1  data-minlength="6"  placeholder="Utiliza 6 caracteres contraseña" class="form-control" type="password"  required>
			                             
			                               
	                                   
                                     </div>
                                      <div class="input-group">
	                                      <div class="input-group-addon">
												<span class="glyphicon glyphicon-lock"></span> 
										   </div>     
	                                       <input id="password2" name=password2 data-minlength="6"  placeholder="Introduce de nuevo la contraseña" class="form-control" type="password"  required>
	                                       
	                                       
	                                       <input name="hiddenemail" value=<%=_eMail %> class="form-control" type="hidden"  required>
	                                       <input name="hiddenauth" value=<%=request.getParameter("auth") %> class="form-control" type="hidden"  required>
	                                       
                                    </div>                                  	                                  
                                  <%  } %> 
                                  
                                   
                                  </div>
                                  <div class="form-group">
                                    <input class="btn btn-lg btn-primary btn-block" value="Enviar Contraseña" type="submit">
                                  </div>
                                </fieldset>
                              </form>
                              
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>    
</div>
<jsp:include page="/common/footer.jsp"></jsp:include>
</body>
</html>

