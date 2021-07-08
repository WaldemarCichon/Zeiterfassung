package de.cisoft.utility;

import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import android.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class DirStructureNavigator {
	
	private DirStructure root;
	private DirStructure current;
	private ListView listView;
	private Context context;
	private TextView breadCrum;
	private Button backButton;
	private FTPAccess ftpAccess;

	public DirStructureNavigator(DirStructure root, Context context) {
		//this.root = root;
		this.current = root;
		this.context = context;
	}
	
	public void refresh() {
		this.listView.setAdapter(current.getAdapter(context));
		breadCrum.setText(current.getBreadCrum());
	}

	public void setList(ListView listView) {
		this.listView = listView;
		this.listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				listClicked(position);
			}

			
			
		});
	}

	public void setBreadCrum(TextView breadCrum) {

		this.breadCrum = breadCrum;
	}

	public void setBackButton(Button backButton) {
		
		this.backButton = backButton;
		this.backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				buttonClicked();
			}
		});
	}
	
	private void buttonClicked() {
		DirStructure parent = current.getParent();
		if (parent!=null) {
			current = parent;
			refresh();
		}
	}
	
	private void listClicked(int position) {
		DirStructure choosen = current.getSubdir(position);
		if (choosen == null) {
			return;
	    	
		}
		if (choosen.getSubdirCount()>0) {
			current = choosen;
			refresh();
		} else {
			ftpAccess.download(choosen.getBreadCrum());
	    	Intent in = new Intent();
	    	in.putExtra("exit", true);
	    	((Activity)context).setResult(Activity.RESULT_CANCELED, in);
	    	((Activity)context).finish();
		}
	}

	public void setFtpAccess(FTPAccess ftpAccess) {
		this.ftpAccess = ftpAccess;
	}
}
