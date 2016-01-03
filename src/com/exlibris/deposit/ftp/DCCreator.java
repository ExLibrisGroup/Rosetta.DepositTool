package com.exlibris.deposit.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import com.exlibris.core.infra.common.shared.dataObjects.KeyValuePair;
import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.core.sdk.formatting.DublinCoreFactory;
import com.exlibris.core.sdk.utils.FileUtil;


public class DCCreator extends LogObject {

	public void createIE(List<String> filenames, List<KeyValuePair<String, String>> metadata) throws NullPointerException {

		logTitle("STEP 1 - CREATING A Dublin Core xml");

		if ((filenames == null) || (filenames.size() == 0)) {
			log("No files received");
			throw new NullPointerException("No files received");
		}

		if ((metadata == null) || (metadata.size() == 0)) {
			log("No metadata received");
			throw new NullPointerException("No metadata received");
		}


		File depositDirectory = new File(DepositProperties.getValue(DepositProperties.DEPOSIT_TEMP_DIR));
		File contentDirectory = new File(depositDirectory + "/content/");
		File streamsDirectory = new File(contentDirectory + "/streams/");

		try {

			log("Creating temporary directories");

			contentDirectory.mkdirs();

			// delete output directory
			try {
				FileUtil.deleteDirectory(contentDirectory);
			} catch (Exception e) {
			}
			streamsDirectory.mkdirs();

			// add dc
			log("Adding descriptive metadata");
			DublinCore dc = DublinCoreFactory.getInstance().createDocument();
			for (KeyValuePair<String, String> entry : metadata) {
				dc.addElement(entry.getKey(), entry.getValue());
			}

			log("Adding files");
			for (String filename : filenames) {
				File file = new File(filename);

				log("Adding file: '" + file.getName() + "' to temp directory");
				FileUtil.copyFile(file, new File(streamsDirectory + File.separator + file.getName()));
				dc.addElement("dc:identifier", file.getName());

			}



			// write DC to file
			log("Writing dc xml in the temporary directory");
			File outputFile = new File(contentDirectory + File.separator + "dc.xml");
			if (outputFile.exists()) {
				outputFile.delete();
			}

			OutputStream outputStream = new FileOutputStream(outputFile);
			outputStream.write(dc.toXml().getBytes());
			outputStream.close();

			log("Succesfully created the dc.xml");
		} catch (Exception e) {
			log("Error occurred: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
