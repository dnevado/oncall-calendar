<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.*"%>
<%@page import="com.guardias.*"%>


<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>


<%

	ResourceBundle RB = ResourceBundle.getBundle(Util.PROPERTIES_FILE, Locale.getDefault());

%>

<div class="row col-lg-4 col-lg-offset-4">
		<div class="form-group">			
			<br>
			<a href="<%=request.getContextPath()%>/registration_tour.jsp"  class="btn btn-primary btn-block"><%= RB.getString("registration.start")%></a>
		
		</div>							
</div>