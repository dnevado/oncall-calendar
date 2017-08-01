
<%@page import="com.guardias.servicios.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="java.util.*"%>
<%@page import="java.text.*"%>
<%@page import="com.guardias.*"%> 
<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>



<nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
			
			 <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
             	<a class="navbar-brand" href="inicio.jsp"><img src="<%=request.getContextPath()%>/public/images/medoncalls4_s.jpg"/></a>
            </div>
			         
            <ul class="nav navbar-top-links navbar-right">
            
            	<li>
            	<% 
            	
            		/* existe cookie */
            		Long Selected=new Long(-1);
            		Cookie[] cookie = request.getCookies();
            		if (cookie.length>0)
            		{
            			
            		
            		for (Cookie micookie : cookie)
            		{
            			if (micookie.getName().equals("ServicioId"))
            			{		
            				
            					String _ValueCookie = micookie.getValue();
            					if (_ValueCookie.contains("|"))
            					{
            						// que no alteren la cookie y que pertenezca al usuario logueadp 
            						Long UserId = Long.parseLong(_ValueCookie.split("\\|",-1)[1]);
            						if (UserId.equals(MedicoLogged.getID()))
            						{
            							Selected = Long.parseLong(_ValueCookie.split("\\|",-1)[0]);  // Servicio|User
    									MedicoLogged.setServicioId(Selected);
            						}
            					}
								
            			}
            				
            		}
            		}
            	
					List<Guardias_Servicios> lItems = new ArrayList<Guardias_Servicios>();
					
					lItems =  Guardias_ServiciosDBImpl.getListGuardias_ServiciosOfUser(MedicoLogged.getID().intValue()); 
					
					if (!lItems.isEmpty()){ %>
					<div>		
						<select class="form-control" name="servicio_logged" id="servicio_logged" onchange="_changeServicio($(this).val(),true)">		
					<% }
					for (int j=0;j<lItems.size();j++)
					{
						Guardias_Servicios oGuardias_Servicios = lItems.get(j);
						
						%>
						
						
						<option <%=oGuardias_Servicios.getIdServicio().equals(MedicoLogged.getServicioId()) ? "selected" : ""%> value="<%=oGuardias_Servicios.getIdServicio() %>"><%=oGuardias_Servicios.getNombre()%></option>
							
					<% 
					}
					if (!lItems.isEmpty()) { %>
					%>
						</select>						
						</div>
				<%	}%>		
            	
            	</li>
                 
                <!-- /.dropdown -->
                <li><a href="javascript:EditarMedico(<%=MedicoLogged.getID()%>)"><%=(MedicoLogged.getApellidos() + " " + MedicoLogged.getNombre())  %></a></li>
                <li class="dropdown">
                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="fa fa-user fa-fw"></i> <i class="fa fa-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu dropdown-user">
                        <li><a href="javascript:EditarMedico(<%=MedicoLogged.getID()%>)"><i class="fa fa-user fa-fw"></i>Tu perfil</a>
                        </li>
                        <!-- <li><a href="#"><i class="glyphicon glyphicon-calendar"></i>Tu calendario</a> 
                        </li>
                        <li><a href="#"><i class="glyphicon glyphicon-thumbs-up"></i>Tus cambios</a>
                        </li> -->
                        <li class="divider"></li>
                        <li><a href="<%=request.getContextPath()%>/logout.jsp"><i class="fa fa-sign-out fa-fw"></i>Salir</a>
                        </li>
                    </ul>
                  
                </li>
                <!-- /.dropdown -->
            </ul>
            <!-- /.navbar-top-links -->



            <div class="navbar-default sidebar" role="navigation">
                <div class="sidebar-nav navbar-collapse">
                    <ul class="nav" id="side-menu">                       
                        <li>
                            <a href="<%=request.getContextPath()%>/inicio.jsp"><i class="glyphicon glyphicon-calendar"></i>Calendario</a>
                        </li>
                      
                        <li data-step='32'  data-intro='Aquí podrás consultar todas tus solicitudes'>
                            <a href="<%=request.getContextPath()%>/cambios_guardias.jsp"><i class="glyphicon glyphicon-thumbs-up"></i>Cambios</a>
                        </li>
                        <li>                        
                            <a href="<%=request.getContextPath()%>/report_totalporfechas.jsp"><i class="glyphicon glyphicon-list-alt"></i>Estadística</a>
                        </li>
                         <li>
                            <a href="<%=request.getContextPath()%>/servicios.jsp"><i class="glyphicon glyphicon-globe"></i>Servicios</a>
                        </li>
                        <li>
                            <a href="<%=request.getContextPath()%>/search_servicios.jsp" class="bs-wizard-dot"><i class="glyphicon glyphicon-hourglass"></i>Comunidad</a>
                        </li>
                          
                        <% 
							if (MedicoLogged.isAdministrator())
						{%>
						<li>
                            <a href="<%=request.getContextPath()%>/medicos.jsp"><i class="glyphicon glyphicon-user"></i>Médicos</a>
                        </li>
                       
                        <li>
                            <a href="<%=request.getContextPath()%>/config.jsp"><i class="glyphicon glyphicon-lock"></i>Configuración</a>
                        </li>
                        <% } %>
                        <!-- <li>
                            <a href="#"><i class="fa fa-bar-chart-o fa-fw"></i> Charts<span class="fa arrow"></span></a>
                            <ul class="nav nav-second-level">
                                <li>
                                    <a href="flot.html">Flot Charts</a>
                                </li>
                                <li>
                                    <a href="morris.html">Morris.js Charts</a>
                                </li>
                            </ul>                            
                        </li> -->                       
                    </ul>
                </div>
                <!-- /.sidebar-collapse -->
            </div>
            <!-- /.navbar-static-side -->
    	            
        </nav>
        
        
    