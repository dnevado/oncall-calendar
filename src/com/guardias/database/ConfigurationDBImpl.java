package com.guardias.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.Configuration;

import com.guardias.Configuracion;
import com.guardias.Medico;
import com.guardias.Util;
import com.guardias.Vacaciones_Medicos;


public class ConfigurationDBImpl {

	
	//yyyy/mm/dd
	
	public static  boolean UpdateConfiguracion(Configuracion _oConfiguracion)
	 {	  
		
		 
	   
	  PreparedStatement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
	  
	  
	  String stSQL = "UPDATE configuracion SET config_value = (?) " +
	   " WHERE config_IdServicio=(?) and config_key=(?)"		  ;
	  
	  
	  
	  
	  
	 try
	 {
	 
		  stmt = MiConexion.prepareStatement(stSQL);
		  stmt.setString(1, _oConfiguracion.getValue());	
		  stmt.setLong(2, _oConfiguracion.getConfig_IdServicio());
		  stmt.setString(3, _oConfiguracion.getKey());

	
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
	
	public static  boolean InicializarConfiguration(Long ServicioTo, Long ServicioFrom )
	{	  
		
		Configuracion oConfiguration= new Configuracion();
		 Statement stmt = null;	 
		 Connection MiConexion =ConexionGuardias.GetConexionGuardias();   
		 
		  
		  
		  String stSQL = " insert into configuracion select config_key,config_value," + ServicioTo + " from configuracion where config_IdServicio = "  + ServicioFrom;
		  
		  try
			 {
			 
				  stmt = MiConexion.prepareStatement(stSQL);
				 			
				  MiConexion.setAutoCommit(true);
				  stmt.executeUpdate(stSQL);
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
	
	
	public static  Configuracion GetConfiguration(String  KeyValue, Long IdServicio)
	{	  
				 	  
	
	 Configuracion oConfiguration= new Configuracion();
	 Statement stmt = null;	 
	 Connection MiConexion =ConexionGuardias.GetConexionGuardias();   
	  
	  
	  String stSQL = "SELECT * FROM configuracion where  config_key='" + KeyValue + "'  and config_IdServicio=" + IdServicio;  
	  
	 
	 try {
		 
	 //stmt.setString(1, KeyValue);
	 stmt = MiConexion.createStatement();
	 ResultSet rs = stmt.executeQuery( stSQL);
		
	
	 	 
	 
	 if (rs.next())
	 {
    	String Key = rs.getString("config_key");         
    	String  Value = rs.getString("config_value");         
    	Long  Servicio = rs.getLong("config_IdServicio");
       
    	oConfiguration.setKey(Key);
    	oConfiguration.setValue(Value);
    	oConfiguration.setConfig_IdServicio(Servicio);
    	
     }
	 
     stmt.close();   
     MiConexion.close();
    
     } 
	 catch (SQLException e) {
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
	
	 
	 return oConfiguration;
	    		
	  }
	
	
	 public static  List<Configuracion>  getConfiguraciones()
	 {	  
		
		 
	  List<Configuracion> lVConfiguration= new ArrayList<Configuracion>();	 
		 
	  Statement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
		
	  
	  try {
		  
		
		  
		stmt = MiConexion.createStatement();
		
		String stSQL= "SELECT * FROM configuracion";
		
		
		ResultSet rs = stmt.executeQuery( stSQL);
		
      while ( rs.next() ) {
    	  
    	  Configuracion oConfiguration= new Configuracion(); 
    	           
    	 	String Key = rs.getString("config_key");         
        	String  Value = rs.getString("config_value");         
        	Long  Servicio = rs.getLong("config_IdServicio");
        	
        	oConfiguration.setKey(Key);
        	oConfiguration.setKey(Value);
          oConfiguration.setConfig_IdServicio(Servicio);
        	
        	lVConfiguration.add(oConfiguration);
         
         
      
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
	
	  return lVConfiguration;    		 
	  } 
	
	
	
	 
	 //return lMedicos;

public static void main( String args[] )
{
	
}
}