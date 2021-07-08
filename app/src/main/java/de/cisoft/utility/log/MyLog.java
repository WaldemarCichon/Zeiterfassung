package de.cisoft.utility.log;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;

public class MyLog {
	private static String MAIN = "MAIN";
	private static FileLogger logger;
	

	public static FileLogger getLogger() {
		if (logger == null) {
			logger = FileLogger.getLogger();
		}
		return logger;
	}
	
	public static void d(String place, String what) {
	if (Log.isLoggable(MAIN, Log.DEBUG)) {
			Log.d(place, what);
			getLogger().log("Debug:",place, what);
		}
	}

	public static void d(String place, String what, Throwable e) {
		if (Log.isLoggable(MAIN, Log.DEBUG)) {
			Log.d(place, what, e);
			getLogger().log("Debug:",place, what, getStacktrace(e));
		}
	}
	
	public static void i(String place, String what, Throwable e) {
		if (Log.isLoggable(MAIN, Log.INFO)) {
			Log.i(place, what, e);
			getLogger().log("Info:",place, what, getStacktrace(e));
		}
	}
	
	public static void w(String place, String what) {
		if (Log.isLoggable(MAIN, Log.WARN)) {
			Log.w(place, what);
			getLogger().log("Warn:",place, what);
		}
	}
	
	public static void i(String place, String what) {
		if (Log.isLoggable(MAIN, Log.INFO)) {
			Log.i(place, what);
			getLogger().log("Info: ", place, what);
		}
	}
	
	public static void e(String place, String what) {
		if (Log.isLoggable(MAIN, Log.ERROR)) {
			Log.e(place, what);
			getLogger().log("Error: ", place, what);
		}
	}
	
	private static String getStacktrace(Throwable e) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : e.getStackTrace()) {
			sb.append(element.toString()).append('\n');
		}
		return sb.toString();
	}

	public static InputStream getLogStream() throws FileNotFoundException {
		// TODO Auto-generated method stub
		return logger.getStream();
	}

	public static void setContextIfNecessary(Context context) {
		if (logger != null) {
			return;
		}
		logger = FileLogger.getLogger(context);
	}
}
