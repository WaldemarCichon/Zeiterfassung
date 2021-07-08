package de.cisoft.zeiterfassung;

import de.cisoft.framework.android.ui.ActivityWithErrorhandling;
import de.cisoft.framework.webservice.Webservice;
import de.cisoft.utility.log.MyLog;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;
import de.cisoft.zeiterfassung.ui.ProjectsView;
import de.cisoft.zeiterfassung.ui.SettingsView;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Vibrator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends ActivityWithErrorhandling implements OnClickListener {
	public static final boolean BRANDING = false;  
	public static final boolean ALTERNATING_VIEWS = false;
	private static Context  lastInstance;
	private static Vibrator vibrator;
	private Button 			button;
	private static final Long SPLASH_DURATION = 1000L;
	

    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.lastInstance = this;
        MainActivity.vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        setContentView(R.layout.activity_main);
        button = (Button) this.findViewById(R.id.btOk);
        button.setVisibility(Button.INVISIBLE);
        button.setOnClickListener(this);
        ImageView hermanImage = (ImageView) this.findViewById(R.id.HermannCommunication);
        hermanImage.setVisibility(View.GONE);
        Handler handler = new Handler();
        
        Settings.init(this);
        final Settings settings = Settings.getInstance();
        if (!settings.isInitialized()) {
        	handler.postDelayed(new Runnable() {
        		

        		@Override
        		public void run() {
        			try {
        				Webservice webservice = Webservice.getCiSoftInstance();
        				String version = webservice.call("Version");
        				if (version == null || !settings.initFromWebservice (webservice)) { 
        					Intent k = new Intent(MainActivity.this, SettingsView.class);
        					MainActivity.this.startActivity(k); // startActivityForResult(k);
        					MainActivity.this.finish();
        				} else {
        					if (!EntitiesFactory.loadData(MainActivity.this, settings.getServerPath(), settings.getUserMailAddress())) {
        						Intent k = new Intent(MainActivity.this, SettingsView.class);
            					MainActivity.this.startActivity(k);
            					finish();
        					}
        				}
        				//MainActivity.this.finish();
        				return;
        			} catch (Exception ex) {
        				throw new RuntimeException(ex);
        				//finish();
        				//return;
        			}
        		}
				
        	}
        	, SPLASH_DURATION);
        	return;

        }
//        Handler handler = new Handler();
        
        // run a thread after 2 seconds to start the home screen
        handler.postDelayed(new Runnable() {
 
            public void run() {
 
                // make sure we close the splash screen so the user won't come back when it presses back key
            	EntitiesFactory ef = EntitiesFactory.getInstance();
            	if (!ef.isInitialized()) {
            		try {
            			ef.initFromFilesystem();
            			MyLog.i("InitMain", "Load from filesystem");
            		} catch (Exception ex) {
            			MyLog.i("InitMain", "Could not load from filesystem");
            		}
            	}
                if (!ef.isInitialized()) {
                	MyLog.i("InitMain", "Load from webservice");
                	ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                	ef.initFromWebservice(progressDialog);
                	ef.initPersonalData(Settings.getInstance().getUserMailAddress(), true);
                	// !!! hier eingreifen, falls projekte leer sind. Dann nicht speichern
                	ef.save();
                	MyLog.i("InitMain", "Load from filesystem sucessfully finished");
                }
                Intent intent = new Intent(MainActivity.this, ProjectsView.class);
                MainActivity.this.startActivity(intent);
                /*
                try {
					wait(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				*/
                finish();
            }
 
        }, SPLASH_DURATION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void onClick(View v) {
	  button.setText("new text");
		MyLog.i("Main","before start activity");
		try {
		Intent k = new Intent(this, ProjectsView.class);
		startActivity(k);
		//throw new Exception ("Error"); 
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		MyLog.i("Main","After start activity");
    }
    
    
    public static void setLastInstance(Context lastInstance) {
    	MainActivity.lastInstance = lastInstance;
    }
    
	public static Context getLastInstance() {
		/*
		if (lastInstance == null) {
			lastInstance = new MainActivity();
		}
		*/
		return lastInstance;
	}

	public static Vibrator getVibrator() {
		return vibrator;
	}
}
