package de.cisoft.zeiterfassung.widget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.cisoft.framework.Entity;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.implementation.entity.Action;
import de.cisoft.zeiterfassung.implementation.entity.Booking;
import de.cisoft.zeiterfassung.implementation.entity.Project;
import de.cisoft.zeiterfassung.implementation.entity.Projects;
import de.cisoft.zeiterfassung.implementation.entity.Task;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

public class BookingListProvider<T extends Entity> implements RemoteViewsFactory {

	
	private Context context;
	private T[] entities;
	private int listKind;

	public BookingListProvider (Context context, Intent intent, int listKind) {
		this.context = context;
		//appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		//entities = BookingsWidget.entities;
		this.listKind = listKind;
		loadLists(listKind);
	}
	
	@Override
	public void onDataSetChanged() {
		loadLists(listKind);
	}
	
	@SuppressWarnings("unchecked")
	private void loadLists(int listKind) {
		
		EntitiesFactory ef = EntitiesFactory.getInstance(context);
		if (!ef.isInitialized()) {
			ef.initFromFilesystem();
		}
		if (listKind == BookingsWidget.PROJECTS_LIST) {
			entities = (T[]) ef.getMyProjects().toSafeArray(true);
		} else if (listKind == BookingsWidget.TASKS_LIST) {
			entities = (T[]) ef.getTasks().toSafeArray(true);
		} else {
			entities = (T[]) ef.getBookings().toSafeArray(true); 
		}
		/*
		this.projects = new Project[1];
		Project project = new Project() {
			public String getKonto() {
				return "konto";
			}
			
			public String getDescription() {
				return "Description";
			}
			
		};
		projects[0] = project;
		*/
		
	}

	
		
	public int getCount() {
		// TODO Auto-generated method stub
		return entities.length;
	}

	public long getItemId(int pos) {
		// TODO Auto-generated method stub
		return pos;
	}

	public RemoteViews getLoadingView() {
		// TODO Auto-generated method stub
		return null;
	}

	public String showTime(Date timeStamp) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(timeStamp);
		return ""+
		format(cal.get(Calendar.DAY_OF_MONTH))+"."+
		format(cal.get(Calendar.MONTH)+1)+"."+
		(cal.get(Calendar.YEAR)-2000)+" "+
		format(cal.get(Calendar.HOUR_OF_DAY))+":"+
		format(cal.get(Calendar.MINUTE));
		
	}
	
	private String format(int i) {
		return (i<10 ? "0" : "") + Integer.toString(i);
	}
	
	public RemoteViews getViewAt(int pos) {
		RemoteViews view = new RemoteViews(
				context.getPackageName(),
				R.layout.booking_list_row
		);
		
		Entity entity = entities[pos];
		String display;
		int id;
		if (entities instanceof Task[]) { 
			Task task = (Task) entity;
			view.setTextViewText(R.id.booking_row_heading, task.getKonto());
			view.setTextViewText(R.id.booking_row_text, task.getName());
			display =  task.getKonto()+" -> "+task.getName();
			id = task.getId();
		} else if (entities instanceof Project[]) {
			Project project = (Project) entity;
			view.setTextViewText(R.id.booking_row_heading, project.getKonto());
			view.setTextViewText(R.id.booking_row_text, project.getDescription());
			display =  project.getKonto()+" -> "+project.getName();
			id = project.getId();
		} else {
			Booking booking = (Booking) entity;
			String time = showTime(booking.getTimestamp());
			String project = getProjectName(booking.getProject());
			String task = getTaskName(booking.getTask());
			String action = getActionName(booking.getAction());
			view.setTextViewText(R.id.booking_row_heading, time + " " + project);
			view.setTextViewText(R.id.booking_row_text, task+" "+action);
			display =  time + " : "+ project + " -> " + task;
			id = booking.getId();
		}
		Intent fillInIntent = new Intent();
		fillInIntent.putExtra(BookingsWidget.ID, id);
		fillInIntent.putExtra(BookingsWidget.DESCRIPTION, display);
	    view.setOnClickFillInIntent (R.id.booking_row_layout, fillInIntent);
		return view;
	}
	
	private String getActionName(Action action) {
		return action == null ? "-------------" : action.getName();
	}
	
	private String getProjectName(Project project) {
		return project == null ? "===========" : project.getDescription();
	}
	
	private String getTaskName(Task task) {
		return task == null ? "===========" : task.getName();
	}

	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	public void onCreate() {
		// TODO Auto-generated method stub

	}

	public void onDestroy() {
		// TODO Auto-generated method stub

	}

}
