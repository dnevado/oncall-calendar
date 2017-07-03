package com.guardias.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.guardias.Medico;
import com.guardias.Util;
import com.guardias.Vacaciones_Medicos;


public class VacacionesDBImpl {

	
	private static String  SQLGENERAL= "SELECT * FROM vacaciones_medicos";
	// distintos meses de vacaciones
	private static String  SQLGENERAL_MESES= "SELECT DISTINCT idmedico, EXTRACT(MONTH FROM date(fday)) fday  FROM vacaciones_medicos";
	
	private static String  SQLCOUNT= "SELECT count(*) idmedico FROM vacaciones_medicos";
	
	
	//yyyy/mm/dd
	public static  boolean AddVacacionesMedico(Long IdMedico, String Day)
	 {	  
				 	  
		 
	  PreparedStatement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
	  
	  
	  String stSQL = "INSERT INTO vacaciones_medicos (IdMedico , FDay) VALUES (" +
	  " (?) ,  (?))";
	  	  
	  
	  
	  
	 try
	 {
	 
		  stmt = MiConexion.prepareStatement(stSQL);
		  stmt.setLong(1, IdMedico);
		  stmt.setString(2, Day);	
		  		  
	  //System.out.println(stSQL + ",IdMedico:" + IdMedico + "," + 	_oMedico.getNombre() + ",setAutoCommit(true);") ;  
	
	  MiConexion.setAutoCommit(true);
	 stmt.executeUpdate();
    stmt.close();
    
    MiConexion.close();
    
    
    
	  } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				if (!MiConexion.isClosed())
					MiConexion.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	
	  return true;    		 
	  }
	
	
	public static  boolean DeleteVacacionesMedico(Long IdMedico, String Day)
	 {	  
		
		 
	  List<Medico> lMedicos = new ArrayList<Medico>();	 
		 
	  PreparedStatement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
	  
	  
			  
	  
	  String stSQL = "DELETE vacaciones_medicos WHERE IdMedico (?) and Day=(?)";
	   
	 try
	 {
	 
		 stmt = MiConexion.prepareStatement(stSQL);
		  stmt.setLong(1, IdMedico);
		  stmt.setString(2, Day);
			  	  
	
	  MiConexion.setAutoCommit(true);
	 stmt.executeUpdate();
     stmt.close();
     
     MiConexion.close();
     
     
     
	  } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				if (!MiConexion.isClosed())
					MiConexion.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	
	  return true;    		 
	  }
	
	public static  boolean DeleteVacacionesMedicoDesde(Long IdMedico, String Day)
	 {	  
		
		 
	  List<Medico> lMedicos = new ArrayList<Medico>();	 
		 
	  PreparedStatement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
	  
	  
			  
	  
	  String stSQL = "DELETE FROM vacaciones_medicos WHERE IdMedico = (?) and FDay>=(?)";
	   
	 try
	 {
	 
		 stmt = MiConexion.prepareStatement(stSQL);
		  stmt.setLong(1, IdMedico);
		  stmt.setString(2, Day);
			  	  
	
	  MiConexion.setAutoCommit(true);
	 stmt.executeUpdate();
    stmt.close();
    
    MiConexion.close();
    
    
    
	  } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				if (!MiConexion.isClosed())
					MiConexion.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	
	  return true;    		 
	  }
	
	
	public static  Vacaciones_Medicos  getTotalVacacionesMedicosDesdeHasta(Long IdMedico, String From, String To)
	 {
		List<Vacaciones_Medicos> oList = getVacacionesMedicosFiltro(IdMedico,"", From,To,SQLCOUNT); 
		return oList.get(0);
	 }
	
	public static  List<Vacaciones_Medicos>  getVacacionesMedicosDesdeHasta(Long IdMedico, String From, String To)
	 {
		return getVacacionesMedicosFiltro(IdMedico,"", From,To,SQLGENERAL); 
	 }
	public static  List<Vacaciones_Medicos>  getMesesVacacionesMedicosDesdeHasta(Long IdMedico, String From, String To)
	{		
		return getVacacionesMedicosFiltro(IdMedico,"", From,To,SQLGENERAL_MESES); 
	}
	
	public static  List<Vacaciones_Medicos>  getVacacionesMedicos(Long IdMedico, String Day )
	 {
		return getVacacionesMedicosFiltro(IdMedico,Day, "","",SQLGENERAL);
	 }
	
	 private static  List<Vacaciones_Medicos>  getVacacionesMedicosFiltro(Long IdMedico, String Day, String From, String To, String QuerySQL)
	 {	  
		
		 
	  List<Vacaciones_Medicos> lVacMedicos= new ArrayList<Vacaciones_Medicos>();	 
		 
	  Statement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
		
	  	  
	  
	  try {
		  
		
		  
		stmt = MiConexion.createStatement();
		
		String stSQL= QuerySQL;
				
		
		if (!IdMedico.equals(new Long(-1)))			
			stSQL+=" WHERE IdMedico=" + IdMedico;
		if (!Day.equals(""))
				stSQL+=" and  FDay='" + Day + "'";
		if (!From.equals("") && !To.equals(""))
			stSQL+=" and  FDay>='" + From + "' and FDay<='" + To + "'";
		ResultSet rs = stmt.executeQuery( stSQL);
		
		
		  
		
      while ( rs.next() ) {
    	  
    	 Vacaciones_Medicos oVacaciones_Medicos= new Vacaciones_Medicos(); 
    	           
         int idmedico  = rs.getInt("idmedico");
         try          
         {
	         String  Dia = rs.getString("fday");                  
	         oVacaciones_Medicos.setDiaVacaciones(Dia);
         }
         catch (Exception e)
         {
        	 
         }
         oVacaciones_Medicos.setIDMEDICO(new Long(idmedico));
          
         lVacMedicos.add(oVacaciones_Medicos);
         
         
      
      }
      rs.close();
      stmt.close();
      MiConexion.close();
      
      
      
	  } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				if (!MiConexion.isClosed())
					MiConexion.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	
	  return lVacMedicos;    		 
	  } 
	
	
	
	 public static  List<Vacaciones_Medicos>  getVacaciones_Medicos()
	 {	  
			 
			 return getVacacionesMedicos(new Long(-1), ""); 
		 
	 }
	 //return lMedicos;

public static void main( String args[] )
{
	
}
}