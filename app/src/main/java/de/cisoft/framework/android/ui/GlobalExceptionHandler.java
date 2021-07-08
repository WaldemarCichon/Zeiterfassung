package de.cisoft.framework.android.ui;

import java.util.Date;

import de.cisoft.zeiterfassung.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GlobalExceptionHandler extends Activity {
	
	private TextView etStackTrace;
	private TextView etMachineInfo;
	private Button btSendMail;
	private String stackTrace;
	private String machineInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createView();
	}
	
	private void createView() {
		setContentView(R.layout.activity_exception_handler);
		etStackTrace = (TextView) findViewById(R.id.etMainExceptionStackTrace);
		etMachineInfo = (TextView) findViewById(R.id.etMachineInfo);
		Bundle extras = this.getIntent().getExtras();
		stackTrace = extras.getString(ActivityWithErrorhandling.EXCEPTION);
		machineInfo = extras.getString(ActivityWithErrorhandling.MACHINE);
		etStackTrace.setText(stackTrace);
		etMachineInfo.setText(machineInfo);
		btSendMail = (Button) findViewById(R.id.btSendMail);
		btSendMail.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				sendMail();
			}
		});
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_settings, menu);
        return true;
    }
	
	private void sendMail() {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"waldemar.cichon.de@gmail.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, "Fehlerbericht vom "+new Date().toString());
		i.putExtra(Intent.EXTRA_TEXT   , stackTrace+"\n------------------------------\n"+machineInfo);
		try {
		    startActivity(Intent.createChooser(i, "Mail versenden"));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(this, "Keine Empfänger vorhanden oder sonstiger Fehler.", Toast.LENGTH_SHORT).show();
		}
	}
}
