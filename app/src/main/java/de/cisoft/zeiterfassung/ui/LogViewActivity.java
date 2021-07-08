package de.cisoft.zeiterfassung.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.cisoft.utility.log.MyLog;
import de.cisoft.zeiterfassung.MainActivity;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LogViewActivity extends Activity {
	private final static DateFormat DF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);
	private TextView logTextView;
	private TextView bookingTextView;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        logTextView.setText(getLogs());
        bookingTextView.setText(getBookings("bookings.dat"));
	}
	
	private void initView() {
		setContentView(R.layout.activity_show_logs);
		logTextView = (TextView) findViewById(R.id.logsTextView);
		bookingTextView = (TextView) findViewById(R.id.bookingsTextView);
	}
	
	public static String getLogs() {
		StringBuilder logs = new StringBuilder();
		Process process;
		try {
			process = Runtime.getRuntime().exec("logcat -d -v time");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		BufferedReader bufferedReader = new BufferedReader(
		new InputStreamReader(process.getInputStream()));
		String line = "";
		try {
			while ((line = bufferedReader.readLine()) != null) {
				logs.append(line).append('\n');
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return logs.toString();
	}
	
	private String getBookings(String fileName) {
		
		FileInputStream fis;
		try {
			fis = Settings.getCurrentContext().openFileInput(fileName);
		} catch (FileNotFoundException e1) {
			return "===== BOOKINGS FILE NOT FOUND =====";
		}
		
		StringBuffer sb = new StringBuffer();
		File file = Settings.getCurrentContext().getFileStreamPath(fileName);
		long lm = file.lastModified();
		sb.append(DF.format(lm));
		sb.append("\n====================================\n");
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		int blockLength=1024;  
		char[] buffer = new char[blockLength];
		
		int charCount=blockLength+1;
		while (charCount>=blockLength) {
			try {
				charCount = br.read(buffer);
				if (charCount>=blockLength) {
					sb.append(new String(buffer));
				} else { if (charCount>0)
					sb.append(new String(buffer,0,charCount));
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	
	public void sendMail(View view) {
		String text = "###############   LOGS ##########\n"+
	                  logTextView.getText() + 
	                  "\n\n############### BOOKINGS ##########\n" +
	                  bookingTextView.getText()+
	                  "\n\n############### BOOKINGS.BAK ######\n" + 
	                  getBookings("bookings.bak");
		MyLog.i("LogViewActivity","Sending mail");
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"waldemar.cichon.de@gmail.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, "Logs vom "+DF.format(new Date()));
		i.putExtra(Intent.EXTRA_TEXT   , text);
		try {
		    startActivity(Intent.createChooser(i, "Mail versenden"));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(this, "Keine Empfänger vorhanden oder sonstiger Fehler.", Toast.LENGTH_SHORT).show();
		}
	}
}
