package com.guardias.servicios;

import java.io.Serializable;
import java.util.Date;

import com.guardias.Util.eTipoCambiosGuardias;

public class Guardias_Servicios implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8421008190352222412L;
	private Long IdServicio = new Long(1);
	private String Nombre;
	private String Descripcion;	
	private Long Activo  = new Long(1);
	private Long Visible = new Long(1);
	private java.sql.Timestamp FechaCreacion;
	private java.sql.Timestamp  FechaModificacion;
	private Long  IdMedicoOwner = new Long(-1);
	private String  CodigoInterno = "";
	private String  MedicoOwner = "";
	private Long  TotalMiembros =new Long(0);
	private String  PathLogo = "";
	
	
	public String getMedicoOwner() {
		return MedicoOwner;
	}
	public void setMedicoOwner(String medicoOwner) {
		MedicoOwner = medicoOwner;
	}
	public Long getTotalMiembros() {
		return TotalMiembros;
	}
	public void setTotalMiembros(Long totalMiembros) {
		TotalMiembros = totalMiembros;
	}
	public String getPathLogo() {
		return PathLogo;
	}
	public void setPathLogo(String pathLogo) {
		PathLogo = pathLogo;
	}
	public Long getIdServicio() {
		return IdServicio;
	}
	public void setIdServicio(Long idServicio) {
		IdServicio = idServicio;
	}
	public String getNombre() {
		return Nombre;
	}
	public void setNombre(String nombre) {
		Nombre = nombre;
	}
	public String getDescripcion() {
		return Descripcion;
	}
	public void setDescripcion(String descripcion) {
		Descripcion = descripcion;
	}
	public Long getActivo() {
		return Activo;
	}
	public void setActivo(Long activo) {
		Activo = activo;
	}
	public Long getVisible() {
		return Visible;
	}
	public void setVisible(Long visible) {
		Visible = visible;
	}
	public java.sql.Timestamp getFechaCreacion() {
		return FechaCreacion;
	}
	public void setFechaCreacion(java.sql.Timestamp fechaCreacion) {
		FechaCreacion = fechaCreacion;
	}
	public java.sql.Timestamp getFechaModificacion() {
		return FechaModificacion;
	}
	public void setFechaModificacion(java.sql.Timestamp fechaModificacion) {
		FechaModificacion = fechaModificacion;
	}
	public Long getIdMedicoOwner() {
		return IdMedicoOwner;
	}
	public void setIdMedicoOwner(Long idMedicoOwner) {
		IdMedicoOwner = idMedicoOwner;
	}
	public String getCodigoInterno() {
		return CodigoInterno;
	}
	public void setCodigoInterno(String codigoInterno) {
		CodigoInterno = codigoInterno;
	}
	
	
	

	
}
