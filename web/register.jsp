<%@page import="guardias.security.SecurityUtil"%>
<%@page import="com.guardias.database.MedicoDBImpl"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.*"%>
<%@page import="java.util.*"%>
<%@page import="java.security.*"%>
<%@page import="org.apache.commons.codec.binary.Hex"%>
 
 

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
<body class="login">
<!--   <a href="#" data-toggle="modal" data-target="#login-modal">Login</a>-->

<%
String Error ="";

if (request.getParameter("email")!=null && request.getParameter("password")!=null)
{
	String User =  request.getParameter("email");
	String Password=  request.getParameter("password");
	
	//System.out.println(request.getServerName());
	
	//if (request.getServerName().contains("demo.")  ||  (User.contains("ana.maria.castano.leon@gmail.com") && Password.equals("102030")))
	if (User!=null && Password!=null)	
	{
		
		
		String resultPassword = SecurityUtil.GenerateEncriptedRandomPassword(Password);
		String resultUser = SecurityUtil.GenerateEncriptedRandomPassword(User);
		Medico oMLogged = MedicoDBImpl.getMedicoByEmail(User, new Long(-1));
		// en bbdd, activo, contraseña correcta y confirmado 
		if (oMLogged!=null &&  oMLogged.isActivo() && oMLogged.getPassWord().equals(resultPassword) && oMLogged.isConfirmado())
		{
			
			HttpSession _session = request.getSession(false);
			_session.setAttribute("User", User);
			_session.setAttribute("MedicoLogged", oMLogged);
			
			
			Cookie cookie = new Cookie("UserCookie",resultUser);
			
			cookie.setHttpOnly(true);
			cookie.setMaxAge(0);
			response.addCookie(cookie);
			response.sendRedirect(request.getContextPath() +"/inicio.jsp");
			
		}
		else
			Error = "true";
		
	}
	else
	{	
		Error = "true";
	    //request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response); // Redisplay login form.
	}
								
}	
%>

<div class="container">
		<div class="row">
			<div class="col-md-12">
            	<nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">			
					 <div class="navbar-header">                
             			<a class="navbar-brand" href="inicio.jsp"><img src="/Guardias/public/images/medoncalls4_s.jpg"></a>
            		</div>		            
    		    </nav>
    		 </div>   
        </div>

        <div class="row">
            <div class="col-md-4 col-md-offset-4">
            	
            	 
            	
 
                <div class="auth login-panel panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Entrar</h3>
                    </div>
                    <div  id=error  class="alert alert-danger" style="display:none">
                        
                            <p>Por favor, introduce un mail y contraseña válidos</p>
                        </div>                                               
                    	 
						<!--  SUCCESS -->    
                    
                    <div class="panel-body auth-fields auth-fields-login" >
                        <form method=post id="login" role="form" data-toggle="validator" >
                            <fieldset>
                               <div class="form-group ">  
                               	  
                               	  <div class="input-group">								   
								   <input class="form-control" required  id="name" name="name" type="text" maxlength="150" placeholder="Nombre"/>								 
								 </div> 
								  <div class="input-group">								   
								   <input class="form-control" required  id="lastname" name="lastname" type="text" maxlength="150" placeholder="Apellidos"/>								 
								 </div> 
								 
								 <div class="input-group">
								   <div class="input-group-addon">
									<span class="glyphicon glyphicon-envelope"></span> 
								   </div>
								   <input class="form-control" required  id="email" name="email" type="text" maxlength="50" placeholder="Introduce tu email"/>
								  </div>
								 </div> 
                               	
								  <div class="input-group">
								   <div class="input-group-addon">
									<span class="glyphicon glyphicon-envelope"></span> 
								   </div>
								   <input class="form-control" required  id="email" name="email" type="text" maxlength="50" placeholder="Introduce tu email"/>
								  </div>
								 </div> 
								                              
                                <!-- Change this to a button or input when using this as a form -->
                                <div class="auth-button-container">
                                	<button class="btn btn-primary btn-padded" type="submit">Entrar</button>
                                	<a class="forgotten-password">Olvidé la contraseña</a></div>
                                </div>
                               <div class="auth-text"><a href="<%=request.getContextPath()%>/register.jsp">¿No dispones de una cuenta todavía?</a></div>
                                
                                <!--  RESDES SOCIALES  -->
                               
                            </fieldset>
                        </form>
                    </div>
                  
            </div>
        </div>
    </div>
<script>
$(document).ready(function() 
{
	$('#login').validator();
	$('#login').removeAttr('novalidate');
	fmedico
	if ("<%=Error%>"=="true")
		$("#error").show();
		
	

});
</script>
</body>
</html>




