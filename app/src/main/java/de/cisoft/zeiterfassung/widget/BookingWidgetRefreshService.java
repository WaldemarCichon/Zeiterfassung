package de.cisoft.zeiterfassung.widget;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.cisoft.utility.log.MyLog;
import de.cisoft.zeiterfassung.MainActivity;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.implementation.entity.Action;
import de.cisoft.zeiterfassung.implementation.entity.Actions;
import de.cisoft.zeiterfassung.implementation.entity.Booking;
import de.cisoft.zeiterfassung.implementation.entity.Bookings;
import de.cisoft.zeiterfassung.implementation.entity.Janitor;
import de.cisoft.zeiterfassung.implementation.entity.Project;
import de.cisoft.zeiterfassung.implementation.entity.Projects;
import de.cisoft.zeiterfassung.implementation.entity.Task;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class BookingWidgetRefreshService extends Service {
	private final static String LOG = "de.cisoft.zeiterfassung.widget";
	static int i=0;	
	
	
	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) { 
		Log.i(LOG, "starting service");

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
		        .getApplicationContext());

		int[] allWidgetIds = intent
		        .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
		int layout = R.layout.booking_widget_layout;
		String callingWidget = intent.getStringExtra(BookingsListWidget.CALLING_WIDGET);
		if (BookingsListWidget.BOOKINGS_LIST_WIDGET.equals(callingWidget)) {
			Log.i("Calling widget ", "Bookings list widget");
			layout = R.layout.booking_list_widget_layout;
		}
        RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),
		         layout);
		appWidgetManager.updateAppWidget(allWidgetIds, remoteViews );
	    //MyLog.i("SendBooking = ", ""+intent.getBooleanExtra(BookingsWidget.SEND_BOOKING, true));
	    
		if (intent.getBooleanExtra(BookingsWidget.SEND_BOOKING, false)) {
			Log.i(getClass().toString(), "Service - sending bookings");
			sendBookings();
			refreshBookingsList(this); //MainActivity.getLastInstance());
		}

		/*

        int displayedList = intent.getIntExtra(BookingsWidget.DISPLAYED_LIST,BookingsWidget.NONE_LIST);
        
		ComponentName thisWidget = new ComponentName(getApplicationContext(),
		        BookingsWidget.class);
		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
		MyLog.w(LOG, "From Intent" + String.valueOf(allWidgetIds.length));
		MyLog.w(LOG, "Direct" + String.valueOf(allWidgetIds2.length));


		Intent bookingListIntent = new Intent(getApplicationContext(), BookingWidgetService.class);
		bookingListIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
		bookingListIntent.putExtra(BookingsWidget.DISPLAYED_LIST, displayedList);
		i++;
		bookingListIntent.setData(Uri.parse(bookingListIntent.toUri(Intent.URI_INTENT_SCHEME)+"/A"+i));
		

	    // Register an onClickListener
		remoteViews.setRemoteAdapter (R.id.widgetListView, bookingListIntent);

		for (int widgetId : allWidgetIds) {

  			Intent clickIntent = new Intent(getApplicationContext(),
  		          BookingsWidget.class);
  		
  			//int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
  			clickIntent.setAction(BookingsWidget.ACTION_CLICK);
  			//clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
  			//          allWidgetIds);
  		    clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
  			           widgetId);
  		    clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)+"/list"));
  		    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
  		          PendingIntent.FLAG_UPDATE_CURRENT);
  		    remoteViews.setPendingIntentTemplate (R.id.widgetListView, pendingIntent);
  		    
  		    clickIntent = new Intent(getApplicationContext(), BookingsWidget.class);
  		    clickIntent.setAction(BookingsWidget.ACTION_CLICK_BUTTON);
  		    clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
			           widgetId);
  		    clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)+"/button"));
  		    pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, clickIntent,
  		          PendingIntent.FLAG_UPDATE_CURRENT);
  		    remoteViews.setOnClickPendingIntent (R.id.confirmationButton, pendingIntent);

  		    clickIntent = new Intent(getApplicationContext(), BookingsWidget.class);
  		    clickIntent.setAction(BookingsWidget.ACTION_CLICK_BACK_BUTTON);
  		    clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
			           widgetId);
  		    clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)+"/back_button"));
  		    pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 2, clickIntent,
  		          PendingIntent.FLAG_UPDATE_CURRENT);
  		    remoteViews.setOnClickPendingIntent (R.id.breadCrumBackButton, pendingIntent);
  		    
  		    clickIntent = new Intent(getApplicationContext(), BookingsWidget.class);
  		    clickIntent.setAction(BookingsWidget.ACTION_CLICK_CANCEL_BUTTON);
  		    clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
			           widgetId);
  		    clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)+"/cancel_button"));
  		    pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 3, clickIntent,
  		          PendingIntent.FLAG_UPDATE_CURRENT);
  		    remoteViews.setOnClickPendingIntent (R.id.confirmationCancelButton, pendingIntent);
  		    
  		    clickIntent = new Intent(getApplicationContext(), BookingsWidget.class);
  		    clickIntent.setAction(BookingsWidget.ACTION_CLICK_BREAK_BUTTON);
  		    clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
			           widgetId);
  		    clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)+"/break_button"));
  		    pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 4, clickIntent,
  		          PendingIntent.FLAG_UPDATE_CURRENT);
  		    remoteViews.setOnClickPendingIntent (R.id.widgetBreakButton, pendingIntent);
  		    
  		    clickIntent = new Intent(getApplicationContext(), BookingsWidget.class);
  		    clickIntent.setAction(BookingsWidget.ACTION_CLICK_WORK_END_BUTTON);
  		    clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
			           widgetId);
  		    clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)+"/work_end_button"));
  		    pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 6, clickIntent,
  		          PendingIntent.FLAG_UPDATE_CURRENT);
  		    remoteViews.setOnClickPendingIntent (R.id.widgetWorkEndButton, pendingIntent);
  		    
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
			
			
		}
		new Timer().scheduleAtFixedRate(new CounterTask(), 0L, 1000L);
		super.onStart(intent, startId);
		
		 */
	}
	
	private void sendBookings() {
		Bookings bookings = EntitiesFactory.getInstance().getBookings();
		Settings settings = Settings.getInstance();
		bookings.sendLastBooking(settings.getWebservice());
		try {
			bookings.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void refreshBookingsList(Context context) {
		Intent listRefreshIntent = new Intent(context, BookingsListWidget.class);
		listRefreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		context.sendBroadcast(listRefreshIntent);
	}
	
	private class CounterTask extends TimerTask {
		
		int i=0;
		public void run() {
			/*
			int[] allWidgetIds = intent
			        .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
			remoteViews.setTextViewText(R.id.tvWidget, "i="+(i++));
			appWidgetManager.updateAppWidget(allWidgetIds, remoteViews);
			*/
		}
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
