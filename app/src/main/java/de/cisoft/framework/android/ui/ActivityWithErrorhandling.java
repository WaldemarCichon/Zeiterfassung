package de.cisoft.framework.android.ui;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public abstract class ActivityWithErrorhandling extends Activity {
	public static final String EXCEPTION = "exception";
	public static final String MACHINE = "machine";
	private static final char SINGLE_LINE_SEPARATOR = '\n';
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		createErrorHandler();
		buildLayout();
		super.onCreate(savedInstanceState);
	}
	
	protected void buildLayout() {

	}
	
	private void createErrorHandler() {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			public void uncaughtException(Thread thread, Throwable ex) {
				onError(thread, ex);
			}
		});
	}
	
	private void fillStackTrace(Throwable t, StringBuffer sb) {
		sb.append(t.getClass()).append(SINGLE_LINE_SEPARATOR);
		if (t.getMessage()!=null) {
			sb.append(t.getMessage()).append(SINGLE_LINE_SEPARATOR);
		}
		StackTraceElement[] err = t.getStackTrace();
		for (StackTraceElement errElement : err) {
			sb.append(errElement.toString());
			sb.append(SINGLE_LINE_SEPARATOR);
		}
	}
	
	private void onError(final Thread thread, final Throwable ex) {
		Intent exceptionIntent = new Intent(ActivityWithErrorhandling.this, GlobalExceptionHandler.class);
		StringBuffer report = new StringBuffer();
		fillStackTrace(ex, report);
		if (ex.getCause()!=null) {
			report.append("-------------------\n");
			report.append("Root cause\n");
			fillStackTrace(ex.getCause(), report);
			
		}
		exceptionIntent.putExtra(MACHINE, getAppInfo()+SINGLE_LINE_SEPARATOR+SINGLE_LINE_SEPARATOR+getMachineInfo());
		exceptionIntent.putExtra(EXCEPTION, report.toString());
        exceptionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        exceptionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(exceptionIntent);
		System.exit(0);
	}
	
	private String getMachineInfo() {
		StringBuffer report = new StringBuffer();
	    report.append("--------- Device ---------\n\n");
	    report.append("Brand: ");
	    report.append(Build.BRAND);
	    report.append(SINGLE_LINE_SEPARATOR);
	    report.append("Device: ");
	    report.append(Build.DEVICE);
	    report.append(SINGLE_LINE_SEPARATOR);
	    report.append("Model: ");
	    report.append(Build.MODEL);
	    report.append(SINGLE_LINE_SEPARATOR);
	    report.append("Id: ");
	    report.append(Build.ID);
	    report.append(SINGLE_LINE_SEPARATOR);
	    report.append("Product: ");
	    report.append(Build.PRODUCT);
	    report.append(SINGLE_LINE_SEPARATOR);
	    report.append(SINGLE_LINE_SEPARATOR);
	    report.append("--------- Firmware ---------\n\n");
	    report.append("SDK: ");
	    report.append(Build.VERSION.SDK_INT);
	    report.append(SINGLE_LINE_SEPARATOR);
	    report.append("Release: ");
	    report.append(Build.VERSION.RELEASE);
	    report.append(SINGLE_LINE_SEPARATOR);
	    report.append("Incremental: ");
	    report.append(Build.VERSION.INCREMENTAL);
	    report.append(SINGLE_LINE_SEPARATOR);
	    return report.toString();
	}
	
	private String getAppInfo() {
		StringBuffer report = new StringBuffer();
		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			report.append("\n\n-------- Application ----------\n\n");
			report.append("Version:");
			report.append(this.getString(R.string.version));
			report.append(SINGLE_LINE_SEPARATOR);
			report.append("Build-Version: ");
			report.append(info.versionCode);
			report.append(", ");
			report.append(info.versionName);
			report.append(SINGLE_LINE_SEPARATOR);
			report.append("Last updated: ");
			report.append((new Date(info.lastUpdateTime)).toString());
			report.append(SINGLE_LINE_SEPARATOR);
			report.append("User: ");
			report.append(Settings.getInstance().getUserMailAddress());
			report.append(SINGLE_LINE_SEPARATOR);
			report.append("Suffix: ");
			report.append(Settings.getInstance().getSuffix());
			return report.toString();
		} catch (Exception ex) {
			return "Getting application's info not possible because of:\n"+ex.toString();
		}
	}
}
