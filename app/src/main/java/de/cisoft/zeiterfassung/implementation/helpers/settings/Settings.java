package de.cisoft.zeiterfassung.implementation.helpers.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.cisoft.framework.webservice.Webservice;
import de.cisoft.utility.Mail;
import de.cisoft.utility.ftp.FtpClient;
import de.cisoft.utility.log.MyLog;
import de.cisoft.zeiterfassung.MainActivity;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.implementation.entity.Action;
import de.cisoft.zeiterfassung.implementation.entity.Actions;
import de.cisoft.zeiterfassung.implementation.entity.Booking;
import de.cisoft.zeiterfassung.implementation.entity.Bookings;
import de.cisoft.zeiterfassung.implementation.entity.Janitor;
import de.cisoft.zeiterfassung.implementation.entity.Janitors;
import de.cisoft.zeiterfassung.implementation.entity.Project;
import de.cisoft.zeiterfassung.implementation.entity.Task;
import de.cisoft.zeiterfassung.implementation.enums.ViewedList;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.settings.enums.ProjectSelectBehavior;
import de.cisoft.zeiterfassung.implementation.helpers.settings.enums.SendBehavior;
import de.cisoft.zeiterfassung.implementation.helpers.settings.enums.TaskSelectBehavior;
import de.cisoft.zeiterfassung.ui.enums.Branding;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.util.Log;
import android.view.MenuItem;

public class Settings {
	
	
	private static final String SEND_BEHAVIOR = "SendBehavior";
	private static final String LIST_BEHAVIOR = "ListBehavior";
	private static final String SELECT_BEHAVIOR = "SelectBehavior";
	private static final String USER_MAIL_ADDRESS = "UserMailAddress";
	private static final String SERVER_PATH = "ServerPath";
	private static final String INITIALIZED = "Initialized";
	private static final String NAMESPACE = "http://gfi-informatik.de/MPZE";
	private static final String SERVICE_NAME = "Service.asmx";
	private static final String STAND_IN = "StandInId";
	private static final String LAST_STATE = "LastState";
	private static final String LAST_TIMESTAMP = "LastTimestamp";
	private static final String LAST_ACTION = "LastAction";
	private static final String SETTINGS = "settings";
	private static final Character SPLIT_CHAR = '~';
	private static final int    EOF = -1;
	//public static final int SINGLE_CLICK = R.id.rbClick;
	//public static final int DOUBLE_CLICK = R.id.rbImmediately;
	//public static final int LONG_CLICK = R.id.rbLongClick;
	//public static final int MENU_BASED = R.id.rbMenu;
	private static final String LAST_LONG_TIMESTAMP = "LastLongTimestamp";
	private static final String JANITOR = "Janitor";
	private static final String TASK_SELECT_BEHAVIOR = "TaskSelectBehavior";
	private static final String DEVICE_ID = "DeviceId";
	private static final String LAST_PROJECT = "LastProject";
	private static final String SHOW_COMMENT = "ShowComment";
	//public static final int TASK_SINGLE_CLICK = R.id.rbClickTask;
	//public static final int TASK_BUTTON = R.id.rbButtonTask;
	//public static final int TASK_MENU = R.id.rbMenuTask;
	public  static final String SWG_DEFAULT_SERVER_PATH = /*"http://mpze.cichons.de"; //*/"http://217.6.190.164:10080/mpze_neu";
	private static final String LAST_VIEWED_LIST = "LastViewedList";
	private static final String TASK_CHOICE_ICONS = "TaskChoiceIcons";
	private static final String LAST_TASK = "LastTask";
	private static final String USE_TASK_END_PANEL = "UseTaskEndPanel";
	private static final String SETTINGS_ID = "SettingsId";
	private static final String BRANDING = "Branding";
	private static final String SUFFIX = "Suffix";
	public  static final String GET_SETTINGS = "GetSettings";
	public  static final String GET_SETTINGS_BY_SUFFIX = "GetSettingsBySuffix";
	private static final int WEB_SETTINGS_LENGTH = 12;
	public  static final String MAIL_SUFFIX = "mailSuffix";


	private final String defaultMailAddress;
	private final String defaultServerPath;
	private static Settings instance, contextInstance;
	private static Context currentContext;
	private SharedPreferences preferences;
	private int listBehavior;
	private String userMailAddress;
	private String webServicePath;
	private boolean initialized;
	private ProjectSelectBehavior projectSelectBehavior;
	private TaskSelectBehavior taskSelectBehavior;
	private SendBehavior sendBehavior;
	private String serverPath;
	private Webservice webservice;
	private Janitor janitor;
	private Janitor standIn;
	private int standInId = -1;
	private int lastAction=-1;
	private String lastTimeStamp;
	private Long lastLongTimeStamp;
	//private String lastAction;
	private int deviceId;
	private boolean showComment;
	private Context context;
	private Project lastProject;
	private Task lastTask;
	private int lastViewedList=ViewedList.NONE.getValue();
	private boolean taskChoiceIcons;
	private boolean useTaskEndPanel;
	private String projectKonto;
	private String taskKonto;
	private int settingsId;
	private String suffix;
	private int branding;
	
	
	private Settings(Context context) {
		this.context = context;
		Settings.currentContext = context;
		//preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
		preferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
		List <String> mailAddresses = Mail.getMailAddresses(context);
		this.defaultMailAddress = mailAddresses == null ? "reinhold.back@swsg-sw.de" : mailAddresses.get(0);		
		this.defaultServerPath = SWG_DEFAULT_SERVER_PATH;
		initialized = false;
		if (preferences!=null) { 
			readSettings();
		}
	}
	
	private void readSettings() {
		this.initialized = preferences.getBoolean(INITIALIZED, false);
		this.serverPath =  preferences.getString(SERVER_PATH, "");
		this.userMailAddress = preferences.getString(USER_MAIL_ADDRESS, "");
		this.projectSelectBehavior = ProjectSelectBehavior.getById(preferences.getInt(SELECT_BEHAVIOR, 0));
		this.listBehavior = preferences.getInt(LIST_BEHAVIOR, 0);
		this.taskSelectBehavior = TaskSelectBehavior.getById(preferences.getInt(TASK_SELECT_BEHAVIOR, 0));
		this.sendBehavior = SendBehavior.getById(preferences.getInt(SEND_BEHAVIOR, 0));
		this.standInId = preferences.getInt(STAND_IN, -1);
		this.lastAction = preferences.getInt(LAST_ACTION, -1);
		this.lastTimeStamp = preferences.getString(LAST_TIMESTAMP, "");
		this.lastLongTimeStamp = preferences.getLong(LAST_LONG_TIMESTAMP, -1);
		this.projectKonto = preferences.getString(LAST_PROJECT, "");
		this.taskKonto = preferences.getString(LAST_TASK, "");
		this.showComment = preferences.getBoolean(SHOW_COMMENT, true);
		this.deviceId = preferences.getInt(DEVICE_ID, 200);
		this.lastViewedList = preferences.getInt(LAST_VIEWED_LIST, ViewedList.NONE.getValue());
		MyLog.d("Settings", "LastViewedList = "+lastViewedList);
		this.taskChoiceIcons = preferences.getBoolean(TASK_CHOICE_ICONS, false);
		this.useTaskEndPanel = preferences.getBoolean(USE_TASK_END_PANEL, false);
		this.branding = preferences.getInt(BRANDING, -1);
		this.suffix = preferences.getString(SUFFIX, "");
		this.settingsId = preferences.getInt(SETTINGS_ID, -1);
		//this.janitorId = preferences.getInt(JANITOR, -1);
		//this.lastState = preferences.getInt(LAST_STATE, -1);
		if (userMailAddress == null || userMailAddress.length()==0) {
			userMailAddress = defaultMailAddress;
		}
		
		if (serverPath == null || serverPath.length()==0) {
			serverPath = defaultServerPath;
		}
		
	}
	
	public void writeSettings() {
		Editor e = preferences.edit();
		e.putBoolean(INITIALIZED, true);
		e.putString(SERVER_PATH, serverPath);
		e.putString(USER_MAIL_ADDRESS, userMailAddress);
		e.putInt(LIST_BEHAVIOR, listBehavior);
		e.putInt(SEND_BEHAVIOR, sendBehavior.ordinal());
		e.putInt(SELECT_BEHAVIOR, projectSelectBehavior.ordinal());
		e.putInt(TASK_SELECT_BEHAVIOR, taskSelectBehavior.ordinal());
		e.putInt(DEVICE_ID, deviceId);
		e.putBoolean(SHOW_COMMENT, showComment);
		if (standIn!=null) {
			e.putInt(STAND_IN, standIn.getId());
		} else {
			e.putInt(STAND_IN, standInId);
		}
		e.putBoolean(TASK_CHOICE_ICONS, taskChoiceIcons);
		e.putBoolean(USE_TASK_END_PANEL, useTaskEndPanel);
		e.putInt(SETTINGS_ID, settingsId);
		e.putInt(BRANDING, branding);
		e.putString(SUFFIX, suffix);
		
		/*
		if (janitor!=null) {
			e.putInt(JANITOR, janitor.getId());
		}
		*/
		
		e.commit();
	}
	
	public void saveAtClose(Booking booking) {
		if (booking!=null) {
			this.setLastTimeStamp(booking.getTimestamp());
			this.setLastAction(booking.getAction());
			this.setLastProject(booking.getProject());
		}
		saveAtClose();
	}
	

	public void setLastProject(Project project) {
		this.lastProject = project;
	}

	public void saveAtClose() {
		Editor e = preferences.edit();
		e.putInt(LAST_ACTION, this.lastAction);
		e.putString(LAST_TIMESTAMP, this.lastTimeStamp);
		e.putLong(LAST_LONG_TIMESTAMP, this.lastLongTimeStamp);
		e.putString(LAST_PROJECT, lastProject == null ? null : this.lastProject.getKonto());
		e.putString(LAST_TASK, lastTask == null ? null : this.lastTask.getKonto());
		e.putInt(LAST_VIEWED_LIST, lastViewedList);
		MyLog.i("Setting","Saving lastViewedList = "+lastViewedList);
		e.commit();
	}
	
	public String getUserMailAddress() {
		return userMailAddress;
	}

	public void setUserMailAddress(String userMailAddress) {
		this.userMailAddress = userMailAddress;
	}

	public String getWebServicePath() {
		if (webServicePath == null) {
			if (serverPath!=null) {
				if (!serverPath.endsWith("/")) {
					webServicePath = serverPath+"/"+SERVICE_NAME;
				} else {
					webServicePath = serverPath+SERVICE_NAME;
				}
			}
		}
		return webServicePath;
	}
	
	public String getWebServicePath(String serverPath) {
		if (!serverPath.endsWith("/")) {
			webServicePath = serverPath+"/"+SERVICE_NAME;
		} else {
			webServicePath = serverPath+SERVICE_NAME;
		}
		return webServicePath;
	}
	
	public String getNamespace() {
		return NAMESPACE;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public SendBehavior getSendBehavior() {
		return sendBehavior;
	}

	public void setSendBehavior(SendBehavior sendBehavior) {
		this.sendBehavior = sendBehavior;
	}
	
	public void setSendBehavior(int sendBehavior) {
		this.sendBehavior = SendBehavior.getById(sendBehavior);
	}

	public static void init(Context context) {
		if (instance == null) {
			instance = new Settings(context);
		} else {
			instance.context = context;
		}
	}
	
	public static Settings getInstance() {
		//if (instance == null) {
		//	instance = new Settings(MainActivity.getLastInstance());
		//}
		return instance;
	}
	
	public static Settings getInstance(Context context) {
		if (contextInstance == null) {
			contextInstance = new Settings(context);
		}
		return contextInstance;
	}

	public void setServerPath(String serverPath) {
		// TODO Auto-generated method stub
		this.serverPath = serverPath;
		webServicePath=null;
		webservice=null;
		
	}
	
	public String getServerPath() {
		return this.serverPath;
	}
	
	public Webservice getWebservice() {
		if (webservice == null) {
			Webservice.init(getWebServicePath(), NAMESPACE);
			webservice = Webservice.getInstance();
		}
		return webservice;
	}
	
	public Janitor getStandIn() {
		if (standIn == null && standInId>-1) {
			standIn = EntitiesFactory.getInstance().getJanitors().find(standInId);
		}
		return standIn;
	}
	
	public void setStandIn(Janitor standIn) {
		this.standIn = standIn; 
		if (standIn!=null) {
			standInId = standIn.getId();
		} else {
			standInId = -1;
		}
	}
	
	public Janitor getJanitor() {
		if (this.janitor == null) {
			this.janitor = EntitiesFactory.getInstance().getJanitors().find("email", userMailAddress);
		}
		return janitor;
	}
	
	public void persistStandIn() {
		Editor e = preferences.edit();
		e.putInt (STAND_IN, standInId);
		e.commit();
	}
	
	public void setStandIn(int id) {
		this.standIn = EntitiesFactory.getInstance().getJanitors().find(id);
	}
	
	public Action getLastAction() {
		return Actions.getAction(lastAction);
	}
	
	public void setLastAction(Action action) {
		this.lastAction=Actions.getInstance().getActionIndex(action);
	}

	//public void setLastAction(int lastAction) {
	//	this.lastAction = lastAction;
	//}

	public Date getLastTimeStamp() {
		if (lastLongTimeStamp == null || lastLongTimeStamp.equals(-1)) {	
			return null;
		}
		return new Date(lastLongTimeStamp);
	}

	public void setLastTimeStamp(Date lastTimeStamp) {
		if (lastTimeStamp == null) {
			this.lastLongTimeStamp = null;
			lastLongTimeStamp = -1L;
			return;
		}
		this.lastLongTimeStamp = lastTimeStamp.getTime();
		this.lastTimeStamp = lastTimeStamp.toString();
	}
	
	
	public int getDeviceId() {
		return deviceId;
	}
	
	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
	
	public ProjectSelectBehavior getProjectSelectBehavior() {
		return projectSelectBehavior;
	}
	
	public void setProjectSelectBehavior( ProjectSelectBehavior projectSelectBehavior ) {
		this.projectSelectBehavior = projectSelectBehavior;
	}
	
	public void setProjectSelectBehavior(int projectSelectBehaviorId) {
		this.projectSelectBehavior = ProjectSelectBehavior.getById(projectSelectBehaviorId);
	}

	public TaskSelectBehavior getTaskSelectBehavior() {
		return taskSelectBehavior;
	}
	
	public void setTaskSelectBehavior(TaskSelectBehavior taskSelectBehavior) {
		this.taskSelectBehavior = taskSelectBehavior;
	}
	
	public void setTaskSelectBehavior (int taskSelectBehaviorId) {
		this.taskSelectBehavior = TaskSelectBehavior.getById(taskSelectBehaviorId);
	}
	
	public boolean showComment() {
		return this.showComment;
	}
	
	public void setShowComment(boolean showComment) {
		this.showComment = showComment;
	}

	public void persistLastValues() {
		Editor e = preferences.edit();
		e.putInt(LAST_ACTION, lastAction);
		e.putString(LAST_TIMESTAMP, lastTimeStamp);
		//e.putInt(LAST_STATE, lastState);
		e.commit();
	}
	
	public void persistLastValues(int lastAction, String lastTimeStamp) {
		this.lastAction = lastAction;
		this.lastTimeStamp = lastTimeStamp;
//		this.lastState = lastState;
		persistLastValues();
	}

	public void deInit() {
		Editor e = preferences.edit();
		e.putBoolean(INITIALIZED, false);
		e.commit();
	}

	public void setLastViewedList (ViewedList lastViewedList) {
		this.lastViewedList = lastViewedList.getValue();
	}
	
	public ViewedList getLastViewedList() {
		return ViewedList.getViewedList(lastViewedList);
	}

	public int getVibrationLength() {
		return 500;
	}
	
	public Context getContext() {
		return context;
	}
	
	public static Context getCurrentContext() {
		return currentContext;
	}

	public boolean isTaskChoiceIcons() {
		return taskChoiceIcons;
	}
	
	public void setTaskChoiceIcons(boolean taskChoiceIcons) {
		this.taskChoiceIcons = taskChoiceIcons;
	}

	public Project getLastProject() {
		if (lastProject == null) {
			EntitiesFactory.getInstance().getProjects().find("Konto", projectKonto);
		}
		return this.lastProject;
	}

	public Task getLastTask() {
		if (lastTask == null) {
			EntitiesFactory.getInstance().getTasks().find("Konto", taskKonto);
		}
		return this.lastTask;
	}
	
	public boolean isUseTaskEndPanel() {
		return useTaskEndPanel;
	}
	
	public void setUseTaskEndPanel(boolean useTaskEndPanel) {
		this.useTaskEndPanel = useTaskEndPanel;
	}

	public void setSettingsId(int id) {
		this.settingsId = id;
	}
	
	public int getSettingsId() {
		return settingsId;
	}
	
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	private void calcSuffix() {
		if (userMailAddress!=null) {
			String[] ss = userMailAddress.split("\\@");
			if (ss.length == 2) {
				suffix = ss[1];
			}
		}
	}
	
	public String getSuffix() {
		calcSuffix();
		return suffix;
	}
	
	public void setBranding(Branding branding) {
		this.branding = branding.getValue();
	}
	
	public void setBranding(int branding) {
		this.branding = branding;
	}
	
	public Branding getBranding() {
		return Branding.getBranding(branding);
	}

	
	private boolean getSettings(String settings) {
		String [] splitSettings = settings.split("\\|");
		
		if (splitSettings.length != WEB_SETTINGS_LENGTH) {
			return false;
		}
		
		int id = Integer.parseInt(splitSettings[0]);
		String suffix = splitSettings[1];
		String webAddress = splitSettings[2];
		int branding = Integer.parseInt(splitSettings[3]);
		int projectSelectMode = Integer.parseInt(splitSettings[4]);
		int taskSelectMode = Integer.parseInt(splitSettings[5]);
		int sendBookingMode = Integer.parseInt(splitSettings[6]);
		boolean listWithIcons = Boolean.parseBoolean(splitSettings[7]);
		boolean taskExplizitClose =  Boolean.parseBoolean(splitSettings[8]);
		int primionId = Integer.parseInt(splitSettings[9]);
		boolean showComment = Boolean.parseBoolean(splitSettings[10]);
		
		this.setSettingsId(id);
		this.setSuffix(suffix);
		this.setServerPath(webAddress);
		this.setBranding(branding);
		this.setProjectSelectBehavior(ProjectSelectBehavior.getById(projectSelectMode));
		this.setTaskSelectBehavior(TaskSelectBehavior.getById(taskSelectMode));
		this.setSendBehavior(SendBehavior.getById(sendBookingMode));
		this.setTaskChoiceIcons(listWithIcons);
		this.setUseTaskEndPanel(taskExplizitClose);
		this.setShowComment(showComment);
		this.setDeviceId(primionId);
		
		writeSettings(); //persistent it
		
		return true;
	}

	public boolean initFromWebservice(Webservice webservice) {
		String settings = null;
		if (defaultMailAddress == null ) {
			return false;
		}
		
		try {
			String suffix = defaultMailAddress.split("\\@")[1];
			if (suffix.equals("gmail.com")) {
				suffix = "swsg-sw.de";
			}
					
			settings = webservice.call(GET_SETTINGS_BY_SUFFIX, MAIL_SUFFIX, suffix);
			
		} catch (Exception e) {
			return false;
		}
		if (settings == null) {
			return false;
		}
		
		return getSettings(settings);
	}

	public boolean isSaveBookingsImmediate() {
		return true;
	}

	public void upload(FtpClient client) {
		StringBuffer settingSb = new StringBuffer();
		settingSb.append(listBehavior).append(SPLIT_CHAR);
		settingSb.append(userMailAddress).append(SPLIT_CHAR);
		settingSb.append(webServicePath).append(SPLIT_CHAR);
		settingSb.append(projectSelectBehavior.ordinal()).append(SPLIT_CHAR);
		settingSb.append(taskSelectBehavior.ordinal()).append(SPLIT_CHAR);
		settingSb.append(sendBehavior.ordinal()).append(SPLIT_CHAR);
		settingSb.append(serverPath).append(SPLIT_CHAR);
		settingSb.append(janitor).append(SPLIT_CHAR);
		settingSb.append(standIn).append(SPLIT_CHAR);
		settingSb.append(standInId).append(SPLIT_CHAR);
		settingSb.append(lastAction).append(SPLIT_CHAR);
		settingSb.append(lastTimeStamp).append(SPLIT_CHAR);
		settingSb.append(lastLongTimeStamp).append(SPLIT_CHAR);
		settingSb.append(deviceId).append(SPLIT_CHAR);
		settingSb.append(showComment).append(SPLIT_CHAR);
		settingSb.append(lastProject).append(SPLIT_CHAR);
		settingSb.append(lastTask).append(SPLIT_CHAR);
		settingSb.append(lastViewedList).append(SPLIT_CHAR);
		settingSb.append(taskChoiceIcons).append(SPLIT_CHAR);
		settingSb.append(useTaskEndPanel).append(SPLIT_CHAR);
		settingSb.append(projectKonto).append(SPLIT_CHAR);
		settingSb.append(taskKonto).append(SPLIT_CHAR);
		settingSb.append(settingsId).append(SPLIT_CHAR);
		settingSb.append(suffix).append(SPLIT_CHAR);
		settingSb.append(branding).append(SPLIT_CHAR);
		try {
			final byte[] bytes = settingSb.toString().getBytes("UTF-8");
			InputStream is = new InputStream() {

				int index = 0;
				
				@Override
				public int read() throws IOException {
					if (index < bytes.length) {
						return bytes[index++];
					}
					return -1;
				}
				
			};
			client.upload(is, SETTINGS);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void settingsGot(List<Byte> bytes) {
		byte[] byteArray = new byte[bytes.size()];
		
		int i=0;
		
		for (Byte b : bytes) {
			byteArray[i++] = b;
		}
		
		
		String s = new String(byteArray, Charset.forName("UTF-8"));
		String[] fields = s.split(SPLIT_CHAR.toString());
		i = 0;
		initialized = true;
		listBehavior  = Integer.parseInt(fields[i++]);
		userMailAddress = fields[i++];
		webServicePath = fields[i++];
		projectSelectBehavior = ProjectSelectBehavior.getById(Integer.parseInt(fields[i++]));
		taskSelectBehavior = TaskSelectBehavior.getById(Integer.parseInt(fields[i++]));
		sendBehavior = SendBehavior.getById(Integer.parseInt(fields[i++]));
		serverPath = fields[i++];
		Janitors janitors = EntitiesFactory.getInstance().getJanitors();
		janitor = janitors.find(Integer.parseInt(fields[i++]));
		standIn = janitors.find(Integer.parseInt(fields[i++]));
		standInId = Integer.parseInt(fields[i++]);
		lastAction = Integer.parseInt(fields[i++]);
		lastTimeStamp = fields[i++];
		lastLongTimeStamp = Long.parseLong(fields[i++]);
		deviceId = Integer.parseInt(fields[i++]);
		showComment = Boolean.parseBoolean(fields[i++]);
		lastProject = EntitiesFactory.getInstance().getProjects().find(Integer.parseInt(fields[i++]));
		lastTask = EntitiesFactory.getInstance().getTasks().find(Integer.parseInt(fields[i++]));
		lastViewedList = Integer.parseInt(fields[i++]);
		taskChoiceIcons = Boolean.parseBoolean(fields[i++]);
		useTaskEndPanel = Boolean.parseBoolean(fields[i++]);
		projectKonto = fields[i++];
		taskKonto = fields[i++];
		settingsId = Integer.parseInt(fields[i++]);
		suffix = fields[i++];
		branding = Integer.parseInt(fields[i++]);
		
		/*
		this.serverPath =  preferences.getString(SERVER_PATH, "");
		this.userMailAddress = preferences.getString(USER_MAIL_ADDRESS, "");
		this.projectSelectBehavior = ProjectSelectBehavior.getById(preferences.getInt(SELECT_BEHAVIOR, 0));
		this.listBehavior = preferences.getInt(LIST_BEHAVIOR, 0);
		this.taskSelectBehavior = TaskSelectBehavior.getById(preferences.getInt(TASK_SELECT_BEHAVIOR, 0));
		this.sendBehavior = SendBehavior.getById(preferences.getInt(SEND_BEHAVIOR, 0));
		this.standInId = preferences.getInt(STAND_IN, -1);
		this.lastAction = preferences.getInt(LAST_ACTION, -1);
		this.lastTimeStamp = preferences.getString(LAST_TIMESTAMP, "");
		this.lastLongTimeStamp = preferences.getLong(LAST_LONG_TIMESTAMP, -1);
		this.projectKonto = preferences.getString(LAST_PROJECT, "");
		this.taskKonto = preferences.getString(LAST_TASK, "");
		this.showComment = preferences.getBoolean(SHOW_COMMENT, true);
		this.deviceId = preferences.getInt(DEVICE_ID, 200);
		this.lastViewedList = preferences.getInt(LAST_VIEWED_LIST, ViewedList.NONE.getValue());
		MyLog.d("Settings", "LastViewedList = "+lastViewedList);
		this.taskChoiceIcons = preferences.getBoolean(TASK_CHOICE_ICONS, false);
		this.useTaskEndPanel = preferences.getBoolean(USE_TASK_END_PANEL, false);
		this.branding = preferences.getInt(BRANDING, -1);
		this.suffix = preferences.getString(SUFFIX, "");
		this.settingsId = preferences.getInt(SETTINGS_ID, -1);
		//this.janitorId = preferences.getInt(JANITOR, -1);
		//this.lastState = preferences.getInt(LAST_STATE, -1);
		if (userMailAddress == null || userMailAddress.length()==0) {
			userMailAddress = defaultMailAddress;
		}
		*/
		if (serverPath == null || serverPath.length()==0) {
			serverPath = defaultServerPath;
		}
		writeSettings();
		saveAtClose();
	}

	public void download(FtpClient client) {

		OutputStream os = new OutputStream() {
			final ArrayList<Byte> bytes = new ArrayList<Byte>();
			
			@Override
			public void write(int character) throws IOException {
				if (character == EOF) {
					settingsGot(bytes);
				}
				bytes.add((byte) character);
			}
			
		};
		
		try {
			client.download("settings", os);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Settings.clear();
	}

	public static void clear() {
		// TODO Auto-generated method stub
		Settings.instance = null;
	}
}
