package de.cisoft.utility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import de.cisoft.utility.ftp.FtpClient;
import de.cisoft.zeiterfassung.implementation.entity.Bookings;
import de.cisoft.zeiterfassung.implementation.entity.Janitor;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;

public class FTPAccess {
	private final String user = "ftp75045053-0";
	private final String password = "Grazyna123!";
	private final String server = "home498542773.1and1-data.host";
	private final String MPZE = "MPZE";
	private FtpClient client;
	private String prefix;
    
	public FTPAccess() throws IOException {
		client = new FtpClient(server, user, password);
		buildPrefix();
	}
	
	private void buildPrefix() {
		EntitiesFactory entitiesFactory = EntitiesFactory.getInstance();
		Janitor janitor = entitiesFactory.getJanitor();
		String mail = janitor.getEMail();
		String[] parts = mail.split("@");
		Date now = new Date();
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(now);
		String date = ""+(calendar.get(Calendar.YEAR))+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+"-"+calendar.get(Calendar.HOUR_OF_DAY)+"-"+calendar.get(Calendar.MINUTE);
		prefix = "/"+user+"/"+MPZE+"/"+parts[1]+"/"+parts[0]+"/"+date+"/";
		
	}
	
	public DirStructure createDirStructure() throws IOException {
		client.connect();
		DirStructure dirStructure = new DirStructure(client, "/"+user+"/"+MPZE);
		return dirStructure;
	}
	
	private void createDirs() {
		String[] parts = prefix.split("/");
		boolean first = true;
		for (String part : parts) {
			if (part.length() == 0) {
				continue;
			}
			if (first) {
				part = "/"+part;
				first = false;
			}
			if (!client.changeDirectory(part)) {
				client.createDirectory(part);
				client.changeDirectory(part);
			}
		}
	}
	
	public void chdir(String path) {
		client.changeDirectory(path);
		prefix = path;
	}
	
	public void chdir() {
		if (!client.changeDirectory(prefix)) {
			createDirs();
		}
	}
	
	public void upload() throws IOException {
		client.connect();
		chdir();
		EntitiesFactory.getInstance().upload(client);
		Settings.getInstance().upload(client);
		//FileInputStream is = EntitiesFactory.getInstance().getBookings().getDefaultInputStream();
		//client.upload(is, "bookings");
		client.disconnect();;
	}
	
	public void download(String path) {
		try {
			client.connect();
			chdir("/"+ user + "/" + path);        
			EntitiesFactory.getInstance().download(client);
			EntitiesFactory.clear();
			Settings.getInstance().download(client);
			Settings.clear();

			//client.download("test.txt", EntitiesFactory.getInstance().getBookings().getDefaultOutputStream());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			client.disconnect();
		}
	}
}
