  <%@page import="guardias.security.SecurityUtil"%>
<%@page import="com.guardias.mail.MailingUtil"%>
<%@page import="com.guardias.database.MedicoDBImpl"%>
<%@page import="com.guardias.servicios.Guardias_Servicios"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>


<%@page import="com.guardias.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.text.SimpleDateFormat"%> 
<%@page import="java.util.*"%>
<%@page import="java.text.*"%>


<%@page import="com.guardias.Vacaciones_Medicos"%>

<%

ResourceBundle RB = ResourceBundle.getBundle(Util.PROPERTIES_FILE, Locale.getDefault());


List<Guardias_Servicios> lItems=null;    		
lItems =  Guardias_ServiciosDBImpl.getListAllGuardias_ServiciosOfUser(MedicoLogged.getID().intValue()); 

String name = request.getParameter("name") !=null ? request.getParameter("name")  : "";

Guardias_Servicios _oServicioSearch = new Guardias_Servicios();
_oServicioSearch.setNombre(name);
_oServicioSearch.setActivo(new Long(1));
_oServicioSearch.setVisible(new Long(1));


List<Guardias_Servicios> lServicioExiste = Guardias_ServiciosDBImpl.getGuardias_ServiciosByName(_oServicioSearch); 
int total = lServicioExiste!=null && !lServicioExiste.isEmpty() ? lServicioExiste.size() : 0;

SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy"); 

%>
  

  
  <div class="modal fade" id="success"  tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
       <button type="button" class="close" data-dismiss="modal">&times;</button>				      
        <h5 class="modal-title" id="exampleModalLabel">Uniéndose al servicio</h5>				        
      </div>
      <div class="modal-body">
        <span><%=RB.getString("group.join")%></span>
      </div>				      
    </div>
  </div>
</div>
  
  <hgroup class="mb20">
		<h2>Resultados</h2>
		<h4  class="lead"><strong><%=total %></strong> resultados han sido encontrados para <strong><%=name %></strong></h4>								
	</hgroup>

    <section class="col-xs-12 col-sm-6 col-md-12">
    <% if (total>=0) 
    {
    	for (Guardias_Servicios oGS : lServicioExiste)
    	{
    		
    		boolean Member = false;
    		boolean Active = false;
    		for (Guardias_Servicios oGSExiste : lItems)
    		{
    			if (oGSExiste.getIdServicio().equals(oGS.getIdServicio()))
    			{
    				Member = true;    		
    				Medico _MedicoServicio  = MedicoDBImpl.getMedicos(MedicoLogged.getID(), oGSExiste.getIdServicio()).get(0);
    				if (_MedicoServicio.getActivoServicio().equals(new Long(1)))
    				{
    					Active=true;
    				}
    				break;
    			}
    		}
    		
    		
		
    		
    	%>
    
		<article class="search-result row">
			<!-- <div class="col-xs-12 col-sm-12 col-md-3">
				<div><img src="http://lorempixel.com/250/140/people" alt="<%=oGS.getNombre() %>" /></div>
			</div> -->			
			<div class="col-xs-12 col-sm-12 col-md-9 excerpet">
				<h3><%=oGS.getNombre() %></h3>
				<p id="descripcion"><%=oGS.getDescripcion() %>.</p>
				<p id="fecha"><i class="glyphicon glyphicon-calendar"></i> <span><%=df.format(oGS.getFechaCreacion()) %></span></p>
				<p id="member"><i class="glyphicon glyphicon-user"></i> <span><%=oGS.getTotalMiembros() %> Miembros</span></p>
				<p id="admin"><i class="glyphicon glyphicon-thumbs-up"></i> <span><%=oGS.getMedicoOwner() %> (Administrador)</span></p>
				
				
				<div class="form-group pull-right">
				
				<% 
				String _Member="";
				String _Pending ="";
				if (!Active)
				{
					_Pending = RB.getString("group.joinpending");
				}
				if (!Member) { 
					_Member = "hide";	
				}	%>
				<div class="yainscrito <%=_Member%>" >Ya eres miembro <%=_Pending %></div>
				<%  if (!Member) {%> 
    					<button type="button" class="btn  btn-primary" onclick="_JoinServicio('<%=oGS.getIdServicio() %>')">Únete</button>
    			<% }  %>    							
  				</div>						
               
			</div>
			
			<span class="clearfix borda"></span>
		</article>

       <%
    	}
    }
       %>

</section>