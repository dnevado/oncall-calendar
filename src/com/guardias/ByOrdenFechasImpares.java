package com.guardias;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

public class ByOrdenFechasImpares implements Comparator<Date>{
 

	@Override
	public int compare(Date c1, Date c2) {
		// TODO Auto-generated method stub
		// IMPAR?
		if((c1.getDate() % 2==1)){
            return -1;
        } else {
            return 1;
        }
	}
}