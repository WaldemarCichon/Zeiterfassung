package de.cisoft.framework.android.ui;

import de.cisoft.framework.webservice.Webservice;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.ui.ActivityWithErrorNotifier;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ErrorPanelHandler {
	private ViewGroup panel;
	private TextView label;
	private Activity parent;

	private  static ViewGroup getPanel(Activity activity) {
		int id = ((ActivityWithErrorNotifier)activity).getPanelId();
		return (ViewGroup) activity.findViewById(id);
	}
	
	private static TextView getLabel(Activity activity) {
		int id = ((ActivityWithErrorNotifier)activity).getLabelId();
		return (TextView) activity.findViewById(id);
	}
	
	private ErrorPanelHandler(ViewGroup panel, TextView label) {
		this.panel = panel;
		this.label = label;

		this.panel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				EntitiesFactory.getInstance().getBookings().resend(Webservice.getInstance());;
			}
		});
	}

	public ErrorPanelHandler(Activity activity) {
		this(ErrorPanelHandler.getPanel(activity), ErrorPanelHandler.getLabel(activity));
		this.parent = activity;
	}
	
	public void setText(String text) {
		label.setText(text);
	}
	
	public void warn() {
		parent.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				panel.setVisibility(View.VISIBLE);
			}
		});
	}
	
	public void resetWarning() {
		parent.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				panel.setVisibility(View.GONE);
			}
		});
	}
	
	public boolean isWarning() {
		return panel.getVisibility() == View.VISIBLE;
	}
	
	public Activity getParent() {
		return parent;
	}
}
