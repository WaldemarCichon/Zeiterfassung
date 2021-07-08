package de.cisoft.utility.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.*;

import de.cisoft.utility.log.MyLog;

import android.util.Log;

public class FtpClient {
	private final String serverAddress;
	private FTPClient ftp;
	private final String user;
	private final String password;

	public FtpClient(String serverAddress, String user, String password) throws IOException {
		this.serverAddress = serverAddress;
		this.user = user;
		this.password = password;
		createClient();
	}
	
	public void upload(InputStream stream, String remoteFileName) throws IOException {
		//connect();
		send(stream, remoteFileName);
		//close();
	}
	
	public void download(String remoteFile, OutputStream stream) throws IOException {
		//connect();
		receive(remoteFile, stream);
		//close();
	}
	
	private void createClient() throws IOException {
		ftp = new FTPClient(); 
		ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
	}
	
	public void connect() throws IOException, IOException {
		ftp.connect(serverAddress);
		ftp.login(user, password);
		if (!isCompleted()) {
			closeWithError();
		}
        ftp.setFileType(FTP.ASCII_FILE_TYPE);
        ftp.enterLocalPassiveMode();
	}
	
	private void closeWithError() throws IOException {
		if (ftp.isConnected()) {
			ftp.disconnect();
		}
		throw new FtpCouldNotConnectException(ftp);
	}
	
	private boolean isCompleted() {
		//return FTPReply.isPositiveCompletion(ftp.getReplyCode());
		MyLog.d("FtpClient", ftp.getReplyCode()+" - "+ftp.getReplyString());
		return true;
	}
	
	private void send(InputStream stream, String remoteFileName) throws IOException {
		ftp.storeFile(remoteFileName, stream);
	}
	
	private void receive(String remoteFileName, OutputStream stream) throws IOException {
		ftp.retrieveFile(remoteFileName, stream);
	}
	
	/*
	private void close() throws IOException {
		ftp.logout();
	}
	*/
	
	public void disconnect()  {
		try {
			if (ftp.isConnected()) {
				ftp.disconnect();
			}
		} catch (Exception ex) {
			// swallow every exception
		}
	}

	public String getReplyString() {
		// TODO Auto-generated method stub
		return ftp.getReplyString();
	}

	public boolean changeDirectory(String pathname)  {
		try {
			return ftp.changeWorkingDirectory(pathname);
		} catch (IOException e) {
			return false;
		}
	}

	public boolean createDirectory(String pathname) {
		try {
			return ftp.makeDirectory(pathname);
		} catch (IOException e) {
			return false;
		}
	}

	public String[] getSubdirectories() throws IOException {
		// TODO Auto-generated method stub
		FTPFile[] directories = ftp.listDirectories();
		String[] directoryNames = new String[directories.length];
		int i=0;
		for (FTPFile directory : directories) {
			directoryNames[i++] = directory.getName();
		}
		return directoryNames;
	}
}
