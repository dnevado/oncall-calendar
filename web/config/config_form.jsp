<%@page import="com.guardias.database.ConfigurationDBImpl"%>
<%@page import="com.guardias.Configuracion"%>
<%@page import="com.guardias.Util"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.*"%>

<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>

 


<script>
	
	/* SIEMPRE QUE HAYA VALORES DE UNO O MAS DE UNO Y RESIDENTES MAS DE 1 */
	function fn_validate()
	{

		if ($("#numero_guardias_residentes").val()>1)
			
			if 	($("#numero_local_y_refuerzos").val()>1 &&  
					$("#numero_local_y_refuerzos").val()>=$("#numero_guardias_residentes").val())
			{	
			
				$("#error").show();
				return false;

			}
		
	}


	function fn_EnableMoreData() {

		if ($("#calendario:checked") != null) {
			$("#calendario1").show();
			$("#calendario2").show();
			$("#calendario3").show();
		} else {
			$("#calendario1").hide();
			$("#calendario2").hide();
			$("#calendario3").hide();
		}

	}
</script>


<%
	if (request.getParameter("saving") != null) {

		Long maxiteraciones = Long.parseLong(request.getParameter("maxiteraciones"));
		//Long request.getParameter("bbdd")
		Long max_dias_seguidos_adjuntos = Long.parseLong(request.getParameter("max_dias_seguidos_adjuntos"));
		//	Long max_dias_seguidos_residentes =  Long.parseLong(request.getParameter("max_dias_seguidos_residentes"));
		Long max_diferencia_entre_simulados_de_adjuntos = Long
				.parseLong(request.getParameter("max_diferencia_entre_simulados_de_adjuntos"));

		String gserviceaccount = request.getParameter("gserviceaccount");

		String poolDay = "N";
		if (request.getParameter("poolDay") != null)
			poolDay = request.getParameter("poolDay");
		
		String usar_secuencia_en_presencia = "N";
		if (request.getParameter("usar_secuencia_en_presencia") != null)
			usar_secuencia_en_presencia = request.getParameter("usar_secuencia_en_presencia");
		
		
		String emailfrom = request.getParameter("emailfrom");
		String emailfrompwd = request.getParameter("emailfrompwd");

		Long minutosrecordatorio = Long.parseLong(request.getParameter("minutosrecordatorio"));

		String activar_cambio_guardias = "N";
		if (request.getParameter("activar_cambio_guardias") != null)
			activar_cambio_guardias = request.getParameter("activar_cambio_guardias");

		String validar_cambio_guardias = "N";
		if (request.getParameter("validar_cambio_guardias") != null)
			validar_cambio_guardias = request.getParameter("validar_cambio_guardias");

		String calendario = "N";

		if (request.getParameter("calendario") != null) {
			calendario = "S";
		}
		
		Long numero_guardias_presencia = new Long(-1);
		if (request.getParameter("numero_guardias_presencia") != null) {
			numero_guardias_presencia = Long.parseLong(request.getParameter("numero_guardias_presencia")) ;
		}
		Long numero_guardias_residentes = new Long(-1);
		if (request.getParameter("numero_guardias_residentes") != null) {
			numero_guardias_residentes = Long.parseLong(request.getParameter("numero_guardias_residentes")) ;
		}
		Long numero_local_y_refuerzos = new Long(-1);
		if (request.getParameter("numero_local_y_refuerzos") != null) {
			numero_local_y_refuerzos = Long.parseLong(request.getParameter("numero_local_y_refuerzos")) ;
		}
		Long max_diferencia_entre_guardias_localizadas_refuerzos = new Long(-1);
		if (request.getParameter("max_diferencia_entre_guardias_localizadas_refuerzos") != null) {
			max_diferencia_entre_guardias_localizadas_refuerzos = Long.parseLong(request.getParameter("max_diferencia_entre_guardias_localizadas_refuerzos")) ;
		}
		Long max_total_presencias_refuerzo_festivas= new Long(-1);
		if (request.getParameter("max_total_presencias_refuerzo_festivas") != null) {
			max_total_presencias_refuerzo_festivas = Long.parseLong(request.getParameter("max_total_presencias_refuerzo_festivas")) ;
		}
		
		String[] meses_ajustar_asignacion_residentes = {""};
		if (request.getParameter("meses_ajustar_asignacion_residentes") != null) {
			meses_ajustar_asignacion_residentes = request.getParameterValues("meses_ajustar_asignacion_residentes");
		}
		
		Configuracion _oConfiguracion = new Configuracion();
		
		
		_oConfiguracion.setConfig_IdServicio(MedicoLogged.getServicioId());
		
		/* _oConfiguracion.setKey(Util.getoCONST_MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS());
		_oConfiguracion.setValue(max_dias_seguidos_residentes.toString());	
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);
		*/
		_oConfiguracion.setKey(Util.getoCONST_MAX_TOTAL_PRESENCIA_REFUERZO_FESTIVAS());
		_oConfiguracion.setValue(max_total_presencias_refuerzo_festivas.toString());
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);
		
		
		_oConfiguracion.setKey(Util.getoCONST_NUMERO_DIAS_SEGUIDOS_ADJUNTOS());
		_oConfiguracion.setValue(max_dias_seguidos_adjuntos.toString());
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);

		_oConfiguracion.setKey(Util.getoCONST_DIFERENCIA_MAX_SIMULADOS_POR_ADJUNTO_MES());
		_oConfiguracion.setValue(max_diferencia_entre_simulados_de_adjuntos.toString());
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);

		_oConfiguracion.setKey(Util.getoCONST_MAXIMAS_ITERACIONES_PERMITIDAS());
		_oConfiguracion.setValue(maxiteraciones.toString());
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);

		_oConfiguracion.setKey(Util.getoCALENDARIO_GMAIL());
		_oConfiguracion.setValue(calendario);
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);

		_oConfiguracion.setKey(Util.getoCONST_CALENDARIO_MINUTOS_RECORDATORIO());
		_oConfiguracion.setValue(minutosrecordatorio.toString());
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);

		_oConfiguracion.setKey(Util.getoCONST__SERVICE_CALENDAR());
		_oConfiguracion.setValue(gserviceaccount);
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);

		_oConfiguracion.setKey(Util.getoCONST_MAIL_FROM());
		_oConfiguracion.setValue(emailfrom);
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);

		_oConfiguracion.setKey(Util.getoCONST_MAIL_FROM_PASSWORD());
		_oConfiguracion.setValue(emailfrompwd);
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);

		_oConfiguracion.setKey(Util.getoCONST_ACTIVAR_CAMBIO_GUARDIAS());
		_oConfiguracion.setValue(activar_cambio_guardias);
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);

		_oConfiguracion.setKey(Util.getoCONST_VALIDAR_CAMBIOS_GUARDIAS_BY_ADM());
		_oConfiguracion.setValue(validar_cambio_guardias);
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);
		
		_oConfiguracion.setKey(Util.getoCONST_EXISTE_POOLDAY());
		_oConfiguracion.setValue(poolDay);
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);
		
		
		_oConfiguracion.setKey(Util.getoCONST_NUMERO_PRESENCIAS());
		_oConfiguracion.setValue(numero_guardias_presencia.toString());
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);
		
		_oConfiguracion.setKey(Util.getoCONST_NUMERO_REFUERZOS_LOCALIZADAS());
		_oConfiguracion.setValue(numero_local_y_refuerzos.toString());
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);
		
		_oConfiguracion.setKey(Util.getoCONST_NUMERO_RESIDENTES());
		_oConfiguracion.setValue(numero_guardias_residentes.toString());
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);
		
		_oConfiguracion.setKey(Util.getoCONST_MAX_DIF_MAYOR_MENOR_ADJUNTO_GUARDIAS_PRE_LOC_REF());
		_oConfiguracion.setValue(max_diferencia_entre_guardias_localizadas_refuerzos.toString());
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);
		
		
		String joined = String.join("|", meses_ajustar_asignacion_residentes);

		_oConfiguracion.setKey(Util.getoCONST_AJUSTE_A_ESTOS_MESES_VACACIONES());
		_oConfiguracion.setValue(joined);
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);
		
		_oConfiguracion.setKey(Util.getoCONST_USAR_SECUENCIA_EN_PRESENCIA());
		_oConfiguracion.setValue(usar_secuencia_en_presencia);
		ConfigurationDBImpl.UpdateConfiguracion(_oConfiguracion);

		
		
		

	}

	//Configuracion oCONST_MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS());
	Configuracion oCONST_NUMERO_DIAS_SEGUIDOS_ADJUNTOS = ConfigurationDBImpl
			.GetConfiguration(Util.getoCONST_NUMERO_DIAS_SEGUIDOS_ADJUNTOS(), MedicoLogged.getServicioId());
	Configuracion oCONST_DIFERENCIA_MAX_SIMULADOS_POR_ADJUNTO_MES = ConfigurationDBImpl
			.GetConfiguration(Util.getoCONST_DIFERENCIA_MAX_SIMULADOS_POR_ADJUNTO_MES(), MedicoLogged.getServicioId());
	Configuracion oCONST_MAXIMAS_ITERACIONES_PERMITIDAS = ConfigurationDBImpl
			.GetConfiguration(Util.getoCONST_MAXIMAS_ITERACIONES_PERMITIDAS(), MedicoLogged.getServicioId());
	Configuracion oCALENDARIO_GMAIL = ConfigurationDBImpl.GetConfiguration(Util.getoCALENDARIO_GMAIL(), MedicoLogged.getServicioId());
	//Configuracion oDATABASE_PATH = ConfigurationDBImpl.GetConfiguration(Util.oDATABASE_PATH, MedicoLogged.getServicioId());
	Configuracion oGOOGLE_OAUTH = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_GOOGLE_SERVICE_ACCOUNT(), new Long(-1));
	Configuracion oGOOGLE_CALENDAR_RECORDATORIO = ConfigurationDBImpl
			.GetConfiguration(Util.getoCONST_CALENDARIO_MINUTOS_RECORDATORIO(), MedicoLogged.getServicioId());

	Configuracion oMAILFROM = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_MAIL_FROM(), MedicoLogged.getServicioId());
	Configuracion oMAILFROM_PWD = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_MAIL_FROM_PASSWORD(), MedicoLogged.getServicioId());

	Configuracion oACTIVAR_CAMBIO_GUARDIAS = ConfigurationDBImpl
			.GetConfiguration(Util.getoCONST_ACTIVAR_CAMBIO_GUARDIAS(), MedicoLogged.getServicioId());
	Configuracion oVALIDAR_CAMBIO_GUARDIAS_BY_ADMIN = ConfigurationDBImpl
			.GetConfiguration(Util.getoCONST_VALIDAR_CAMBIOS_GUARDIAS_BY_ADM(), MedicoLogged.getServicioId());
	
	Configuracion oUSAR_SECUENCIA_EN_PRESENCIA = ConfigurationDBImpl
			.GetConfiguration(Util.getoCONST_USAR_SECUENCIA_EN_PRESENCIA(), MedicoLogged.getServicioId());
	
	
	Configuracion oEXISTE_POOL_DAY = ConfigurationDBImpl
			.GetConfiguration(Util.getoCONST_EXISTE_POOLDAY(), MedicoLogged.getServicioId());
	
	Configuracion oNUMERO_GUARDIAS_PRESENCIA = ConfigurationDBImpl
			.GetConfiguration(Util.getoCONST_NUMERO_PRESENCIAS(), MedicoLogged.getServicioId());
	Configuracion oNUMERO_REFUERZOS_LOCALIZADAS = ConfigurationDBImpl
			.GetConfiguration(Util.getoCONST_NUMERO_REFUERZOS_LOCALIZADAS(), MedicoLogged.getServicioId());
	Configuracion oNUMERO_RESIDENTES_POR_DIA = ConfigurationDBImpl
			.GetConfiguration(Util.getoCONST_NUMERO_RESIDENTES(), MedicoLogged.getServicioId());

	Configuracion oAJUSTE_A_ESTOS_MESES_VACACIONES = ConfigurationDBImpl
			.GetConfiguration(Util.getoCONST_AJUSTE_A_ESTOS_MESES_VACACIONES(), MedicoLogged.getServicioId());  // id meses + 1 ENERO=1  "6|5"
	
	Configuracion oMAXIMO_TOTAL_PRESENCIAS_LOCALIZADAS_FESTIVAS = ConfigurationDBImpl
					.GetConfiguration(Util.getoCONST_MAX_TOTAL_PRESENCIA_REFUERZO_FESTIVAS(), MedicoLogged.getServicioId());  // id meses + 1 ENERO=1  "6|5"
			
	
			
	
	Configuracion oMAX_DIF_MAYOR_MENOR_ADJUNTO_GUARDIAS_PRE_LOC_REF = ConfigurationDBImpl
					.GetConfiguration(Util.getoCONST_MAX_DIF_MAYOR_MENOR_ADJUNTO_GUARDIAS_PRE_LOC_REF(), MedicoLogged.getServicioId());
	
	 ResourceBundle RB = ResourceBundle.getBundle(Util.PROPERTIES_FILE, Locale.getDefault());
	 
	 

	
%>

<form id=fconfig method=post role="form" data-toggle="validator" onsubmit="return fn_validate()">

	<div id=success class="panel panel-success" style="display: none">
		<div class="panel-heading">Correcto.</div>
		<div class="panel-body">
			<p>Los datos han sido guardados satisfactoriamente</p>
		</div>
	</div>
	<div  id=error  class="alert alert-danger" style="display:none">
	   	<div class="panel-heading"></div>
	   	<div class="panel-body">
    	   	<p>El número de LOCALIZADAS y REFUERZOS debe ser menor que el número de RESIDENTES por día</p>
   		</div>                                               
</div> 

	<!--  SUCCESS -->
	<div class="form-group">
		<label class="control-label"> Máximas iteraciones (En caso de
			embuclarse por condiciones, detectar y salir) <span class="glyphicon glyphicon-question-sign" title=""></span>
			</label> <input maxlength="6"
			required class="ui-textfield form-control" type="number"
			name="maxiteraciones" id="maxiteraciones"
			value="<%=oCONST_MAXIMAS_ITERACIONES_PERMITIDAS.getValue()%>" />
	</div>
	<div class="form-group">
		<label class="control-label">Ruta/_Name Base Datos SqlLite <span class="glyphicon glyphicon-question-sign" title="info"></label> <input
			disabled="disabled" maxlength="6" class="ui-textfield form-control"
			type="text" name="bbdd" id="bbdd"
			value='<%=System.getProperty("catalina.home")%>)\BBDD_sqllite\guardias.db' />
	</div>
	<div class="form-group">
		<label class="control-label">Máximo Número de Días Seguidos
			Adjuntos (Excepto Presencia - Presencia) <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.numerodias_seguidos_adjuntos")%>"></label> <input maxlength="2"
			required class="ui-textfield form-control" type="number"
			name="max_dias_seguidos_adjuntos" id="max_dias_seguidos_adjuntos"
			value='<%=oCONST_NUMERO_DIAS_SEGUIDOS_ADJUNTOS.getValue()%>' />
	</div>

	<div class="form-group">
		<label class="control-label">Máxima diferencia entre simulados
			asignados a los adjuntos por mes <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.max_diferencia_entre_simulados_adjuntos")%>"></label> <input maxlength="2" required
			class="ui-textfield form-control" type="number"
			name="max_diferencia_entre_simulados_de_adjuntos"
			id="max_diferencia_entre_simulados_de_adjuntos"
			value='<%=oCONST_DIFERENCIA_MAX_SIMULADOS_POR_ADJUNTO_MES.getValue()%>' />
	</div>
	<div class="form-group">
			
			<label class="control-label">Máxima diferencia entre el mayor y menor número de guardias(LOCALIZADAS y REFUERZOS, -1 no aplica)  entre adjuntos
			<span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.max_diferencia_entre_guardias_localizadas_refuerzos")%>"></label> <input
			maxlength="6" required class="ui-textfield form-control" type="number"  name="max_diferencia_entre_guardias_localizadas_refuerzos" 
			id="max_diferencia_entre_guardias_localizadas_refuerzos"
			value='<%=oMAX_DIF_MAYOR_MENOR_ADJUNTO_GUARDIAS_PRE_LOC_REF.getValue()%>' />
		
	</div>
	<div class="form-group">
			
			<label class="control-label">Máximo valor  entre guardias presencia + refuerzo FESTIVAS (-1 no aplica)  por  adjunto
			<span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.max_total_presencias_refuerzo_festivas")%>"></label> <input
			maxlength="6" required class="ui-textfield form-control" type="number"  name="max_total_presencias_refuerzo_festivas" 
			id="max_total_presencias_refuerzo_festivas"
			value='<%=oMAXIMO_TOTAL_PRESENCIAS_LOCALIZADAS_FESTIVAS.getValue()%>' />
		
	</div>
	<div class="form-group">
		
		<div class="checkbox checkbox-primary">
         	               <input id="calendario" name="calendario" type="checkbox" value="S"
			<%=(oCALENDARIO_GMAIL.getValue().equals("S") ? "checked" : "")%> />
                        <label for="calendario">
                           Publicar y Sincronizar Calendario 	Gmail <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.calendario_gmail")%>"/>
                        </label>
           </div>
	
	</div>
	<!-- <div id="datoscalendario1" class="form-group">
		<label class="control-label">Email From <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.email_from")%>"></label> <input maxlength="250"
			class="ui-textfield form-control" type="text" name="emailfrom"
			id="emailfrom" value='<%=oMAILFROM.getValue()%>' />
	</div>
	<div id="datoscalendario1" class="form-group">
		<label class="control-label">Email From Password <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.email_password")%>"></label> <input
			maxlength="250" class="ui-textfield form-control" type="password"
			name="emailfrompwd" id="emailfrompwd"
			value='<%=oMAILFROM_PWD.getValue()%>' />
	</div>
	<div id="datoscalendario1" class="form-group">
		<label class="control-label">Service Account Google Name
			(oAuth2 autenticación) <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.service_google_account")%>"></label> <input maxlength="250" required
			class="ui-textfield form-control" type="text" name="gserviceaccount"
			id="gserviceaccount" value='<%=oGOOGLE_OAUTH.getValue()%>' />
	</div> -->
	<div id="datoscalendario2" class="form-group">
		<label class="control-label">Enviar Recordatorio Calendario
			(Número de minutos antes de que ocurra,0 no se envían) <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.service_google_account_calendar_reminder")%>"></label> <input
			maxlength="6" required class="ui-textfield form-control"
			type="number" name="minutosrecordatorio" id="minutosrecordatorio"
			value='<%=oGOOGLE_CALENDAR_RECORDATORIO.getValue()%>' />
	</div>
	<div id="datoscalendario3" class="form-group">
		<label class="control-label">Ruta Fichero Clave
			Pública/Privada P12 <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.service_google_account_P12")%>"></label> <input disabled="disabled" maxlength="400"
			class="ui-textfield form-control" type="text" name="ficherop12"
			id="ficherop12"
			value='<%=System.getProperty("catalina.home")%>)\GOOGLE_p12_auth\Guardias-a1592342c053.p12' />
	</div>
	<div class="form-group">
			<div class="checkbox checkbox-primary">
         	               <input id="activar_cambio_guardias" name="activar_cambio_guardias" type="checkbox" value="S"
			<%=(oACTIVAR_CAMBIO_GUARDIAS.getValue().equals("S") ? "checked" : "")%> />
                        <label for="activar_cambio_guardias">
                           Activar Cambio Guardias <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.activar_cambio_guardias")%>">
                        </label>
           </div>
	
	</div>
	
	<div class="form-group">
			<div class="checkbox checkbox-primary">
                        <input id="validar_cambio_guardias" name="validar_cambio_guardias" type="checkbox" value="S"
						<%=(oVALIDAR_CAMBIO_GUARDIAS_BY_ADMIN.getValue().equals("S") ? "checked" : "")%> />
                        <label for="validar_cambio_guardias">
                           Validar Cambio Guardias por el Administrador <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.max_diferencia_entre_simulados_adjuntos")%>">
                        </label>
           </div>
		
	</div>
	
	<div class="form-group">
	
		<div class="checkbox checkbox-primary">
         	               <input id="poolDay" name="poolDay" type="checkbox" value="S"
			<%=(oEXISTE_POOL_DAY.getValue().equals("S") ? "checked" : "")%> />
                        <label for="poolDay">
                          PoolDay (S/N) <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.pool_day")%>">
                        </label>
           </div>
		
	</div>
	<div class="form-group">
	
		<div class="checkbox checkbox-primary">
         	               <input id="usar_secuencia_en_presencia" name="usar_secuencia_en_presencia" type="checkbox" value="S"
			<%=(oUSAR_SECUENCIA_EN_PRESENCIA.getValue().equals("S") ? "checked" : "")%> />
                        <label for="usar_secuencia_en_presencia">
                          Secuencia en Presencia (S/N) <span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.usar_secuencia_en_presencia")%>">
                        </label>
           </div>
		
	</div>
	
	<div class="form-group">
			
			<label class="control-label">Número de guardias PRESENCIA por día
			<span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.numero_guardias_presencia")%>"></label> <input
			maxlength="6" required class="ui-textfield form-control"
			type="number" name="numero_guardias_presencia" id="numero_guardias_presencia"
			value='<%=oNUMERO_GUARDIAS_PRESENCIA.getValue()%>' />
		
	</div>
	
	<div class="form-group">
			
			<label class="control-label">Número de RESIDENTES  por día
			<span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.numero_guardias_residente")%>"></label> <input
			maxlength="6" required class="ui-textfield form-control"
			type="number" name="numero_guardias_residentes" id="numero_guardias_residentes"
			value='<%=oNUMERO_RESIDENTES_POR_DIA.getValue()%>' />
		
	</div>
	
	<div class="form-group">
			
			<label class="control-label">Número de LOCALIZADAS y REFUERZOS  por día
			<span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.numero_guardias_localizadas_y_refuerzos")%>"></label> <input
			maxlength="6" required class="ui-textfield form-control"
			type="number"  name="numero_local_y_refuerzos" id="numero_local_y_refuerzos"
			value='<%=oNUMERO_REFUERZOS_LOCALIZADAS.getValue()%>' />
		
	</div>
	<div class="form-group" >
			<%  
			String [] values = oAJUSTE_A_ESTOS_MESES_VACACIONES.getValue().split("|");
			List<String> Meses = Arrays.asList(values);
                        		
            %>
			<label class="control-label">Forzar en estos meses asignación por nivel de vacaciones
			<span class="glyphicon glyphicon-question-sign" title="<%=RB.getString("config.ajustar_asignacion_residentes")%>"></label>
			<select class="ui-textfield form-control" multiple="multiple" name="meses_ajustar_asignacion_residentes">
			<option  <%=Meses.contains("1") ? "selected" : ""%> value=1>Enero</option>
			<option <%=Meses.contains("2") ? "selected" : ""%> value=2>Febrero</option>
			<option <%=Meses.contains("3") ? "selected" : ""%> value=3>Marzo</option>
			<option <%=Meses.contains("4") ? "selected" : ""%> value=4>Abril</option>
			<option <%=Meses.contains("5") ? "selected" : ""%> value=5>Mayo</option>
			<option <%=Meses.contains("6") ? "selected" : ""%> value=6>Junio</option>
			<option <%=Meses.contains("7") ? "selected" : ""%> value=7>Julio</option>
			<option <%=Meses.contains("8") ? "selected" : ""%> value=8>Agosto</option>
			<option <%=Meses.contains("9") ? "selected" : ""%> value=9>Septiembre</option>
			<option <%=Meses.contains("10") ? "selected" : ""%> value=10>Octubre</option>
			<option <%=Meses.contains("11") ? "selected" : ""%> value=11>Noviembre</option>
			<option <%=Meses.contains("12") ? "selected" : ""%> value=12>Diciembre</option>
			</select> 
			
					
	</div>
	
	

	<input type="hidden" name="saving" value='1' />
	<button type="submit" class="btn btn-block  btn-primary">Guardar</button>
</form>


<%
	if (request.getParameter("saving") != null) {
%>
<script>
	$('#success').show();
</script>
<%
	}
%>
<!-- SUCCESS  -->
<script>
	$(document).ready(function() {
		$('#fconfig').validator();
		$('#fconfig').removeAttr('novalidate');
	});
</script>