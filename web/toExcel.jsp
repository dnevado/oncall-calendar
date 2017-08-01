

<%@ page language="java" import="java.io.*" %>
<%@ page language="java" import="com.guardias.excel.*" %>
<%@ page language="java" import="java.text.SimpleDateFormat" %>
<%@ page language="java" import="java.text.DateFormat" %>
<%@ page language="java" import="java.util.Calendar" %>
<%@page import="com.guardias.database.MedicoDBImpl"%>
<%@page import="com.guardias.ProcesarMedicos"%>
<%@page import="com.guardias.*"%>
<%@page import="com.guardias.Util"%>
<%@page import="java.util.*"%>


<jsp:useBean id="MedicoLogged" class="com.guardias.Medico" scope="session"/>


<% 

  String GuardiasJSON = request.getParameter("guardias");

  boolean ByEmail  = false;
  
  if (request.getParameter("ByEmail")!=null && request.getParameter("ByEmail").equals("1"))
  		ByEmail  = true;
  

  String _excel = (String) request.getParameter("filecontent");
  String _Date = (String) request.getParameter("MesGuardia");
  
  DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");
  
  java.util.Date cDate = _format.parse(_Date);
  
  Calendar caled = Calendar.getInstance();
  caled.setTime(cDate);
  //cDate
  
  CalendarToExcel cE = new CalendarToExcel();
  
  CalendarToExcel.GenerateExcel(System.getProperty("java.io.tmpdir") + _Date+".xlsx",caled ,GuardiasJSON,MedicoLogged);

  //response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
  
  

  //response.setHeader("Content-Disposition","attachment; filename=\"SIPInvestment_531.xls\"");                           
  
  
  if (!ByEmail)
  {
	  
  
  
	  response.setContentType("application/vnd.ms-excel charset=ISO-8859-1");
	  response.setHeader("Content-Disposition","attachment;filename=" + _Date+".xlsx"); 
	  response.setHeader("Cache-Control","no-cache");
	  response.setHeader("Content-Transfer-Encoding","binary");    
	  response.setCharacterEncoding("UTF-8");
	
	  
	  
	  	FileInputStream  inputStream = new FileInputStream(new File(System.getProperty("java.io.tmpdir") + _Date+".xlsx"));
	  	
	  	
	  	
	  	try
	  	{
	  		ServletOutputStream outputStream = response.getOutputStream();
	  		//OutputStream outputStream = response.getOutputStream();
	  	  	 int line;
	  	     //StringBuilder sb = new StringBuilder();
	  	     while ((line = inputStream.read()) != -1) {
	  	         //sb.append(line);
	  	         outputStream.write(line);
	  	     }
	  	     inputStream.close();
	  	     outputStream.flush();
	  	     outputStream.close();	
	  	     
	  	     // intentamos borrarlo
	  	     File _TmpExcel = new File(System.getProperty("java.io.tmpdir") + _Date+".xlsx");
	  	     if (_TmpExcel.canWrite() && _TmpExcel.exists())
	  	    	_TmpExcel.delete();
	  	     
	  	     
	  	}
	  	catch (Exception e)
	  	{
	  	}
  }
  
  else // por email
  {
	  	List<Medico> lMedicos = MedicoDBImpl.getMedicos(new Long(-1), MedicoLogged.getServicioId());
		List<String> lMails = new ArrayList();
			
	    for (Medico  oMedico :lMedicos)
	    {
	    	if (oMedico.isActivo())
	    		
	    		lMails.add(oMedico.getEmail());
	    	
	    }
	    String[] aMails = lMails.toArray(new String[lMails.size()]);        
	     
	    try 
	    {

	    	Util.sendFromGMail(aMails, Util.MAIL_SUBJECT + _Date, Util.MAIL_BODY + _Date, System.getProperty("java.io.tmpdir") + _Date+".xlsx", _Date+".xlsx");
			
		    // intentamos borrarlo
	 	     File _TmpExcel = new File(System.getProperty("java.io.tmpdir") + _Date+".xlsx");
	 	     if (_TmpExcel.canWrite() && _TmpExcel.exists())
	 	    	_TmpExcel.delete();
	 	     
	 	    out.println("OK");
	    }
	    
 	     
		 catch (Exception e)
		{
			System.out.println (e.getMessage());
			
		}
  }
	  		
  

   
  
%>