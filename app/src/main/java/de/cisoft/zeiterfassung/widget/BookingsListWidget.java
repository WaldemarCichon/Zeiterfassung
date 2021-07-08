package de.cisoft.zeiterfassung.widget;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Timer;

import de.cisoft.framework.Entity;
import de.cisoft.utility.log.MyLog;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.R.layout;
import de.cisoft.zeiterfassung.implementation.entity.Action;
import de.cisoft.zeiterfassung.implementation.entity.Actions;
import de.cisoft.zeiterfassung.implementation.entity.Booking;
import de.cisoft.zeiterfassung.implementation.entity.Bookings;
import de.cisoft.zeiterfassung.implementation.entity.Janitor;
import de.cisoft.zeiterfassung.implementation.entity.Project;
import de.cisoft.zeiterfassung.implementation.entity.Projects;
import de.cisoft.zeiterfassung.implementation.entity.Task;
import de.cisoft.zeiterfassung.implementation.entity.Tasks;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Visibility;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class BookingsListWidget extends AppWidgetProvider {

	private static int i = 1;

	// private static Project[] projects = null;
	// private static Task[] tasks = null;
	// public static Entity[] entities = null;

	public static final String ACTION_REFRESH = "ACTION_REFRESH_WIDGET";
	public static final String CLICKED_LIST = "ID_CLICKED_LIST";
	public static final String CLICKED_ITEM = "ID_CLICKED_ITEM";
	public static final String ID = "ID_ID";
	public static final String DESCRIPTION = "ID_DESCRIPTION";
	public static final String REFRESH = "REFRESH";
	public static final int BOOKINGS_LIST = 999;

	public static final String BOOKINGS_LIST_WIDGET = "BOOKINGS_LIST_WIDGET";

	public static final String CALLING_WIDGET = "CALLING_WIDGET";
	//private Context context;


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		//this.context = context;
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		MyLog.setContextIfNecessary(context);
		MyLog.i("BookingsListWidget", "Update with appWidetId=" + appWidgetIds[0]
				+ " Context " + context);
		// Toast.makeText(context, "Update", Toast.LENGTH_SHORT).show();
		if (Settings.getInstance() == null) {
			Settings.init(context);
		}
		RemoteViews views = new RemoteViews(context.getPackageName(),
				layout.booking_list_widget_layout);
		// i++;
		ComponentName thisWidget = new ComponentName(context,
				BookingsListWidget.class);
		initWidget(context, thisWidget, views, appWidgetIds);
		

		appWidgetManager.updateAppWidget(thisWidget, views);

	}

	private void initWidget(Context context, ComponentName thisWidget,
			RemoteViews remoteViews, int[] appWidgetIds) {
		MyLog.d("initWidget", "starting service");
		
		EntitiesFactory ef = EntitiesFactory.getInstance(context);
		if (!ef.isInitialized()) {
			ef.initFromFilesystem();
		}
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);

		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
		MyLog.w("initWidget",
				"From Intent" + String.valueOf(appWidgetIds.length));
		MyLog.w("InitWidget", "Direct" + String.valueOf(allWidgetIds2.length));

		Intent bookingListIntent = new Intent(context,
				BookingListWidgetService.class);
		bookingListIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				appWidgetIds);
		bookingListIntent
				.putExtra(BookingsWidget.DISPLAYED_LIST, BOOKINGS_LIST);

		bookingListIntent.putExtra("random", 0);
		bookingListIntent.setData(Uri.parse(bookingListIntent
				.toUri(Intent.URI_INTENT_SCHEME) + "/B0"));

		remoteViews.setRemoteAdapter(R.id.bookingListWidgetListView, bookingListIntent);
		appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,
				R.id.bookingListWidgetListView);
		context.startService(bookingListIntent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		String action = intent.getAction();
		Log.i("--> Action = ", action);

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.booking_list_widget_layout);
		// views.setTextViewText(R.id.tvWidget,
		// "Hallo!"+i+intent.getStringExtra("Dödel"));

		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		ComponentName thisWidget = new ComponentName(context,
				BookingsListWidget.class);
		

		if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
			Log.i("BookingsListWidget", "Refreshing");

			int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			Log.i("Count of bookinglist widgets",""+allWidgetIds.length);
			//refreshWidget(context, appWidgetManager, allWidgetIds);
			appWidgetManager.notifyAppWidgetViewDataChanged(allWidgetIds,
					R.id.bookingListWidgetListView);
	
			appWidgetManager.updateAppWidget(allWidgetIds, views);
			//appWidgetManager.updateAppWidget(thisWidget, views);
			//Intent refreshIntent = new Intent(context, BookingWidgetRefreshService.class);
			//refreshIntent.putExtra(CALLING_WIDGET, BOOKINGS_LIST_WIDGET);
			//refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
			//context.startService(refreshIntent);
		}
	}

	private void refreshWidget (Context context,
			AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		Intent bookingListIntent = new Intent(context,
				BookingListWidgetService.class); 
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				layout.booking_list_widget_layout);
		bookingListIntent
				.putExtra(BookingsWidget.DISPLAYED_LIST, BOOKINGS_LIST);
		bookingListIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		bookingListIntent.putExtra(BookingsListWidget.REFRESH, true);
		bookingListIntent.putExtra("random", i++);
		
		bookingListIntent.setData(Uri.parse(bookingListIntent
				.toUri(Intent.URI_INTENT_SCHEME) + "/B" + i));

		
		remoteViews.setRemoteAdapter(R.id.bookingListWidgetListView, bookingListIntent);
		context.startService(bookingListIntent);
	}
}
