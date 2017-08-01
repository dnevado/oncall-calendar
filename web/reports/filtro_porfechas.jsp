<%@ page language="java" contentType="text/html; charset=UTF-8"    pageEncoding="UTF-8"%>
    
<%@page import="com.guardias.Medico"%>    
<%@page import="com.guardias.cambios.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="com.guardias.Util"%>
<%@page import="java.text.SimpleDateFormat"%>


<%@page import="java.util.*"%>
<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>
<script>
$(document).ready(function() 
{
	$('#ffilter').validator();
	
});
</script>
<% 
String desde  = request.getParameter("desde")!=null ? request.getParameter("desde") : "";
String hasta  = request.getParameter("hasta")!=null ? request.getParameter("hasta") : "";

SimpleDateFormat _sdf = new SimpleDateFormat("yyy-MM-dd");
 
if (desde.equals(""))
{
	Calendar _c = Calendar.getInstance();
	_c.set(Calendar.DATE, 1);
	Date _HOY = _c.getTime();
	_c.add(Calendar.MONTH, -1);
	Date _HOY_1 = _c.getTime();
	
	desde = _sdf.format(_HOY_1);
	hasta = _sdf.format(_HOY);
	
	
}


%>
<form  id=ffilter method=post role="form" data-toggle="validator">                                     						

<div class="form-group">
<label  class="control-label">Desde:</label>
<input   required  class="ui-textfield form-control" type="date" name="desde" id="desde" value="<%= desde%>"/>						
</div>						
<div class="form-group">
<label  class="control-label" >Hasta:</label>
<input    required class="ui-textfield form-control" type="date" name="hasta" id="hasta" value="<%= hasta%>"/>
</div>
<div class="form-group">
					<button type="submit"  class="btn btn-block  btn-primary">Enviar</button>
</div>				
</form>



