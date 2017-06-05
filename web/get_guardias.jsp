
<%@page import="com.guardias.database.GuardiasDBImpl"%>
<%@page import="com.guardias.database.MedicoDBImpl"%>
<%@page import="java.util.*"%>

<%@page import="java.util.Calendar"%>
<%@page import="java.text.*"%>
<%@page import="com.guardias.*"%>
<%@page import="org.json.*"%>
<%@page import="com.guardias.*"%>
<%@page import="java.util.*"%>
<%@page import="java.util.Map.*"%>



<%


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



/* SUMAMOS UN MES siempore y cuando no sea el dia uno */


_cINI.setTimeInMillis(_dINI.getTime());
_cINI.set(Calendar.DAY_OF_MONTH, 1);
if (_dINI.getDate()!=1)
	_cINI.add(Calendar.MONTH, 1);

_cFIN.setTimeInMillis(_dFIN.getTime());
_cFIN.set(Calendar.DAY_OF_MONTH, 1);



System.out.println(_format.format(_cINI.getTime()));
System.out.println(_format.format(_cFIN.getTime()));
System.out.println(_start);
System.out.println(_end);



List<Guardias> lGuardias;


int _daysOfMonth = com.guardias.Util.daysDiff(_cINI.getTime(),_cFIN.getTime());


boolean  bEncontrado = false;
boolean _EsFestivo = false; 	

String _Festivo = ""; // o B

Calendar _cINICIO = Calendar.getInstance();
Calendar _cFINAL = Calendar.getInstance();

_cINICIO.setTimeInMillis(_cINI.getTimeInMillis());
_cFINAL.setTimeInMillis(_cFIN.getTimeInMillis());

String _EventsJSON = "[";

	

for (int j=1;j<=_daysOfMonth;j++)	
{
	
	String _DATE = _format.format(_cINICIO.getTime());
	lGuardias = GuardiasDBImpl.getGuardiasPorFecha(_DATE);

	if (lGuardias.size()>0)
	{
		
		_EventsJSON += "{\"title\":\"";
	
	   int classContador=1;
	
		for (int x=0;x<lGuardias.size();x++)
		{
		
			
			String _classTipo= "";
			
			Guardias oGuardia = lGuardias.get(x);
			
			_classTipo = oGuardia.getTipo();
			
			_EsFestivo = oGuardia.isEsFestivo().equals(new Long(1)) ? true : false;
			  
			 String sFestivo = _EsFestivo ? "festivoc" : "";
			   
			List<Medico> oLM =  MedicoDBImpl.getMedicos(lGuardias.get(x).getIdMedico());
			
			Medico oM = (Medico) oLM.get(0);
			
			String _EsSimulado= "NO_" + Util.eSubtipoResidente.SIMULADO.toString();
			if (oM.isResidenteSimulado())
				_EsSimulado= Util.eSubtipoResidente.SIMULADO.toString();
			
			// PARA EL TOUR, SOLO AL PRIMER Y ULTIMO DIA */
			String _classINTROTour ="";
			if (j==1)			
				_classINTROTour ="data-step='" + j +  "' data-intro='Selecciona el día que estés de guardias y desees cambiar o ceder. Un icono aparecerá sobre el día del calendario'";
			if (j==28)			
				_classINTROTour ="data-step='" + j +  "' data-intro='Arrastra con el ratón el día seleccionado hasta el elegido. Se aplicarán validaciones para verificar que todo está OK'";		
			
			
			_EventsJSON += "<div " + _classINTROTour + " class='orden" + classContador + " " + oM.getTipo().toString().toLowerCase()  +  " " + _classTipo+" " + _EsSimulado.toLowerCase() + " " +  sFestivo + "' id=" + oM.getID() + ">"   + oM.getApellidos()+ "," + oM.getNombre() + "</div>";
			
			classContador+=1;
								
		}		
		_EventsJSON += "\",\"start\": \"" + _DATE + "\",";
		_EventsJSON += "\"textEscape\": false";		 
		_EventsJSON += "},";

	}
	// end (lGuardias.size()>0)	
 
	_cINICIO.add(Calendar.DAY_OF_MONTH, 1);
	  
	  
}	   


_EventsJSON += "]";
_EventsJSON=_EventsJSON.replace("},]", "}]");
		   

System.out.println(_EventsJSON);
out.println(_EventsJSON);


%>