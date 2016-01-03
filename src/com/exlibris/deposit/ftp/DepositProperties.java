package com.exlibris.deposit.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class DepositProperties {

	private static String DEPOSIT_PROPERTIES_FILE = "deposit.properties";

	public static String DEPOSIT_TEMP_DIR = "deposit.temp.dir";
	
	public static final String PDS_URL = "pds.url";
	public static final String DEPOSIT_URL = "deposit.url";
	public static final String REPOSITORY_URL = "repository.url";
	public static final String BACKOFFICE_URL = "backoffice.url";

	public static final String DPS_DIR = "dps.dir";
	public static final String INSTITUTION = "institution";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String MATERIAL_FLOW = "material.flow";

	public static final String FTP_URL = "ftp.url";
	public static final String FTP_PORT = "ftp.port";
	public static final String FTP_USERNAME = "ftp.username";
	public static final String FTP_PASSWORD = "ftp.password";
	public static final String FTP_TEMP_DIR = "ftp.temp.dir";

	public static final String UI_ROOT_FOLDER = "ui.root.folder";
	public static final String UI_ICON = "ui.icon";
	public static final String UI_LOADING_IMAGE = "ui.loading.image";
	
	private static Properties depositProperties;
	
	private static void init() {

		depositProperties = new Properties();
		try {
			File file = new File(System.getProperty("user.dir") + "/" + DEPOSIT_PROPERTIES_FILE);
			InputStream is = new FileInputStream(file);
			depositProperties.load(is);
					//Thread.currentThread().getContextClassLoader().getResourceAsStream(DEPOSIT_PROPERTIES_FILE));
		} catch (Exception e) {
		}
	}
	
	public static String getValue(String key) {
		if (depositProperties == null) {
			init();
		}
		
		try {
			return depositProperties.getProperty(key);
		} catch (Exception e) {
			return null;
		}
	}
}
