package de.cisoft.utility.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import de.cisoft.zeiterfassung.MainActivity;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;

import android.content.Context;

public class FileLogger {
	private static FileLogger fileLogger;
	
	private File logger;
	
	private FileLogger(Context context)  {
		
		logger = new File(context.getFilesDir(),"mpze.log");
		if (!logger.exists()) {
			try {
				logger.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Exception at opening logger file"+e.getStackTrace());
			}
		}
	}
	
	public void log(String... logEntries) {
		append(buildString(logEntries));
	}

	private String buildString(String[] logEntries) {
		StringBuilder sb = new StringBuilder(new Date().toLocaleString()).append(" -> ");
		for (String logEntry : logEntries) {
			if (sb.length() > 0) {
				sb.append(" : ");
			}
			sb.append(logEntry);
		}
		return sb.toString();
	}
	
	private void append(String str) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(logger, true);
			bw = new BufferedWriter(fw);
			bw.write(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Exception while appending to log writer "+e.getStackTrace());
		}
		finally {
			try {
				if (bw!=null) {
					bw.close();
				}
				if (fw!=null) {
					fw.close();
				}
			} catch (IOException e) {
				throw new RuntimeException("Exception while close log writer"+e.getStackTrace());
			}
			bw = null;
			fw = null;
		}
	}

	public InputStream getStream() throws FileNotFoundException {
		
		return Settings.getCurrentContext().openFileInput(logger.getName());
	}
	
	public static FileLogger getLogger() {
		if (fileLogger == null) {
			fileLogger = new FileLogger(MainActivity.getLastInstance());
		}
		return fileLogger;
	}

	public static FileLogger getLogger(Context context) {
		// TODO Auto-generated method stub
		if (fileLogger == null) {
			fileLogger = new FileLogger(context);
		}
		return fileLogger;
	}

}
