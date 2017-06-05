<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.database.*"%> 
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
<link href='<%=request.getContextPath()%>/css/bootstrap.min.css' rel='stylesheet' />
<link href='<%=request.getContextPath()%>/css/jquery-ui.css' rel='stylesheet' />
<link href='<%=request.getContextPath()%>/css/fullcalendar.css' rel='stylesheet' />
<link href='<%=request.getContextPath()%>/css/custom.css?er4544423423423' rel='stylesheet'/> 
<link href="<%=request.getContextPath()%>/vendor/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">
<link href="<%=request.getContextPath()%>/dist/css/sb-admin-2.css" rel="stylesheet">	
<script src='<%=request.getContextPath()%>/js/lib/moment.min.js'></script>
<script src='<%=request.getContextPath()%>/js/lib/jquery.min.js'></script>
<script src='<%=request.getContextPath()%>/js/bootstrap.min.js'></script>
<script src='<%=request.getContextPath()%>/js/jquery-ui.js'></script>
<script src='<%=request.getContextPath()%>/js/jquery.ui.es.js'></script>
<script src='<%=request.getContextPath()%>/js/fullcalendar.min.js'></script>
<script src='<%=request.getContextPath()%>/js/locale-all.js'></script>
<script src='<%=request.getContextPath()%>/js/guardias.js?timestamp=23423'></script>
</head>
<body>
    <div id="wrapper">
       <jsp:include page="common/navigation.jsp"/>
    </div>
     <div id="page-wrapper">
            <div class="row">
                <div class="col-lg-12">
                    <h1 class="page-header">Configuración de Email de Bienvenida</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.row -->
            <div class="row">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            Contenido
                        </div>
                        <!-- /.panel-heading -->
                        <div class="panel-body">                        	
                           <jsp:include page="email/config_emails.jsp"/>
					    </div>    
     				</div>
     			</div>
     		</div>
      </div> 	      
</body>
</html>

