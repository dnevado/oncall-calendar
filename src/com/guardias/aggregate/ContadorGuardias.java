/**
 * 
 */
package com.guardias.aggregate;

import java.util.Hashtable;

import com.guardias.Util;

/**
 * @author DAVIDNEVADO
 *
 */
public interface  ContadorGuardias {
	
	
	
	
	/* private Hashtable<Long,String> _ldaysGuardia = new Hashtable<Long, String>(); // 2  PRESENCIA , 1  POOL
	private Util.eTipo _tipoGuardia;*/ 
	
	public Hashtable<Long, String> get_ldaysGuardia();
	public void set_ldaysGuardia(Hashtable<Long, String> _ldaysGuardia);
	public Util.eTipo get_tipoGuardia();
	public void set_tipoGuardia(Util.eTipo _tipoGuardia);
	
	public void initContador();
	public Util.eTipo get_TipoMedico();;	
	public void set_TipoMedice(Util.eTipo _tipoMedico);	
	public Util.eSubtipoResidente get_subtipoResidente();
	public void set_subtipoResidente(Util.eSubtipoResidente _subtipoGuardia);
	public long getGuardiasMesDiarias();;
	public void setGuardiasMesDiarias(long guardiasMesDiarias);;
	public long getGuardiasMesFestivas();;
	public void setGuardiasMesFestivas(long guardiasMesFestivas);;
	public long getHistoricoGuardiasMesDiarias();;
	public void setHistoricoGuardiasMesDiarias(long historicoGuardiasMesDiarias);;
	public long getHistoricoGuardiasMesFestivas();;
	public void setHistoricoGuardiasMesFestivas(long historicoGuardiasMesFestivas);;
}
