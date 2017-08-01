<%@page import="com.guardias.database.Guardias_ServiciosDBImpl"%>
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
		<link rel="stylesheet" type="text/css" href="//fonts.googleapis.com/css?family=Open+Sans" />
	
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

if ((request.getParameter("oauth_token")!=null && request.getParameter("oauth_token")!=null) || request.getParameter("code")!=null)
{
	
	SocialAuthManager manager = new SocialAuthManager();
	manager = (SocialAuthManager) session.getAttribute("authManager");
	
	AuthProvider provider = manager.connect(SocialAuthUtil.getRequestParametersMap(request));
	
	Profile p = provider.getUserProfile();
	
	try 
	{
		List<Contact> _lContacts = provider.getContactList();	
		for (Contact _Contacto : _lContacts)
		{
			System.out.println(_Contacto.getEmail());
		}
	}
	catch (Exception e)
	{
		
	}
	
	
	//System.out.println(p.getFirstName() + "," + p.getEmail());
	
	
	Medico oMNew;
	String PageConfirmation = request.getContextPath().concat("/inicio.jsp");
	if (p.getEmail()!=null)  // no existe y el mail viene de twitter	
	{
		
		oMNew = MedicoDBImpl.getMedicoByEmail(p.getEmail(),  new Long(-1));
		System.out.println(oMNew);
		if (oMNew==null || (oMNew!=null && oMNew.getEmail().equals("")))
		{
			
			
			PageConfirmation = request.getContextPath().concat("/guest/confirmacion.jsp");
			
			oMNew = new Medico();
			
			Medico ThisMedico = MedicoDBImpl.getUltimoIDMedico();
			
			oMNew.setActivo(true);
			oMNew.setAdministrator(false);
			oMNew.setApellidos(p.getLastName()!=null ? p.getLastName() : "Sin apellido"); 
			oMNew.setConfirmado(true);
			oMNew.setEmail(p.getEmail());
			oMNew.setNombre(p.getFirstName()!=null ? p.getFirstName() : "Sin nombre");
			oMNew.setID(ThisMedico.getID());
			oMNew.setOrigen(provider.getProviderId());
			
			MedicoDBImpl.AddMedico(oMNew);
		
		}
		else
		{
			// si viene el servicio a -1 buscamos un servicio por defecto ya que probablemente exiata previamente.
			Medico _ServiceOfMedico = MedicoDBImpl.getServicioPorDefecto(oMNew.getID());
			if (_ServiceOfMedico!=null)
			{
				oMNew.setServicioId(_ServiceOfMedico.getServicioId());	
			}
			
		}
		
		HttpSession _session = request.getSession(false);
		_session.setAttribute("User",  p.getEmail());
		_session.setAttribute("MedicoLogged", oMNew);
		
		String resultUser = SecurityUtil.GenerateEncriptedRandomPassword( p.getEmail());
		
		Cookie cookie = new Cookie("UserCookie",resultUser);
		
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
		response.sendRedirect(PageConfirmation);
	
	}
	
	
	
	
	
	
	
	//List contactsList = provider.getContactList();
	
	//System.out.println(contactsList);
	
	// you can obtain profile information System.out.println(p.getFirstName());

	// OR also obtain list of contacts List contactsList = provider.getContactList();
			
}


if (request.getParameter("loginrrss")!=null && request.getParameter("loginrrss")!=null)
{

	//Create an instance of SocialAuthConfgi object
	SocialAuthConfig _config = SocialAuthConfig.getDefault();

	//load configuration. By default load the configuration from oauth_consumer.properties. 
	//You can also pass input stream, properties object or properties file name.
	_config.load();

	//Create an instance of SocialAuthManager and set config
	SocialAuthManager manager = new SocialAuthManager();
	manager.setSocialAuthConfig(_config);

	//URL of YOUR application which will be called after authentication
	String _IsHttps = "";
	if (request.getHeader("x-forwarded-proto")!=null)		
		_IsHttps =request.getHeader("x-forwarded-proto");
	
	String successUrl = request.getRequestURL().toString(); /*  + "?loginRRSSSuccess=OK */
	if (_IsHttps.contains("https"))
		successUrl=successUrl.replace("http", "https");
	// get Provider URL to which you should redirect for authentication.
	// id can have values "facebook", "twitter", "yahoo" etc. or the OpenID URL
	String url = manager.getAuthenticationUrl(request.getParameter("loginrrss"), successUrl);

	// Store in session
	session.setAttribute("authManager", manager);
	response.sendRedirect(url);		
}








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
		
		if (oMLogged!=null &&  oMLogged.getServicioId().equals(new Long(-1)))  // servicio por defecto
		{
			Medico MedicoServicioDefault = MedicoDBImpl.getServicioPorDefecto(oMLogged.getID());
			oMLogged.setServicioId(MedicoServicioDefault.getServicioId());
			
		}
		
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
								   <div class="input-group-addon">
									<span class="glyphicon glyphicon-envelope"></span> 
								   </div>
								   <input class="form-control" required  id="email" name="email" type="text" maxlength="50" placeholder="Introduce tu email"/>
								  </div>
								 </div> 
								 <div class="form-group ">  
								  <div class="input-group">
								   <div class="input-group-addon">
									<span class="glyphicon glyphicon-lock"></span> 
								   </div>
								    <input class="form-control" required maxlength="50" placeholder="Introduce tu contraseña" name="password" type="password" value="">
								  </div>
								 </div> 
								                              
                                <!-- Change this to a button or input when using this as a form -->
                                <div class="auth-button-container">
                                	<button class="btn btn-primary btn-padded pull-right" type="submit">Entrar</button>
                                	</div>
                                </div>
                               <div class="auth-text"><a class="forgotten-password"><a href="<%=request.getContextPath() %>/guest/olvidar_contrasena.jsp">Olvidé la contraseña</a></div>
                                
                                <!--  RESDES SOCIALES  -->
                               
                            </fieldset>
                        </form>
                    </div>
                     <div class="auth-text">
                                
                                 		<div class="social-login">o entra con (* Condiciones legales aceptadas)
                                 			
			                        		<div class="social-login-buttons">
				                        	<a class="btn btn-link-1 btn-link-1-facebook" href="login.jsp?loginrrss=facebook">
				                        		<i class="fa fa-facebook"></i> Facebook
				                        	</a>
				                        	<!-- <a class="btn btn-link-1 btn-link-1-twitter" href="login.jsp?loginrrss=twitter">
				                        		<i class="fa fa-twitter"></i> Twitter
				                        	</a> -->
				                        	<a class="btn btn-link-1 btn-link-1-google-plus" href="login.jsp?loginrrss=googleplus">
				                        		<i class="fa fa-google-plus"></i> Google
				                        	</a>
				                        	<a class="btn btn-link-1 btn-link-1-microsoft" href="login.jsp?loginrrss=hotmail">
				                        		<i class="fa fa-windows"></i> Microsoft
				                        	</a>
			                        		</div>
	                     			   </div>
	      
                </div>
            </div>
        </div>
    </div>
<script>
$(document).ready(function() 
{
	$('#login').validator();
	$('#login').removeAttr('novalidate');	
	if ("<%=Error%>"=="true")
		$("#error").show();
	
});
</script>
<jsp:include page="/common/footer.jsp"></jsp:include>
</body>
</html>





