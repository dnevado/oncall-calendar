package com.guardias.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.guardias.Medico;
import com.guardias.Util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MedicoDBImpl {

	
	
	public static  boolean AddMedicoAlServicio(Medico _oMedico)
	{
		
		 PreparedStatement stmt = null;	 
		  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
		  String stSQL2 = "INSERT INTO guardias_servicios_medicos (IdServicio , IdMedico , IsAdministrator, ActivoServicio) VALUES (" +
				  " (?),(?),(?),(?)) ";
		  try
			 {
			  
			  stmt = MiConexion.prepareStatement(stSQL2);
			  
			  stmt.setLong(1, _oMedico.getServicioId());
			  stmt.setLong(2, _oMedico.getID());
			  stmt.setLong(3, _oMedico.isAdministrator() ? new Long(1): new Long(0));
			  stmt.setLong(4, _oMedico.getActivoServicio());
			  
			  stmt.executeUpdate();
			  stmt.close();
			  	
			  MiConexion.close();
			  
			 } catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();			
					try {
						if (!MiConexion.isClosed())
						{							
							MiConexion.close();					
						}
							
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
		  return true;
		
	}
	
	public static  boolean AddMedico(Medico _oMedico)
	 {	  
				 	  
		 
	  PreparedStatement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
	  
	  
	  String stSQL = "INSERT INTO medicos (nombre , idmedico , maxguardias , " +
	  "guardiassolo , orden , apellidos , tipo , subtipo, activo, email, password, administrator,id, origen,confirmado) VALUES (" +
	  " (?),(?) ,  (?),  (?) , (?) ,  (?),  (?),  (?) ,  (?),  (?),(?), (?), (?), (?), (?)) ";
	  	  

	  String stSQL2 = "INSERT INTO guardias_servicios_medicos (IdServicio , IdMedico , IsAdministrator, ActivoServicio) VALUES (" +
			  " (?),(?),(?),(?)) ";

	  
	  
	  
	 try
	 {
		  
		  MiConexion.setAutoCommit(false);	
		 
		  stmt = MiConexion.prepareStatement(stSQL);
		  stmt.setString(1, _oMedico.getNombre());	
		  stmt.setLong(2, _oMedico.getIDMEDICO());
		  stmt.setLong(3, _oMedico.getMax_NUM_Guardias());
		  stmt.setLong(4, _oMedico.isGuardiaSolo() ? new Long(1): new Long(0));
		  stmt.setLong(5, _oMedico.getOrden());
		  stmt.setString(6, _oMedico.getApellidos());
		  stmt.setString(7, _oMedico.getTipo().toString());
		  stmt.setString(8, _oMedico.getSubTipoResidente().toString());
		  //stmt.setLong(9, _oMedico.isActivo()? new Long(1): new Long(0));
		  // 20170218 , por defecto, se dejan desactivados hasta  que no se activen por mail con la confirmacion o por el administrador
		  stmt.setLong(9,_oMedico.isActivo() ? new Long(1): new Long(0));
		  stmt.setString(10, _oMedico.getEmail());
		  stmt.setString(11, _oMedico.getPassWord());
		  stmt.setLong(12, _oMedico.isAdministrator() ? new Long(1): new Long(0));
		  stmt.setLong(13, _oMedico.getID());
		  stmt.setString(14, _oMedico.getOrigen());
		  stmt.setLong(15, _oMedico.isConfirmado() ? new Long(1): new Long(0));
	
		  
		  stmt.executeUpdate();
		  stmt.close();
		  
		  if (!_oMedico.getServicioId().equals(new Long(-1))) // viene con un servicio 
		  {
			  
		  
		  stmt = MiConexion.prepareStatement(stSQL2);
		  
		  stmt.setLong(1, _oMedico.getServicioId());
		  stmt.setLong(2, _oMedico.getID());
		  stmt.setLong(3, _oMedico.isAdministrator() ? new Long(1): new Long(0));
		  stmt.setLong(4, _oMedico.getActivoServicio());
		  
		  stmt.executeUpdate();
		  stmt.close();
		  
		  }
		  
		  //System.out.println(stSQL + ",IdMedico:" + IdMedico + "," + 	_oMedico.getNombre() + ",setAutoCommit(true);") ;  
		
		 // MiConexion.setAutoCommit(true);
		  
	
		  MiConexion.commit();
		  MiConexion.setAutoCommit(true);	
		  MiConexion.close();
    
    
    
	  } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
			try {
				if (!MiConexion.isClosed())
				{
					MiConexion.rollback();
					MiConexion.close();					
				}
					
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	
	  return true;    		 
	  }
	
	
	public static  boolean UpdateMedico(Long IdMedico, Medico _oMedico, boolean bChangedEMail)
	 {	  
		
		 
	  List<Medico> lMedicos = new ArrayList<Medico>();	 
		 
	  PreparedStatement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
	  
	  
			  
	  
	  String stSQL = "UPDATE medicos SET nombre = (?), idmedico = (?), maxguardias = (?), " +
	  "guardiassolo = (?), orden = (?), apellidos = (?), tipo = (?), subtipo= (?), activo = (?), Email=(?), Password=(?), Confirmado=(?)" +
	   " WHERE id=(?)";
	  
	  
	  String stSQL2 = "UPDATE guardias_servicios_medicos SET  ActivoServicio=(?) WHERE IdServicio=(?) and IdMedico=(?) ";
	  
	  
	  
	 try
	 {
	 
		  stmt = MiConexion.prepareStatement(stSQL);
		  stmt.setString(1, _oMedico.getNombre());	
		  stmt.setLong(2, _oMedico.getIDMEDICO());
		  stmt.setLong(3, _oMedico.getMax_NUM_Guardias());
		  stmt.setLong(4, _oMedico.isGuardiaSolo() ? new Long(1): new Long(0));
		  stmt.setLong(5, _oMedico.getOrden());
		  stmt.setString(6, _oMedico.getApellidos());
		  stmt.setString(7, _oMedico.getTipo().toString());
		  stmt.setString(8, _oMedico.getSubTipoResidente().toString());
		  if (bChangedEMail)
			  stmt.setLong(9, new Long(0));
		  else
			  stmt.setLong(9, _oMedico.isActivo()? new Long(1): new Long(0));
		  stmt.setString(10, _oMedico.getEmail());
		  stmt.setString(11, _oMedico.getPassWord());
		  stmt.setLong(12, _oMedico.isConfirmado()? new Long(1): new Long(0));
		  stmt.setLong(13,IdMedico);
	
		  
	  //System.out.println(stSQL + ",IdMedico:" + IdMedico + "," + 	_oMedico.getNombre() + ",setAutoCommit(true);") ;  
	
	  MiConexion.setAutoCommit(true);
	 stmt.executeUpdate();
     stmt.close();
     

	  
	  stmt = MiConexion.prepareStatement(stSQL2);
	  
	  stmt.setLong(1, _oMedico.getActivoServicio());
	  stmt.setLong(2, _oMedico.getServicioId());
	  stmt.setLong(3, _oMedico.getID());
	  
	  
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
	
	/* PONEMOS LOS ULTIMOS INSERTADOS PARA QUE SEAN LOS ULTIMOS EN EL SIGUIENTE MES */
	public static  boolean OrdenarSecuenciasMedicoUltimaGuardia(Long OrdenHasta, Long MaxOrden, String TipoMedico)
	 {	  
		
	  PreparedStatement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
	  
	  
			  
	  
	  String stSQL = "UPDATE medicos SET orden = orden + (?) where orden <= (?) and tipo=(?)";	   
	  
	  
	 try
	 {
	 
		  stmt = MiConexion.prepareStatement(stSQL);
		  stmt.setLong(1, MaxOrden);
		  stmt.setLong(2, OrdenHasta);	
		  stmt.setString(3, TipoMedico);
		  
	  System.out.println(stSQL) ;  
	
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
	
	 
	/* MEDICOS RESIDENTES ORDENADOS POR SIMULADO AL FINAL */
	public static  List<Medico>  getMedicosByTypeOrdenBySimulados(Util.eTipo _Tipo, Long ServicioId )
	 {	  
		return getMedicos(new Long(-1), "", _Tipo, new Long(-1), "Subtipo", ServicioId);  // Simulado los ultimos 
	 }
	  
	public static  List<Medico>  getMedicosByType(Util.eTipo _Tipo, Long ServicioId )
	 {	  
		return getMedicos(new Long(-1), "", _Tipo, new Long(-1),"",ServicioId);
	 }
	
	 public static  List<Medico>  getMedicos(Long IdMedico, Long ServicioId)
	 {	  
		return getMedicos(IdMedico, "", null, new Long(-1),"",ServicioId);
	 }
	
	
	 public static  Medico  getServicioPorDefecto(Long IdMedico)
	 {	  
		
		 Medico lReturn=null;
		 Statement stmt = null;	 
		 Connection MiConexion =ConexionGuardias.GetConexionGuardias();
		 Medico MedicoDatabase =null;
		  
		 try {
			  
			stmt = MiConexion.createStatement(); 
				
			StringBuilder stSQL = new StringBuilder("");
			
			stSQL.append("SELECT  IdServicio FROM guardias_servicios_medicos Where IdMedico=" + IdMedico + " and ActivoServicio=1 ORDER BY IdServicio DESC");
			ResultSet rs = stmt.executeQuery( stSQL.toString());
			
		    while ( rs.next() ) {
		    	 
		    	  MedicoDatabase = new Medico(); 
		    	  
		         int id = rs.getInt("IdServicio");
		         MedicoDatabase.setServicioId(new Long(id));
		         
		      
		         
		         
		   	  }
		    rs.close();
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
		 return MedicoDatabase;
		    	 
		 
		 
		 
	 }	
	 
	
	 public static  Medico  getMedicoByEmail(String Email,Long ServicioId)
	 {	  
		
		 List<Medico> lReturn=null;  		 
		 lReturn = getMedicos(new Long(-1), Email,null, new Long(-1),"",ServicioId);
		 if (lReturn!=null && lReturn.size()>0)
			 return lReturn.get(0);
		 else
			 return null;
		 
		 
	 }	
	
	 private static  List<Medico>  getMedicos(Long IdMedico, String EmailAddress,Util.eTipo _Tipo, Long Administrador, String SortField, Long ServicioId)
	 {	  
		
		 
	  List<Medico> lMedicos = new ArrayList<Medico>();	 
		 
	  Statement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
		
	  	  
	  
	  try {
		  
		
		  
		stmt = MiConexion.createStatement(); 
		
		
		
		StringBuilder stSQL = new StringBuilder("");
		
		stSQL.append("SELECT  DISTINCT medicos.*");
		
		//if (!ServicioId.equals(new Long(-1)))
		stSQL.append(",GSERVICIO.isAdministrator, GSERVICIO.ActivoServicio ");
		
		stSQL.append(" FROM medicos");
		  		  		
		//if (!ServicioId.equals(new Long(-1)))
		stSQL.append(" left join guardias_servicios_medicos GSERVICIO ");
		
		//String stSQL= "SELECT * FROM medicos";
		stSQL.append(" ON  GSERVICIO.IdMedico = medicos.Id WHERE 1=1");
		/* if (!IdMedico.equals(new Long(-1)) || !EmailAddress.equals("") || _Tipo!=null || !Administrador.equals(new Long(-1)) || !ServicioId.equals(new Long(-1)))
			stSQL.append(" WHERE 1=1");				
		*/
		if (!IdMedico.equals(new Long(-1)))
			stSQL.append(" AND id=" + IdMedico); 
		if (!EmailAddress.equals(""))
			stSQL.append(" AND email='" + EmailAddress + "'");
		if (_Tipo!=null)
			stSQL.append(" AND tipo='" + _Tipo + "'");
		if (!ServicioId.equals(new Long(-1))  && !Administrador.equals(new Long(-1)))
			stSQL.append(" AND GSERVICIO.administrator=" + Administrador);
		
		if (!ServicioId.equals(new Long(-1)))
		{
			stSQL.append(" AND GSERVICIO.IdServicio =" +  ServicioId);
		}
		if (!SortField.equals(""))
			stSQL.append(" ORDER BY " + SortField);
		
		
		//System.out.println(stSQL);
		
		ResultSet rs = stmt.executeQuery( stSQL.toString());
		
		
		  
		
      while ( rs.next() ) {
    	  
    	 Medico MedicoDatabase = new Medico(); 
    	  
         int id = rs.getInt("id");
         String  name = rs.getString("nombre");
         int idmedico  = rs.getInt("idmedico");
         int maxguardias  = rs.getInt("maxguardias");
         int guardiassolo  = rs.getInt("guardiassolo");
         int  administrator = rs.getInt("isAdministrator");
         int  confirmado = rs.getInt("confirmado");
         int  activoServicio = rs.getInt("ActivoServicio");
         
         
         int orden  = rs.getInt("orden");
         String  apellidos = rs.getString("apellidos");
         String  tipo = rs.getString("tipo") !=null ? rs.getString("tipo") : "";
         String  subtipo = rs.getString("subtipo")!=null && !rs.getString("subtipo").equals("") ? rs.getString("subtipo") : "";
         String  email = rs.getString("email")!=null && !rs.getString("email").equals("") ? rs.getString("email") : "";
         String  password = rs.getString("password")!=null && !rs.getString("password").equals("") ? rs.getString("password") : "";
         String  origen = rs.getString("origen")!=null && !rs.getString("origen").equals("") ? rs.getString("origen") : "";
         int activo  = rs.getInt("activo");
         
         MedicoDatabase.setID(new Long(id));
         MedicoDatabase.setNombre(name);
         MedicoDatabase.setActivo(activo==0 ? false : true);
         MedicoDatabase.setApellidos(apellidos);
         MedicoDatabase.setGuardiaSolo(guardiassolo==0 ? false : true);
         MedicoDatabase.setAdministrator(administrator==0 ? false : true);
         MedicoDatabase.setConfirmado(confirmado==0 ? false : true);
         MedicoDatabase.setMax_NUM_Guardias(new Long(maxguardias));
         MedicoDatabase.setIDMEDICO(new Long(idmedico));
         MedicoDatabase.setTipo(Util.eTipo.valueOf(tipo));         
         MedicoDatabase.setEmail(email);
         MedicoDatabase.setOrden(new Long(orden));
         MedicoDatabase.setPassWord(password);
         MedicoDatabase.setOrigen(origen);
         MedicoDatabase.setServicioId(ServicioId);
         MedicoDatabase.setActivoServicio(new Long(activoServicio));
         
         if (!subtipo.equals(""))
        	 MedicoDatabase.setSubTipoResidente(Util.eSubtipoResidente.valueOf(subtipo));
         
         
         lMedicos.add(MedicoDatabase);
         
         
      
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
	
	  return lMedicos;    		 
	  } 
	
	 
	 public static  Medico  getUltimoIDMedico()
	 {	  
			
		 	 
		  Statement stmt = null;	 
		  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
			
		  Medico MedicoDatabase = new Medico();
		  
		  try {
			  
			
			  
		  stmt = MiConexion.createStatement();
			
		  String stSQL= "SELECT max(ID) ID FROM medicos";
						
			
		  ResultSet rs = stmt.executeQuery( stSQL);
			
		    
		  int id =1;
		  MedicoDatabase.setID(new Long(id));
	      while ( rs.next() ) {
	    	  
	    	 //MedicoDatabase = new Medico(); 
	    	  
	          id = rs.getInt("ID");
	         
	        	 
	         
	        	         
	         MedicoDatabase.setID(new Long(id)+1);
	        
	      //   lMedicos.add(MedicoDatabase);
	         
	         
	      
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
		
		  return MedicoDatabase;    		 
		  } 
	
	
	 public static  List<Medico>  getMedicosAdministradores(Long ServicioId)
	 {	  
			 
			 return getMedicos(new Long(-1),"",null, new Long(1),"",ServicioId); 
		 
	 }

	 /* public static  List<Medico>  getMedicos(Long MedicoId, Long ServicioId)
	 {	  
			 
			 return getMedicos(MedicoId,ServicioId); 
		 
	 }*/
	 //return lMedicos;
	 
	 public static  Long  getUltimoOrden(String TipoMedico)
	 {	  
			
		 
		  Long _MX_ORDEN = new Long(99999); 	 
			 
		  Statement stmt = null;	 
		  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
			
		  	  
		  
		  try {
			  
			
			  
			stmt = MiConexion.createStatement();
			
			String stSQL= "SELECT max(orden) orden FROM medicos where tipo='" + TipoMedico + "'";
						
			
			ResultSet rs = stmt.executeQuery( stSQL);
			
			
			  
			
	      while ( rs.next() ) {
	    	  _MX_ORDEN= rs.getLong("orden");
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
		
		  return _MX_ORDEN;	        	         
    		 
		  } 
	

public static void main( String args[] )
{
	
}
}