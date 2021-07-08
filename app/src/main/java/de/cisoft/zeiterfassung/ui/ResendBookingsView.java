package de.cisoft.zeiterfassung.ui;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import de.cisoft.framework.webservice.Webservice;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.implementation.entity.Booking;
import de.cisoft.zeiterfassung.implementation.entity.Bookings;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.ResendCallback;

public class ResendBookingsView extends Activity {
	
	Button okButton;
	Button cancelButton;
	RadioButton sendAll;
	RadioButton sendNotSent;
	EditText fromDate;
	EditText toDate;
	TextView infoText;
	ProgressBar percentageProgress;
	Calendar cal = GregorianCalendar.getInstance(Locale.GERMAN);
	private TextView percentageTextView;
	private TextView progressTextView;
	private int maxCount;
	private int sentCount;
	boolean answer=false;
	private Bookings selected;
	private Bookings toSend;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //MyLog.i("ProjectsView", "Vor Projects");
        
        setContentView(R.layout.activity_resend_booking);
        //MyLog.i("ProjectsView", "before creating projects");

        
        //MyLog.i("Main","after setContentView");
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        createView();
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_resend_bookings:
				resend();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_choose_task, menu);
        return true;
    }

	private void showAlert() {
    	AlertDialog ad = new AlertDialog.Builder(this).create();
    	ad.setCancelable(false);
    	ad.setMessage("Eines der Datumsfelder wurde nicht oder falsch ausgefüllt!");
    	ad.setTitle(("Buchungen versenden"));
    	ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	ad.show();
    }
    
    private Date getDate(TextView tv) {
    	return getDate(tv.getText().toString());
    }
    
    private Date getDate(String date) {
    	cal.clear();
    	String[] dateParts = date.split("\\.");
    	if (dateParts.length != 3) {
    		return null;
    	}
    	try {
    		int day = Integer.parseInt(dateParts[0]);
    		cal.set(Calendar.DAY_OF_MONTH,  day);
    		int month = Integer.parseInt(dateParts[1]);
    		cal.set(Calendar.MONTH, month-1);
    		int year = Integer.parseInt(dateParts[2]);
    		if (year<100) {
    			year+=2000;
    		}
    		cal.set(Calendar.YEAR, year);
    		return cal.getTime();
    	} catch (Exception ex) {
    		return null;
    	}
    }

    @SuppressWarnings("deprecation")
	private boolean showConfirmationDialog(int count) {
    	
    	AlertDialog ad = new AlertDialog.Builder(this).create();
    	String s = MessageFormat.format("{0} Buchungen gefunden,  versenden?", count);
    	ad.setMessage(s);
    	ad.setButton( DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				answer = true;
				resend();
				dialog.dismiss();
				
			}
		});

    	ad.setButton( DialogInterface.BUTTON_NEGATIVE, "Abbrechen", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				answer = false;
				dialog.dismiss();
			}
		});
    	
    	ad.show();
    	return answer;
    }
    
    private void okButtonClicked() {
    	Bookings selected = sendNotSent.isChecked() ? toSend : this.selected;
    	if (selected.getCount() > 0) {
			showConfirmationDialog(selected.getCount());
			return;
		}
		AlertDialog ad = new AlertDialog.Builder(this).create();
		ad.setTitle("Keine Buchungen zum Versenden vorhanden");
		ad.setMessage("Bei der gewünschten Auswahl wurden keine Datensätze zum Versenden gefunden");
		ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (DialogInterface dialog, int which) -> {
			dialog.dismiss();
		});
		ad.show();

    }
    
    private void resend() {
    	Bookings selected = sendNotSent.isChecked() ? toSend : this.selected;
    	maxCount = selected.getCount();
    	sentCount = 0;
    	selected.resend(Webservice.getInstance(), this.updateProgress);
    	recalc();
    }

    ResendCallback updateProgress = (int currentCount, final Booking sentBooking) -> {
    	final int percentage = currentCount * 100 / maxCount;
    	this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ResendBookingsView.this.percentageProgress.setProgress(percentage);
				ResendBookingsView.this.percentageTextView.setText("" + percentage + "%");
				ResendBookingsView.this.progressTextView.setText(sentBooking.toString());
				if (sentBooking.isSent()) {
					sentCount++;
				}
				if (currentCount == maxCount) {
					ResendBookingsView.this.displayResendEndedDialog();
				}
			}
		});
	};


    private void displayResendEndedDialog() {
		AlertDialog ad = new AlertDialog.Builder(this).create();
		String s = MessageFormat.format("Buchungen wurden nachversandt, {0} von {1} wurden erfolgreich versandt", this.sentCount, this.maxCount);
		ad.setMessage(s);
		ad.setTitle("Buchungen nachversenden");
		ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		ad.show();
	}

    private void cancelButtonClicked() {
    	finish();
    }
    
    
    
    private void createView() {
    	okButton = (Button) this.findViewById(R.id.oKButton);
    	cancelButton = (Button) this.findViewById(R.id.cancelButton);
    	fromDate = (EditText) this.findViewById(R.id.fromDate);
    	toDate = (EditText) this.findViewById(R.id.toDate);
    	sendAll = (RadioButton) this.findViewById(R.id.rbSendAll);
    	sendNotSent = (RadioButton) this.findViewById(R.id.rbSendNotSent);
    	infoText = (TextView) this.findViewById(R.id.tvResendinfo);
    	this.percentageProgress = (ProgressBar) this.findViewById(R.id.progressProgressBar);
    	this.percentageProgress.setProgress(0);
    	this.percentageTextView = (TextView) this.findViewById(R.id.tvProgressPercentage);
    	this.progressTextView = (TextView) this.findViewById(R.id.tvProgress);
    	recalc();
    	
    	sendNotSent.setChecked(true);
    	
    	okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				okButtonClicked();
			}
		});
    	cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				cancelButtonClicked();
			}
		});
    	
    	fromDate.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					recalc(v);
				}
			}
		});
    	
    	toDate.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					recalc(v);
				}
			}
		});
    }
    
    private void recalc(View view) {
    	Date date = getDate((TextView) view);
    	if (date == null) {
    		showAlert();
    		view.requestFocus();
    	}
    	recalc();
    }


    
    private void recalc() {
    	Bookings bookings = EntitiesFactory.getInstance().getBookings();
    	Date from = getDate(fromDate);
    	Date to = getDate(toDate);
    	selected = bookings.select(from, to);
    	toSend = selected.getToSend();
    	infoText.setText(""+selected.getCount()+" Buchungen im Zeitraum gefunden, davon noch "+toSend.getCount()+" ungesendet");
    }
}
