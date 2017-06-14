package com.guardias;

import java.util.Comparator;

public class ByVacacionesDESCResidentes implements Comparator<Medico>{
 

	@Override
	public int compare(Medico o1, Medico o2) {
		// TODO Auto-generated method stub
		 if(o1.getVacacionesMes() <= o2.getVacacionesMes()){
	            return 1;
	        } else {
	            return -1;
	        }
	}
}