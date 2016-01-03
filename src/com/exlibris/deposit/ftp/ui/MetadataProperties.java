package com.exlibris.deposit.ftp.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import com.exlibris.core.infra.common.shared.dataObjects.KeyValuePair;
import com.exlibris.core.sdk.strings.StringUtils;

public class MetadataProperties {

	private static String METADATA_FILE = "metadata.txt";

	private static List<KeyValuePair<String, String>> metadataFields;
	
	private static void init() {
		metadataFields = new LinkedList<KeyValuePair<String, String>>();
		
        try {
        	
        	
        	File file = new File(System.getProperty("user.dir") + "/" + METADATA_FILE);
        	BufferedReader br = new BufferedReader(new FileReader(file));
        	
//            BufferedReader br = new BufferedReader(
//            		new InputStreamReader(
//            				Thread.currentThread().getContextClassLoader().getResourceAsStream(METADATA_FILE)));
//        
            // read lines from HTML
            String line;
            String[] lineParts;
            while ((line = br.readLine()) != null) {
            	if (StringUtils.notEmptyString(line)) {
            		lineParts = line.split("=");
            		if (lineParts.length == 2) {
            			metadataFields.add(new KeyValuePair<String, String>(lineParts[0], lineParts[1]));
            		} else {
            			metadataFields.add(new KeyValuePair<String, String>(lineParts[0], ""));
            		}
            	}
            }
            
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public static List<KeyValuePair<String, String>> getMetadataFields() {
		if (metadataFields == null) {
			init();
		}
		
		return metadataFields;
	}
	
	public static String getValue(String value) {
		String[] parts = value.split(":");
		if (parts.length == 2) {
			return parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
		} else {
			return parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
		}
	}
}
