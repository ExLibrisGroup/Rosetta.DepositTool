package com.exlibris.deposit.ftp.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.exlibris.core.sdk.strings.StringUtils;

public class UILabels {

	private static String UILABLES_FILE = "uilabels.txt";

	private static Map<String, String> uiLabels;

	private static void init() {
		uiLabels = new HashMap<String, String>();

        try {

        	File file = new File(System.getProperty("user.dir") + "/" + UILABLES_FILE);
        	BufferedReader br = new BufferedReader(new FileReader(file));

        			//new BufferedReader(
        			//new InputStreamReader(
            				//Thread.currentThread().getContextClassLoader().getResourceAsStream(UILABLES_FILE)));

            // read lines from HTML
            String line;
            String[] lineParts;
            while ((line = br.readLine()) != null) {
            	if (StringUtils.notEmptyString(line)) {
            		lineParts = line.split("=");
            		if (lineParts.length == 2) {
            			uiLabels.put(lineParts[0], lineParts[1]);
            		} else {
            			uiLabels.put(lineParts[0], "");
            		}
            	}
            }

        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	public static Map<String, String> getLabels() {
		if (uiLabels == null) {
			init();
		}

		return uiLabels;
	}

	public static String getLabel(String key) {
		if (uiLabels == null) {
			init();
		}

		if (uiLabels.containsKey(key)) {
			return uiLabels.get(key);
		} else {
			return "";
		}
	}
}
