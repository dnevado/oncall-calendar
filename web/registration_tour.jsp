<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.*"%>
<%@page import="java.util.*"%>

<!DOCTYPE html>
<html lang="es">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge"> 
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    <title>Distribución Guardias Médicos Inicio </title>
	<link href='<%=request.getContextPath()%>/css/jquery-ui.css' rel='stylesheet' /> 
	<link href='<%=request.getContextPath()%>/css/bootstrap.min.css' rel='stylesheet' />	
	<link href='<%=request.getContextPath()%>/css/fullcalendar.css' rel='stylesheet' />
	<link href='<%=request.getContextPath()%>/css/fullcalendar.print.css' rel='stylesheet' media='print' />     
    <link href="<%=request.getContextPath()%>/vendor/datatables-plugins/dataTables.bootstrap.css" rel="stylesheet">
    <link href="<%=request.getContextPath()%>/vendor/datatables-responsive/dataTables.responsive.css" rel="stylesheet">
	<link href="<%=request.getContextPath()%>/vendor/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">
	<link href="<%=request.getContextPath()%>/dist/css/sb-admin-2.css" rel="stylesheet">
	<link href="<%=request.getContextPath()%>/css/registration_tour.css" rel="stylesheet">
	<link href='<%=request.getContextPath()%>/css/custom.css?erddd444' rel='stylesheet'/>
		<link rel="stylesheet" type="text/css" href="//fonts.googleapis.com/css?family=Open+Sans" />
	
	<script src='<%=request.getContextPath()%>/js/lib/moment.min.js'></script>
	<script src='<%=request.getContextPath()%>/js/lib/jquery.min.js'></script>
	<script src='<%=request.getContextPath()%>/js/bootstrap.min.js'></script>
	<script src='<%=request.getContextPath()%>/js/jquery-ui.js'></script>
	<script src='<%=request.getContextPath()%>/js/jquery.ui.es.js'></script>
	<script src='<%=request.getContextPath()%>/js/fullcalendar.min.js'></script>
	<script src='<%=request.getContextPath()%>/js/locale-all.js'></script>	
	<script src="<%=request.getContextPath()%>/vendor/datatables/js/jquery.dataTables.patched.es.js"></script>
	<script src="<%=request.getContextPath()%>/vendor/datatables-plugins/dataTables.bootstrap.min.js"></script>
	<script src="<%=request.getContextPath()%>/vendor/datatables-responsive/dataTables.responsive.js"></script>
	<script src='<%=request.getContextPath()%>/js/form/validator.js'></script>
	<script src='<%=request.getContextPath()%>/js/jquery-ui.multidatespicker.js'></script>
	<script src='<%=request.getContextPath()%>/js/ga.js'></script>
	
	
	

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

</head>
<script>  		
		var _REQUEST_CONTEXT ='<%=request.getContextPath()%>/';
		var _REQUEST_URI ="<%=request.getRequestURI()%>";		
		var _APROBADA ="<%=Util.eEstadoCambiosGuardias.APROBADA.toString()%>";
		var _CANCELADA ="<%=Util.eEstadoCambiosGuardias.CANCELADA.toString()%>";
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
                    <h1 class="page-header">Gracias por registrarte en medONcalls</h1>
                    <h3>Ya te queda muy poco, registra tu grupo o servicio y añade a todos los integrantes</h3>
                </div>
               
            </div>
      
		    <div class="row bs-wizard" style="border-bottom:0;">
                
                <div class="col-xs-3 bs-wizard-step complete">
                  <div class="text-center bs-wizard-stepnum"><strong>Registro</strong></div>
                  <div class="progressreg"><div class="progress-bar"></div></div>
                  <a href="#" class="bs-wizard-dot"></a>
                  <div class="bs-wizard-info text-center"></div>
                </div>
                
                <div class="col-xs-3 bs-wizard-step complete"><!-- complete -->
                  <div class="text-center bs-wizard-stepnum"><strong>Confirma tu cuenta</strong></div>
                  <div class="progressreg"><div class="progress-bar"></div></div>
                  <a href="#" class="bs-wizard-dot"></a>
                  <div class="bs-wizard-info text-center"></div>
                </div>
                
                <div class="col-xs-3 bs-wizard-step active"><!-- complete -->
                  <div class="text-center bs-wizard-stepnum"><strong>Genera tu servicio o busca uno existente</strong></div>
                  <div class="progressreg"><div class="progress-bar"></div></div>
                  <a href="<%=request.getContextPath()%>/search_servicios.jsp" class="bs-wizard-dot"></a>
                  <div class="bs-wizard-info text-center"></div>
                </div>
                
                <div class="col-xs-3 bs-wizard-step disabled"><!-- active -->
                  <div class="text-center bs-wizard-stepnum"><strong>Recibe o gestiona las guardias</strong></div>
                  <div class="progressreg"><div class="progress-bar"></div></div>
                  <a href="#" class="bs-wizard-dot"></a>
                  <div class="bs-wizard-info text-center"> </div>
                </div>
            </div>
</div>      	
<jsp:include page="/common/footer.jsp"></jsp:include>			   
</body>
</html>


