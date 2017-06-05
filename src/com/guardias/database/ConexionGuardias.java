package com.guardias.database;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

class ConexionGuardias  
{
  
	private static Connection cConnection = null;  		
	 public static  void  CerrarConexionGuardias()	  
	  {
		  if (cConnection!=null)
		  {
			  try {
				cConnection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		  
	  }
  public static  Connection  GetConexionGuardias()
  {	  
	  if (cConnection==null)
	  {
		  Connection c = null;
		    try {
		     // Class.forName("org.sqlite.JDBC");
		    		Class.forName("com.mysql.jdbc.Driver"); 
		    	 
		      
		      Properties p = new Properties();
		      
		      InputStream i = ConexionGuardias.class.getClassLoader().getResourceAsStream("base.properties");
		      if (i!=null)
		    	  p.load(i);
		      else
		    	  throw new FileNotFoundException("base.properties");
		      // 
		      
		      //c = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("catalina.home") + "/BBDD_sqllite/guardias.db");
		      //c = DriverManager.getConnection("jdbc:sqlite:" + p.getProperty("database.path")); 
		      c= DriverManager.getConnection("jdbc:" + p.getProperty("database.path"));
		      
		     // System.out.println("Opening jdbc:sqlite:" + p.getProperty("database.path"));
		      
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(0);
		    }
		    return c;
		    //System.out.println("Opened database successfully");
	  }
	  else		  
		  return cConnection;
  }
  
	
  public static void main( String args[] )
  {
    Connection c = null;
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:d:\\test.db");
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
    System.out.println("Opened database successfully");
  }
}