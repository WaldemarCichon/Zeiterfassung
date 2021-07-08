package de.cisoft.zeiterfassung.widget;

import java.util.Collection;

import de.cisoft.utility.log.MyLog;
import de.cisoft.zeiterfassung.implementation.entity.Booking;
import de.cisoft.zeiterfassung.implementation.entity.Project;
import de.cisoft.zeiterfassung.implementation.entity.Projects;
import de.cisoft.zeiterfassung.implementation.entity.Task;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

public class BookingListWidgetService extends RemoteViewsService {
	
	private static BookingListProvider<Booking> bookingListProvider;
	
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		//int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 
		//		AppWidgetManager.INVALID_APPWIDGET_ID);
		int displayedList = intent.getIntExtra(BookingsWidget.DISPLAYED_LIST, BookingsWidget.NONE_LIST);
		Log.i("inGetViewFactory1", "Entering for "+displayedList);
		Context context = this.getApplicationContext(); 
		//boolean refresh = intent.getBooleanExtra(BookingsListWidget.REFRESH, false);
		return getBookingListProvider(context, intent);
	}
	
	private BookingListProvider<Booking> getBookingListProvider(Context context, Intent intent) {
		MyLog.setContextIfNecessary(context);
		Log.i("BookingService", "Getting bookings");
		if (bookingListProvider == null) {
			bookingListProvider = new BookingListProvider<Booking>(context, intent, BookingsListWidget.BOOKINGS_LIST);
		}
		Log.i("BookingService", "Got bookings - "+bookingListProvider.getCount());
		return bookingListProvider;
	}
	
}
