package com.guardias.cambios;

import java.io.Serializable;
import java.util.Date;

import com.guardias.Util.eTipoCambiosGuardias;

public class CambiosGuardias implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8421008190352222412L;
	private Long IdCambio = new Long(1);
	private Long  IdMedicoSolicitante = new Long(-1);
	private String FechaIniCambio;
	private String FechaFinCambio;
	private String FechaCreacion;
	private String FechaAprobacion;
	private String Estado;
	private String TipoCambio;
	private Long  IdMedicoDestino = null;
	private Long  IdServicio=  new Long(1);
	
	
	public String getEstado() {
		return Estado;
	}
	public String getTipoCambio() {
		return TipoCambio;
	}
	public void setTipoCambio(String tipoCambio) {
		TipoCambio = tipoCambio;
	}
	public Long getIdMedicoDestino() {
		return IdMedicoDestino;
	}
	public void setIdMedicoDestino(Long idMedicoDestino) {
		IdMedicoDestino = idMedicoDestino;
	}
	public void setEstado(String estado) {
		Estado = estado;
	}
	public String getFechaIniCambio() {
		return FechaIniCambio;
	}
	public void setFechaIniCambio(String fechaIniCambio) {
		FechaIniCambio = fechaIniCambio;
	}
	public String getFechaFinCambio() {
		return FechaFinCambio;
	}
	public void setFechaFinCambio(String fechaFinCambio) {
		FechaFinCambio = fechaFinCambio;
	}

	public Long getIdMedicoSolicitante() {
		return IdMedicoSolicitante;
	}
	public void setIdMedicoSolicitante(Long idMedicoSolicitante) {
		IdMedicoSolicitante = idMedicoSolicitante;
	}
	private Long  UsuarioAprobacion = new Long(1); 
	public Long getUsuarioAprobacion() {
		return UsuarioAprobacion;
	}
	public void setUsuarioAprobacion(Long usuarioAprobacion) {
		UsuarioAprobacion = usuarioAprobacion;
	}
	private String  Observaciones="";
	
	
	public Long getIdCambio() {
		return IdCambio;
	}
	public void setIdCambio(Long idCambio) {
		IdCambio = idCambio;
	}
	
	public String getFechaCreacion() {
		return FechaCreacion;
	}
	public void setFechaCreacion(String fechaCreacion) {
		FechaCreacion = fechaCreacion;
	}
	public String getFechaAprobacion() {
		return FechaAprobacion;
	}
	public void setFechaAprobacion(String fechaAprobacion) {
		FechaAprobacion = fechaAprobacion;
	}

	public String getObservaciones() {
		return Observaciones;
	}
	public void setObservaciones(String observaciones) {
		Observaciones = observaciones;
	}
	public Long getIdServicio() {
		return IdServicio;
	}
	public void setIdServicio(Long idServicio) {
		IdServicio = idServicio;
	}
	

	
}
