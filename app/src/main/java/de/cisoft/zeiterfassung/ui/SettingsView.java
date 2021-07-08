package de.cisoft.zeiterfassung.ui;

import java.io.IOException;

import de.cisoft.framework.android.ui.ActivityWithErrorhandling;
import de.cisoft.framework.webservice.Webservice;
import de.cisoft.utility.FTPAccess;
import de.cisoft.utility.Saveable;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.icons.Icons;
import de.cisoft.zeiterfassung.implementation.entity.Actions;
import de.cisoft.zeiterfassung.implementation.entity.Projects;
import de.cisoft.zeiterfassung.implementation.entity.Tasks;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SettingsView extends ActivityWithErrorhandling implements Saveable {
	private static final int NONE = 0;
	private Menu menu;
	private Settings settings;
	private EditText etServerAddress;
	private EditText etMailAddress;
	private RadioGroup rgSelectBehavior;
	private RadioGroup rgSendBehavior;
	private RadioGroup rgTaskSelectBehavior;
	private CheckBox cbShowComment;
	private CheckBox cbTaskChoiceIcons;
	private EditText etDeviceId;
	private String wsVersion;
	private CheckBox cbShowTaskEndPanel;

	@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_settings);
	        //ActionBar actionBar = get; 
	        createView();
	        
	    }


		@Override
	    public boolean onCreateOptionsMenu(Menu menu) {

	        getMenuInflater().inflate(R.menu.activity_settings, menu);
	        this.menu = menu;
	        return super.onCreateOptionsMenu(menu);
	    }
	    
		
	    private void createView() {
	    	this.settings = Settings.getInstance();
	    	
	    	etServerAddress = (EditText) this.findViewById(R.id.etServerAddress);
	    	String serverAddress = settings.getServerPath();
	    	if (serverAddress.contains("-")) {
	    		serverAddress = Settings.SWG_DEFAULT_SERVER_PATH;
			}
	    	etServerAddress.setText(serverAddress);
	    	
	    	etMailAddress = (EditText) this.findViewById(R.id.etMailAddress);
	    	etMailAddress.setText(settings.getUserMailAddress());
	    	
	    	etDeviceId = (EditText) this.findViewById(R.id.etDeviceId);
	    	etDeviceId.setText(Integer.toString(settings.getDeviceId()));
	    	
	    	rgSelectBehavior = (RadioGroup) this.findViewById(R.id.rgChoiceKind);
	    	rgSelectBehavior.check(settings.getProjectSelectBehavior().getButtonId());
	    	
	    	rgSendBehavior = (RadioGroup) this.findViewById(R.id.rgSendKind);
	    	rgSendBehavior.check(settings.getSendBehavior().getButtonId());
	    	
	    	rgTaskSelectBehavior = (RadioGroup) this.findViewById(R.id.rgTaskChoiceKind);
	    	rgTaskSelectBehavior.check(settings.getTaskSelectBehavior().getButtonId());
	    	
	    	cbShowComment = (CheckBox) this.findViewById(R.id.cbVuewComment);
	    	cbShowComment.setChecked(settings.showComment());
	    	
	    	cbTaskChoiceIcons = (CheckBox) this.findViewById(R.id.cbTaskChoiceIcons);
	    	cbTaskChoiceIcons.setChecked(settings.isTaskChoiceIcons());
	    	
	    	cbShowTaskEndPanel = (CheckBox) this.findViewById(R.id.cbShowTaskEndPanel);
	    	cbShowTaskEndPanel.setChecked(settings.isUseTaskEndPanel());
	    }
	    
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	    	Integer id = item.getItemId();
	    	switch (id) {
	    		case R.id.menu_load_data: loadData(); break;
	    		case R.id.menu_save_settings: saveSettings(); break;
	    		case R.id.menu_test: doTest(); break;
	    		case R.id.menu_load_icons: loadIcons(); break;
	    		case R.id.cancel: cancel(); break;
	    		case R.id.menu_download: download(); break;
	    		case R.id.menu_upload: upload(); break;
	    	}
	    	return true;
	    }
	    
	    private void download() {
	    	Intent uploadChooser = new Intent(this, ActivityUploadChooser.class);
	    	startActivityForResult(uploadChooser, NONE);
	    }
	    
	    private void upload() {
	    	try {
				new FTPAccess().upload();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }


	    public void save() {
	    	saveSettings();
	    }
	    
	    private void saveSettings() {
	    	settings.setServerPath(etServerAddress.getText().toString());
	    	settings.setUserMailAddress(etMailAddress.getText().toString()); 
	    	settings.setDeviceId(Integer.parseInt(etDeviceId.getText().toString()));
	    	settings.setProjectSelectBehavior(rgSelectBehavior.getCheckedRadioButtonId());
	    	settings.setSendBehavior(rgSendBehavior.getCheckedRadioButtonId());
	    	settings.setTaskSelectBehavior(rgTaskSelectBehavior.getCheckedRadioButtonId());
	    	settings.setShowComment(cbShowComment.isChecked());
	    	settings.setTaskChoiceIcons(cbTaskChoiceIcons.isChecked());
	    	settings.setUseTaskEndPanel(cbShowTaskEndPanel.isChecked());
	    	settings.writeSettings();
	    }
	    
		private void loadData() {
			EntitiesFactory.loadData(this, 
					etServerAddress.getText().toString(), 
					etMailAddress.getText().toString());
	    	Intent in = new Intent();
	    	in.putExtra("exit", true);
	    	setResult(Activity.RESULT_CANCELED, in);
			//finish();
		}

		
		private void showAlertDialog(String s) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(s);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
				
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		
		private void doTest() {
			//saveSettings();
			Webservice.init(
					Settings.getInstance().getWebServicePath(etServerAddress.getText().toString()),
					Settings.getInstance().getNamespace());
			
			Webservice service = Webservice.getInstance();
			String v = null;
			try {
				v = service.call("Version");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (v!=null) {
				showAlertDialog("Serververbindung geglückt!");
			} else {
				showAlertDialog("Server nicht gefunden");
			}
			//this.wsVersion = v;
			
		}

		private void loadIcons() {
			Icons icons = Icons.getInstance();
			icons.fillRemoteIcons();
			icons.loadMissingIcons();
		}
		

		private void cancel() {
			// TODO Auto-generated method stub
			setResult(RESULT_CANCELED);
			finish();
		}
		
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (resultCode == RESULT_CANCELED || data!=null && data.getBooleanExtra("EXIT", false)) {
				setResult(RESULT_CANCELED, data);
				finish();
			}
		}
}
