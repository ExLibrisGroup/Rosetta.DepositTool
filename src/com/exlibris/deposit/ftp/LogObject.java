package com.exlibris.deposit.ftp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.widgets.Text;

public abstract class LogObject {

	private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
	
	private Text log;
	
	public void setLog(Text log) {
		this.log = log;
	}
	
	private String getDate() {
		return dateFormat.format(new Date());
	}
	
	public void log(String text) {
		log.append(getDate() + ": " + text);
		log.append(System.getProperty("line.separator"));
	}
	
	public void logTitle(String title) {
		log("-------------------------------------------------------------------------");
		log(title);
		log("-------------------------------------------------------------------------");
	}
}
