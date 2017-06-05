<%@page import="com.guardias.database.MedicoDBImpl"%>
<%@page import="guardias.security.SecurityUtil"%>
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
	<link href='<%=request.getContextPath()%>/css/bootstrap.min.css' rel='stylesheet' />
	<link href='<%=request.getContextPath()%>/css/jquery-ui.css' rel='stylesheet' />
	<link href='<%=request.getContextPath()%>/css/fullcalendar.css' rel='stylesheet' />
	<link href='<%=request.getContextPath()%>/css/fullcalendar.print.css' rel='stylesheet' media='print' />
    <link href="<%=request.getContextPath()%>/vendor/datatables-plugins/dataTables.bootstrap.css" rel="stylesheet">
    <link href="<%=request.getContextPath()%>/vendor/datatables-responsive/dataTables.responsive.css" rel="stylesheet">
	<link href="<%=request.getContextPath()%>/vendor/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">
	<link href="<%=request.getContextPath()%>/dist/css/sb-admin-2.css" rel="stylesheet">	
	<link href='<%=request.getContextPath()%>/css/custom.css?erddd444' rel='stylesheet'/>
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
</head>
<%

String _Token = "";
boolean bCorrect=false;
if (request.getParameter("auth")!=null && !request.getParameter("auth").equals(""))
{
	_Token = request.getParameter("auth");
	String EmailUser = SecurityUtil.DesEncriptarTokenEmail(_Token);	
	Medico AuthUser = MedicoDBImpl.getMedicoByEmail(EmailUser);
	
	if (!AuthUser.isConfirmado())  // ya está confirmado, no hacemos nada		
	{			
		AuthUser.setConfirmado(true);
		AuthUser.setActivo(true);
	
		MedicoDBImpl.UpdateMedico(AuthUser.getID(), AuthUser, false);
		bCorrect=true;
	
	}
}
  


%>


<body>

    <div id="wrapper">
	   
       <jsp:include page="/common/navigation.jsp"/>

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
                        <div class="panel-heading">
                            Confirmación de la cuenta de usuario
                        </div>
                        <!-- /.panel-heading -->
                        <div class="panel-body">                        	
                           <% if (bCorrect) { %>                           
                           La activación de la cuenta ha sido satisfactoria. A partir de estos momentos formas parte del sistema MEDONCALLS recibiendo en tu calendario tus guardias 
                           de manera periódica. Recuerda que activándola, aceptas las condiciones legales de uso marcadas en el pie de la página. 
                           <% } 
                           else 
                           { %>
                           Se ha procedido un error. Puede deberse a que la cuenta ya está previamente activada
                           <% } %>
					    </div>    
     				</div>
     			</div>
     		</div>
      </div> 	      
</body>
</html>


