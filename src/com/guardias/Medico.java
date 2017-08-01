package com.guardias;

import java.util.ArrayList;

import com.guardias.Util.eSubtipoResidente;
import com.guardias.Util.eTipo;

public class Medico {
	private String Nombre;
	private Long  ID;
	private Long  IDMEDICO = new Long(1);
	
	private String Apellidos;
	private boolean Activo=true;	
	private boolean GuardiaSolo=false;  // puede hacer guardia solo 
	private eSubtipoResidente SubTipoResidente=eSubtipoResidente.R1; // tipo de residente r1 R... 
	//private boolean ResidenteSimulado; // no tiene nombre adjudicado, como comodin para las series de asignacion
	
	private eTipo Tipo = eTipo.ADJUNTO;
	
	private Long Max_Guardias = new Long(6);
	private Long Orden = new Long(1);
	
	private String Email = "";
	private String PassWord = "";
	private String Origen = "";
	
	private Long ServicioId = new Long(-1);
	private Long ActivoServicio = new Long(0);
	
	
	public Long getActivoServicio() {
		return ActivoServicio;
	}
	public void setActivoServicio(Long activoServicio) {
		ActivoServicio = activoServicio;
	}
	public String getOrigen() {
		return Origen;
	}
	public void setOrigen(String origen) {
		Origen = origen;
	}
	private boolean Administrator=false;
	private boolean Confirmado=false;

	
	private Long VacacionesMes = new Long(-1);
	
	public Long getVacacionesMes() {
		return VacacionesMes;
	}
	public void setVacacionesMes(Long vacacionesMes) {
		VacacionesMes = vacacionesMes;
	}
	public boolean isConfirmado() {
		return Confirmado;
	}
	public void setConfirmado(boolean confirmado) {
		Confirmado = confirmado;
	}
	public boolean isAdministrator() {
		return Administrator;
	}
	public void setAdministrator(boolean administrator) {
		Administrator = administrator;
	}
	public String getPassWord() {
		return PassWord;
	}
	public void setPassWord(String passWord) {
		PassWord = passWord;
	}
	public String getEmail() {
		return Email;
	}
	public void setEmail(String email) {
		Email = email;
	}
	public Long getMax_NUM_Guardias() {
		return Max_Guardias;
	}
	public void setMax_NUM_Guardias(Long max_NUM_Guardias) {
		Max_Guardias = max_NUM_Guardias;
	}
	public Long getIDMEDICO() {
		return IDMEDICO;
	}
	public void setIDMEDICO(Long iDMEDICO) {
		IDMEDICO = iDMEDICO;
	}
	
	public Long getOrden() {
		return Orden;
	}
	public void setOrden(Long orden) {
		Orden = orden;
	}
	
	public String getNombre() {
		return Nombre;
	}
	public void setNombre(String nombre) {
		Nombre = nombre;
	}
	public String getApellidos() {
		return Apellidos;
	}
	public boolean isActivo() {
		return Activo;
	}
	public void setActivo(boolean activo) {
		Activo = activo;
	}
	public boolean isGuardiaSolo() {
		return GuardiaSolo;
	}
	public void setGuardiaSolo(boolean guardiaSolo) {
		GuardiaSolo = guardiaSolo;
	}
	public eSubtipoResidente getSubTipoResidente() {
		return SubTipoResidente;
	}
	public void setSubTipoResidente(eSubtipoResidente subTipoResidente) {
		SubTipoResidente = subTipoResidente;
	}
	public boolean isResidenteSimulado() {
		return SubTipoResidente.equals(eSubtipoResidente.SIMULADO);
	}
	
	public void setApellidos(String apellidos) {
		Apellidos = apellidos;
	}
		
	public Long getID() {
		return ID;
	}
	public void setID(Long iD) {
		ID = iD;
	}
	public eTipo getTipo() {
		return Tipo;
	}
	public void setTipo(eTipo tipo) {
		Tipo = tipo;
	}
	public Long getServicioId() {
		return ServicioId;
	}
	public void setServicioId(Long servicioId) {
		ServicioId = servicioId;
	}

}
