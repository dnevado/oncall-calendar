package com.ibatis.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.guardias.Guardias;
import com.guardias.Util;

public interface CambiosGuardias {
	
	CambiosGuardias getCambiosGuardiasById(int id);
	
	CambiosGuardias getMaxIDCambiosGuardiasID();
	
	/* NOS DA EL NUMERO DE CAMBIOS HECHOS O CEDIDOS DE UN TIPO , POR UN MEDICO [idsolicitante]  Y EN UN PERIODO [fechaini / fechafincambio] */
	List<com.guardias.cambios.CambiosGuardias> getTotalCambiosHechosPorTipoMedicoFecha(CambiosGuardias Cambio); /*@Param("Inicio") String Inicio, 
			@Param("Fin") String Fin, @Param("TipoGuardia") String TipoGuardia,
			@Param("IdSolicitante") Long IdSolicitante,
			@Param("TipoCambio") Util.eTipoCambiosGuardias TipoCambio,
			@Param("Festivo") Long Festivo; */
	
	/* NOS DA EL NUMERO DE CAMBIOS RECIBIDOS O DISFRUTADOS  DE UN TIPO , POR UN MEDICO [iDDESTINATOARIO] ]  Y EN UN PERIODO [fechaini / fechafincambio] */
	List<com.guardias.cambios.CambiosGuardias> getTotalCambiosRecibidosPorTipoMedicoFecha(CambiosGuardias Cambio); /*@Param("Inicio") String Inicio, 
			@Param("Fin") String Fin, @Param("TipoGuardia") String TipoGuardia,
			@Param("IdDestinatorio") Long IdDestinatorio,
			@Param("TipoCambio") Util.eTipoCambiosGuardias TipoCambio,
			@Param("Festivo") Long Festivo; */
	
	void  updateCambiosGuardias(CambiosGuardias cambio);
	
	List<CambiosGuardias> getCambiosGuardiasBySolicitante(int solicitante);
	
	
	List<CambiosGuardias> getCambioGuardiasByMedicoSolicitanteYFecha(CambiosGuardias Cambio);
	
	List<CambiosGuardias> getCambioGuardiasByMedicoSolicitanteYFechaYEstado(CambiosGuardias Cambio);
	
	List<CambiosGuardias> getCambioGuardia();
	
	
	
	void insertCambiosGuardias(CambiosGuardias cambio);

}

