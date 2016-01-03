package com.exlibris.deposit.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.sftp.SftpFileObject;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;

public class FTPUploader extends LogObject {

//	private void copyFile(FTPClient client, File dir) throws Exception {
//		client.setFileType(FTP.BINARY_FILE_TYPE);
//
//		InputStream stream;
//		Date start;
//		for (File file : dir.listFiles()) {
//			if (file.isFile()) {
//				try {
//					stream = new FileInputStream(file);
//			
//					start = new Date();
//					boolean success = client.storeFile(file.getName(), stream);
//					if (success) {
//						log("Uploaded file: " + file.getName() + " (" + (new Date().getTime() - start.getTime()) + "ms)");
//					} else {
//						log("Error occurred while uploading file: " + file.getName());
//					}
//					
//					stream.close();
//				} catch (Exception e) {
//				}
//			}
//		}
//	}
//	
//	private void copyDirectory(FTPClient client, File dir) throws Exception {
//
//		// first copy all the files
//		copyFile(client, dir);
//		
//		// copy all the subdirectories
//		for (File file : dir.listFiles()) {
//			if (file.isDirectory()) {
//				log("Creating subdirectory: " + file.getName());
//				client.makeDirectory(file.getName());
//				
//				client.changeWorkingDirectory(file.getName());
//				copyDirectory(client, file);
//				client.changeWorkingDirectory("..");
//			}
//		}
//	}
	
	public static void copyWithFtpDirectory(File srcPath, FileObject dstPath) throws IOException {

		if (srcPath.isDirectory()){
			if(!srcPath.getName().contains("CVS")){
				if (!dstPath.exists()){
					dstPath.createFolder();
				}

				String files[] = srcPath.list();

				for(int i = 0; i < files.length; i++){
					FileSystemManager fsManager = null;
					SftpFileObject ftpFile = null;
					try {
						FileSystemOptions fsOptions = new FileSystemOptions();
						SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
								fsOptions, "no");
						fsManager = VFS.getManager();
						ftpFile = (SftpFileObject)fsManager.resolveFile(dstPath.getName().getURI() + "/" + files[i],fsOptions);
						copyWithFtpDirectory(new File(srcPath, files[i]),
								ftpFile);
					} finally {
						/* remove it from cache */
						if (fsManager!=null) fsManager.getFilesCache().removeFile(ftpFile.getFileSystem(), ftpFile.getName());
						if (ftpFile!=null) {
							ftpFile.refresh();
							ftpFile.close();
						}
					}
				}
			}
		}

		else{
			if(!srcPath.exists()){
				System.out.println("File or directory does not exist.");
			} else {
				InputStream in = null;
				OutputStream out = null;
				try{
					in = new FileInputStream(srcPath);
					out = dstPath.getContent().getOutputStream();
					// Transfer bytes from in to out
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
				finally{
					in.close();
					out.close();
				}
			}
		}
		System.out.println("Directory copied.");

	}
	
	public String upload() throws Exception {

		logTitle("STEP 2 - UPLOADING CONTENT");
		
		log("Connecting to the FTP site");
		
		String ftpSubDir = "Dep" + new Date().getTime();
		String sftpUser = DepositProperties.getValue(DepositProperties.FTP_USERNAME) + ":" + 
								DepositProperties.getValue(DepositProperties.FTP_PASSWORD);
		String host = DepositProperties.getValue(DepositProperties.FTP_URL);
		String path = DepositProperties.getValue(DepositProperties.FTP_TEMP_DIR) + ftpSubDir;
		FileSystemManager fsManager = VFS.getManager();
		FileSystemOptions fsOptions = new FileSystemOptions();
		
		SftpFileObject sftpDir = (SftpFileObject)fsManager.resolveFile(
				"sftp://"+sftpUser+"@"+host+":22/"+
						path, fsOptions);
			
		log("Copying deposit directory to server");
		
		File depositDirectory = new File(DepositProperties.getValue(DepositProperties.DEPOSIT_TEMP_DIR));
		
		copyWithFtpDirectory(depositDirectory, sftpDir);
		
		
		
//		FTPSClient client = new FTPSClient();
//		client.enterLocalPassiveMode();
//		client.setBufferSize(1024 * 1024);
//		client.connect(
//				DepositProperties.getValue(DepositProperties.FTP_URL),
//				Integer.valueOf(DepositProperties.getValue(DepositProperties.FTP_PORT)));
//		log("Login using credentials: " + 
//				DepositProperties.getValue(DepositProperties.FTP_USERNAME) + "/********");
//		client.login(
//				DepositProperties.getValue(DepositProperties.FTP_USERNAME),
//				DepositProperties.getValue(DepositProperties.FTP_PASSWORD));
//		client.changeWorkingDirectory(
//				DepositProperties.getValue(DepositProperties.FTP_TEMP_DIR));
//		
//		File depositDirectory = new File(DepositProperties.getValue(DepositProperties.DEPOSIT_TEMP_DIR));
//
//		try {
//			log("Creating new deposit directory");
//			client.makeDirectory(ftpSubDir);
//
//			client.changeWorkingDirectory(ftpSubDir);
//			
//			copyDirectory(client, depositDirectory);
//			
//		} catch (Exception e) {
//			log("Error occurred: " + e.getMessage());
//			e.printStackTrace();
//		}
		
//		log("Disconnecting from the FTP site");
//		client.disconnect();
		log("Succesfully uploaded content to the FTP site");
		
		return ftpSubDir;
	}
	
	public static void main(String[] args) throws Exception {
		FTPUploader uploader = new FTPUploader();
		uploader.upload();
	}
}
