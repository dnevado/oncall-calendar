package com.ibatis;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.guardias.cambios.CambiosGuardias;
import com.guardias.persistence.*;

public class MyBatisTest {

	public static void main(String[] args){

		String resource = "com/guardias/persistence/ConfigurationIBATIS.xml";
		try { 
			Reader reader = Resources.getResourceAsReader(resource);
			SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder().build(reader);
			SqlSession session = sqlMapper.openSession();
			 
			try{
				
				System.out.println("***********Customer.getCustomerById***************");
				
				CambiosGuardias oCambio = (CambiosGuardias) session.selectOne("CambiosGuardias.getCambiosGuardiasById", new Integer(1));
				System.out.println("Customer " + oCambio);
				
				System.out.println("***********Customer.getCustomerListByName***************");
				
				List<CambiosGuardias> lCambio = session.selectList("CambiosGuardias.getCambiosGuardiasBySolicitante", new Integer(1));
				for (CambiosGuardias Cambio  : lCambio) {
					System.out.println("Customer " + Cambio);
				}	
				
					
				System.out.println("***********Customer.insertCustomer***************");
				
				/* Customer c1 = new Customer();
				c1.setId(300);
				c1.setFirstName("pepe");
				c1.setLastName("loria");
				c1.setGender("FEMALE");
				c1.setRefereeId(10);
				session.insert("Customer.insertCustomer", c1);
				session.commit();
				*/
				
			} finally {
				session.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}