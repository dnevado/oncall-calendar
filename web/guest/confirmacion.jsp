<%@page import="com.guardias.database.Guardias_ServiciosDBImpl"%>
<%@page import="com.guardias.servicios.*"%>

<%@page import="com.guardias.database.MedicoDBImpl"%>
<%@page import="guardias.security.SecurityUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@page import="com.guardias.*"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.*"%>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">

<title>Distribución Guardias Médicos Inicio</title>
<link href='<%=request.getContextPath()%>/css/jquery-ui.css'
	rel='stylesheet' />
<link href='<%=request.getContextPath()%>/css/bootstrap.min.css'
	rel='stylesheet' />
<link href='<%=request.getContextPath()%>/css/fullcalendar.css'
	rel='stylesheet' />
<link href='<%=request.getContextPath()%>/css/fullcalendar.print.css'
	rel='stylesheet' media='print' />

<!--  <link rel="stylesheet" type="text/css" href="//fonts.googleapis.com/css?family=Open+Sans" /> -->
<!-- Custom Fonts -->
<!-- DataTables Responsive CSS -->
<link
	href="<%=request.getContextPath()%>/vendor/datatables-plugins/dataTables.bootstrap.css"
	rel="stylesheet">
<link
	href="<%=request.getContextPath()%>/vendor/datatables-responsive/dataTables.responsive.css"
	rel="stylesheet">
<link
	href="<%=request.getContextPath()%>/vendor/font-awesome/css/font-awesome.min.css"
	rel="stylesheet" type="text/css">
<link href="<%=request.getContextPath()%>/dist/css/sb-admin-2.css"
	rel="stylesheet">
<link href='<%=request.getContextPath()%>/css/custom.css?erddd444'
	rel='stylesheet' />
<script src='<%=request.getContextPath()%>/js/lib/moment.min.js'></script>
<script src='<%=request.getContextPath()%>/js/lib/jquery.min.js'></script>
<script src='<%=request.getContextPath()%>/js/bootstrap.min.js'></script>
<script src='<%=request.getContextPath()%>/js/jquery-ui.js'></script>
<script src='<%=request.getContextPath()%>/js/jquery.ui.es.js'></script>
<script src='<%=request.getContextPath()%>/js/fullcalendar.min.js'></script>
<script src='<%=request.getContextPath()%>/js/locale-all.js'></script>
<script
	src="<%=request.getContextPath()%>/vendor/datatables/js/jquery.dataTables.patched.es.js"></script>
<script
	src="<%=request.getContextPath()%>/vendor/datatables-plugins/dataTables.bootstrap.min.js"></script>
<script
	src="<%=request.getContextPath()%>/vendor/datatables-responsive/dataTables.responsive.js"></script>
<script src='<%=request.getContextPath()%>/js/form/validator.js'></script>
<script src='<%=request.getContextPath()%>/js/ga.js'></script>
<script src='<%=request.getContextPath()%>/js/rrss.js?dsd'></script>
</head>


<jsp:useBean id="MedicoLogged" class="com.guardias.Medico"
	scope="session" />



<%



ResourceBundle RB = ResourceBundle.getBundle(Util.PROPERTIES_FILE, Locale.getDefault());

String _Token = "";
boolean bCorrect=false;
String EmailUser = "";	
Medico AuthUser=null;

String _TokenServiceID;
String ServicioID ;

Guardias_Servicios _oServicio =null; 

/* MAIL Y SERVICIO DEL QUE SE INVITA */
if (request.getParameter("auth")!=null && !request.getParameter("auth").equals(""))
{
	_Token = request.getParameter("auth");
	 EmailUser = SecurityUtil.DesEncriptarTokenEmail(_Token);	
	AuthUser = MedicoDBImpl.getMedicoByEmail(EmailUser, new Long(-1));
	
	/* servicio de la invitacion */
	if (request.getParameter("serviceauth")!=null && !request.getParameter("serviceauth").equals(""))
	{
		_TokenServiceID = request.getParameter("serviceauth");
		ServicioID = SecurityUtil.DesEncriptarTokenEmail(_TokenServiceID);
		_oServicio =Guardias_ServiciosDBImpl.getGuardias_ServiciosById(Integer.parseInt(ServicioID)); 
		
		AuthUser.setServicioId(Long.parseLong(ServicioID));
		
	}
	
	
}
if (MedicoLogged!=null && MedicoLogged.getEmail()!=null && !MedicoLogged.getEmail().equals(""))
{

	AuthUser = MedicoLogged;
	EmailUser = AuthUser.getEmail();
}


if (!AuthUser.isConfirmado())  // ya está confirmado, no hacemos nada		
{			
	AuthUser.setConfirmado(true);
	AuthUser.setActivo(true);
}


if (!_Token.equals(""))
{
	AuthUser.setActivoServicio(new Long(1));
	

	MedicoDBImpl.UpdateMedico(AuthUser.getID(), AuthUser, false);
	
	/* por si existe */
	try
	{
		MedicoDBImpl.AddMedicoAlServicio(AuthUser);	
	}
	catch (Exception e)
	{}
	
	
	
}





HttpSession _session = request.getSession(false);
_session.setAttribute("User",  EmailUser);
_session.setAttribute("MedicoLogged", AuthUser);

String resultUser = SecurityUtil.GenerateEncriptedRandomPassword(EmailUser);

Cookie cookie = new Cookie("UserCookie",resultUser);

cookie.setHttpOnly(true);
cookie.setMaxAge(0);
response.addCookie(cookie);
//response.sendRedirect(request.getContextPath() +"/inicio.jsp");

bCorrect=true;


  


%>



<body>

	<div id="wrapper">

		<jsp:include page="/common/navigation.jsp" />

	</div>
	<!-- /#wrapper -->
	<div id="page-wrapper">
		<div class="row">
			<div class="col-lg-12">
				<h1 class="page-header">Bienvenido/a</h1>
			</div>
			<!-- /.col-lg-12 -->
		</div>
		<!-- /.row -->
		<div class="row">
			<div class="col-lg-12">
				<div class="panel panel-default">
					<div class="panel-heading">Confirmación de la cuenta de 						usuario</div>
					<!-- /.panel-heading -->
					<div class="panel-body">
						<div class="container">
							<div class="row text-center">
								<div class="col-lg-12  col-md-8">
									<br> <br>
									<h2 style="color: #0fad00"><%=RB.getString("registration.success")%></h2>
									<img src="<%=request.getContextPath()%>/img/check-true.jpg">
									<h3>Estimado usuario</h3>
									<p class="f18px"><%=RB.getString("registration.message")%></p>
									<%
							        		if (_oServicio!=null)
							        		{%>
									<p class="f18px welcome_service"><%=RB.getString("registration.service")%>
										<%=_oServicio.getNombre() %></p>
									<%  
							        		}
							        		%>
									<p class="f18px"><%=RB.getString("registration.shareit")%></p>
									<jsp:include page="/common/rrss.jsp" />
									<script>
							        			$(".rrss .name").html("<%=RB.getString("registration.sharing")%>");
							        			$(".rrss .enlace").attr("href","<%=RB.getString("registration.url")%>");

										mostrarRRSS('rrss', 'enlace', 'name');
									</script>

									<jsp:include page="/common/start_tour.jsp" />

								</div>

							</div>
						</div>



					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>


