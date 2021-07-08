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

public class BookingsWidget extends AppWidgetProvider {

	private static int i = 0;

	// private static Project[] projects = null;
	// private static Task[] tasks = null;
	// public static Entity[] entities = null;

	public static final String SEND_BOOKING = "SEND_BOOKING";
	public static final String ACTION_CLICK = "ACTION_CLICK_WIDGET";
	public static final String ACTION_CLICK_BUTTON = "ACTION CLICK BUTTON";
	public static final String ACTION_CLICK_BACK_BUTTON = "ACTION_CLICK_BACK_BUTTON";
	public static final String ACTION_CLICK_BREAK_BUTTON = "ACTION_CLICK_BREAK_BUTTON";
	public static final String ACTION_CLICK_CANCEL_BUTTON = "ACTION_CLICK_CANCEL_BUTTON";
	public static final String ACTION_CLICK_WORK_END_BUTTON = "ACTION_CLICK_WORK_END_BUTTON";
	public static final String ACTION_REFRESH = "ACTION_REFRESH_WIDGET";
	public static final String CLICKED_LIST = "ID_CLICKED_LIST";
	public static final String CLICKED_ITEM = "ID_CLICKED_ITEM";
	public static final String DISPLAYED_LIST = "ID_DISPLAYED_LIST";
	public static final String ID = "ID_ID";
	public static final String DESCRIPTION = "ID_DESCRIPTION";
	public static final int PROJECTS_LIST = 0;
	public static final int TASKS_LIST = 1;
	public static final int CONFIRMATION_PANEL = 2;
	public static final int NONE_LIST = -1;
	public static final int BREAK = 99;

	private static final CharSequence PLEASE_CHOOSE = "Bitte Projekt wählen";

	public static final String PROJECT_ID = "PROJECT_ID";
	public static final String TASK_ID = "TASK_ID";
	public static final String ACTION_ID = "ACTION_ID";

	// private static HashMap<Integer, Integer> currentStates = new
	// HashMap<Integer, Integer>();
	private static int currentState = PROJECTS_LIST;
	private static int projectId = -1;
	private static int taskId = -1;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		MyLog.setContextIfNecessary(context);
		MyLog.i("BookingsWidget", "Update with appWidetId=" + appWidgetIds[0]
				+ " Context " + context);
		// Toast.makeText(context, "Update", Toast.LENGTH_SHORT).show();
		if (Settings.getInstance() == null) {
			Settings.init(context);
		}
		RemoteViews views = new RemoteViews(context.getPackageName(),
				layout.booking_widget_layout);
		// i++;
		ComponentName thisWidget = new ComponentName(context,
				BookingsWidget.class);
		initWidget(context, thisWidget, views, appWidgetIds);
		// !!!! startService (context, appWidgetIds);
		views.setTextViewText(R.id.tvWidget, PLEASE_CHOOSE);

		EntitiesFactory ef = EntitiesFactory.getInstance(context);
		if (!ef.isInitialized()) {
			ef.initFromFilesystem();
		}
		setButtonState(views);

		appWidgetManager.updateAppWidget(thisWidget, views);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	private void setButtonState(RemoteViews views) {
		Actions actions = Actions.getInstance();
		if (actions.getCurrentAction() == Actions.ENDE) {
			views.setViewVisibility(R.id.widgetBreakButton, View.GONE);
			views.setViewVisibility(R.id.widgetWorkEndButton, View.GONE);
			views.setViewVisibility(R.id.breadCrumBackButton, View.GONE);
			return;
		}
		if (actions.getCurrentAction() == Actions.PAUSE) {
			views.setViewVisibility(R.id.widgetBreakButton, View.VISIBLE);
			views.setViewVisibility(R.id.widgetWorkEndButton, View.GONE);
			views.setViewVisibility(R.id.breadCrumBackButton, View.GONE);
			return;
		}
		views.setViewVisibility(R.id.widgetBreakButton, View.VISIBLE);
		views.setViewVisibility(R.id.widgetWorkEndButton, View.VISIBLE);
		views.setViewVisibility(R.id.breadCrumBackButton, View.GONE);
	}

	private void initWidget(Context context, ComponentName thisWidget,
			RemoteViews remoteViews, int[] appWidgetIds) {
		MyLog.d("initWidget", "starting service");
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);

		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
		MyLog.w("initWidget",
				"From Intent" + String.valueOf(appWidgetIds.length));
		MyLog.w("InitWidget", "Direct" + String.valueOf(allWidgetIds2.length));

		Intent bookingListIntent = new Intent(context,
				BookingWidgetService.class);
		bookingListIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				appWidgetIds);
		bookingListIntent
				.putExtra(BookingsWidget.DISPLAYED_LIST, PROJECTS_LIST);

		bookingListIntent.setData(Uri.parse(bookingListIntent
				.toUri(Intent.URI_INTENT_SCHEME) + "/A0"));

		// Register an onClickListener
		remoteViews.setRemoteAdapter(R.id.widgetListView, bookingListIntent);

		for (int widgetId : appWidgetIds) {

			Intent clickIntent = new Intent(context, BookingsWidget.class);
			clickIntent.setAction(BookingsWidget.ACTION_CLICK);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			clickIntent.setData(Uri.parse(clickIntent
					.toUri(Intent.URI_INTENT_SCHEME) + "/list"));
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setPendingIntentTemplate(R.id.widgetListView,
					pendingIntent);

			clickIntent = new Intent(context, BookingsWidget.class);
			clickIntent.setAction(BookingsWidget.ACTION_CLICK_BUTTON);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			clickIntent.setData(Uri.parse(clickIntent
					.toUri(Intent.URI_INTENT_SCHEME) + "/button"));
			pendingIntent = PendingIntent.getBroadcast(context, 1, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.confirmationButton,
					pendingIntent);

			clickIntent = new Intent(context, BookingsWidget.class);
			clickIntent.setAction(BookingsWidget.ACTION_CLICK_BACK_BUTTON);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			clickIntent.setData(Uri.parse(clickIntent
					.toUri(Intent.URI_INTENT_SCHEME) + "/back_button"));
			pendingIntent = PendingIntent.getBroadcast(context, 2, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.breadCrumBackButton,
					pendingIntent);

			clickIntent = new Intent(context, BookingsWidget.class);
			clickIntent.setAction(BookingsWidget.ACTION_CLICK_CANCEL_BUTTON);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			clickIntent.setData(Uri.parse(clickIntent
					.toUri(Intent.URI_INTENT_SCHEME) + "/cancel_button"));
			pendingIntent = PendingIntent.getBroadcast(context, 3, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.confirmationCancelButton,
					pendingIntent);

			clickIntent = new Intent(context, BookingsWidget.class);
			clickIntent.setAction(BookingsWidget.ACTION_CLICK_BREAK_BUTTON);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			clickIntent.setData(Uri.parse(clickIntent
					.toUri(Intent.URI_INTENT_SCHEME) + "/break_button"));
			pendingIntent = PendingIntent.getBroadcast(context, 4, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.widgetBreakButton,
					pendingIntent);

			clickIntent = new Intent(context, BookingsWidget.class);
			clickIntent.setAction(BookingsWidget.ACTION_CLICK_WORK_END_BUTTON);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			clickIntent.setData(Uri.parse(clickIntent
					.toUri(Intent.URI_INTENT_SCHEME) + "/work_end_button"));
			pendingIntent = PendingIntent.getBroadcast(context, 6, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.widgetWorkEndButton,
					pendingIntent);

			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		// new Timer().scheduleAtFixedRate(new CounterTask(), 0L, 1000L);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		String action = intent.getAction();
		Log.i("Action = ", action);

		RemoteViews views = new RemoteViews(context.getPackageName(),
				layout.booking_widget_layout);
		// views.setTextViewText(R.id.tvWidget,
		// "Hallo!"+i+intent.getStringExtra("Dödel"));

		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		ComponentName thisWidget = new ComponentName(context,
				BookingsWidget.class);
		boolean startService = false;
		boolean sendBooking = false;
		// int currentState = getCurrentState()
		String s = "";
		if (ACTION_REFRESH.equals(action)) {
			refreshWidget(views);
			refreshBookingsList(context);
			appWidgetManager.updateAppWidget(thisWidget, views);
			return;
		}
		Integer i = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		if (ACTION_CLICK.equals(action)) {
			s = "click" + currentState + " - " + i;
			if (currentState == PROJECTS_LIST) {// entities instanceof
												// Project[]) {
				projectId = intent.getIntExtra(ID, -1);
				views.setTextViewText(R.id.tvWidget,
						intent.getStringExtra(DESCRIPTION));
				views.setViewVisibility(R.id.cancelButton, View.VISIBLE);
				views.setViewVisibility(R.id.breadCrumBackButton, View.VISIBLE);
				currentState = TASKS_LIST;

				views.setViewVisibility(R.id.widgetBreakButton, View.GONE);
				views.setViewVisibility(R.id.widgetWorkEndButton, View.GONE);
			} else {
				views.setViewVisibility(R.id.breadCrumBackButton, View.GONE);
				views.setTextViewText(R.id.tvConfirmation,
						intent.getStringExtra(DESCRIPTION));
				views.setViewVisibility(R.id.widgetListView, View.INVISIBLE);
				views.setViewVisibility(R.id.tvConfirmation, View.VISIBLE);
				views.setViewVisibility(R.id.confirmationButton, View.VISIBLE);
				views.setViewVisibility(R.id.confirmationCancelButton,
						View.VISIBLE);
				currentState = CONFIRMATION_PANEL;
				taskId = intent.getIntExtra(ID, -1);
			}
			// appWidgetManager.updateAppWidget(thisWidget, views);
			startService = true;
		}

		if (ACTION_CLICK_BACK_BUTTON.equals(action)) {
			s = "click_back_button" + currentState + " - " + i;
			if (currentState > PROJECTS_LIST) {
				startService = true;
				if (currentState == CONFIRMATION_PANEL) {
					views.setViewVisibility(R.id.tvConfirmation, View.GONE);
					views.setViewVisibility(R.id.confirmationButton, View.GONE);
					views.setViewVisibility(R.id.confirmationCancelButton,
							View.GONE);
					views.setViewVisibility(R.id.widgetListView, View.VISIBLE);

					appWidgetManager.updateAppWidget(thisWidget, views);
					currentState = TASKS_LIST;
				} else if (currentState == TASKS_LIST) {
					views.setViewVisibility(R.id.cancelButton, View.GONE);
					currentState = PROJECTS_LIST;
				}
			}
			views.setViewVisibility(R.id.breadCrumBackButton, View.GONE);
			views.setViewVisibility(R.id.widgetBreakButton, View.VISIBLE);
			views.setViewVisibility(R.id.widgetWorkEndButton, View.VISIBLE);
		}

		if (ACTION_CLICK_BUTTON.equals(action)) {
			s = "click_button" + currentState + " - " + i;
			currentState = PROJECTS_LIST;
			views.setViewVisibility(R.id.tvConfirmation, View.GONE);
			views.setViewVisibility(R.id.confirmationButton, View.GONE);
			views.setViewVisibility(R.id.confirmationCancelButton, View.GONE);
			views.setViewVisibility(R.id.widgetListView, View.VISIBLE);
			views.setViewVisibility(R.id.breadCrumBackButton, View.GONE);
			views.setViewVisibility(R.id.widgetBreakButton, View.VISIBLE);
			views.setViewVisibility(R.id.widgetWorkEndButton, View.VISIBLE);

			// appWidgetManager.updateAppWidget(thisWidget, views);
			appendBooking();
			startService = true;
			sendBooking = true;
		}

		if (ACTION_CLICK_CANCEL_BUTTON.equals(action)) {
			s = "click_button" + currentState + " - " + i;
			currentState = PROJECTS_LIST;
			views.setViewVisibility(R.id.tvConfirmation, View.GONE);
			views.setViewVisibility(R.id.confirmationButton, View.GONE);
			views.setViewVisibility(R.id.confirmationCancelButton, View.GONE);
			views.setViewVisibility(R.id.widgetListView, View.VISIBLE);
			views.setViewVisibility(R.id.breadCrumBackButton, View.GONE);
			views.setViewVisibility(R.id.widgetBreakButton, View.VISIBLE);
			views.setViewVisibility(R.id.widgetWorkEndButton, View.VISIBLE);

			currentState = PROJECTS_LIST;
			// appWidgetManager.updateAppWidget(thisWidget, views);
			startService = true;
		}

		if (ACTION_CLICK_BREAK_BUTTON.equals(action)) {
			s = "click_button" + currentState + " - " + i;
			if (currentState == BREAK) {
				endBreak(views, appWidgetManager, thisWidget);
			} else {
				startBreak(views, appWidgetManager, thisWidget);
			}
			startService = true;
			sendBooking = true;
		}

		if (ACTION_CLICK_WORK_END_BUTTON.equals(action)) {
			s = "click_button" + currentState + " - " + i;
			currentState = PROJECTS_LIST;
			// views.setViewVisibility(R.id.tvConfirmation, View.GONE);
			// views.setViewVisibility(R.id.confirmationButton, View.GONE);
			// views.setViewVisibility(R.id.confirmationCancelButton,
			// View.GONE);
			// views.setViewVisibility(R.id.widgetListView, View.VISIBLE );

			views.setViewVisibility(R.id.widgetBreakButton, View.GONE);
			views.setViewVisibility(R.id.widgetWorkEndButton, View.GONE);
			currentState = PROJECTS_LIST;
			appWidgetManager.updateAppWidget(thisWidget, views);
			Bookings bookings = EntitiesFactory.getInstance().getBookings();
			bookings.add(new Booking(
					EntitiesFactory.getInstance().getJanitor(), Actions.ENDE));
			startService = true;
			sendBooking = true;
		}

		if (startService) {
			int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			appWidgetManager.notifyAppWidgetViewDataChanged(allWidgetIds,
					R.id.widgetListView);
			appWidgetManager.updateAppWidget(thisWidget, views);
			startService(context, appWidgetManager, allWidgetIds, sendBooking);
		}
		Log.i(getClass().toString(), "action started " + action);
		// Toast.makeText(context, "Receive"+s, Toast.LENGTH_SHORT).show();

	}

	private void refreshWidget(RemoteViews remoteViews) {
		setButtonState(remoteViews);
	}

	private void startService(Context context,
			AppWidgetManager appWidgetManager, int[] appWidgetIds,
			boolean sendBooking) {

		Intent bookingListIntent = new Intent(context,
				BookingWidgetService.class);
		bookingListIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				appWidgetIds);
		bookingListIntent.putExtra(SEND_BOOKING, sendBooking);
		int displayedList = (currentState >= CONFIRMATION_PANEL) ? NONE_LIST
				: currentState;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				layout.booking_widget_layout);
		bookingListIntent
				.putExtra(BookingsWidget.DISPLAYED_LIST, displayedList);
		i++;
		bookingListIntent.setData(Uri.parse(bookingListIntent
				.toUri(Intent.URI_INTENT_SCHEME) + "/A" + i));
		MyLog.i("DisplayedList", "" + displayedList);
		remoteViews.setRemoteAdapter(R.id.widgetListView, bookingListIntent);
		MyLog.i("DisplayedList was", "" + displayedList);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
		context.startService(bookingListIntent);
		
		Intent bookingRefreshService = new Intent(context, BookingWidgetRefreshService.class);
		bookingRefreshService.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				appWidgetIds);
		bookingRefreshService.putExtra(SEND_BOOKING, sendBooking);
		//MyLog.i("SendBooking =", ""+sendBooking);
		context.startService(bookingRefreshService);
		refreshBookingsList(context);
	}
	
	private void refreshBookingsList(Context context) {
		Intent listRefreshIntent = new Intent(context, BookingsListWidget.class);
		listRefreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		context.sendBroadcast(listRefreshIntent);
	}
	

	private void startBreak(RemoteViews views,
			AppWidgetManager appWidgetManager, ComponentName thisWidget) {
		currentState = BREAK;
		views.setTextViewText(R.id.widgetBreakButton, "Pause beenden");
		views.setViewVisibility(R.id.tvConfirmation, View.GONE);
		views.setViewVisibility(R.id.confirmationButton, View.GONE);
		views.setViewVisibility(R.id.confirmationCancelButton, View.GONE);
		views.setViewVisibility(R.id.widgetListView, View.VISIBLE);
		views.setViewVisibility(R.id.widgetWorkEndButton, View.GONE);
		currentState = BREAK;
		Bookings bookings = EntitiesFactory.getInstance().getBookings();
		bookings.add(new Booking(EntitiesFactory.getInstance().getJanitor(),
				Actions.PAUSE));
	}

	private void endBreak(RemoteViews views, AppWidgetManager appWidgetManager,
			ComponentName thisWidget) {
		currentState = PROJECTS_LIST;
		views.setTextViewText(R.id.widgetBreakButton, "Pause");
		views.setViewVisibility(R.id.tvConfirmation, View.GONE);
		views.setViewVisibility(R.id.confirmationButton, View.GONE);
		views.setViewVisibility(R.id.confirmationCancelButton, View.GONE);
		views.setViewVisibility(R.id.widgetListView, View.VISIBLE);
		views.setViewVisibility(R.id.cancelButton, View.GONE);
		views.setViewVisibility(R.id.widgetWorkEndButton, View.VISIBLE);
		currentState = PROJECTS_LIST;
		Bookings bookings = EntitiesFactory.getInstance().getBookings();
		bookings.add(new Booking(EntitiesFactory.getInstance().getJanitor(),
				Actions.PAUSE_ENDE));
	}

	private void appendBooking() {
		EntitiesFactory entities = EntitiesFactory.getInstance();
		Settings settings = Settings.getInstance();
		Janitor janitor = settings.getJanitor();
		Bookings bookings = entities.getBookings();
		Project project = entities.getProjects().find(projectId);
		Task task = entities.getTasks().find(taskId);
		Action action = manageState(bookings);
		MyLog.i("Widget", "Created Booking P=" + project.getName() + " ,T="
				+ task.getName() + " ,A=" + action.getName());

		Booking booking = new Booking(janitor, project, action, task,
				"Vom Widget");
		bookings.add(booking);
	}

	private Action manageState(Bookings bookings) {
		Actions actions = Actions.getInstance();
		Action action = actions.getCurrentAction();
		if (action.notWorking() && !action.doEnd()) {
			action = actions.getEndAction();
			bookings.add(new Booking(
					EntitiesFactory.getInstance().getJanitor(), action));
			// actions.setCurrentAction(action);
			// actions.changeMenu(menu);
		}
		action = actions.getNextWorkingStateAction(action);
		actions.setCurrentAction(action);
		return action;
	}
}
