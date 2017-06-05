
<%@page import="java.util.*"%>

<%@page import="java.util.Calendar"%>
<%@page import="java.text.*"%>
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.database.*"%>
<%@page import="org.json.*"%>
<%@page import="com.guardias.*"%>
<%@page import="java.util.*"%>        



<%


/*{“Employees”:[

{
“Name”:”ABC”,
“Designation”:”Manager”,
“Pay”:”Rs. 60000/-“,
“PhoneNumbers”:[{
“LandLine” : “11-2xxxx99”,
“Mobile” : “11xxxxxx11”
}]
},
*/

//javax.json.JsonObject object = Json.createObjectBuilder().build();



JSONObject allEmps=new JSONObject();
JSONObject empObj=null;

 
 


String _start = request.getParameter("start"); //2016-08-28
String _end = request.getParameter("end");

Date _dINI = new Date();
Date _dFIN = new Date();

DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");

_dINI = _format.parse(_start);
_dFIN = _format.parse(_end);


Calendar _cINI = Calendar.getInstance();
Calendar _cFIN = Calendar.getInstance();

/* SUMAMOS UN MES */
_cINI.setTimeInMillis(_dINI.getTime());
/* siempre llega el primer dia de la semana incluso del mes anterior */
if (_cINI.get(Calendar.DAY_OF_MONTH)!=1)
{
_cINI.set(Calendar.DAY_OF_MONTH, 1);
_cINI.add(Calendar.MONTH, 1);
}

_cFIN.setTimeInMillis(_dFIN.getTime());
_cFIN.set(Calendar.DAY_OF_MONTH, 1);



System.out.println(_format.format(_cINI.getTime()));
System.out.println(_format.format(_cFIN.getTime()));
System.out.println(_start);
System.out.println(_end); 

List lItems = new ArrayList();

lItems = MedicoDBImpl.getMedicos();


List<Long> lGenerada = new ArrayList();


int _daysOfMonth = com.guardias.Util.daysDiff(_cINI.getTime(),_cFIN.getTime());

String _EventsJSON = "[";
int _Index;

boolean  bEncontrado = false;


String poolDay = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_EXISTE_POOLDAY()).getValue();


// para ir iterando por bloques 
//for (int j=1;j<=_daysOfMonth;j+=(lItems.size()-1))
for (int j=1;j<=_daysOfMonth;j++)	
{
	
	bEncontrado = false;
	
	String _Title = "";
	
	String _esFestivo  = "";
	

//	if (_cINI.get(Calendar.DAY_OF_WEEK))
	if (_cINI.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY || 
			_cINI.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY)
	
			_esFestivo = "checked";

	
	
	for (int x=0;x<lItems.size();x++)
	{
	
		bEncontrado = false;
		
		String _DATE = _format.format(_cINI.getTime());
		
		Medico oM = (Medico) lItems.get(x);
		
		
		// sabado o doming 
		
		
		if (ProcesarMedicos.NoTieneVacaciones(oM, _DATE)
				&& (lGenerada.size()==0 || !lGenerada.contains(oM.getID())))
		{
			
			// RELLENAMOS LA LISTA DE CONTROL
			
			
			//_EventsJSON += "{\"title\": \"<div id=adjunto>"   + oM.getApellidos() + "," + oM.getNombre()+  "</div><div id=residente>R.A<input name=tipo" + x+1 + " id=tipo" + j + " type=radio value='A'>R.B<input id=tipo" + j  + " name=tipo" + j + " type=radio value=B></div><div id=refuerzo" + (j) + ">Con / Sin refuerzo</div><div class=festivo>Festivo<input name=vacaciones" + (x+1)  + " type=checkbox value=V></div>\",";
			//_EventsJSON += "{\"title\": \"<div id=residente>RM<input name=tipo" + j + " id=tipo type=radio value='A'>Rm<input id=tipo name=tipo" + j + " type=radio value=B></div><div class=festivo>Festivo<input name=vacaciones" + (x+1)  + " type=checkbox value=V " + _esFestivo + "></div>\",";
			// solo meto los festivos
			_EventsJSON += "{\"title\": \"<div class=festivo>Festivo<input name=vacaciones" + (x+1)  + " type=checkbox value=V " + _esFestivo + "></div>";
			if (poolDay.equals("S"))
				_EventsJSON +="<div id=poolday>PoolDay<input name=pool" + j + " id=pool type=checkbox value='POOL'></div>";
				
			_EventsJSON +="\",";
			_EventsJSON += "\"start\": \"" + _DATE + "\",";
			_EventsJSON += "\"textEscape\": false";		 
			_EventsJSON += "},";
			
			lGenerada.add(oM.getID());
			bEncontrado = true;													
			break;
			
			
		}
		// que no he encontrado a nadie, limpio la lista y recorro el mismo dia
		if (x==lItems.size()-1)			
		{
			lGenerada.clear();
			j--;
		}
		
		
	}
	// si no ha encontrado a nadie mas, vaciamos la lista de control de asignaciones
	
	if (bEncontrado)	
		_cINI.add(Calendar.DAY_OF_MONTH, 1);
	
}




_EventsJSON += "]";
_EventsJSON=_EventsJSON.replace("},]", "}]"); 



out.print(_EventsJSON);
%>