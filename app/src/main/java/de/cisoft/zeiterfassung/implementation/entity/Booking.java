package de.cisoft.zeiterfassung.implementation.entity;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import de.cisoft.framework.Entity;
import de.cisoft.framework.Informer.Informer;
import de.cisoft.framework.android.ui.ErrorNotifier;
import de.cisoft.framework.annotations.ID;
import de.cisoft.framework.annotations.Field;
import de.cisoft.framework.webservice.Webservice;
import de.cisoft.utility.log.MyLog;
import de.cisoft.zeiterfassung.MainActivity;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.implementation.MetaEntity.Month;
import de.cisoft.zeiterfassung.implementation.enums.SaveMode;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;



public class Booking extends Entity {

	private static final int MIN_BOOKING_COUNT = 500;
	private static final String PARAMETER = "export";
	private static final String SINGLE_SEND_METHOD = "SWriteExport";
	private static final String SINGLE_RESEND_METHOD = "SWriteExportWithCheck";
	private static int genId = Integer.MAX_VALUE;
	//private static final String DIFF_SEND_METHOD = "SWriteExportDiff";
										//	dd*hh*mm*ss*__ms 
	private static final long MAX_TIME_SPAN = 90*24*60*60*1000;
	
	private static final Date now = new Date();

	
	private int id;
	private Janitor janitor;
	private Project project;
	private Task task;
	private int  type=1;
	private Action action;
	private Date timestamp;
	private Date sendTs;
	private String comment;
	private Integer systemId;
	private boolean sortByDate = true;
	private Month month;
	//private Week week;
	//private Day day;
	

	public Booking() {
		super();
	}

	public Booking(String row, String separator, de.cisoft.framework.Field[] assignments) {
		super(row, separator, assignments);
		setGenId(id);
	}

	public Booking(String[] fields, de.cisoft.framework.Field[] assignments) {
		super(fields, assignments);
		setGenId (id);
	}

	public Booking(Date timestamp, Janitor janitor, Project project, Action action,
			Task task, CharSequence comment) {
		this.id = genId--;  
		this.timestamp = timestamp;
		this.janitor = janitor;
		this.project = project;
		this.action = action;
		this.task = task;
		if (comment!=null) {
			this.comment = comment.toString();
		}
	}
	
	public Booking(long timestamp, int janitorId, int projectId, int actionId, 
			int taskId, CharSequence comment) {
		this.id = genId--;
		this.timestamp = new Date(timestamp);
		EntitiesFactory ef = EntitiesFactory.getInstance();
		this.janitor = ef.getJanitors().find(janitorId);
		this.project = ef.getProjects().find(projectId);
		this.task = ef.getTasks().find(taskId);
		this.action = Actions.getInstance().getPossibleAction(actionId);
		if (comment!=null) {
			this.comment = comment.toString();
		}
	}
	
	public Booking(Janitor janitor, Project project, Action action, Task task, CharSequence comment) {
		this(new Date(), janitor, project, action, task, comment);
	}
	
	public Booking(Janitor janitor, Action action) {
		this(janitor, null, action, null, action.getName());
	}
	
	private static void setGenId(int id) {
		if (id < genId) {
			genId = id;
		}
	}

	@ID
	@Field(name="id", alias="id", type=Integer.class)
	public int getId() {
		return id;
	}

	public void setId(int id) {
//		System.out.println("SetId to"+id);
//		if (id>genId) {
//			id=genId;
//		}
		setGenId(id);
		this.id = id;
	}

	@Field(name="timestamp", alias="timestamp", type=Date.class) 
	public Date getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	@Field(name="janitor", alias="janitor", type=Janitor.class) 
	public Janitor getJanitor() {
		return janitor;
	}
	
	public void setJanitor(Janitor janitor) {
		this.janitor = janitor;
	}
	
	@Field(name="project", alias="project", type=Project.class) 
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Field(name="task", alias="task", type=Task.class)
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}
	
	@Field(name="type", alias="type", type=Integer.class)
	public Integer getType() {
		return type;
	}
	
	public void setType (Integer type) {
		this.type = type;
	}

	@Field(name="action", alias="action", type=Action.class)
	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}
	
	@Field(name="comment", alias="comment", type=String.class)
	public String getComment() {
		return this.comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	@Field(name="systemId", alias="systemId", type=Integer.class)
	public Integer getSystemId() {
		return this.systemId;
	}
	
	public void setSystemId(Integer systemId) {
		this.systemId = systemId;
	}
	
	@Field(name="sendTs", alias="sendTs", type=Date.class)
	public Date getSendTs() {
		return sendTs;
	}
	
	public void setSendTs(Date sendTs) {
		this.sendTs = sendTs;
	}
	
	@SuppressWarnings("deprecation")
	public String toString() {
		return  "["+id+"]"+
				(timestamp == null ? "null" : timestamp.toLocaleString()+" - ")+
				(project == null ? "null" : project.toString()+" - ")+
				(action == null ? "null" : action.toString());
	}

	public void setMonth(Month month) {
		this.month = month;
	}
	
	private void appendNumberFromCalendar(StringBuilder sb, Calendar cal, int field) {
		int val = cal.get(field);
		if (field == Calendar.MONTH) {
			val++;
		}
		if (val<10) {
			sb.append('0');
		}
		sb.append(val);
	}
	
	private StringBuilder format (Date date) {
		Calendar cal = GregorianCalendar.getInstance();
		StringBuilder sb=new StringBuilder();
		appendNumberFromCalendar(sb, cal, Calendar.DAY_OF_MONTH);
		sb.append(".");
		appendNumberFromCalendar(sb, cal, Calendar.MONTH);
		sb.append(".");
		sb.append(cal.get(Calendar.YEAR)).append(" ");
		appendNumberFromCalendar(sb, cal, Calendar.HOUR_OF_DAY);
		sb.append(":");
		appendNumberFromCalendar(sb, cal, Calendar.MINUTE);
		sb.append(":");
		appendNumberFromCalendar(sb, cal, Calendar.SECOND);

		
		/*
		sb.append(cal.get(Calendar.DAY_OF_MONTH)).append('.');
		sb.append(cal.get(Calendar.MONTH)+1).append(".");
		sb.append(cal.get(Calendar.YEAR)).append(" ");
		sb.append(cal.get(Calendar.HOUR_OF_DAY)).append(":");
		sb.append(cal.get(Calendar.MINUTE)).append(":");
		sb.append(cal.get(Calendar.SECOND));
		*/
		return sb;
	}
	
	private String getProjectKonto() {
		if (project == null) {
			return "";
		} else {
			return project.getKonto();
		}
	}
	
	private StringBuilder getTextParam() {
		StringBuilder sb=new StringBuilder();
		String tp = action.getTextParam();
		if (tp == null) {
			sb.append(getProjectKonto()).append("*").append(task == null ? "-1" : task.getKonto()).
				append("**").append(comment == null || comment.length()==0 ? task.getName() : comment);
		} else {
			sb.append(tp);
		}
		return sb;
	}
	
	private String getDestId() {
		return action.getKonto();
	}
	
	private int getIntParam() {
		return Settings.getInstance().getDeviceId();
	}
	
	private Janitor getJanitorNullSafe() {
		if (janitor == null) {
			janitor = Settings.getInstance().getJanitor();
		}
		return janitor;
	}
	
	@Override
	public String serialize(String fieldseparator, de.cisoft.framework.Field[] assignments) {
		if (fieldseparator.length()==2) { // separator is escaped 
			fieldseparator = fieldseparator.substring(1);
		}
		if (assignments!=null) {
			String s = super.serialize(fieldseparator, assignments);
			MyLog.i("Booking.serialize","saving "+s);
			return s;
		}
		StringBuilder sb = new StringBuilder();
		//sb.append(this.janitor.getKonto()).append(fieldseparator);
		sb.append(this.getJanitorNullSafe().getId())/*getKonto())*/.append(fieldseparator);
		sb.append(format(timestamp)).append(fieldseparator);
		sb.append(getType()).append(fieldseparator);
		sb.append(getDestId()).append(fieldseparator);
		sb.append(getTextParam()).append(fieldseparator);
		sb.append(getIntParam());//.append(fieldseparator);
		//sb.append(format(new Date()));
		return sb.toString();
	}
	
	public void resend(Webservice webservice, String fieldseparator,
			de.cisoft.framework.Field[] assignments) {
		send (webservice, SINGLE_RESEND_METHOD, PARAMETER, fieldseparator, assignments);
	}
	
	public void send(Webservice webservice, String fieldseparator, de.cisoft.framework.Field[] assignments) {
		send (webservice, SINGLE_SEND_METHOD, PARAMETER, fieldseparator, assignments);
	}
	
	public synchronized void sendLazy(final Webservice webservice, 
			 			 final String fieldseparator,
			 			 final de.cisoft.framework.Field[] assignments) {

		new Thread() {
			
			@Override
			public void run() {
				send(webservice, fieldseparator, assignments);
			}
		}.start();

	}
	
	public void send(final Webservice webservice, 
					 final String method, 
					 final String parameter,
					 final String fieldseparator,
					 final de.cisoft.framework.Field[] assignments) {
		try {
			if (Settings.getInstance().isSaveBookingsImmediate()) {
				saveAll(SaveMode.Backup);
				MyLog.i("Booking.send", "Bookngs saved to backup");
			}
			String s = serialize(fieldseparator, null);
			MyLog.i("Booking.send","String->"+s);
			String result = webservice.call(method, parameter, s);
			MainActivity.getVibrator().vibrate(Settings.getInstance().getVibrationLength());
			MyLog.i("Booking.send", "Result->"+result);
			sendTs = new Date();
			try {
				systemId = Integer.parseInt(result);
			} catch (NumberFormatException nfe) {
				throw new RuntimeException(result);
				//nfe.printStackTrace();
			}
			Informer.getInstance().inform();
			ErrorNotifier.getInstance().resetWarning();
			if (Settings.getInstance().isSaveBookingsImmediate()) {
				MyLog.i("Bookings.save", "All data saved");
				saveAll(SaveMode.Main);
			}
		} catch (ConnectException cex) {
			MyLog.d("Booking-send failes, Connection Exception", cex.toString());
			ErrorNotifier.getInstance().warn();
			saveAll(SaveMode.Main);
		} catch (SocketTimeoutException stex) {
			MyLog.d("Booking-send failes, Socket Timeout Exception", stex.toString());
			ErrorNotifier.getInstance().warn();
			saveAll(SaveMode.Main);
		} catch (SocketException sex) {
			MyLog.d("Booking-send failes, Socket Exception", sex.toString());
			ErrorNotifier.getInstance().warn();
			saveAll(SaveMode.Main);
		}
		
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean isSent() {
		return sendTs != null && systemId != null;
	}

	boolean isFirstOfMonth() {

		return month.isFirstBooking(this);
	}


	@SuppressWarnings("deprecation")
	@Override
	protected void fill(View view, int position) {
		TextView tvGrouppingItem = (TextView) view.findViewById(R.id.tvGrouppingItem);
		if (isFirstOfMonth()) {
			tvGrouppingItem.setVisibility(View.VISIBLE);
			tvGrouppingItem.setText(month.getName());
		} else {
			tvGrouppingItem.setVisibility(View.GONE);
		}
		TextView tvTimestamp = (TextView) view.findViewById(R.id.tvTimestamp);
		TextView tvObjectAction = (TextView) view.findViewById(R.id.tvObjectAction);
		tvObjectAction.setText((this.getProject()==null ?  "" : 
			("("+this.getProject().getKonto()+") "+this.getProject().getStrasse()+" - "))+
			(this.getTask() == null ? "=====" : this.getTask().getName()));
		tvTimestamp.setText(getTimestamp()==null ? "--.--.---- --:--" :getTimestamp().toLocaleString()+
				(action == null ? "" : (" ="+action.getKonto()+"="))+" ("+
		        (getSendTs() != null ? getSendTs().toLocaleString() : "--.--.---- --:--")+" "+
		        (getSystemId() != null ? getSystemId() : "--")+")");
	}
	
	@Override
	public int compareTo(Entity other) {
		if (!(other instanceof Booking)) {
			return 0;
		}
		if (sortByDate ) {
			return ((Booking) other).getTimestamp().compareTo(this.timestamp);
		} else {
			return ((Integer)other.getIdValue()).compareTo(id);
		}
	}

	private void saveAll(SaveMode saveMode) {
		try 
		{
			EntitiesFactory.getInstance().getBookings().save(saveMode);
			Settings.getInstance().saveAtClose(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public boolean shouldSave() {
		//System.out.println(toString());
		if (timestamp == null || action == null) {
			return false;
		}
		long timeSpan = now.getTime() - timestamp.getTime();
		if (timeSpan<=MAX_TIME_SPAN) {
			return true;
		}
		
		if (EntitiesFactory.getInstance().getBookings().getCount()<MIN_BOOKING_COUNT) {
			return true;
		}
		return false;
	}
}
