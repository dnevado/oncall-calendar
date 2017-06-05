package com.guardias.database;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.guardias.Medico;
import com.guardias.Util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SuscriptoresDBImpl {

	
	public static  boolean AddSuscriptores(String Email)
	 {	  
				 	  
		
	  DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");
	
		
	  PreparedStatement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
	  
	  
	  String stSQL = "INSERT INTO subscriptores (Email , FechaCreacion) VALUES (" +
	  " (?),(?) ) ";
	  	  
	  
	  
	  
	 try
	 {
	 
		  stmt = MiConexion.prepareStatement(stSQL);
		  stmt.setString(1, Email);	
		  stmt.setString(2, _format.format(Calendar.getInstance().getTime()));
		  
		  
	  //System.out.println(stSQL + ",IdMedico:" + IdMedico + "," + 	_oMedico.getNombre() + ",setAutoCommit(true);") ;  
	
	  MiConexion.setAutoCommit(true);
	 stmt.executeUpdate();
    stmt.close();
    
    MiConexion.close();
    
    return true;
    
    
    
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
			return false;
		}
	
	      		 
	  }
	

public static void main( String args[] )
{
	
}
}