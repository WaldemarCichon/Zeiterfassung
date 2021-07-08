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

public class BookingWidgetService extends RemoteViewsService {
	
	private static BookingListProvider<Project> projectListProvider;
	private static BookingListProvider<Task> taskListProvider;
	private static BookingListProvider<Booking> bookingListProvider;
	
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		//int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 
		//		AppWidgetManager.INVALID_APPWIDGET_ID);
		int displayedList = intent.getIntExtra(BookingsWidget.DISPLAYED_LIST, BookingsWidget.NONE_LIST);
		Log.i("inGetViewFactory", "Entering for "+displayedList);
		Context context = this.getApplicationContext(); 
		//boolean refresh = intent.getBooleanExtra(BookingsListWidget.REFRESH, false);
		return (displayedList == BookingsWidget.PROJECTS_LIST) ?
				getProjectListProvider(context, intent) :
				(displayedList == BookingsWidget.TASKS_LIST) ?
						getTaskListProvider(context, intent) : 
						(displayedList == BookingsListWidget.BOOKINGS_LIST) ?
							getBookingListProvider(context, intent) :
							getProjectListProvider(context, intent); //null;
	}
	
	private BookingListProvider<Project> getProjectListProvider(Context context, Intent intent) {
		MyLog.setContextIfNecessary(context);
		if (projectListProvider == null) {
			projectListProvider = new BookingListProvider<Project>(context, intent, BookingsWidget.PROJECTS_LIST);
		}
		return projectListProvider;
	}
	
	private BookingListProvider<Task> getTaskListProvider(Context context, Intent intent) {
		MyLog.setContextIfNecessary(context);
		if (taskListProvider == null) {
			taskListProvider = new BookingListProvider<Task>(context, intent, BookingsWidget.TASKS_LIST);
		}
		return taskListProvider;
	}
	
	private BookingListProvider<Booking> getBookingListProvider(Context context, Intent intent) {
		MyLog.setContextIfNecessary(context);
		Log.i("BookingService", "Getting bookings");
		//if (bookingListProvider == null) {
			bookingListProvider = new BookingListProvider<Booking>(context, intent, BookingsListWidget.BOOKINGS_LIST);
		//} else {
		//	bookingListProvider.refresh();
		//}
		Log.i("BookingService", "Got bookings - "+bookingListProvider.getCount());
		return bookingListProvider;
	}
}
