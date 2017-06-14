package com.guardias.aggregate;

import java.util.Hashtable;

import com.guardias.Util;
import com.guardias.Util.eTipo;

public class ContadorAdjuntos  implements ContadorGuardias {
	 
	
	private long GuardiasMesPresencia;
	private long GuardiasMesRefuerzo;
	private long GuardiasMesLocalizada;
	
	private long GuardiasMesPresenciaFestivo;
	
	private long GuardiasMesRefuerzoFestivo;
	private long GuardiasMesLocalizadaFestivo;
	
	private long GuardiasMesDiarias;
	private long GuardiasMesFestivas;
	
	private long HistoricoGuardiasMesDiarias;
	private long HistoricoGuardiasMesFestivas;
	
	private long HistoricoGuardiasMesDiariasPresencia;
	private long HistoricoGuardiasMesDiariasRefuerzo;
	private long HistoricoGuardiasMesDiariasLocalizada;
	
	private long HistoricoGuardiasMesFestivasPresencia;
	private long HistoricoGuardiasMesFestivasRefuerzo;
	private long HistoricoGuardiasMesFestivasLocalizada;
	
	private long HistoricoFestivasSimulado;
	private long HistoricoSimulado;
	
	
	public long getHistoricoFestivasSimulado() {
		return HistoricoFestivasSimulado;
	}
	public void setHistoricoFestivasSimulado(long historicoFestivasSimulado) {
		HistoricoFestivasSimulado = historicoFestivasSimulado;
	}
	public long getHistoricoSimulado() {
		return HistoricoSimulado;
	}
	public void setHistoricoSimulado(long historicoSimulado) {
		HistoricoSimulado = historicoSimulado;
	}
	
	public long getGuardiasMesPresencia() {
		return GuardiasMesPresencia;
	}
	public void setGuardiasMesPresencia(long guardiasMesPresencia) {
		GuardiasMesPresencia = guardiasMesPresencia;
	}
	public long getGuardiasMesRefuerzo() {
		return GuardiasMesRefuerzo;
	}
	public void setGuardiasMesRefuerzo(long guardiasMesRefuerzo) {
		GuardiasMesRefuerzo = guardiasMesRefuerzo;
	}
	public long getGuardiasMesLocalizada() {
		return GuardiasMesLocalizada;
	}
	public void setGuardiasMesLocalizada(long guardiasMesLocalizada) {
		GuardiasMesLocalizada = guardiasMesLocalizada;
	}
	public long getGuardiasMesPresenciaFestivo() {
		return GuardiasMesPresenciaFestivo;
	}
	public void setGuardiasMesPresenciaFestivo(long guardiasMesPresenciaFestivo) {
		GuardiasMesPresenciaFestivo = guardiasMesPresenciaFestivo;
	}
	public long getGuardiasMesRefuerzoFestivo() {
		return GuardiasMesRefuerzoFestivo;
	}
	public void setGuardiasMesRefuerzoFestivo(long guardiasMesRefuerzoFestivo) {
		GuardiasMesRefuerzoFestivo = guardiasMesRefuerzoFestivo;
	}
	public long getGuardiasMesLocalizadaFestivo() {
		return GuardiasMesLocalizadaFestivo;
	}
	public void setGuardiasMesLocalizadaFestivo(long guardiasMesLocalizadaFestivo) {
		GuardiasMesLocalizadaFestivo = guardiasMesLocalizadaFestivo;
	}
	public long getHistoricoGuardiasMesDiariasPresencia() {
		return HistoricoGuardiasMesDiariasPresencia;
	}
	public void setHistoricoGuardiasMesDiariasPresencia(long historicoGuardiasMesDiariasPresencia) {
		HistoricoGuardiasMesDiariasPresencia = historicoGuardiasMesDiariasPresencia;
	}
	public long getHistoricoGuardiasMesFestivasRefuerzo() {
		return HistoricoGuardiasMesFestivasRefuerzo;
	}
	public void setHistoricoGuardiasMesFestivasRefuerzo(long historicoGuardiasMesFestivasRefuerzo) {
		HistoricoGuardiasMesFestivasRefuerzo = historicoGuardiasMesFestivasRefuerzo;
	}
	public long getHistoricoGuardiasMesFestivasLocalizada() {
		return HistoricoGuardiasMesFestivasLocalizada;
	}
	public void setHistoricoGuardiasMesFestivasLocalizada(long historicoGuardiasMesFestivasLocalizada) {
		HistoricoGuardiasMesFestivasLocalizada = historicoGuardiasMesFestivasLocalizada;
	}
	
	public long getHistoricoGuardiasMesDiariasRefuerzo() {
		return HistoricoGuardiasMesDiariasRefuerzo;
	}
	public void setHistoricoGuardiasMesDiariasRefuerzo(long historicoGuardiasMesDiariasRefuerzo) {
		HistoricoGuardiasMesDiariasRefuerzo = historicoGuardiasMesDiariasRefuerzo;
	}
	public long getHistoricoGuardiasMesDiariasLocalizada() {
		return HistoricoGuardiasMesDiariasLocalizada;
	}
	public void setHistoricoGuardiasMesDiariasLocalizada(long historicoGuardiasMesDiariasLocalizada) {
		HistoricoGuardiasMesDiariasLocalizada = historicoGuardiasMesDiariasLocalizada;
	}
	public long getHistoricoGuardiasMesFestivasPresencia() {
		return HistoricoGuardiasMesFestivasPresencia;
	}
	public void setHistoricoGuardiasMesFestivasPresencia(long historicoGuardiasMesFestivasPresencia) {
		HistoricoGuardiasMesFestivasPresencia = historicoGuardiasMesFestivasPresencia;
	}
	public long getGuardiasMesDiarias() {
		return GuardiasMesDiarias;
	}
	public void setGuardiasMesDiarias(long guardiasMesDiarias) {
		GuardiasMesDiarias = guardiasMesDiarias;
	}
	public long getGuardiasMesFestivas() {
		return GuardiasMesFestivas;
	}
	public void setGuardiasMesFestivas(long guardiasMesFestivas) {
		GuardiasMesFestivas = guardiasMesFestivas;
	}
	public long getHistoricoGuardiasMesDiarias() {
		return HistoricoGuardiasMesDiarias;
	}
	public void setHistoricoGuardiasMesDiarias(long historicoGuardiasMesDiarias) {
		HistoricoGuardiasMesDiarias = historicoGuardiasMesDiarias;
	}
	public long getHistoricoGuardiasMesFestivas() {
		return HistoricoGuardiasMesFestivas;
	}
	public void setHistoricoGuardiasMesFestivas(long historicoGuardiasMesFestivas) {
		HistoricoGuardiasMesFestivas = historicoGuardiasMesFestivas;
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
