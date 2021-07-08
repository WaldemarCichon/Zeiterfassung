package de.cisoft.zeiterfassung.ui;

import java.io.IOException;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import de.cisoft.framework.android.ui.ActivityWithErrorhandling;
import de.cisoft.utility.DirStructure;
import de.cisoft.utility.DirStructureNavigator;
import de.cisoft.utility.FTPAccess;
import de.cisoft.zeiterfassung.R;

public class ActivityUploadChooser extends ActivityWithErrorhandling {
	
	private Button backButton;
	private TextView breadCrum;
	private ListView chooserList;
	
	@Override
	protected void buildLayout() {
		setContentView(R.layout.activity_upload_chooser);
		backButton = (Button) findViewById(R.id.breadCrumBackButton);
		breadCrum = (TextView) findViewById(R.id.breadCrum);
		chooserList = (ListView) findViewById(R.id.directoyChoosenListView);
		try {
			FTPAccess ftpAccess = new FTPAccess();
			DirStructure dirs = ftpAccess.createDirStructure();
			DirStructureNavigator navigator = new DirStructureNavigator(dirs, this);
			navigator.setList(chooserList);
			navigator.setBreadCrum(breadCrum);
			navigator.setBackButton(backButton);
			navigator.setFtpAccess(ftpAccess);
			navigator.refresh();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
