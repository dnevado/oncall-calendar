package com.guardias.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.guardias.Guardias;
import com.guardias.Medico;
import com.guardias.Util;


public class GuardiasDBImpl {

	
	public static  boolean UpdateGuardiasFechayCalId(String CalIdGA, String FGuardia, Long IdGuardia, Long Festivo, String TipoGuardia) 
	 {	  
		
		 
	   
	  PreparedStatement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
	  
	  
			  
	  
	  String stSQL = "UPDATE guardias_medicos SET fguardia = (?) , IDEventoGCalendar=(?), Festivo=(?),Tipo=(?)" +
	   " WHERE id=(?)"		  ;
	  
	  
	  
	  
	  
	 try
	 {
	 
		  stmt = MiConexion.prepareStatement(stSQL);
		  stmt.setString(1, FGuardia);
		  stmt.setString(2, CalIdGA);
		  stmt.setLong(3, Festivo);
		  stmt.setString(4, TipoGuardia);
		  stmt.setLong(5, IdGuardia);	
		  	   
	
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
	
	
	public static  boolean UpdateGuardiasCalId(String EventIdCal, String FGuardia) 
	 {	  
		
		 
	   
	  PreparedStatement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
	  
	  
			  
	  
	  String stSQL = "UPDATE guardias_medicos SET ideventogcalendar = (?) " +
	   " WHERE fguardia=(?)"		  ;
	  
	  
	  
	  
	  
	 try
	 {
	 
		  stmt = MiConexion.prepareStatement(stSQL);
		  stmt.setString(1, EventIdCal);
		  stmt.setString(2, FGuardia);	
		  	   
	
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
	
	
	public static  boolean DeleteGuardia(Long IdMedico, String Day)
	 {	  
		
		 
	  List<Medico> lMedicos = new ArrayList<Medico>();	 
		 
	  PreparedStatement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
	  
	  
			  
	  
	  String stSQL = "DELETE FROM guardias_medicos WHERE FGuardia=(?)";
	  
	  if (!IdMedico.equals(new Long(-1)))
	  {
			stSQL+=" and  IdMedico=" + IdMedico;
	  }
	  	   
	 try
	 {
	 
		 stmt = MiConexion.prepareStatement(stSQL);
		 stmt.setString(1, Day);
		  /* if (!IdMedico.equals(new Long(-1)))
		  {
			  stmt.setLong(2, IdMedico);
		  }*/
		  
		  
			  	  
	
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
	
	public static  boolean AddGuardia(Guardias _oG)
	 {	  
				 	  
		 
	  PreparedStatement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
	  
	  
	  String stSQL = "INSERT INTO guardias_medicos (IdMedico , FGuardia , Festivo, Tipo, IdEventoGCalendar " +
	  ") VALUES (" +
	  " (?) ,  (?),  (?) , (?), (?) ) ";
	  	  
	  
	  
	  
	 try
	 {
	 
		  stmt = MiConexion.prepareStatement(stSQL);
		  stmt.setLong(1, _oG.getIdMedico());
		  stmt.setString(2, _oG.getDiaGuardia());	
		  stmt.setLong(3, _oG.isEsFestivo());
		  stmt.setString(4, _oG.getTipo());
		  stmt.setString(5, _oG.getIdEventoGCalendar());
		  
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
	
	public static  List<Guardias>  getGuardiasEventoGCalendar(String CalId)
	{
		return getGuardias(new Long(-1), "", "", new Long(-1),CalId, "");
				
	}
	
	public static  List<Guardias>  getGuardiasMedicoFecha(Long IdMedico, String _FGuardia)
	{
		return getGuardias(IdMedico, _FGuardia, "", new Long(-1),"", "");
				
	}
	public static  List<Guardias>  getGuardiasFechaTipo(String _FGuardia, String _TipoGuardia)
	{
		return getGuardias(new Long(-1), _FGuardia, _TipoGuardia, new Long(-1),"", "");
				
	}
	public static  List<Guardias>  getGuardiasMedicoFechaTipo(Long IdMedico, String _FGuardia, String _TipoGuardia)
	{
		return getGuardias(IdMedico, _FGuardia, _TipoGuardia, new Long(-1),"", "");
				
	}
	public static  List<Guardias>  getReportGuardiasEntreFechasMedico(String _FGuardia, String _FGuardia2,Long MedicoIdFilter)
	{
		return getReportGuardiasEntreFechasyMedico( _FGuardia, _FGuardia2, MedicoIdFilter);
	}
	public static  List<Guardias>  getReportGuardiasEntreFechas(String _FGuardia, String _FGuardia2)
	{
		return getReportGuardiasEntreFechasyMedico( _FGuardia, _FGuardia2, new Long(-1));
	}
	
	private static  List<Guardias>  getReportGuardiasEntreFechasyMedico(String _FGuardia, String _FGuardia2, Long MedicoIdFilter)
	 {
		 {	  
				
			 
			  List<Guardias> lGuardias = new ArrayList<Guardias>();	 
				 
			  Statement stmt = null;	 
			  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
				
			  	  
			  
			  try {
				  
				
				  
				stmt = MiConexion.createStatement();
				
				
				/* 1.presencias, localizadas y residentes */ 
				StringBuilder _sbSQL = new StringBuilder("SELECT count(*) AS ID, R.Tipo, Festivo, R.IdMedico, G.Tipo as eTipo,G.apellidos FROM guardias_medicos R,medicos  G ");				
				_sbSQL.append(" WHERE  R.idmedico = G.id  AND fGuardia>='" + _FGuardia + "' and fGuardia<='" + _FGuardia2 + "'");
				if (!MedicoIdFilter.equals(new Long(-1)))
						_sbSQL.append(" AND R.idmedico = " + MedicoIdFilter);
				_sbSQL.append(" GROUP BY  R.Tipo, Festivo, R.IdMedico");
				/* 2. simulados  */
				_sbSQL.append(" UNION  ");
				_sbSQL.append(" SELECT  count(*), 'SIMULADO','', e.IdMedico,'" + Util.eTipo.ADJUNTO.toString() + "',M.apellidos from guardias_medicos t, medicos M,");
				_sbSQL.append(" (SELECT *  FROM guardias_medicos as R  WHERE  R.tipo='" + Util.eTipoGuardia.PRESENCIA.toString().toLowerCase() + "'  AND fGuardia>='" + _FGuardia + "'"); 
				if (!MedicoIdFilter.equals(new Long(-1)))
					_sbSQL.append(" AND R.idmedico = " + MedicoIdFilter);
				_sbSQL.append(" and fGuardia<='" + _FGuardia2 + "')  e ");
				_sbSQL.append(" WHERE  e.fGuardia = t.fGuardia and t.tipo='' and M.SubTipo='" + Util.eTipoGuardia.SIMULADO.toString() + "' and t.idmedico = M.ID");
				_sbSQL.append(" GROUP BY e.IdMedico");
				_sbSQL.append(" ORDER  BY  eTipo, IdMedico, Festivo");
				
	
			//	stmt = MiConexion.prepareStatement(_sbSQL. toString());
				
				System.out.println(_sbSQL.toString());
				ResultSet rs = stmt.executeQuery(_sbSQL.toString());
				
				
				
				
		       while ( rs.next() ) {
		    	  
		    	 Guardias oGuardias = new Guardias(); 
		    	  
		         int id = rs.getInt("id");
		         int idmedico  = rs.getInt("idmedico");
		         String  tipo = rs.getString("tipo");         
		         int festivo   = rs.getInt("festivo");
		         oGuardias.setID(new Long(id));
		         oGuardias.setIdMedico(new Long(idmedico));
		         oGuardias.setTipo(tipo);
		         oGuardias.setEsFestivo(new Long(festivo));
		         lGuardias.add(oGuardias);
		      
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
			
			  return lGuardias;    		 
			  } 
	 }
	
	public static  List<Guardias>  getGuardiasEntreFechas(String _FGuardia, String _FGuardia2)
	 {
		 return getGuardias(new Long(-1), _FGuardia,  "", new Long(-1),"",_FGuardia2);
	 }
	
	 public static  List<Guardias>  getGuardiasPorFecha(String _FGuardia)
	 {
		 return getGuardias(new Long(-1), _FGuardia,  "", new Long(-1),"","");
	 }
	 
	 /* HISTORICO 
	  * _TipoGuardia --> presencia, localizada , refuerzo
	  * 	si es residente, ""
	  * */
	 public static  int  getTotalGuardiasPorMedicoTipo(Long IdMedico, String _TipoGuardia, Long Festivo)
	 {
		 return getGuardias(IdMedico, "", _TipoGuardia,Festivo,"","").size();
	 }
	 
	 public static  int  getTotalGuardiasPorMedicoTipoEntreFechas(Long IdMedico, String _TipoGuardia, Long Festivo, String _FGuardia, String _FGuardia2)
	 {
		 return getGuardias(IdMedico, _FGuardia, _TipoGuardia,Festivo,"",_FGuardia2).size();
	 }
	 
	 public static  int  getTotalGuardiasPorMedico_DeSimulados(Long IdMedico,  Long Festivo, String FGuardia, String FGuardia2)
	 {
		 return getGuardiasSimulados(IdMedico, Festivo,FGuardia, FGuardia2);
	 }	 
	
	 
	 public static  List<Guardias>  getGuardiasPorMedico(Long IdMedico)
	 {
		 return getGuardias(IdMedico, "", "", new Long(-1),"", "");
	 }	  
	 
	 private  static  int  getGuardiasSimulados(Long IdMedico, Long Festivo, String FGuardia, String _FGuardiaHasta)
	 {
		 Connection MiConexion =ConexionGuardias.GetConexionGuardias();
			int total = 0;
		  try {
			  
			
			  Statement stmt = null;	   
			stmt = MiConexion.createStatement();
			
			String stSQL= "SELECT  COUNT(*) total  FROM guardias_medicos g1, medicos m1,guardias_medicos g2, medicos m2 "
					+ " WHERE  g1.idmedico = m1.id and m1.tipo='" + Util.eTipo.ADJUNTO + "' and g1.tipo='" + Util.eTipoGuardia.PRESENCIA.toString().toLowerCase() + "'"					
					+ " AND g2.fguardia=g1.fguardia AND g2.tipo='' and m2.id = g2.idmedico and m2.subtipo='" + Util.eSubtipoResidente.SIMULADO + "'";
			
			if (!IdMedico.equals(new Long(-1)))
			{
				stSQL+=" AND m1.Id=" + IdMedico;
			}			
			if (!Festivo.equals(new Long(-1)))
			{
				stSQL+=" AND  g1.Festivo=" + Festivo;
			}
			if (!FGuardia.equals("")) 
				if (!_FGuardiaHasta.equals(""))
				{
					stSQL+=" and  g1.FGuardia>='" + FGuardia + "' and g1.FGuardia<='" + _FGuardiaHasta + "'";
					stSQL+=" and  g2.FGuardia = g1.FGuardia";	
				}				
				else
				{
					stSQL+=" and  g1.FGuardia='" + FGuardia + "' and  g2.FGuardia = g1.FGuardia";
				}
			
			
			ResultSet rs = stmt.executeQuery( stSQL);
			
		
	       while ( rs.next() ) {
	    	  
	    	 Guardias oGuardias = new Guardias(); 
	    	  
	         total = rs.getInt("total");
	      
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
		
		  return total;    		 
		  } 
		 	
	 //public static  int  getTotalGuardiasPorMedicoTipoEntreFechas(Long IdMedico, String _TipoGuardia, Long Festivo, String _FGuardia, String _FGuardia2)
	 
	 // para saber que asignar a los nuevos para el historico del año 
	 public  static  int  getMediaTotalGuardiasTipoEntreFechas( Util.eTipo TipoMedico, String TipoGuardia ,Long Festivo, String FGuardia, String _FGuardiaHasta)
	 {
		 Connection MiConexion =ConexionGuardias.GetConexionGuardias();
		 int total = 0;
		 try {
			  
			
			Statement stmt = null;	   
			stmt = MiConexion.createStatement();
			
			String stSQL= "SELECT  count(g1.Id) / count(DISTINCT m1.ID)  total  FROM guardias_medicos g1, medicos m1 "
					+ " WHERE  g1.idmedico = m1.id and m1.tipo='" + TipoMedico + "' and g1.tipo='" + TipoGuardia.toString().toLowerCase() + "'";										
						
			if (!Festivo.equals(new Long(-1)))
			{
				stSQL+=" AND  g1.Festivo=" + Festivo;
			}
			if (!FGuardia.equals("")) 
				if (!_FGuardiaHasta.equals(""))
				{
					stSQL+=" and  g1.FGuardia>='" + FGuardia + "' and g1.FGuardia<='" + _FGuardiaHasta + "'";					
				}				
				else
				{
					stSQL+=" and  g1.FGuardia='" + FGuardia;
				}
			
			
			ResultSet rs = stmt.executeQuery( stSQL);
			
		
	       while ( rs.next() ) {
	    	  
	    	 Guardias oGuardias = new Guardias(); 
	    	  
	         total = rs.getInt("total");
	      
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
		
		  return total;    		 
		  }
	 
	 
	
	 private  static  List<Guardias>  getGuardias(Long IdMedico, String _FGuardia , String _TipoGuardia, Long Festivo, String EventID, String _FGuardiaHasta)
	 {	  
		
		 
	  List<Guardias> lGuardias = new ArrayList<Guardias>();	 
		 
	  Statement stmt = null;	 
	  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
		
	  	  
	  
	  try {
		  
		
		  
		stmt = MiConexion.createStatement();
		 
		
		/* NECESITO ORDEM '', LOCALIZADA, REFUERZO Y PRESENCIA 
		 * NO VALE POR ID POR LOS CAMBIOS DE GUARDIA QUE LO ALTERAN 
		 * */
		
		String stSQL= "SELECT *, CASE WHEN Tipo = 'localizada' THEN 'flocalizada' WHEN Tipo = 'refuerzo' THEN 'flocalizada' WHEN Tipo='' THEN 'guardiaresidente'  ELSE Tipo END as Sorted ";
		stSQL +=" FROM guardias_medicos WHERE 1=1";
		
		
		if (!IdMedico.equals(new Long(-1)))
		{
			stSQL+=" AND IdMedico=" + IdMedico;
		}
		if (!_FGuardia.equals(""))
				if (!_FGuardiaHasta.equals(""))
					stSQL+=" and  FGuardia>='" + _FGuardia + "' and FGuardia<='" + _FGuardiaHasta + "'";
				else
					stSQL+=" and  FGuardia='" + _FGuardia + "'";
		
		if (!_TipoGuardia.equals(""))		 
			stSQL+=" AND Tipo = '" + _TipoGuardia + "'";
		
		if (!Festivo.equals(new Long(-1)))
		{
			stSQL+=" AND  Festivo=" + Festivo;
		}
		if (!EventID.equals("")) {				
			stSQL+=" AND  IdEventoGCalendar='" + EventID  + "'";
		}
		
		//stSQL+=" ORDER BY Id desc";
		
		stSQL+=" ORDER BY Sorted ";
		
		ResultSet rs = stmt.executeQuery( stSQL);
		
		
       while ( rs.next() ) {
    	  
    	 Guardias oGuardias = new Guardias();  
    	  
         int id = rs.getInt("id");
         int idmedico  = rs.getInt("idmedico");
         String  fguardia = rs.getString("fguardia");         
         String  tipo = rs.getString("tipo");         
         int festivo   = rs.getInt("festivo");
         String  calId   = rs.getString("ideventogcalendar");         
         
         oGuardias.setID(new Long(id));
         oGuardias.setIdMedico(new Long(idmedico));
         oGuardias.setDiaGuardia(fguardia);
         oGuardias.setTipo(tipo);
         oGuardias.setEsFestivo(new Long(festivo));
         oGuardias.setIdEventoGCalendar(calId);
         
         lGuardias.add(oGuardias);
         
         
      
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
	
	  return lGuardias;    		 
	  } 
	
	 
	 public static  Medico  getUltimoIDMedico()
	 {	  
			
		 
		  Medico Medicos = new Medico();	 
			 
		  Statement stmt = null;	 
		  Connection MiConexion =ConexionGuardias.GetConexionGuardias();
			
		  	  
		  
		  try {
			  
			
			  
			stmt = MiConexion.createStatement();
			
			String stSQL= "SELECT max(ID) ID FROM medicos";
						
			
			ResultSet rs = stmt.executeQuery( stSQL);
			
			
			  
			
	      while ( rs.next() ) {
	    	  
	    	 Medico MedicoDatabase = new Medico(); 
	    	  
	         int id = rs.getInt("ID");
	        	         
	         MedicoDatabase.setID(new Long(id));
	        
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
		
		  return Medicos;    		 
		  } 
	
	
	 

public static void main( String args[] )
{
	
}
}