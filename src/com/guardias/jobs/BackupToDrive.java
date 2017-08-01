package com.guardias.jobs;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.guardias.Util;
import com.guardias.database.ConfigurationDBImpl;
import com.guardias.repository.*;

public class BackupToDrive implements Job {

public BackupToDrive() {
}

public void execute(JobExecutionContext context)
    throws JobExecutionException {

	RepositoryUtil oRepository = new RepositoryUtil();
	Date oDate = new Date();
	DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	System.out.println("Starting backup...");
	
	String _Prefix = ConfigurationDBImpl.GetConfiguration(Util.getoCONST_ENTORNO_PREFIJO_BACKUPS(),new Long(-1)).getValue(); 
	
	try {
		oRepository.CreateBackupDB("Guardias_" +  _Prefix + "_" + sdf.format(oDate) + ".db" , "Backup en SQLLite del software de Guardias de Médicos" , Util.MIME_TYPE_SQLLITE);
	} catch (IOException | GeneralSecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	System.out.println("Finished backup...");

}
}