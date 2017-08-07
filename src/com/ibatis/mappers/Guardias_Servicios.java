package com.ibatis.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.guardias.Guardias;
import com.guardias.Util;

public interface Guardias_Servicios {
	
	Guardias_Servicios getGuardias_ServiciosById(int id);
	
	
	List<Guardias_Servicios> getGuardias_ServiciosByName(Guardias_Servicios GS);

	Guardias_Servicios getMaxIDGuardias_ServiciosID();
	
	/* NOS DA EL NUMERO DE CAMBIOS HECHOS O CEDIDOS DE UN TIPO , POR UN MEDICO [idsolicitante]  Y EN UN PERIODO [fechaini / fechafincambio] 
	List<com.guardias.servicios.Guardias_Servicios> getTotalCambiosHechosPorTipoMedicoFecha(Guardias_Servicios GS); /*@Param("Inicio") String Inicio, 
			@Param("Fin") String Fin, @Param("TipoGuardia") String TipoGuardia,
			@Param("IdSolicitante") Long IdSolicitante,
			@Param("TipoCambio") Util.eTipoCambiosGuardias TipoCambio,
			@Param("Festivo") Long Festivo; 
	*/
	/* NOS DA EL NUMERO DE CAMBIOS RECIBIDOS O DISFRUTADOS  DE UN TIPO , POR UN MEDICO [iDDESTINATOARIO] ]  Y EN UN PERIODO [fechaini / fechafincambio] 
	List<com.guardias.servicios.Guardias_Servicios> getTotalCambiosRecibidosPorTipoMedicoFecha(Guardias_Servicios GS); /*@Param("Inicio") String Inicio, 
			@Param("Fin") String Fin, @Param("TipoGuardia") String TipoGuardia,
			@Param("IdDestinatorio") Long IdDestinatorio,
			@Param("TipoCambio") Util.eTipoCambiosGuardias TipoCambio,
			@Param("Festivo") Long Festivo; */
	
	void  updateGuardias_Servicios(Guardias_Servicios GS);

	
	List<Guardias_Servicios> getGuardias_ServiciosByOwner(Long OwnerGs);
	
	Guardias_Servicios getGuardias_ServiciosOfUser(Long User);
	
	List<Guardias_Servicios>  getListGuardias_ServiciosOfUser(Long User);
	
	
	
	
	void insertGuardias_Servicios(Guardias_Servicios GS);

}

