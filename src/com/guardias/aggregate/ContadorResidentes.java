package com.guardias.aggregate;

import java.util.Hashtable;

import com.guardias.Util;
import com.guardias.Util.eTipo;

public class ContadorResidentes  implements  ContadorGuardias {
	
	private Util.eSubtipoResidente _subtipoResidente; 
	private long GuardiasResidentesMesDiarias;
	private long GuardiasResidentesMesFestivas;
	
	private long HistoricoResidentesGuardiasMesDiarias;
	private long HistoricoResidentesGuardiasMesFestivas;
	
	public Util.eSubtipoResidente get__subtipoResidente() {
		return _subtipoResidente;
	}
	public void set_subtipoGuardia(Util.eSubtipoResidente _subtipoGuardia) {
		this._subtipoResidente = _subtipoGuardia;
	}
	public long getGuardiasMesDiarias() {
		return GuardiasResidentesMesDiarias;
	}
	public void setGuardiasMesDiarias(long guardiasMesDiarias) {
		GuardiasResidentesMesDiarias = guardiasMesDiarias;
	}
	public long getGuardiasMesFestivas() {
		return GuardiasResidentesMesFestivas;
	}
	public void setGuardiasMesFestivas(long guardiasMesFestivas) {
		GuardiasResidentesMesFestivas = guardiasMesFestivas;
	}
	public long getHistoricoGuardiasMesDiarias() {
		return HistoricoResidentesGuardiasMesDiarias;
	}
	public void setHistoricoGuardiasMesDiarias(long historicoGuardiasMesDiarias) {
		HistoricoResidentesGuardiasMesDiarias = historicoGuardiasMesDiarias;
	}
	public long getHistoricoGuardiasMesFestivas() {
		return HistoricoResidentesGuardiasMesFestivas;
	}
	public void setHistoricoGuardiasMesFestivas(long historicoGuardiasMesFestivas) {
		HistoricoResidentesGuardiasMesFestivas = historicoGuardiasMesFestivas;
	}
	@Override
	public Hashtable<Long, String> get_ldaysGuardia() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void set_ldaysGuardia(Hashtable<Long, String> _ldaysGuardia) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public eTipo get_tipoGuardia() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void set_tipoGuardia(eTipo _tipoGuardia) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void initContador() {
		// TODO Auto-generated method stub
		
	}
	
}
