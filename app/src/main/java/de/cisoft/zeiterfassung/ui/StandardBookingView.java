package de.cisoft.zeiterfassung.ui;

import de.cisoft.framework.Entity;
import de.cisoft.framework.android.ui.ErrorNotifier;
import de.cisoft.framework.webservice.Webservice;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.R.layout;
import de.cisoft.zeiterfassung.R.menu;
import de.cisoft.zeiterfassung.implementation.entity.Action;
import de.cisoft.zeiterfassung.implementation.entity.Actions;
import de.cisoft.zeiterfassung.implementation.entity.Booking;
import de.cisoft.zeiterfassung.implementation.entity.Bookings;
import de.cisoft.zeiterfassung.implementation.entity.Janitor;
import de.cisoft.zeiterfassung.implementation.entity.Janitors;
import de.cisoft.zeiterfassung.implementation.entity.Project;
import de.cisoft.zeiterfassung.implementation.entity.Projects;
import de.cisoft.zeiterfassung.implementation.entity.Tasks;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class StandardBookingView extends Activity implements ActivityWithErrorNotifier {

	private ListView lvBookings;
	private ArrayAdapter<Booking> adapter;
	private Bookings bookings;
	private Menu menu;
	private View  selectedView;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_bookings_standard);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        bookings = EntitiesFactory.getInstance().getBookings();
        bookings.recalc();
        //settings = Settings.getInstance();
        addRadioButtonListeners();
        createUiew();
        
        ErrorNotifier.getInstance().addHandler(this);
    }
	
	private void addRadioButtonListeners() {
		
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_bookings, menu);
        this.menu = menu;
        return true;
    }

    public View getSelectedView() {
    	return selectedView;
    }
    
    private void showAlert() {
    	AlertDialog ad = new AlertDialog.Builder(this).create();
    	ad.setCancelable(false);
    	ad.setMessage("Kommt in K�rze!");
    	ad.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	ad.show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
        switch (id) {
        	case R.id.rebooking : {
        		Intent i = new Intent(this, ResendBookingsView.class);
        		startActivity(i);
        		return true;
        	}
        	case R.id.extendedBookings : {
        		showAlert();
        		return true;
        	}
            case de.cisoft.zeiterfassung.R.id.cancel: {
            	Intent i = new Intent();
            	i.putExtra("exit", false);
            	setResult(RESULT_CANCELED, i);
            	finish();
            	return true;
            }
            case de.cisoft.zeiterfassung.R.id.Exit: {
            	Intent i = new Intent();
            	i.putExtra("exit", true);
            	setResult(RESULT_CANCELED, i);
            	finish();
            	return true;
            }
            
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void createUiew() {
    	lvBookings = (ListView) this.findViewById(R.id.lvBookings);
    	adapter = bookings.getAdapter(this, R.layout.textview_booking_entry);
    	lvBookings.setAdapter(adapter);
    	/*
    	lvUsers.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				StandardBookingView.selectedPosition = position;
				StandardBookingView.selectedText = ((TextView) view).getText();
				selected = janitors.getFromSortedById(selectedPosition);
				tvStandInName.setText(selected.toString());
				selectedView = view;
				Toast.makeText(getApplicationContext(), selectedText, Toast.LENGTH_SHORT).show();
			}
		});
		*/
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	ErrorNotifier.getInstance().removeHandler(this);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK &&
    			event.getRepeatCount() == 0) {
    		Intent i = new Intent();
    		i.putExtra("exit", false);
    		setResult(RESULT_CANCELED, i);
    	}
    	return super.onKeyDown(keyCode, event);
    }

	@Override
	public int getPanelId() {
		return R.id.bookingStandardWarningPanel;
	}

	@Override
	public int getLabelId() {
		return R.id.bookingStandardWarningLabel;
	}
}
