package com.guardias.database;

import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.guardias.Guardias;
import com.guardias.Medico;
import com.guardias.Util;
import com.guardias.cambios.CambiosGuardias;
import com.guardias.servicios.Guardias_Servicios;


public class Guardias_ServiciosDBImpl extends Guardias_Servicios {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static  void AddGuardias_Servicios(Guardias_Servicios GS) 
	{
		SqlSession _oSession = _createSession();
		try
		{
			 //_oSession.selectOne("CambiosGuardias.insertCambiosGuardias", oCambioG);
			
			 _oSession.insert("Guardias_Servicios.insertGuardias_Servicios", GS);
			 _oSession.commit(); 
			 //_oSession.s
			 
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }

		
		
	}
	
	public static  void UpdateCambioGuardias(Guardias_Servicios GS) 
	 {	  
		SqlSession _oSession = _createSession();
		int _return=0; 
		try
		{
			  //org.apache.ibatis.logging.LogFactory.useLog4JLogging();

			 _return =  _oSession.update("Guardias_Servicios.updateGuardias_Servicios", GS);
			 _oSession.commit(); 			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
	//	return _return;
		
	 }
	
	
	public static Guardias_Servicios getMaxIDGuardias_ServiciosID() {
		
		SqlSession _oSession = _createSession();
		Guardias_Servicios oGS = null;
		try
		{
			oGS =  _oSession.selectOne("Guardias_Servicios.getMaxIDGuardias_ServiciosID");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		 return oGS;
		
	}
	
	public static  List<Guardias_Servicios> getListAllGuardias_ServiciosOfUser(Integer Gs) 
	 {	  
		SqlSession _oSession = _createSession();
		List<Guardias_Servicios> oGS = null;
		try
		{
			oGS =  _oSession.selectList("Guardias_Servicios.getListAllGuardias_ServiciosOfUser", Gs); 
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		 return oGS;
	 }
	
	public static  List<Guardias_Servicios> getListGuardias_ServiciosOfUser(Integer Gs) 
	 {	  
		SqlSession _oSession = _createSession();
		List<Guardias_Servicios> oGS = null;
		try
		{
			oGS =  _oSession.selectList("Guardias_Servicios.getListGuardias_ServiciosOfUser", Gs); 
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		 return oGS;
	 }
	
	
	public static  Guardias_Servicios getGuardias_ServiciosOfUser(Integer Gs) 
	 {	  
		SqlSession _oSession = _createSession();
		Guardias_Servicios oGS = null;
		try
		{
			oGS =  _oSession.selectOne("Guardias_Servicios.getGuardias_ServiciosOfUser", Gs); 
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		 return oGS;
	 }
	
	public static  List<Guardias_Servicios> getGuardias_ServiciosByOwner(Integer Gs) 
	 {	  
		SqlSession _oSession = _createSession();
		List<Guardias_Servicios> oGS = null;
		try
		{
			oGS =  _oSession.selectList("Guardias_Servicios.getGuardias_ServiciosByOwner", Gs); 
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		 return oGS;
	 }
	
	
	public static   List<Guardias_Servicios>  getGuardias_ServiciosByName(Guardias_Servicios GS) 
	 {	  
		SqlSession _oSession = _createSession();
		 List<Guardias_Servicios>  oGS = null;
		try
		{
			oGS =  _oSession.selectList("Guardias_Servicios.getGuardias_ServiciosByName", GS);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		 return oGS;
	 }
	
	public static  Guardias_Servicios getGuardias_ServiciosById(Integer Gs) 
	 {	  
		SqlSession _oSession = _createSession();
		Guardias_Servicios oGS = null;
		try
		{
			oGS =  _oSession.selectOne("Guardias_Servicios.getGuardias_ServiciosById", Gs);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		 return oGS;
	 }
	
	
	
	
	 private static SqlSession _createSession()
	 {
		 	SqlSession session=null;
			String resource = "com/guardias/persistence/ConfigurationIBATIS.xml";
			try 
			{ 
				Reader reader = Resources.getResourceAsReader(resource);
				SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder().build(reader);
				session = sqlMapper.openSession(true);  // autocommit;
			
			}
			 catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return session;
			
	 }
	
}	
		 
	   
	  	
	 
