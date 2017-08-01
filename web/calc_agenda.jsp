
<%@page import="com.guardias.database.CambiosGuardiasDBImpl"%>
<%@page import="com.guardias.database.GuardiasDBImpl"%>
<%@page import="com.guardias.database.MedicoDBImpl"%>
<%@page import="com.guardias.database.ConfigurationDBImpl"%>

<%@page import="java.util.*"%>

<%@page import="java.util.Calendar"%>
<%@page import="java.text.*"%>
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.cambios.CambiosGuardias"%> 
<%@page import="org.json.*"%>
<%@page import="com.guardias.*"%>
<%@page import="java.util.*"%>
<%@page import="java.util.Map.*"%>


<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>



<%
	/*{â€œEmployeesâ€:[

{
â€œNameâ€:â€ABCâ€,
â€œDesignationâ€:â€Managerâ€,
â€œPayâ€:â€Rs. 60000/-â€œ,
â€œPhoneNumbersâ€:[{
â€œLandLineâ€ : â€œ11-2xxxx99â€,
â€œMobileâ€ : â€œ11xxxxxx11â€
}]
},
*/

//javax.json.JsonObject object = Json.createObjectBuilder().build();


JSONObject allEmps=new JSONObject();
JSONObject empObj=null;

 
 


String _start = request.getParameter("start_calc"); //2016-08-28
String _end = request.getParameter("end_calc");

Date _dINI = new Date();
Date _dFIN = new Date();

DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");

_dINI = _format.parse(_start);
_dFIN = _format.parse(_end);


Calendar _cINI = Calendar.getInstance();
Calendar _cFIN = Calendar.getInstance();

/* SUMAMOS UN MES */
_cINI.setTimeInMillis(_dINI.getTime());
_cINI.set(Calendar.DAY_OF_MONTH, 1);
_cFIN.setTimeInMillis(_dFIN.getTime());
_cFIN.set(Calendar.DAY_OF_MONTH, 1);
_cFIN.add(Calendar.MONTH, 1);

List lItems = new ArrayList();

List lGuardias = new ArrayList();

List<Long> lFestivos = new ArrayList<Long>(); 



Long MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS = Long.parseLong(ConfigurationDBImpl.GetConfiguration(Util.getoCONST_MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS(),MedicoLogged.getServicioId()).getValue());

Long NUMERO_GUARDIAS_PRESENCIA = Long.parseLong(ConfigurationDBImpl.GetConfiguration(Util.getoCONST_NUMERO_PRESENCIAS(),MedicoLogged.getServicioId()).getValue());
Long NUMERO_REFUERZOS_LOCALIZADAS = Long.parseLong(ConfigurationDBImpl.GetConfiguration(Util.getoCONST_NUMERO_REFUERZOS_LOCALIZADAS(),MedicoLogged.getServicioId()).getValue());
Long NUMERO_RESIDENTES_POR_DIA = Long.parseLong(ConfigurationDBImpl.GetConfiguration(Util.getoCONST_NUMERO_RESIDENTES(),MedicoLogged.getServicioId()).getValue());

String PRESENCIA_EN_SECUENCIA= ConfigurationDBImpl.GetConfiguration(Util.getoCONST_USAR_SECUENCIA_EN_PRESENCIA(),MedicoLogged.getServicioId()).getValue();


boolean _bMES_VACACIONAL =  Util.EsMesVacaciones(_cINI,MedicoLogged.getServicioId()); // QUITAMOS O NO LOS ALEATORIOS 



/* INCLUYENDO SIMULADOS Y ACTIVOS*/
Long  _NUM_TOTAL_RESIDENTES =new Long(0);
Long  _NUM_TOTAL_GUARDIAS_CUBIERTAS_RESIDENTES=new Long(0);  // numero de simulados o guardias sin residentes-
Long  _NUM_SIMULADOS_POR_SEMANA =new Long(0);

Long  _NUM_RESIDENTES_ASIGNADOS_PREVIOS_A_ADJUNTOS =new Long(0);  // CUANTOS ADJUNTOS REQUIEREN UN RESIDENTE SI O SI 

/* 
	[1] --> [DIA] 
	[PRESENCIA] --> IDMEDICO			
	[REFUERZO] --> IDMEDICO O NO
	[LOCALIZADO] --> IDMEDICO	 O NO	
	


/*<xml version='1.0'/>
<medicos>
	<m nombre="M0" tipo="G">
		<vacaciones>
	<v>01-01-2016 / 01-01-2017</v>
		</vacaciones>
	</m>
*/




ProcesarMedicos oUtilMedicos = new ProcesarMedicos();

lItems = MedicoDBImpl.getMedicos(new Long(-1),MedicoLogged.getServicioId());



int _daysOfMonth = com.guardias.Util.daysDiff(_cINI.getTime(),_cFIN.getTime());

int _Index;

boolean  bEncontrado = false;
boolean _EsFestivo = false; 	

String _Festivo = ""; // o B

long _TOTAL_FESTIVOS_MES=0;

HashMap<Long, Hashtable> lMedicosGuardias  = new HashMap<Long, Hashtable>();
List<Medico> _lResidentes  = new ArrayList<Medico>();
List<Medico> _lAdjuntos  = new ArrayList<Medico>();


Calendar cLASTDAYMONTH= Calendar.getInstance();
cLASTDAYMONTH.setTimeInMillis(_cFIN.getTimeInMillis());
cLASTDAYMONTH.add(Calendar.DATE, -1);


_lAdjuntos  =ProcesarMedicos.getAdjuntos(lItems, true);
_lResidentes  =MedicoDBImpl.getMedicosByTypeOrdenBySimulados(Util.eTipo.RESIDENTE,MedicoLogged.getServicioId());
_lResidentes  =ProcesarMedicos.getResidentesOrdenadosVacacionesDESC(_lResidentes, _format.format(_cINI.getTime()), _format.format(cLASTDAYMONTH.getTime()));



/* LO PRIMERO ASIGNAR LOS ADJUNTOS POR ORDEN DE APARICION O SECUENCIA PARA LAS PRESENCIAS 


	[_TIPO] --> ADJUNTO	
	[_TOTAL_GUARDIAS_MES]
	[_TOTAL_FESTIVOS_MES]		
	[_TOTAL_PRESENCIA_MES]
	[_TOTAL_REFUERZO_MES]
	[_TOTAL_LOCALIZADA_MES]					
	[_TOTAL_PRESENCIA_FESTIVOS_MES]
	[_TOTAL_LOCALIZADA_FESTIVOS_MES]
	[_TOTAL_REFUERZO_FESTIVOS_MES]							
	[_TOTAL_SIMULADO_MES]		
	[_TOTAL_SIMULADO_FESTIVOS_MES]
	[0]  --> [PRESENCIA, LOCALIZADA, REFUERZO]
	[1]  --> DIA GUARDIA
	[2]	 --> DIA GUARDIA	

*/

Calendar _cINICIO = Calendar.getInstance();
Calendar _cFINAL = Calendar.getInstance();

_cINICIO.setTimeInMillis(_cINI.getTimeInMillis());
_cFINAL.setTimeInMillis(_cFIN.getTimeInMillis());


/* 20170201 ACUMULAMOS LO DEL AÑO EN CURSO PARA VER SI CUADRAN MEJOR LAS CIFRAS EQUITATIVAS>*/
/* OJO, QUITAMOS LOS DEL MES EN CURSO, DE PRINCIPIOS DE AÑO HASTA EL MES  ANTERIOR EN CURSO  
FECHA INICIO --> SIEMPRE DIA 1, MES 1, AÑO EN CURSO
FECHA FIN  --> RESTAMOS UN DIA (<=30, 31)*/
Calendar _cANYO_INICIO= Calendar.getInstance();

_cANYO_INICIO.set(_cINI.get(Calendar.YEAR), 0, 1);
String _ANYO_INICIO =  _format.format(_cANYO_INICIO.getTime());				
_cANYO_INICIO.setTimeInMillis(_cINI.getTimeInMillis());
// RESTAMOS UN DIA 
_cANYO_INICIO.add(Calendar.DATE, -1);
String _ANYO_HASTA =  _format.format(_cANYO_INICIO.getTime());

/* MEDIA DE CONTADORES POR TIPO ACUMULADOS DESDE EL AÑO, EN CASO DE QUE ALGUN MEDICO SE INCORPORE. SE ASIGNARA ESTA MEDIA */
/* 1. ADJUNTOS */
int MEDIA_TOTAL_PRESENCIA = GuardiasDBImpl.getMediaTotalGuardiasTipoEntreFechas(Util.eTipo.ADJUNTO,Util.eTipoGuardia.PRESENCIA.toString(),new Long(0),_ANYO_INICIO,_ANYO_HASTA, MedicoLogged.getServicioId());	
int MEDIA_TOTAL_LOCALIZADA = GuardiasDBImpl.getMediaTotalGuardiasTipoEntreFechas(Util.eTipo.ADJUNTO,Util.eTipoGuardia.LOCALIZADA.toString(),new Long(0),_ANYO_INICIO,_ANYO_HASTA, MedicoLogged.getServicioId());
int MEDIA_TOTAL_REFUERZO = GuardiasDBImpl.getMediaTotalGuardiasTipoEntreFechas(Util.eTipo.ADJUNTO,Util.eTipoGuardia.REFUERZO.toString(),new Long(0),_ANYO_INICIO,_ANYO_HASTA, MedicoLogged.getServicioId());						
int MEDIA_TOTAL_PRESENCIA_FESTIVO = GuardiasDBImpl.getMediaTotalGuardiasTipoEntreFechas(Util.eTipo.ADJUNTO,Util.eTipoGuardia.PRESENCIA.toString(),new Long(1),_ANYO_INICIO,_ANYO_HASTA, MedicoLogged.getServicioId());
int MEDIA_TOTAL_LOCALIZADA_FESTIVO = GuardiasDBImpl.getMediaTotalGuardiasTipoEntreFechas(Util.eTipo.ADJUNTO,Util.eTipoGuardia.LOCALIZADA.toString(),new Long(1),_ANYO_INICIO,_ANYO_HASTA, MedicoLogged.getServicioId());	
int MEDIA_TOTAL_REFUERZO_FESTIVO = GuardiasDBImpl.getMediaTotalGuardiasTipoEntreFechas(Util.eTipo.ADJUNTO,Util.eTipoGuardia.REFUERZO.toString(),new Long(1),_ANYO_INICIO,_ANYO_HASTA, MedicoLogged.getServicioId());
/* 2. RESIDENTES  */
int MEDIA_TOTAL_DIARIA = GuardiasDBImpl.getMediaTotalGuardiasTipoEntreFechas(Util.eTipo.RESIDENTE,"",new Long(0),_ANYO_INICIO,_ANYO_HASTA, MedicoLogged.getServicioId());	
int MEDIA_TOTAL_DIARIA_FESTIVA = GuardiasDBImpl.getMediaTotalGuardiasTipoEntreFechas(Util.eTipo.RESIDENTE,"",new Long(1),_ANYO_INICIO,_ANYO_HASTA, MedicoLogged.getServicioId());
/* MEDIA DE CONTADORES POR TIPO ACUMULADOS DESDE EL AÑO, EN CASO DE QUE ALGUN MEDICO SE INCORPORE. SE ASIGNARA ESTA MEDIA */





List<Medico> lAdjuntosVacacionesDESC =ProcesarMedicos.getAdjuntosOrdenadosVacacionesDESC(_lAdjuntos, _format.format(_cINI.getTime()), _format.format(cLASTDAYMONTH.getTime()));


/* NUMERO DE GUARDIAS POR 30 DIAS DE MEDIA, OJO AL TRUNCATE  */
int MEDIA_GUARDIAS_VACACIONAL_X_ADJUNTO_NORMAL = (NUMERO_GUARDIAS_PRESENCIA.intValue() +  NUMERO_REFUERZOS_LOCALIZADAS.intValue()) * 30 / lAdjuntosVacacionesDESC.size();
/* 30 DIAS POR ADJUNTOS , DISPONIBILIDAD DE 300 DIAS */
int DIAS_TOTALES_DISPONIBLE_MES_NORMAL = lAdjuntosVacacionesDESC.size() * 30;
/* AJUSTE REAL DE GUARDIAS A  REALIZAR */

int DIAS_TOTALES_DISPONIBLE_MES_CURSO=0;

//int GUARDIAS_VACACIONAL_X_ADJUNTO = NUMERO_GUARDIAS_PRESENCIA.intValue() *  NUMERO_REFUERZOS_LOCALIZADAS.intValue() * 30 / lAdjuntosVacacionesDESC.size();

int  _NUM_TOTAL_VACACIONES_ADJUNTOS=0;


//Long NUMERO_RESIDENTES_POR_DIA = Long.parseLong(ConfigurationDBImpl.GetConfiguration(Util.getoCONST_NUMERO_RESIDENTES()).getValue());




/* INICIALIZAMOS CONTADORES */
for (Medico oM :_lAdjuntos)
{
	
	Hashtable _lDatosGuardiasMedico=null;
	
	_lDatosGuardiasMedico  = ProcesarMedicos.InitContadoresMedico(oM, _ANYO_INICIO, _ANYO_HASTA,
	MEDIA_TOTAL_PRESENCIA, MEDIA_TOTAL_LOCALIZADA, MEDIA_TOTAL_REFUERZO, MEDIA_TOTAL_PRESENCIA_FESTIVO,
	MEDIA_TOTAL_LOCALIZADA_FESTIVO, MEDIA_TOTAL_REFUERZO_FESTIVO, MEDIA_TOTAL_DIARIA,MEDIA_TOTAL_DIARIA_FESTIVA, _cINI, MedicoLogged.getServicioId());

	lMedicosGuardias.put(oM.getID(),_lDatosGuardiasMedico);
	
	Calendar cVacacionesFIN = Calendar.getInstance();
	
	cVacacionesFIN.setTimeInMillis(_cFIN.getTimeInMillis());
	cVacacionesFIN.add(Calendar.DATE, -1);
	
	_NUM_TOTAL_VACACIONES_ADJUNTOS += ProcesarMedicos.getNumeroVacacionesMedico(oM,_cINI, cVacacionesFIN); // ULTIMO DIA DEL MES  
	
}

/* SUMATORIO DIAS DE CADA ADJUNTO MENOS LAS VACACIONES */ 
DIAS_TOTALES_DISPONIBLE_MES_CURSO = DIAS_TOTALES_DISPONIBLE_MES_NORMAL-_NUM_TOTAL_VACACIONES_ADJUNTOS;
/* 1. ASIGNACION DE PRESENCIAS EN ORDEN */

float _FACTOR_GUARDIAS_CORRECCION =   DIAS_TOTALES_DISPONIBLE_MES_CURSO  / (float) DIAS_TOTALES_DISPONIBLE_MES_NORMAL  ;

float  _OBJETIVO_MES_GUARDIAS_ADJUNTOS = MEDIA_GUARDIAS_VACACIONAL_X_ADJUNTO_NORMAL * (1 + _FACTOR_GUARDIAS_CORRECCION);
/* SACAMOS LOS OBJETIVOS EN BASE A LA DISPONIBILIDAD DE LOS RESIDENTES */
float  _REFUERZOS_MES_NORMAL_X_ADJUNTO = ProcesarMedicos.getDisponibilidadRefuerzo(_lResidentes) / (float)_lResidentes.size();
float _LOCALIZADAS_MES_NORMAL_X_ADJUNTO  = ProcesarMedicos.getDisponibilidadLocalizada(_lResidentes) / (float) _lResidentes.size();

 


 
int  _OBJETIVO_MES_ADJUNTOS_LOCALIZADAS = Math.round(_LOCALIZADAS_MES_NORMAL_X_ADJUNTO * (1 + _FACTOR_GUARDIAS_CORRECCION)/2);

int  _OBJETIVO_MES_ADJUNTOS_REFUERZO =   Math.round(_REFUERZOS_MES_NORMAL_X_ADJUNTO * (1 + _FACTOR_GUARDIAS_CORRECCION)/2);
int  _OBJETIVO_MES_ADJUNTOS_PRESENCIAS =  Math.round((_OBJETIVO_MES_GUARDIAS_ADJUNTOS - (_OBJETIVO_MES_ADJUNTOS_LOCALIZADAS + _OBJETIVO_MES_ADJUNTOS_REFUERZO))/2);

/* EL OBJETIVO DE PRESENCIAS TIENE QUE SER EL 50 DEL OBJETIVO CALCULADO PUESTO QUE LA DISPONIBLIDAD LA ESTA CONSIDERANDO ENTRE LAS TRES TIPOS DE GUARDIAS,
ENNTONCES, UNA PRESENCIA NO ES COMPATIBLE CON UN REFUERZO O LOCALIZADA, POR TANTO, DIVIDIMOS AL 50% PARA DISTRIBUIRLO */


 
/*int  _OBJETIVO_MES_ADJUNTOS_LOCALIZADAS = (int) (_LOCALIZADAS_MES_NORMAL_X_ADJUNTO * (1 + _FACTOR_GUARDIAS_CORRECCION));
int  _OBJETIVO_MES_ADJUNTOS_REFUERZO =   (int) (_REFUERZOS_MES_NORMAL_X_ADJUNTO * (1 + _FACTOR_GUARDIAS_CORRECCION));
int  _OBJETIVO_MES_ADJUNTOS_PRESENCIAS =  (int) (_OBJETIVO_MES_GUARDIAS_ADJUNTOS - (_OBJETIVO_MES_ADJUNTOS_LOCALIZADAS + _OBJETIVO_MES_ADJUNTOS_REFUERZO));
*/

// la usamos aqui para tener control sobre la listagenerada de la clase
ProcesarMedicos UtilMedicos = new ProcesarMedicos();
UtilMedicos.setlGeneradaSecuencia(new ArrayList<Long>());



for (int j=1;j<=_daysOfMonth;j++)	
{
	
	
_EsFestivo = Boolean.parseBoolean(request.getParameter("festivo" + j));

/* si es festivo y no lo encuentra en la lista de festivos*/
if (_EsFestivo && !lFestivos.contains(new Long(j)))
	lFestivos.add(new Long(j));
	
	 
	/* CUANTAS PRESENCIAS HAY QUE RELLENAR */
	for (int countPresencias=0;countPresencias<NUMERO_GUARDIAS_PRESENCIA;countPresencias++)
	{ 
		
		_EsFestivo = Boolean.parseBoolean(request.getParameter("festivo" + j));
		
		if (PRESENCIA_EN_SECUENCIA.equalsIgnoreCase("S"))
	lMedicosGuardias = UtilMedicos.setGuardiaPresenciaSecuencia(lMedicosGuardias, _lAdjuntos,_cINICIO.getTime(), _EsFestivo);
		else
	lMedicosGuardias = ProcesarMedicos.setGuardiaPresenciaAleatoria(lMedicosGuardias, lAdjuntosVacacionesDESC,_cINICIO.getTime(), _EsFestivo,_daysOfMonth, _OBJETIVO_MES_ADJUNTOS_PRESENCIAS, MedicoLogged.getServicioId());

			//ProcesarMedicos.setlGeneradaSecuencia(new ArrayList(Long));

			// si no ha encontrado a nadie mas, vaciamos la lista de control de asignaciones

		} // fin de cuantas presencias hay que generar

		//if (bEncontrado)	
		_cINICIO.add(Calendar.DAY_OF_MONTH, 1);

	}

	ArrayList<Long> _lPoolDAYS = new ArrayList();
	/* VERIFICAMOS QUE ESTÉ EL POOLDAY RELLENO PARA ASIGNARLO AL R1 */

	System.out.println("Verificamos el poool...");

	for (int j = 1; j <= _daysOfMonth; j++) {
		if (request.getParameter("poolday" + j) != null && !request.getParameter("poolday" + j).equals(""))
			_lPoolDAYS.add(new Long(j));
		_EsFestivo = Boolean.parseBoolean(request.getParameter("festivo" + j));
		if (_EsFestivo) {
			_TOTAL_FESTIVOS_MES++;
		}

	}

	/* DATOS PARA CALCULAR LA DISTRIBUCION */

	/* 1. ASIGNAR POOL, EL RESTO PUEDEN IR EN SECUENCIA O NO.
	2. OBTENER NUMERO DE DIAS LABORALES
	3. OBTENER NUMERO DE DIAS FESTIVOS
	4. RELLENAR SEGUN CRITERIO 
	*/

	/* LISTA DE CONTROL RESIDENTES
	
		[1] --> IDMEDICO
		
		    [_TIPO] --> RESIDENTE
		    [_SUBTIPO] --> R1....R2....SIMULADO...ROTANTE
			[_NUMERO_GUARDIAS_RESIDENTE_MES]
			[_TOTAL_GUARDIAS_RESIDENTE_FESTIVO]				
			[0]  --> DIA GUARDIA    o POOL DAY
			[1]  --> DIA GUARDIA    o POOL DAY
			[2]	 --> DIA GUARDIA	o POOL DAY
					
	/* LISTA DE CONTROL ADJUNTOS 
	
		[5] --> IDMEDICO
	
				[_TIPO] --> ADJUNTO	
				[_TOTAL_GUARDIAS_MES]
				[_TOTAL_FESTIVOS_MES]		
				[_TOTAL_PRESENCIA_MES]
				[_TOTAL_REFUERZO_MES]
				[_TOTAL_LOCALIZADA_MES]					
				[_TOTAL_PRESENCIA_FESTIVOS_MES]
				[_TOTAL_LOCALIZADA_FESTIVOS_MES]
				[_TOTAL_REFUERZO_FESTIVOS_MES]							
				[_TOTAL_SIMULADO_DIARIO_MES]		
				[_TOTAL_SIMULADO_FESTIVOS_MES]
				[0]  --> [PRESENCIA, LOCALIZADA, REFUERZO]
				[1]  --> [PRESENCIA, LOCALIZADA, REFUERZO]
				[2]	 --> [PRESENCIA, LOCALIZADA, REFUERZO]	
						
						
		*/

	//_NUM_TOTAL_RESIDENTES = ProcesarMedicos.getNumeroResidentes(lItems);			

	//_lResidentes  =ProcesarMedicos.getResidentes(lItems);

	//_format.format(_cINI.getTime())

	//_lResidentes  =ProcesarMedicos.getResidentes(lItems);
	//_lResidentes  =ProcesarMedicos.getResidentesOrdenadosVacacionesDESC(lItems, _format.format(_cINI.getTime()), _format.format(cLASTDAYMONTH.getTime()));

	//_format.format(_cANYO_INICIO.getTime());

	_NUM_TOTAL_RESIDENTES = new Long(_lResidentes.size());

	// no contemplamos vacaciones etc...es una aproximacion
	_NUM_TOTAL_GUARDIAS_CUBIERTAS_RESIDENTES = ProcesarMedicos.getGuardiasSinCubrir(Util.eTipo.RESIDENTE,
			_lResidentes, _cINI, _cFIN); // SIMULADOS

	_cINI.setFirstDayOfWeek(Calendar.MONDAY); //sets first day to monday, for example.
	_cINI.setMinimalDaysInFirstWeek(1);
	int NUM_SEMANAS_MES = _cINI.getActualMaximum(Calendar.WEEK_OF_MONTH);

	/* TOTAL SIMULADOS / TOTAL SEMANAS */
	/* OJO, QUE LA SEMANA ULTIMA PUEDE CONTENER UN UNICO DIA */
	//_NUM_SIMULADOS_POR_SEMANA = _NUM_TOTAL_GUARDIAS_SIN_CUBRIR_RESIDENTES / NUM_SEMANAS_MES;
	/* RESTO MAYOR DE 0 , SUMAMOS UNO MAS PARA PODER ASIGNARLOS , MAS FLEXIBLES */

	/* INICIALIZAMOS LOS VALORES  DE RESIDENTES */
	for (int j = 0; j < _lResidentes.size(); j++) {

		Medico oMedicoINIT = (Medico) _lResidentes.get(j);
		if (!lMedicosGuardias.containsKey(oMedicoINIT.getID())) {
			// la generamos

			Hashtable _lDatosGuardiasResidente = new Hashtable();
			lMedicosGuardias.put(oMedicoINIT.getID(), _lDatosGuardiasResidente);
			_lDatosGuardiasResidente.put("_TIPO", Util.eTipo.RESIDENTE);
			_lDatosGuardiasResidente.put("_SUBTIPO", oMedicoINIT.getSubTipoResidente());
			_lDatosGuardiasResidente.put("_NUMERO_GUARDIAS_" + Util.eTipo.RESIDENTE + "_MES", 0);
			_lDatosGuardiasResidente.put("_TOTAL_GUARDIAS_" + Util.eTipo.RESIDENTE + "_FESTIVO", 0);

			/* ACUMULAMOS EL HISTORICO DEL AÑO  LA PRIMERA VEZ */
			int TOTAL_DIARIAS = GuardiasDBImpl.getTotalGuardiasPorMedicoTipoEntreFechas(oMedicoINIT.getID(), "",
					new Long(0), _ANYO_INICIO, _ANYO_HASTA, _cINI, MedicoLogged.getServicioId());
			if (TOTAL_DIARIAS == 0)
				TOTAL_DIARIAS = MEDIA_TOTAL_DIARIA;
			int TOTAL_DIARIAS_FESTIVO = GuardiasDBImpl.getTotalGuardiasPorMedicoTipoEntreFechas(
					oMedicoINIT.getID(), "", new Long(1), _ANYO_INICIO, _ANYO_HASTA, _cINI,
					MedicoLogged.getServicioId());
			if (TOTAL_DIARIAS_FESTIVO == 0)
				TOTAL_DIARIAS_FESTIVO = MEDIA_TOTAL_DIARIA_FESTIVA;

			_lDatosGuardiasResidente.put("HISTORICO_NUMERO_GUARDIAS_" + Util.eTipo.RESIDENTE + "_MES",
					TOTAL_DIARIAS);
			_lDatosGuardiasResidente.put("HISTORICO_TOTAL_GUARDIAS_" + Util.eTipo.RESIDENTE + "_FESTIVO",
					TOTAL_DIARIAS_FESTIVO);

		}

	}

	/* AGREGAMOS LOS R1  A LOS DIAS POOOL */
	System.out.println("Agregamos el poool...");

	List<Medico> _lResidentesPOOLRandom = new ArrayList<Medico>(_lResidentes);

	Collections.shuffle(_lResidentesPOOLRandom);

	for (int x = 0; x < _lPoolDAYS.size(); x++) // recorremos los pool dates
	{

		Long _PoolDay = _lPoolDAYS.get(x);

		/* for (int poolcount=0;poolcount<2;poolcount++) // recorremos médicos residentes
		{*/

		for (int k = 0; k < _lResidentesPOOLRandom.size(); k++) // recorremos médicos residentes
		{

			Medico oM = (Medico) _lResidentesPOOLRandom.get(k);

			// R1 y residentes 
			if (oM.getTipo().equals(Util.eTipo.RESIDENTE)
					&& oM.getSubTipoResidente().equals(Util.eSubtipoResidente.R1)) {

				// existe el medico residente en su lista de control
				Hashtable _lDatosGuardiasMedico = null;
				// la obtenemos la que haya 
				_lDatosGuardiasMedico = (Hashtable) lMedicosGuardias.get(oM.getID());

				/* PONEMOS EL DIA */
				_lDatosGuardiasMedico.put(_PoolDay, "POOLDAY"); // redundate, dia key , dia value

				/* if  (!_lDatosGuardiasMedico.containsKey("_NUMERO_GUARDIAS_RESIDENTE_MES"))
					_lDatosGuardiasMedico.put("_NUMERO_GUARDIAS_RESIDENTE_MES",1);  // la primera
				else*/ // SI NO, SUMAMOS UNO			

				/* SI ES FESTIVO, SUMAMOS EL FESTIVO TAMBIEN */
				_EsFestivo = Boolean.parseBoolean(request.getParameter("festivo" + _PoolDay.longValue()));
				if (_EsFestivo) {
					/*if  (!_lDatosGuardiasMedico.containsKey("_TOTAL_GUARDIAS_RESIDENTE_FESTIVO"))
						_lDatosGuardiasMedico.put("_TOTAL_GUARDIAS_RESIDENTE_FESTIVO",1);  // la primera
					else  */// SI NO, SUMAMOS UNO
					_lDatosGuardiasMedico.put("_TOTAL_GUARDIAS_" + Util.eTipo.RESIDENTE + "_FESTIVO",
							Integer.valueOf(_lDatosGuardiasMedico
									.get("_TOTAL_GUARDIAS_" + Util.eTipo.RESIDENTE + "_FESTIVO").toString())
									+ 1);
				} else // guardias diarias 03-01-2017
					_lDatosGuardiasMedico.put("_NUMERO_GUARDIAS_" + Util.eTipo.RESIDENTE + "_MES",
							Integer.valueOf(_lDatosGuardiasMedico
									.get("_NUMERO_GUARDIAS_" + Util.eTipo.RESIDENTE + "_MES").toString()) + 1);
				//_lDatosGuardiasMedico.replace(oM.getID(), _lDatosGuardiasMedico);

				//System.out.println("Agregado al pool : " + _PoolDay + ", médico:" + oM.getID() + ",");

				break;
			}

		}
		// fin de total de pooles definidos

	} // fin de dias poool

	/* PROCESO PARA RESIDENTES CONTROLANDO LOS QUE TENGA EL CHECK DE QUE NO PUEDEN ESTAR SOLOS (ADJUNTOS) COMO GUARDIA Y POR TANTO, DEBEN TENER UN RESIDENTE SI O SI 
	SI NO SE HACE AQUI TE ARRIESGAS A TENERLO EL ULTIMO DIA DEL MES Y ASIGNARLO SIN RESIDENTE 
	*/

	Calendar cGuardiaDia = Calendar.getInstance();
	cGuardiaDia.setTimeInMillis(_cINI.getTimeInMillis());

	System.out.println("Relleno de los médicos que tienen que tener un residente...");

	/* ENTRAMOS SI SOLO HAY UNA PRESENCIA Y UN RESIDENTE POR DIA, EN OTRO CASO , NO SE ENTRA */

	if (NUMERO_GUARDIAS_PRESENCIA.intValue() == 1 && NUMERO_RESIDENTES_POR_DIA.intValue() == 1
			&& ProcesarMedicos.ExisteMedicoConGuardiaSolo_EnElMes(lMedicosGuardias, _lAdjuntos, new Long(-1))) {

		int value = 0;
		for (int j = 1; j <= _daysOfMonth; j++) {

			boolean bExisteMedicoConGuardiaSolo = ProcesarMedicos
					.ExisteMedicoConGuardiaSolo_EnElMes(lMedicosGuardias, _lAdjuntos, new Long(j));

			if (bExisteMedicoConGuardiaSolo && !_lPoolDAYS.contains(new Long(j))) // hay un medico adjunto que no puede estar solo en el dia y ya no hay residente pool asignado
			{

				List<Medico> _lResidentesRANDOM = new ArrayList<Medico>(_lResidentes);

				bEncontrado = false;
				Medico oM = null;
				Medico _oAdjunto = null;
				boolean IgnorarMenorNumeroGuardias = false; // cuando el que menos ha hecho guardias le toque esta asignacion (p.e. festivo)  pero ha trabajado el dia anterior

				while (!bEncontrado) {

					oM = (Medico) _lResidentesRANDOM.get(value);

					String _DATE = _format.format(cGuardiaDia.getTime());
					boolean NoTieneVacaciones = oUtilMedicos.NoTieneVacaciones(oM, _DATE);
					boolean ExcedeLimiteGuardiasMes = false;
					boolean ExcedeHorasSeguidas = false;
					boolean EsResidenteConMenosFestivos = false;
					boolean TienePoolDayAsignadoDiaSgte = false;
					boolean SimuladosEquitativosConAdjunto = false; // repartimos de manera proporcional entre la semana los simulados y equitativo entre los adjuntos
					int _GuardiasPreviasSeguidas = 0;

					_EsFestivo = Boolean.parseBoolean(request.getParameter("festivo" + j));

					Hashtable _lDatosTemp;

					if (lMedicosGuardias.containsKey(oM.getID())) // existe el medico con guardias
					{

						_lDatosTemp = (Hashtable) lMedicosGuardias.get(oM.getID());

						Long _total = Long.parseLong(
								_lDatosTemp.get("_NUMERO_GUARDIAS_" + Util.eTipo.RESIDENTE + "_MES").toString())
								+ Long.parseLong(
										_lDatosTemp.get("_TOTAL_GUARDIAS_" + Util.eTipo.RESIDENTE + "_FESTIVO")
												.toString());
						//System.out.println("Médico: " + oM.getID() + ",guardiasmes:" + _total);
						// lleva más guardias calculadas que total tiene predefinidas
						if (_total.intValue() >= oM.getMax_NUM_Guardias().intValue()) {
							ExcedeLimiteGuardiasMes = true;
						}
						// verificamos dias anteriores
						_GuardiasPreviasSeguidas = ProcesarMedicos.getDiasPreviosConGuardiasSeguidos(j,
								_lDatosTemp, _daysOfMonth);
						if (_GuardiasPreviasSeguidas >= MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS.intValue()) {
							ExcedeHorasSeguidas = true;
						}
						/* TIENE EL MEDICO ASIGNADO EL POOL EL DIA J+1*/
						if (_lDatosTemp.containsKey(new Long(j + 1))
								&& _lDatosTemp.get(new Long(j + 1)).equals("POOLDAY")) {
							TienePoolDayAsignadoDiaSgte = true;
						}

						_EsFestivo = Boolean.parseBoolean(request.getParameter("festivo" + j));

						EsResidenteConMenosFestivos = true;
						Long ResidenteConMenosFestivos = new Long(-1);
						// IgnorarMenorNumeroGuardias = false;
						if (_EsFestivo) {
							//	ResidenteConMenosFestivos  = ProcesarMedicos.ResidenteMenosFestivosyNoExcedeDelTotal(lMedicosGuardias, _lResidentes, _DATE,j);
							/* PUEDE DARSE QUE EL RESIDENTE CON MENOS FESTIVOS HAYA TRABAJADO EL VIERNES, CON LO QUE NO SE LE PUEDE ASIGNAR EL SABADO Y ENTRA EN BUCLE */
							ResidenteConMenosFestivos = ProcesarMedicos.ResidenteMenosGuardiasyNoExcedeDelTotal(
									lMedicosGuardias, _lResidentes, _DATE, j, _daysOfMonth, !_bMES_VACACIONAL);
							EsResidenteConMenosFestivos = oM.getID().equals(ResidenteConMenosFestivos);
							if (EsResidenteConMenosFestivos)
								IgnorarMenorNumeroGuardias = true;
							/* else 
									IgnorarMenorNumeroGuardias =false; */

						}
						if (!oM.isResidenteSimulado() && NoTieneVacaciones && !ExcedeLimiteGuardiasMes
								&& !ExcedeHorasSeguidas && !TienePoolDayAsignadoDiaSgte
								&& (!_EsFestivo || (_EsFestivo && (IgnorarMenorNumeroGuardias
										|| (!IgnorarMenorNumeroGuardias && EsResidenteConMenosFestivos)))))
							bEncontrado = true;

					} // fin 	if (lMedicosGuardias.containsKey(oM.getID())) // existe el medico con guardias
					else if (!oM.isResidenteSimulado() && NoTieneVacaciones && !TienePoolDayAsignadoDiaSgte)
						bEncontrado = true;

					/* SI YA HAN SIDO VERIFICADOS TODOS, LE ASIGNO AL ADJUNTO EL PRIMER RESIDENTE QUE NO SEA SIMULADO Y CUMPLA LAS CONDICIONES */

					/* _lResidentesRANDOM , YA SOLO HAY UN RESIDENTE Y UN SIMULADO */
					if (_lResidentesRANDOM.size() == 2 && !oM.isResidenteSimulado() && NoTieneVacaciones
							&& !TienePoolDayAsignadoDiaSgte)
						bEncontrado = true;

					if (!bEncontrado)
						_lResidentesRANDOM.remove(oM);

				} // end while 

				Hashtable _lDatosGuardiasMedico = null;

				// la obtenemos la que haya 
				_lDatosGuardiasMedico = (Hashtable) lMedicosGuardias.get(oM.getID());

				/* PONEMOS EL DIA */
				_lDatosGuardiasMedico.put(new Long(j), "NOPOOLDAY"); // redundate, dia key , dia value
				_EsFestivo = Boolean.parseBoolean(request.getParameter("festivo" + j));
				if (_EsFestivo) {
					_lDatosGuardiasMedico.put("_TOTAL_GUARDIAS_RESIDENTE_FESTIVO",
							Integer.valueOf(
									_lDatosGuardiasMedico.get("_TOTAL_GUARDIAS_RESIDENTE_FESTIVO").toString())
									+ 1);
				} else /* 03-01-2017 */
					_lDatosGuardiasMedico.put("_NUMERO_GUARDIAS_" + Util.eTipo.RESIDENTE + "_MES",
							Integer.valueOf(_lDatosGuardiasMedico
									.get("_NUMERO_GUARDIAS_" + Util.eTipo.RESIDENTE + "_MES").toString()) + 1);

				_NUM_RESIDENTES_ASIGNADOS_PREVIOS_A_ADJUNTOS++; // un adjunto ya tiene preasingado el residente antes que nadie 

			}

			cGuardiaDia.add(Calendar.DATE, 1);
		}
	}
	System.out.println("Fin relleno de los médicos que tienen que tener un residente...");

	/*NO RELLENAR EN SECUENCIA PUESTO QUE SI HAY FALTA DE HUECOS PARA RELLENAR , DEBEN SER ADJUDICADOS POR SIMULADOS Y NO DEBEN QUEDAR TODOS AL FINAL DEL MES*/
	/* PROCESO PARA RESIDENES  
	NO RELLENAR EN SECUENCIA PUESTO QUE SI HAY FALTA DE HUECOS PARA RELLENAR , DEBEN SER ADJUDICADOS POR SIMULADOS Y NO DEBEN QUEDAR TODOS AL FINAL DEL MES
	*/

	System.out.println("Starting proceso para residentes..");

	Calendar cWeek = Calendar.getInstance();
	cGuardiaDia.setTimeInMillis(_cINI.getTimeInMillis());
	/* 1. RECORRER LOS FESTIVOS PRIMERO DE LA SEMANA 
	2. DESPUES LOS DIARIOS PRIMERO DE LA SEMANA
	3. ORDENARIA LA LISTA lDiasSemanaAleatorio POR FESTIVOS Y DESPUES DIARIOS PERO ALEATORIOS 
	
	
	1. ES FESTIVO (ordenados por festivos) 
	
		--> bFilledFestivosPorResidentes:false;
		--> Faltan _RESIDENTES_SEMANA
		--> No sobrepasan el total semana / mes 
		--> MAS TODAS LAS VALIDACIONES INDIVIDUALES DEL MEDICO
		
	2. 	ES DIARIO
		bFilledFestivosPorResidentes:true; --> si es un residente 
		Faltan _RESIDENTES_SEMANA
		No sobrepasan el total semana / mes
		
		
	HINT --> ORDENAR RESIDENTES POR RESIDENTES Y ULTIMO SIMULADOS 	
	*/
	/* recorremos semanas mes */
	/* TOTAL SIMULADOS / TOTAL SEMANAS */
	/* OJO, QUE LA SEMANA ULTIMA PUEDE CONTENER UN UNICO DIA */

	/* RESTO MAYOR DE 0 , SUMAMOS UNO MAS PARA PODER ASIGNARLOS , MAS FLEXIBLES */

	/*if (NUM_SEMANAS_MES==6)	// numero semanas mes, caso enero 2017  
	{
		_RESIDENTES_SEMANA+=1;
	}
	*/

	//System.out.println (_RESIDENTES_SEMANA);

	cWeek.setTimeInMillis(_cINI.getTimeInMillis());
	cGuardiaDia.setTimeInMillis(_cINI.getTimeInMillis());

	int MONTH_ACTIVE = cGuardiaDia.get(Calendar.MONTH);

	/* CAMBIAMOS NUMERO RESIDENTES POR SEMANA QUE SEA VARIABLE 20170103 */
	/* PORCENTAJE DE GUARDIAS CUBIERTAS SOBRE EL MES */
	//float _PERCENT_RESIDENTES_GUARDIAS_MES =_NUM_TOTAL_GUARDIAS_CUBIERTAS_RESIDENTES.intValue() / _daysOfMonth; //  
	/* if (_NUM_TOTAL_GUARDIAS_CUBIERTAS_RESIDENTES.intValue() / Util.CALC_NUM_SEMANAS_MES>0)	// cubiertas por residentes 
	{
		_RESIDENTES_SEMANA+=1;
	}
	
	System.out.println("_RESIDENTES_SEMANA:" + _RESIDENTES_SEMANA);
	*/

	//int n= 1;

	// RESTAMOS LAS GUARDIAS TOTALES DE RESIDENTES INICIAL A LAS QUE SE HAN METIDO PREVIAMENTE A LOS ADJUNTOS SOLOS  
	//_NUM_TOTAL_GUARDIAS_CUBIERTAS_RESIDENTES = _NUM_TOTAL_GUARDIAS_CUBIERTAS_RESIDENTES - _NUM_RESIDENTES_ASIGNADOS_PREVIOS_A_ADJUNTOS; 

	/* INTRODUCCION DE LOS RESIDENTES/SIMULADOS  DE MANERA INTELIGENTE */

	for (int j = 1; j <= NUM_SEMANAS_MES; j++) {

		// aleatatorio de los dias diarios  Y FESTIVOS
		// 2017 se solucionaria si establecisesemos los dias en funcion de los adjuntos en orden de mas simulados a menos , solo para los diarios */
		//List<Long> lDiasDSemanaAleatorio = ProcesarMedicos.ListaDiasSemanaAleatoria(cWeek, lFestivos, Util.eTipoDia.DIARIO, MONTH_ACTIVE);

		/* CUANTOS RESIDENTE HAY QUE RELLENAR ???*/
		for (int countResidentes = 0; countResidentes < NUMERO_RESIDENTES_POR_DIA; countResidentes++) {

			/* COGEMOS LOS DIAS SEGUN ORDEN ASCENDENTE DE ADJUNTOS CON MAS SIMULADOS , ASI SE VAN ASIGNANDO A LOS RESIDENTES */
			List<Long> lDiasDSemanaAleatorio = null;
			if (_bMES_VACACIONAL)
				lDiasDSemanaAleatorio = ProcesarMedicos.ListaDiasSemanaOrdenImpar(cWeek, lFestivos,
						Util.eTipoDia.DIARIO, MONTH_ACTIVE, lMedicosGuardias, _lAdjuntos);
			else
				lDiasDSemanaAleatorio = ProcesarMedicos.ListaDiasSemanaPorOrdenMenorSimulados(cWeek, lFestivos,
						Util.eTipoDia.DIARIO, MONTH_ACTIVE, lMedicosGuardias, _lAdjuntos);

			List<Long> lDiasFSemanaAleatorio = ProcesarMedicos.ListaDiasSemanaAleatoria(cWeek, lFestivos,
					Util.eTipoDia.FESTIVO, MONTH_ACTIVE);

			// Generamos un LinkedHashMap  uniendo ambas estructuras, día y si es festivo, por orden 
			Map<Long, Util.eTipoDia> _mapDiasSemana = new LinkedHashMap<Long, Util.eTipoDia>();
			// FESTIVOS 
			for (int cDia = 0; cDia < lDiasFSemanaAleatorio.size(); cDia++)
				_mapDiasSemana.put(lDiasFSemanaAleatorio.get(cDia), Util.eTipoDia.FESTIVO);
			// DIARIOS 
			for (int cDia = 0; cDia < lDiasDSemanaAleatorio.size(); cDia++)
				_mapDiasSemana.put(lDiasDSemanaAleatorio.get(cDia), Util.eTipoDia.DIARIO);

			/* DEPENDE DEL NUMERO DE DIAS DE SEMANA 
			
			REGLA DE TRES, GUARDIAS_A_CUBRIR  --> DIAS MES, SOBRE X DIAS SEMANA x SERAN tanto 
			
			*/

			int RESIDENTES_VARIABLE_SEMANA = (int) Math
					.ceil(_NUM_TOTAL_GUARDIAS_CUBIERTAS_RESIDENTES.intValue() * _mapDiasSemana.size() / 31f); // 7 DIAS SEMANA	
			/* RENDONDAMOS AL ENTERO MAS CERCANO */

			for (Map.Entry<Long, Util.eTipoDia> DiaSemana : _mapDiasSemana.entrySet()) {

				//System.out.println("valorn:" + n);
				Long DIASEMANAMES = new Long(DiaSemana.getKey());

				//System.out.println("1Dia:" + DIASEMANA);
				Util.eTipoDia _TIPODIA = DiaSemana.getValue();

				//System.out.println("_TIPODIA:" + _TIPODIA);
				cGuardiaDia.set(Calendar.DATE, DIASEMANAMES.intValue());

				boolean bExisteMedicoConGuardiaSolo = ProcesarMedicos
						.ExisteMedicoConGuardiaSolo_EnElMes(lMedicosGuardias, _lAdjuntos, DIASEMANAMES);

				/* 20170301 --> CONTEMPLAMOS QUE HAYA UN POOLDAY ASIGNADO Y UN MEDICO PREVIO CUANDO SOLO HAYA UN MEDICO DE GUARDIA */

				if (NUMERO_GUARDIAS_PRESENCIA.intValue() > 1 || (NUMERO_GUARDIAS_PRESENCIA.intValue() == 1
						&& !_lPoolDAYS.contains(DIASEMANAMES) && !bExisteMedicoConGuardiaSolo)
						|| NUMERO_RESIDENTES_POR_DIA.intValue() > 1) {

					/* INVOCAMOS CADA VEZ PARA QUE ME DE ORDENADO POR VACACIONES, RANDOM PARA EL RESTO Y SIMULADOS AL FINAL*/
					_lResidentes = ProcesarMedicos.getResidentesOrdenadosVacacionesDESC(_lResidentes,
							_format.format(_cINI.getTime()), _format.format(cLASTDAYMONTH.getTime()));
					List<Medico> _lResidentesRANDOM = new ArrayList<Medico>(_lResidentes);
					/* ALEATORIO O NO */
					int value = 0; // integer entre 0 y size -1
					/* if (!_bMES_VACACIONAL)  // aleatorio entoncexs 
					{
					Random rand = new Random();
					value = rand.nextInt(_lResidentesRANDOM.size());  // integer entre 0 y size -1
					}
					*/
					bEncontrado = false;
					Medico oM = null;
					Medico _oAdjunto = null;

					//Long AdjuntoIDPresenciaDia = new Long(-1);
					//List<Long> _ListaIDMedicosVerificados = new ArrayList<Long>();

					boolean IgnorarMenorNumeroGuardias = false; // cuando el que menos ha hecho guardias le toque esta asignacion (p.e. festivo)  pero ha trabajado el dia anterior 
					//boolean TodosResidentesCupoMaximo = ProcesarMedicos.TodosResidentesCupoMaximo(lMedicosGuardias, _lResidentes);  // si todos los residentes menos SIMULADO tienen el cupo relleno de maximo de guardias
					boolean NoTieneVacaciones = true;
					boolean ExcedeLimiteGuardiasMes = false;
					boolean EsResidenteConMenosFestivos = false;
					boolean TienePoolDayAsignadoDiaSgte = false;
					boolean SimuladosEquitativosConAdjunto = false; // repartimos de manera proporcional entre la semana los simulados y equitativo entre los adjuntos
					List<Long> lAdjuntosConMenosSimulados = new ArrayList<Long>();
					Long ResidenteConMenosFestivos = new Long(-1);
					int _GuardiasPreviasSeguidas = 0;
					/* SIMULADOS */
					boolean bAdjuntoConMenosSimulados = false;
					boolean bAsignadosSimuladosSemana = false;
					/* RESIDENTES  */
					boolean bAsignadosResidentesSemana = false;
					boolean AllMedicosVerificados = false; // si todos los medicos están verificados, se los asigno a simulado
					boolean SimuladoYaVerificado = false; // para que no asigne residentes de primera

					boolean bEstaGuardiaEnElDia = false; // cuando haya mas de un residente por dia

					_EsFestivo = _TIPODIA.equals(Util.eTipoDia.FESTIVO);
					int _DIAS_SEMANA_EN_CURSO = 0;

					int _NUM_SIMULADOS_ACTUALES_SEMANA = 0;
					int _NUM_RESIDENTES_ACTUALES_SEMANA = 0;

					_NUM_RESIDENTES_ACTUALES_SEMANA = ProcesarMedicos.NumeroResidentesSemana(cGuardiaDia,
							lMedicosGuardias, false, MONTH_ACTIVE);

					bAsignadosResidentesSemana = false;
					/* if (!_bMES_VACACIONAL || j==4)   // aleatorio entoncexs, establecemos el numero medio por residentes semana 
					{	*/
					if (RESIDENTES_VARIABLE_SEMANA <= _NUM_RESIDENTES_ACTUALES_SEMANA)
						bAsignadosResidentesSemana = true; // ya estan asignados ya que hay menos dias de los que exige
					else
						bAsignadosResidentesSemana = false; // ya estan asignados ya que hay menos dias de los que exige	 

					//}

					List<Long> lIDAdjuntoGuardiaDia = ProcesarMedicos.getMedicoGuardiaDia(
							DIASEMANAMES.intValue(), lMedicosGuardias, Util.eTipo.ADJUNTO, _lAdjuntos);

					List<Long> lIDResidenteGuardiaDia = ProcesarMedicos.getMedicoGuardiaDia(
							DIASEMANAMES.intValue(), lMedicosGuardias, Util.eTipo.RESIDENTE, _lResidentes);

					/*  TESTING 			
					List<Long> ListAdjuntoConMenosSimulados = ProcesarMedicos.ListAdjuntoConMenosSimulados(lMedicosGuardias, _lAdjuntos, DIASEMANAMES.intValue(), _EsFestivo);
					bAdjuntoConMenosSimulados = ListAdjuntoConMenosSimulados.contains(IDAdjuntoGuardiaDia);
					*/

					/* FIN TESTING */

					while (!bEncontrado) {

						// VERIFICAMOS PARA EL RESIDENTE, QUE 
						/* 1. NO ESTE DE VACACIONES 
						/* 2. NO SOBREPASE EL LIMITE DE GUARDIAS
						/* 3. QUE EL DIA ANTERIOR (24) PUEDA O NO TRABAJAR
						/* 4. SI ES UN FESTIVO, SEA EL QUE MENOS TIENE
						/* 5. SI ES EL SIMULADO, QUE NO HAYA MÁS QUE EL NUMERO POR SEMANA ASIGNADAS  (CUIDADO LA ULTIMA SEMANA) ,
						QUE ESTEN REPARTIDOS EQUITATIVAMENTE ENTRE LOS ADJUNTOS (OJO AL FLAG DE QUE NO PUEDE ASIGNARSE SIMULADO SI NO PUEDE ESTAR SOLO) 
						*/

						ExcedeLimiteGuardiasMes = false;
						EsResidenteConMenosFestivos = false;
						TienePoolDayAsignadoDiaSgte = false;
						SimuladosEquitativosConAdjunto = false; // repartimos de manera proporcional entre la semana los simulados y equitativo entre los adjuntos
						boolean ExcedeHorasSeguidas = false;
						lAdjuntosConMenosSimulados = new ArrayList<Long>();

						ResidenteConMenosFestivos = new Long(-1);
						_GuardiasPreviasSeguidas = 0;
						/* SIMULADOS */
						bAdjuntoConMenosSimulados = false;

						/* HAY LA ASIGNACION MINIMA DE SIMULADOS, POR DEFECTO, LO INTENTAMOS , SIEMPRE QUE SE CUMPLAN LAS CONDICIONES */

						//	bAsignadosSimuladosSemana = true;

						oM = (Medico) _lResidentesRANDOM.get(value);

						String _DATE = _format.format(cGuardiaDia.getTime());
						NoTieneVacaciones = oUtilMedicos.NoTieneVacaciones(oM, _DATE);

						bEstaGuardiaEnElDia = false; // cuando haya mas de un residente por dia
						if (lIDResidenteGuardiaDia.isEmpty() == false
								&& lIDResidenteGuardiaDia.contains(oM.getID()))
							bEstaGuardiaEnElDia = true;

						/* para no embuclarse, se asignan a simulados */
						//AllMedicosVerificados = ProcesarMedicos.TodosMedicosVerificadosDia(_lResidentes, _ListaIDMedicosVerificados);

						Hashtable _lDatosTemp = null;
						if (lMedicosGuardias.containsKey(oM.getID())) // existe el medico con guardias en la lista de control
						{

							_lDatosTemp = (Hashtable) lMedicosGuardias.get(oM.getID());
							Long _total = Long.parseLong(_lDatosTemp
									.get("_NUMERO_GUARDIAS_" + Util.eTipo.RESIDENTE + "_MES").toString())
									+ Long.parseLong(_lDatosTemp
											.get("_TOTAL_GUARDIAS_" + Util.eTipo.RESIDENTE + "_FESTIVO")
											.toString());
							//System.out.println("Médico: " + oM.getID() + ",guardiasmes:" + _total);
							// lleva más guardias calculadas que total tiene predefinidas
							if (_total.intValue() >= oM.getMax_NUM_Guardias().intValue()) {
								ExcedeLimiteGuardiasMes = true;
							}
							// verificamos dias anteriores
							_GuardiasPreviasSeguidas = ProcesarMedicos.getDiasPreviosConGuardiasSeguidos(
									DIASEMANAMES.intValue(), _lDatosTemp, _daysOfMonth);

							/* RESIDENTES NO PUEDEN ESTAR MAS DE 24 SEGUIDAS, NO CONFIGURABLE */
							//Long MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS = Long.parseLong(ConfigurationDBImpl.GetConfiguration(Util.getoCONST_MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS()).getValue());
							//	 System.out.println(Util.CONST_MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTES  + "," + _GuardiasPreviasSeguidas);
							if (_GuardiasPreviasSeguidas >= MAX_NUMERO_DIAS_SEGUIDOS_RESIDENTESS.intValue()) {
								ExcedeHorasSeguidas = true;
							}
							/* TIENE EL MEDICO ASIGNADO EL POOL EL DIA J+1*/
							if (_lDatosTemp.containsKey(new Long(DIASEMANAMES + 1))
									&& _lDatosTemp.get(new Long(DIASEMANAMES + 1)).equals("POOLDAY")) {
								TienePoolDayAsignadoDiaSgte = true;
							}

						} // fin de calculo de medico aleatorio

						EsResidenteConMenosFestivos = true;
						if (_EsFestivo) //&& !_bMES_VACACIONAL
						{
							//	ResidenteConMenosFestivos  = ProcesarMedicos.ResidenteMenosFestivosyNoExcedeDelTotal(lMedicosGuardias, _lResidentes, _DATE,j);
							/* PUEDE DARSE QUE EL RESIDENTE CON MENOS FESTIVOS HAYA TRABAJADO EL VIERNES, CON LO QUE NO SE LE PUEDE ASIGNAR EL SABADO Y ENTRA EN BUCLE */
							ResidenteConMenosFestivos = ProcesarMedicos.ResidenteMenosGuardiasyNoExcedeDelTotal(
									lMedicosGuardias, _lResidentes, _DATE, DIASEMANAMES.intValue(),
									_daysOfMonth, !_bMES_VACACIONAL);
							EsResidenteConMenosFestivos = oM.getID().equals(ResidenteConMenosFestivos);

						}
						// simulado y ya tengo los festivos rellenos, verifico el simulado

						if (oM.isResidenteSimulado()) {
							if (_lResidentesRANDOM.size() == 1) // solo esta él.						
								bEncontrado = true;
							/* else 
							{				
							  if (!_ListaIDMedicosVerificados.contains(oM.getID())) _ListaIDMedicosVerificados.add(oM.getID());
							}	*/

						} else // residente, si están todos ya rellenos a nivel de semana, dejamos hueco al simulado
						/* 2017-23-01 */
						/*&& !bAdjuntoConMenosSimulados */
						{
							if (!bAsignadosResidentesSemana && NoTieneVacaciones && !ExcedeLimiteGuardiasMes
									&& !bEstaGuardiaEnElDia && !ExcedeHorasSeguidas
									&& !TienePoolDayAsignadoDiaSgte
									&& (!_EsFestivo || (_EsFestivo && EsResidenteConMenosFestivos)))
								bEncontrado = true;
							else {
								_lResidentesRANDOM.remove(oM);
							}

							// eliminamos el medico ya verificado para que no salga en el random

						}

						value = 0; // integer entre 0 y size -1
						if (!_bMES_VACACIONAL) // aleatorio entoncexs 
						{
							Random rand = new Random();
							value = rand.nextInt(_lResidentesRANDOM.size()); // integer entre 0 y size -1
						}

					} // fin de encontrado

					Hashtable _lDatosGuardiasMedico = null;

					_lDatosGuardiasMedico = (Hashtable) lMedicosGuardias.get(oM.getID());

					/* PONEMOS EL DIA */
					_lDatosGuardiasMedico.put(DIASEMANAMES, "NOPOOLDAY"); // redundate, dia key , dia value

					/* if  (!_lDatosGuardiasMedico.containsKey("_NUMERO_GUARDIAS_RESIDENTE_MES"))
						_lDatosGuardiasMedico.put("_NUMERO_GUARDIAS_RESIDENTE_MES",1);  // la primera
					else*/ // SI NO, SUMAMOS UNO
					/* SI ES FESTIVO, SUMAMOS EL FESTIVO TAMBIEN */
					if (_EsFestivo) {
						_lDatosGuardiasMedico.put("_TOTAL_GUARDIAS_" + Util.eTipo.RESIDENTE + "_FESTIVO",
								Integer.valueOf(_lDatosGuardiasMedico
										.get("_TOTAL_GUARDIAS_" + Util.eTipo.RESIDENTE + "_FESTIVO").toString())
										+ 1);
					} else /* 03-01-2017 */
					{
						_lDatosGuardiasMedico.put("_NUMERO_GUARDIAS_" + Util.eTipo.RESIDENTE + "_MES",
								Integer.valueOf(_lDatosGuardiasMedico
										.get("_NUMERO_GUARDIAS_" + Util.eTipo.RESIDENTE + "_MES").toString())
										+ 1);

					}
					/*  SI ES UN SIMULADO, LE METEMOS AL MEDICO ADJUNTO UN CONTADOR DE SIMULADOS  */
					/*  OJO, 2017041 --> NO TIENE SENTIDO AUMENTAR CONTADOR DE SIMULADOS CUANDO HAY MAS MEDICOS PRESENTES EN EL DIA */
					if (oM.isResidenteSimulado()) {
						Hashtable _lDatosGuardiasAdjunto = null;

						boolean _bHayUnAdjuntoSoloDePresencia = true;
						if (!lIDAdjuntoGuardiaDia.isEmpty() && lIDAdjuntoGuardiaDia.size() > 1)
							_bHayUnAdjuntoSoloDePresencia = false;

						if (_bHayUnAdjuntoSoloDePresencia) {

							Long IDAdjuntoGuardiaDia = lIDAdjuntoGuardiaDia.get(0);
							if (lMedicosGuardias.containsKey(IDAdjuntoGuardiaDia)) {
								_lDatosGuardiasAdjunto = (Hashtable) lMedicosGuardias.get(IDAdjuntoGuardiaDia);
								String _Key = "_TOTAL_" + Util.eSubtipoResidente.SIMULADO.toString()
										+ "_DIARIO_MES";
								if (_EsFestivo)
									_Key = "_TOTAL_" + Util.eSubtipoResidente.SIMULADO.toString()
											+ "_FESTIVOS_MES";

								_lDatosGuardiasAdjunto.put(_Key,
										Integer.valueOf(_lDatosGuardiasAdjunto.get(_Key).toString()) + 1);

							}
						}

					}

					//System.out.println("dia:" + DIASEMANAMES + ",_NUM_RESIDENTES_ACTUALES_SEMANA:" + _NUM_RESIDENTES_ACTUALES_SEMANA + ",RESIDENTES_VARIABLE_SEMANA:" + RESIDENTES_VARIABLE_SEMANA);

					Medico _oMenosFestivos = MedicoDBImpl
							.getMedicos(ResidenteConMenosFestivos, MedicoLogged.getServicioId()).get(0);

					/* System.out.println("Dia:" + DIASEMANAMES  + ",Residente:" +oM.getApellidos() +  
									"DiariasGuardias:" + _lDatosGuardiasMedico.get("_NUMERO_GUARDIAS_" + Util.eTipo.RESIDENTE + "_MES") + 
									",Festivos:" + _lDatosGuardiasMedico.get("_TOTAL_GUARDIAS_RESIDENTE_FESTIVO") + 
									"HistoricoDiarias:" + _lDatosGuardiasMedico.get("HISTORICO_NUMERO_GUARDIAS_" + Util.eTipo.RESIDENTE + "_MES") + 
									",HistoricoFestivos:" + _lDatosGuardiasMedico.get("HISTORICO_TOTAL_GUARDIAS_RESIDENTE_FESTIVO") +
									",ResidenteMenosFestivos:" + _oMenosFestivos.getApellidos());
						 
						*/

				} // fin de if (!_lPoolDAYS.contains(DIASEMANA) && !bExisteMedicoConGuardiaSolo)

			} // for dias de semana

		} // FIN DE ITERACION DE RESIDENTES

		cWeek.add(Calendar.DAY_OF_MONTH, 7);

	} // for semanas del mes 

	//for (int j=1;j<=_daysOfMonth;j++)	

	/*[_TIPO] --> ADJUNTO	
				[_TOTAL_GUARDIAS_MES]
				[_TOTAL_FESTIVOS_MES]		
				[_TOTAL_PRESENCIA_MES]
				[_TOTAL_REFUERZO_MES]
				[_TOTAL_LOCALIZADA_MES]					
				[_TOTAL_PRESENCIA_FESTIVOS_MES]
				[_TOTAL_LOCALIZADA_FESTIVOS_MES]
				[_TOTAL_REFUERZO_FESTIVOS_MES]							
				[_TOTAL_SIMULADOS_DIARIO_MES]		
				[_TOTAL_SIMULADOS__FESTIVOS_MES]
				[0]  --> [PRESENCIA, LOCALIZADA, REFUERZO]
				[1]  --> [PRESENCIA, LOCALIZADA, REFUERZO]
				[2]	 --> [PRESENCIA, LOCALIZADA, REFUERZO]
	*/

	/* DISTRIBUIMOS LAS LOCALIZADAS Y LOS REFUERZOS DE LOS ADJUNTOS */
	cGuardiaDia.setTimeInMillis(_cINI.getTimeInMillis());
	for (int j = 1; j <= _daysOfMonth; j++) {

		Medico oM = null;

		//System.out.println("Generamos aleatorio entre los residentes, entre 0 y  " + _lResidentes.size() + ",valor:" + value);
		// obtenemos el residente para ese dia

		Medico oResidente = null;
		List<Long> lIDResidenteGuardiaDia = new ArrayList<Long>();
		List<Long> lIDAdjuntoGuardiaDia = new ArrayList<Long>();

		List<Medico> lAdjuntosRANDOM = new ArrayList<Medico>(_lAdjuntos);

		/* CUANTAS LOCALIZADAS Y REFUERZOS  HAY QUE RELLENAR */
		// puede ser que sean menos que residentes existan o incluso mas o igual
		// 1. IGUAL, CRITERIO DE TIPO DE RESIDENTE --> RECORREMOS
		// 2. MENOS, CONTAMOS SI HAY  R1 EXISTENTES PARA AJUSTAR LOS REFUERZOS --> LOCALIZADAS TODAS
		// 3. SI HAY MAS, PUES INDETERMINADO --> NO SE LE DEJARA

		lIDResidenteGuardiaDia = ProcesarMedicos.getMedicoGuardiaDia(j, lMedicosGuardias, Util.eTipo.RESIDENTE,
				_lResidentes);

		/* consideramos que si hay menos refuerzos que residentes, ya no tiene sentido los refuerzos */
		boolean _GuardiasTodasLocalizadas = NUMERO_REFUERZOS_LOCALIZADAS < lIDResidenteGuardiaDia.size();

		// bucle de numero de localizadas y refuerzos
		for (int countLocalizadasRefuerzos = 0; countLocalizadasRefuerzos < NUMERO_REFUERZOS_LOCALIZADAS; countLocalizadasRefuerzos++) {

			bEncontrado = false;
			//PRESENCIA 
			lIDAdjuntoGuardiaDia = ProcesarMedicos.getMedicoGuardiaDia(j, lMedicosGuardias, Util.eTipo.ADJUNTO,
					_lAdjuntos);

			if (lIDResidenteGuardiaDia.isEmpty()) {
				System.out.println(
						"Buscando localizada y refuerzo dia " + j + ",no se ha encontrado el residente");
				break;
			}

			Util.eTipoGuardia _TipoGuardiaAdjunto = Util.eTipoGuardia.LOCALIZADA;
			if (!_GuardiasTodasLocalizadas) {

				Long IDResidenteGuardiaDia = lIDResidenteGuardiaDia.get(countLocalizadasRefuerzos);

				oResidente = ProcesarMedicos.GetMedicoPorID(_lResidentes, IDResidenteGuardiaDia);
				//oResidente = _lResidentes.get(IDResidenteGuardiaDia.intValue());
				Util.eSubtipoResidente _TipoResidente = oResidente.getSubTipoResidente();
				_TipoGuardiaAdjunto = Util.eTipoGuardia.REFUERZO;
				/* R4, R5 */
				if (_TipoResidente.equals(Util.eSubtipoResidente.R4)
						|| _TipoResidente.equals(Util.eSubtipoResidente.R5)) {
					_TipoGuardiaAdjunto = Util.eTipoGuardia.LOCALIZADA;

				}
			}
			String _Key = "";
			String _DATE = _format.format(cGuardiaDia.getTime());
			Long AdjuntoConMenosGuardias = new Long(-1);
			List<Long> ListAdjuntoConMenosSimulados = null;

			_EsFestivo = Boolean.parseBoolean(request.getParameter("festivo" + j));
			// GUARDIAS TOTALES 				  
			_Key = "_TOTAL_" + _TipoGuardiaAdjunto.toString();
			if (_EsFestivo)
				_Key = _Key.concat("_FESTIVOS");
			_Key = _Key.concat("_MES");

			/* TESTING */
			//ListAdjuntoConMenosSimulados = ProcesarMedicos.ListAdjuntoConMenosSimulados(lMedicosGuardias, _lAdjuntos, j, _EsFestivo);
			//bAdjuntoConMenosSimulados = ListAdjuntoConMenosSimulados.contains(IDAdjuntoGuardiaDia);
			AdjuntoConMenosGuardias = ProcesarMedicos.AdjuntoMenosGuardiasYHorasSeguidas(false,
					_TipoGuardiaAdjunto, _Key, lMedicosGuardias, _lAdjuntos, lIDAdjuntoGuardiaDia, j,
					_daysOfMonth, _DATE, MedicoLogged.getServicioId());

			System.out.println("ListAdjuntoConMenosSimulados:" + ListAdjuntoConMenosSimulados);

			if (_bMES_VACACIONAL) {
				int _OBJETIVO_MINIMO_GUARDIA_POR_ADJUNTO = _OBJETIVO_MES_ADJUNTOS_REFUERZO;
				if (_TipoGuardiaAdjunto.equals(Util.eTipoGuardia.LOCALIZADA))
					_OBJETIVO_MINIMO_GUARDIA_POR_ADJUNTO = _OBJETIVO_MES_ADJUNTOS_LOCALIZADAS;

				oM = ProcesarMedicos.setGuardiaLocalizadasRefuerzosEnVacaciones(_Key, j, _daysOfMonth,
						lIDAdjuntoGuardiaDia, AdjuntoConMenosGuardias, _DATE, lAdjuntosVacacionesDESC,
						lMedicosGuardias, _TipoGuardiaAdjunto, _OBJETIVO_MINIMO_GUARDIA_POR_ADJUNTO,MedicoLogged.getServicioId());
			} else
				oM = ProcesarMedicos.setGuardiaLocalizadasRefuerzos(j, _daysOfMonth, lIDAdjuntoGuardiaDia,
						AdjuntoConMenosGuardias, _DATE, lAdjuntosRANDOM, lMedicosGuardias, _TipoGuardiaAdjunto);

			//System.out.println("NoTieneVacaciones:" + NoTieneVacaciones + "," + ) ;

			// metemos los datos 
			// existe el medico residente en su lista de control
			Hashtable _lDatosGuardiasMedico = null;

			System.out.println(
					"Agregado al dia : " + j + ", médico adjunto:" + oM.getID() + "," + _TipoGuardiaAdjunto
							+ " " + _Key + ",Adjunto de Guardia:" + lIDAdjuntoGuardiaDia.toString());

			/* AQUI YA TIENEN QUE  ESTAR TODOS LOS ADJUNTOS CON LAS PRESENCIAS  RELLENAS */

			_lDatosGuardiasMedico = (Hashtable) lMedicosGuardias.get(oM.getID());

			/* PONEMOS EL DIA */

			String _key = "_TOTAL_" + _TipoGuardiaAdjunto.toString() + "_FESTIVOS_MES";
			if (!_EsFestivo)
				_key = "_TOTAL_" + _TipoGuardiaAdjunto.toString() + "_MES";

			int _ContadorPrevio = 0;
			if (_lDatosGuardiasMedico.containsKey(_key))
				_ContadorPrevio = Integer.valueOf(_lDatosGuardiasMedico.get(_key).toString());

			_lDatosGuardiasMedico.put(new Long(j), _TipoGuardiaAdjunto.toString()); // redundate, dia key , dia value
			/* 03-01-2017  _lDatosGuardiasMedico.put("_TOTAL_GUARDIAS_MES", Integer.valueOf(_lDatosGuardiasMedico.get("_TOTAL_GUARDIAS_MES").toString()) +1);*/
			_lDatosGuardiasMedico.put(_key, _ContadorPrevio + 1);

			if (!_GuardiasTodasLocalizadas && oResidente.isResidenteSimulado()) {
				if (_EsFestivo)
					_lDatosGuardiasMedico.put(
							"_TOTAL_" + Util.eSubtipoResidente.SIMULADO.toString() + "_FESTIVOS_MES",
							Integer.valueOf(_lDatosGuardiasMedico.get(
									"_TOTAL_" + Util.eSubtipoResidente.SIMULADO.toString() + "_FESTIVOS_MES")
									.toString()) + 1);
				else /* 03-01-2017 */
					_lDatosGuardiasMedico.put(
							"_TOTAL_" + Util.eSubtipoResidente.SIMULADO.toString() + "_DIARIO_MES",
							Integer.valueOf(_lDatosGuardiasMedico
									.get("_TOTAL_" + Util.eSubtipoResidente.SIMULADO.toString() + "_DIARIO_MES")
									.toString()) + 1);

			}

		} // FIN DE COUNT DE LOCALIZADAS Y REFUERZOS	

		cGuardiaDia.add(Calendar.DATE, 1);

	}

	HashMap<Long, Hashtable> lResultados = new HashMap<Long, Hashtable>();

	cGuardiaDia.setTimeInMillis(_cINI.getTimeInMillis());

	for (int j = 1; j <= _daysOfMonth; j++) {

		String _DATE = _format.format(cGuardiaDia.getTime());
		_EsFestivo = Boolean.parseBoolean(request.getParameter("festivo" + j));
		_Festivo = "";
		if (_EsFestivo)
			_Festivo = "checked";
		/* RECORREMOS LA LISTA DE CADA GUARDIA Y MEDICO */
		Long IDMEDICO = new Long(-1);
		Iterator entries = lMedicosGuardias.entrySet().iterator();
		bEncontrado = false;
		Hashtable _lDatos = null;

		// la generamos
		_lDatos = new Hashtable();
		lResultados.put(new Long(j), _lDatos);

		while (entries.hasNext()) {

			Entry thisEntry = (Entry) entries.next();
			Long keyMEDICO = (Long) thisEntry.getKey();
			// puede ser un adjunto  en una lista de residente
			// CAMBIO 20161226, INCLUIMOS EN LOS RESULTADOS A LOS RESIDENTES 
			/* if (!ProcesarMedicos.ExisteMedicoPorId(_lAdjuntos,keyMEDICO))		  
			  continue;*/
			// CAMBIO 20161226, INCLUIMOS EN LOS RESULTADOS A LOS RESIDENTES
			Medico _oM = ProcesarMedicos.GetMedicoPorID(lItems, keyMEDICO);
			Hashtable lDatosTemp = (Hashtable) thisEntry.getValue();
			/* cada subclave del medico, buscamos si tiene el dia DiaMes */
			Iterator entriesDAYS = lDatosTemp.entrySet().iterator();

			if (lDatosTemp.containsKey(new Long(j))) // encuentra el dia 
			{
				String _Temp = (String) lDatosTemp.get(new Long(j));
				String _Temp2 = "";

				/* 20170403 --> puede duplicarse las claves de presencia / localizadas, con lo que me está machacando con el ultimo valor */

				/* AL INCLUIR RESIDENTES, PODEMOS OBTENER POOLDAY, NOPOOLDAY*/
				_Temp2 = _Temp.replace("NOPOOLDAY", Util.eTipo.RESIDENTE.toString()).replace("POOLDAY",
						Util.eTipo.RESIDENTE.toString());

				// PASAMOS DE {RESIDENTE=5|MEDICO|ROTANTE|NO_SIMULADO, PRESENCIA=12|MARTIN MUNARRIZ|PABLO |NO_SIMULADO}
				//A {RESIDENTE5_=5|MEDICO|ROTANTE|NO_SIMULADO, 12_PRESENCIA=12|MARTIN MUNARRIZ|PABLO |NO_SIMULADO}

				_lDatos.put(_Temp2 + "_" + _oM.getID(),
						_oM.getID() + "|" + _oM.getApellidos() + "|" + _oM.getNombre() + "|"
								+ (_oM.getSubTipoResidente().equals(Util.eSubtipoResidente.SIMULADO)
										? Util.eSubtipoResidente.SIMULADO
										: "NO_".concat(Util.eSubtipoResidente.SIMULADO.toString())));

			}

		}
		cGuardiaDia.add(Calendar.DAY_OF_MONTH, 1);

	}

	/* PINTAMOS LOS DATOS  EN EL CALENDARIO */

	cGuardiaDia.setTimeInMillis(_cINI.getTimeInMillis());

	String _EventsJSON = "[";

	Hashtable _lDatos = null;

	// la generamos
	_lDatos = new Hashtable();

	Iterator entries = lResultados.entrySet().iterator();

	/* POR CADA DIA DE RESULTADOS */
	while (entries.hasNext()) {

		String _DATE = _format.format(cGuardiaDia.getTime());

		Entry thisEntry = (Entry) entries.next();
		Long keyDAY = (Long) thisEntry.getKey();
		// puede ser un adjunto  en una lista de residente		  		  		  
		Hashtable lDatosTemp = (Hashtable) thisEntry.getValue();
		/* cada subclave del medico, buscamos si tiene el dia DiaMes */
		Iterator entriesGuardias = lDatosTemp.entrySet().iterator();
		/* POR CADA DIA TIPO DE GUARDIA */

		_EventsJSON += "{\"title\":\"";

		_EsFestivo = Boolean.parseBoolean(request.getParameter("festivo" + keyDAY.intValue()));

		String sFestivo = _EsFestivo ? "festivoc" : "";

		int classContador = 1;

		while (entriesGuardias.hasNext()) {
			//12_PRESENCIA=12|MARTIN MUNARRIZ|PABLO |NO_SIMULADO}
			Entry thisGuardiaDay = (Entry) entriesGuardias.next();
			String sthisGuardiaDay = "";
			sthisGuardiaDay = String.valueOf(thisGuardiaDay.getValue());
			String[] aDatosMedico = sthisGuardiaDay.split("\\|");

			Long _GuardiaMedicoID = (Long) Long.valueOf(aDatosMedico[0]);
			String _GuardiaApellidos = (String) aDatosMedico[1];
			String _GuardiaNombre = (String) aDatosMedico[2];
			String _GuardiaSimulado = (String) aDatosMedico[3];

			String _classTipo = "";
			String _sGuardia = Util.eTipo.RESIDENTE.toString();
			String _sSubTipoGuardia = "";

			String _GuardiaKEY = thisGuardiaDay.getKey().toString();
			// QUITAMOS EL ID POR DELANTE //12_PRESENCIA=12|MARTIN MUNARRIZ|PABLO |NO_SIMULADO}
			_GuardiaKEY = _GuardiaKEY.replace("_" + _GuardiaMedicoID.toString(), "");
			/* GUARDIA ADJUNTOS */
			if (!_GuardiaKEY.equals(Util.eTipo.RESIDENTE.toString())) {
				Util.eTipoGuardia _Guardia = com.guardias.Util.eTipoGuardia.valueOf(_GuardiaKEY);
				_sGuardia = _Guardia.toString();
				_classTipo = "adjunto";
			}
			//if 
			if (_sGuardia.toString().equals(Util.eTipoGuardia.LOCALIZADA.toString())
					|| _sGuardia.toString().equals(Util.eTipoGuardia.REFUERZO.toString()))
				classContador = 2;
			else {
				if (_sGuardia.toString().equals(Util.eTipoGuardia.PRESENCIA.toString()))
					classContador = 3;
				else
					classContador = 1;

			}

			_EventsJSON += "<div class='orden" + classContador + " " + _classTipo + " "
					+ _sGuardia.toString().toLowerCase() + " " + _GuardiaSimulado.toLowerCase() + " " + sFestivo
					+ "' id=" + _GuardiaMedicoID + ">" + _GuardiaApellidos + "," + _GuardiaNombre + "</div>";
			// classContador+=1;
		}

		_EventsJSON += "\",\"start\": \"" + _DATE + "\",";
		_EventsJSON += "\"textEscape\": false";
		_EventsJSON += "},";

		cGuardiaDia.add(Calendar.DAY_OF_MONTH, 1);

	}

	_EventsJSON += "]";
	_EventsJSON = _EventsJSON.replace("},]", "}]");

	System.out.println(_EventsJSON);
	out.println(_EventsJSON);
%>