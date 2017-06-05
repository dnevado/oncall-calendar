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


public class CambiosGuardiasDBImpl extends CambiosGuardias {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static  void AddCambioGuardias(CambiosGuardias oCambioG) 
	{
		SqlSession _oSession = _createSession();
		try
		{
			 //_oSession.selectOne("CambiosGuardias.insertCambiosGuardias", oCambioG);
			
			 _oSession.insert("CambiosGuardias.insertCambiosGuardias", oCambioG);
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
	
	public static  void UpdateCambioGuardias(CambiosGuardias oCambioG) 
	 {	  
		SqlSession _oSession = _createSession();
		int _return=0; 
		try
		{
			  //org.apache.ibatis.logging.LogFactory.useLog4JLogging();

			 _return =  _oSession.update("CambiosGuardias.updateCambiosGuardias", oCambioG);
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
	
	public static  int getMaxIDCambiosGuardiasID() 
	 {	  
		SqlSession _oSession = _createSession();
		CambiosGuardias olCambio = null;
		try
		{
			 olCambio =  _oSession.selectOne("CambiosGuardias.getMaxIDCambiosGuardiasID");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		
		 return (olCambio!=null ? olCambio.getIdCambio().intValue() +1 : 1);
	 }
	
	
	/* NOS DA EL NUMERO DE CAMBIOS HECHOS O CEDIDOS DE UN TIPO , POR UN MEDICO [idsolicitante]  Y EN UN PERIODO [fechaini / fechafincambio] */
	public static   List<CambiosGuardias> getTotalCambiosHechosPorTipoMedicoFecha(CambiosGuardias oCambio)
	{
		SqlSession _oSession = _createSession();
		List<CambiosGuardias> olCambio = null;
		
		/* Map<String, Object> _parametersMap = new HashMap<String, Object>();
		_parametersMap.put("Inicio", Inicio);
		_parametersMap.put("Fin", Fin);
		_parametersMap.put("IdSolicitante", IdSolicitante);
		_parametersMap.put("TipoCambio", TipoCambio);
		*/
		
		try
		{
			 olCambio =  _oSession.selectList("CambiosGuardias.getTotalCambiosHechosPorTipoMedicoFecha", oCambio);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		 return olCambio;
	}
	
	/* NOS DA EL NUMERO DE CAMBIOS RECIBIDOS O DISFRUTADOS  DE UN TIPO , POR UN MEDICO [iDDESTINATOARIO] ]  Y EN UN PERIODO [fechaini / fechafincambio] */
	public static    List<CambiosGuardias> getTotalCambiosRecibidosPorTipoMedicoFecha(CambiosGuardias oCambio)
	{
		SqlSession _oSession = _createSession();
		List<CambiosGuardias> olCambio = null;
		
		/* Map<String, Object> _parametersMap = new HashMap<String, Object>();
		_parametersMap.put("Inicio", Inicio);
		_parametersMap.put("Fin", Fin);
		_parametersMap.put("IdDestinatario", IdDestinatorio);
		_parametersMap.put("TipoCambio", TipoCambio);
		*/
		try
		{
			 olCambio =  _oSession.selectList("CambiosGuardias.getTotalCambiosRecibidosPorTipoMedicoFecha", oCambio);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		 return olCambio;
	}
	
	public static  CambiosGuardias getCambioGuardiasById(Integer IdCambio) 
	 {	  
		SqlSession _oSession = _createSession();
		CambiosGuardias olCambio = null;
		try
		{
			 olCambio =  _oSession.selectOne("CambiosGuardias.getCambiosGuardiasById", IdCambio);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		 return olCambio;
	 }
	public static  List<CambiosGuardias> getCambioGuardiasByMedicoSolicitante(Integer IdMedico) 
	 {
		SqlSession _oSession = _createSession();
		List<CambiosGuardias> olCambio = null;
		try
		{
			 olCambio =  _oSession.selectList("CambiosGuardias.getCambiosGuardiasBySolicitante", IdMedico);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		 return olCambio;
	 }
	
	
	public static  List<CambiosGuardias> getCambiosGuardia()
	 {
		SqlSession _oSession = _createSession();
		List<CambiosGuardias> olCambio = null;
		try
		{
			 olCambio =  _oSession.selectList("CambiosGuardias.getCambioGuardia");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		 finally {
			 _oSession.close();
		 }
		 return olCambio;
	 }
	
	public static List<CambiosGuardias>   getCambioGuardiasByMedicoSolicitanteYFecha(CambiosGuardias _Cambio) 
	 {	  
		SqlSession _oSession = _createSession();
		List<CambiosGuardias> olCambio = null;  
		try
		{ 
			 olCambio =  _oSession.selectList("CambiosGuardias.getCambioGuardiasByMedicoSolicitanteYFecha", _Cambio);
		}
		catch (Exception e)
		{
			
		}
		 finally {
			 _oSession.close();
		 }
		 return olCambio;
	 }
	public static List<CambiosGuardias>   getCambioGuardiasByMedicoSolicitanteYFechaYEstado(CambiosGuardias _Cambio) 
	 {	  
		SqlSession _oSession = _createSession();
		List<CambiosGuardias> olCambio = null;
		try
		{
			 olCambio =  _oSession.selectList("CambiosGuardias.getCambioGuardiasByMedicoSolicitanteYFechaYEstado", _Cambio);
		}
		catch (Exception e)
		{
			
		}
		 finally {
			 _oSession.close();
		 }
		 return olCambio;
	 }
	public static  List<CambiosGuardias> getCambioGuardiasByMedicoAprobacion(Integer IdMedicoApr) 
	 {	  
		SqlSession _oSession = _createSession();
		List<CambiosGuardias> olCambio = null;
		try
		{
			 olCambio =  _oSession.selectOne("CambiosGuardias.getCambiosGuardiasById", IdMedicoApr);
		}
		catch (Exception e)
		{
			
		}
		 finally {
			 _oSession.close();
		 }
		 return olCambio;
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
		 
	   
	  	
	 
