package com.guardias;

import java.util.ArrayList;
import java.util.List;

import com.guardias.database.MedicoDBImpl;

public class GuardiasReportingUtilContainer {

	private Long Presencias = new Long(0);
	private  Long Localizadas =  new Long(0);
	private  Long Refuerzos	 = new Long(0);
	private Long PresenciasF  = new Long(0);
	private Long LocalizadasF =   new Long(0);
	private  Long RefuerzosF =   new Long(0);
	private  Long Cesiones =   new Long(0);
	private Long TotalSimulados =   new Long(0);
	private Long IdMedico =   new Long(0);
	private String Nombre; 
	private String Apellidos;
	private String TipoMedico;
	private String SubTipoMedico;
	
	
	

	public List<GuardiasReportingUtilContainer> getTotalByDoctor(List<Guardias> lItems)
	{
		
		List<GuardiasReportingUtilContainer> lReportGuardias =  new ArrayList<GuardiasReportingUtilContainer>();
		
		Long IDMEDICO = new Long(-1);
		GuardiasReportingUtilContainer _oReport=null;
		for (int j=0;j<lItems.size();j++)
		{
			Guardias _oGuardia = lItems.get(j);
			Medico _oMedico = MedicoDBImpl.getMedicos(_oGuardia.getIdMedico()).get(0);
			
			/* VA CAMBIANDO */
			if (!IDMEDICO.equals(_oMedico.getID()))
			{
				if (_oReport!=null) 
					lReportGuardias.add(_oReport);
				
				_oReport = new GuardiasReportingUtilContainer();
				_oReport.setApellidos(_oMedico.getApellidos());
				_oReport.setNombre(_oMedico.getNombre());
				_oReport.setIdMedico(_oMedico.getID());
				_oReport.setTipoMedico(_oMedico.getTipo().toString());
			}
			
			if (_oGuardia.getTipo().toString().equalsIgnoreCase(Util.eTipoGuardia.LOCALIZADA.toString()))
			{
				if (_oGuardia.isEsFestivo().equals(new Long(1)))
					_oReport.setLocalizadasF(_oGuardia.getID());
				else
					_oReport.setLocalizadas(_oGuardia.getID());

			}
			if (_oGuardia.getTipo().toString().equalsIgnoreCase(Util.eTipoGuardia.PRESENCIA.toString()))
			{
				if (_oGuardia.isEsFestivo().equals(new Long(1)))
					_oReport.setPresenciasF(_oGuardia.getID());
				else
					_oReport.setPresencias(_oGuardia.getID());
			}
			if (_oGuardia.getTipo().toString().equalsIgnoreCase(Util.eTipoGuardia.REFUERZO.toString()))
			{
				if (_oGuardia.isEsFestivo().equals(new Long(1)))
					_oReport.setRefuerzosF(_oGuardia.getID());
				else
					_oReport.setRefuerzos(_oGuardia.getID());
			}
			if (_oGuardia.getTipo().toString().equalsIgnoreCase(Util.eTipoGuardia.SIMULADO.toString()))
			{
					_oReport.setTotalSimulados(_oGuardia.getID());
				
			}
			if (_oGuardia.getTipo().toString().equalsIgnoreCase(Util.eTipoCambiosGuardias.CESION.toString()))
			{
					_oReport.setCesiones(_oGuardia.getID());
				
			}
			// RESIDENTES 
			if (_oGuardia.getTipo().toString().equals(""))
			{
				if (_oGuardia.isEsFestivo().equals(new Long(1)))
					_oReport.setPresenciasF(_oGuardia.getID());
				else
					_oReport.setPresencias(_oGuardia.getID());
				
			}
			
			IDMEDICO  = _oMedico.getID();
			
			}
		
		// metemos el ultimo 
		if (_oReport!=null) 
			lReportGuardias.add(_oReport);
				
		return lReportGuardias;		
	}





	public Long getPresencias() {
		return Presencias;
	}





	public void setPresencias(Long presencias) {
		Presencias = presencias;
	}





	public Long getLocalizadas() {
		return Localizadas;
	}





	public void setLocalizadas(Long localizadas) {
		Localizadas = localizadas;
	}





	public Long getRefuerzos() {
		return Refuerzos;
	}





	public void setRefuerzos(Long refuerzos) {
		Refuerzos = refuerzos;
	}





	public Long getPresenciasF() {
		return PresenciasF;
	}





	public void setPresenciasF(Long presenciasF) {
		PresenciasF = presenciasF;
	}





	public Long getLocalizadasF() {
		return LocalizadasF;
	}





	public void setLocalizadasF(Long localizadasF) {
		LocalizadasF = localizadasF;
	}





	public Long getRefuerzosF() {
		return RefuerzosF;
	}





	public void setRefuerzosF(Long refuerzosF) {
		RefuerzosF = refuerzosF;
	}





	public Long getTotalSimulados() {
		return TotalSimulados;
	}





	public void setTotalSimulados(Long totalSimulados) {
		TotalSimulados = totalSimulados;
	}





	public Long getIdMedico() {
		return IdMedico;
	}





	public void setIdMedico(Long idMedico) {
		IdMedico = idMedico;
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





	public void setApellidos(String apellidos) {
		Apellidos = apellidos;
	}
					
			
			
			
	
	public String getTipoMedico() {
		return TipoMedico;
	}





	public void setTipoMedico(String tipoMedico) {
		TipoMedico = tipoMedico;
	}





	public String getSubTipoMedico() {
		return SubTipoMedico;
	}





	public void setSubTipoMedico(String subTipoMedico) {
		SubTipoMedico = subTipoMedico;
	}





	public Long getCesiones() {
		return Cesiones;
	}





	public void setCesiones(Long cesiones) {
		Cesiones = cesiones;
	}



}
