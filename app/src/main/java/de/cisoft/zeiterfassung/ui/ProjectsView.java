package de.cisoft.zeiterfassung.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.cisoft.framework.android.ui.ActivityWithErrorhandling;
import de.cisoft.framework.android.ui.ErrorNotifier;
import de.cisoft.framework.webservice.Webservice;
import de.cisoft.utility.FTPAccess;
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
import de.cisoft.zeiterfassung.implementation.entity.Tasks;
import de.cisoft.zeiterfassung.implementation.enums.ViewedList;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;
import de.cisoft.zeiterfassung.implementation.helpers.settings.enums.ProjectSelectBehavior;
import de.cisoft.zeiterfassung.ui.enums.Branding;
import de.cisoft.zeiterfassung.widget.BookingsListWidget;
import de.cisoft.zeiterfassung.widget.BookingsWidget;

public class ProjectsView extends ActivityWithErrorhandling implements ActivityWithErrorNotifier {

	private static final String COMMENT = "comment";
	private static final String TASK_ID = "taskId";
	private static final String EXIT = "exit";
	private static final int MY_REQUEST = 1;
	private static final int FROM_CHOOSE_STANDIN = 2;
	private static final long OFFSET = 60*60*1000;
	@SuppressLint("SimpleDateFormat")
	private static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	private ListView lvProjects;
	private ArrayAdapter<String> myObjectsAdapter;
	private Projects projects;
	private Menu menu;
	private Webservice webservice;
	//private Tasks tasks;
	private Actions actions;
	protected int choosenPosition;
	protected CharSequence choosenText;
	private MenuItem miMyObjects;
	private MenuItem miAllObjects;
	private MenuItem lastMenuItem;
	private MenuItem miStandInObjects;
	private MenuItem miBothObjects;
	//private ListAdapter allObjectsAdapter;
	//private ArrayAdapter<String> bothObjectsAdapter;
	//private ArrayAdapter<String> standInObjectsAdapter;
	private EditText etSearch;
	private Action action;
	private Bookings bookings;
	private Project project;
	private ViewedList lastViewedList;
	private TextView tvProjectTask;
	private TextView tvElapsedTime;
	private TextView tvWaitTaskEnd;
	private TextView tvProjectInWaitPanel;
	private Button btWaitTaskEnd;
	private LinearLayout waitTaskEndPanel;
	private LinearLayout buttonPanel;
	private Button btFunction;
	private Button btEndWork;
	private Booking initialBooking;
	private Button btBreak;
	private Button btWorkEndPanel;
	private static boolean taskEndPanelVisible = Settings.getInstance().isUseTaskEndPanel();
	private static boolean withIcons = Settings.getInstance().isTaskChoiceIcons();

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.setLastInstance(this); // reset lastInstance while MainActivity will cancel after a short while
        //MyLog.i("ProjectsView", "Vor Projects");
        //requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        setContentView(R.layout.activity_projects_view);
        //MyLog.i("ProjectsView", "before creating projects");

        
        //MyLog.i("Main","after setContentView");
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        //Webservice.init("http://217.6.190.164:10080/mpze_neu/Service.asmx", "http://gfi-informatik.de/MPZE");
        webservice = Webservice.getInstance();
        
        if (projects == null) {
        	initEntities();
        }
        
        
        this.initialBooking = bookings.getLastBooking();
        
        createView();
        
		this.tvProjectTask.setText(getLastTaskName(initialBooking));
        
        checkForDateOverflow();
        
        checkForValidLicenseInBackground();
        
        ErrorNotifier.getInstance().addHandler(this);
    }
	
    
    private void initEntities() {
       	try {
    		this.lastViewedList = Settings.getInstance().getLastViewedList();
    		if (lastViewedList == ViewedList.NONE) {
    			lastViewedList = ViewedList.MY_OBJECTS;
    		}
    		
    		projects = EntitiesFactory.getInstance().getProjects(lastViewedList);
    		bookings = EntitiesFactory.getInstance().getBookings();
    		actions = Actions.getInstance();
    		if (bookings==null) {
    			this.tvProjectTask.setText("----"); 
    			return;
    		}
    		Booking booking = bookings.getLastBookingNotNull();

    		if (booking == null) {
    			return;
    		} //System.out.println("In initEntities");
			actions.setCurrentAction(booking.getAction());
			this.initialBooking = booking;
			if (booking.getProject()==null) {
				booking = bookings.getLastBookingWithProject();
			} else {
			}

			actions.setCurrentProject(booking.getProject());
			actions.setCurrentTask(booking.getTask());

    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
    private String getLastTaskName(Booking booking) {
		if (booking == null || booking.getAction() == Actions.ENDE) {
			return "Freizeit";
		}
		if (booking.getAction() == Actions.PAUSE) {
			return "Pause";
		}
		if (booking.getProject() != null) {
			return booking.getProject().getDescription()+(booking.getTask() != null ? " / "+booking.getTask().getName() : "");
		}
		return "======";
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_projects_view, menu);
        this.menu = menu;
        actions.addTo(menu);
        initMenuEntries(menu);
        return true;
    }

    private void initMenuEntries(Menu menu) {
    	miMyObjects = menu.findItem(R.id.menu_choose_my_objects);
    	if (miMyObjects.isChecked()) {
    		lastMenuItem = miMyObjects;
    	}
    	miAllObjects = (MenuItem) menu.findItem(R.id.menu_choose_all);
    	if (miAllObjects.isChecked()) {
    		lastMenuItem = miAllObjects;
    	}
    	miStandInObjects = (MenuItem) menu.findItem(R.id.menu_choose_standins_objects);
    	if (miStandInObjects.isChecked()) {
    		lastMenuItem = miStandInObjects;
    	}
    	miBothObjects = (MenuItem) menu.findItem(R.id.menu_choose_both);
    	if (miBothObjects.isChecked()) {
    		lastMenuItem = miBothObjects;
    	}
    	boolean uncheck = false;
    	switch (lastViewedList) {
    		case STANDINS_OBJECTS : lastMenuItem = miStandInObjects; uncheck = true; break;
    		case BOTH_OBJECTS : lastMenuItem = miBothObjects; uncheck = true; break;
    		case ALL_OBJECTS : lastMenuItem = miAllObjects; uncheck = true; break;
		default:
			break;
    	}
    	if (uncheck) {
    		lastMenuItem.setChecked(true);
    		miMyObjects.setChecked(false);
    	}
		boolean enabled = Settings.getInstance().getStandIn() != null;
		miStandInObjects.setEnabled(enabled);
		miBothObjects.setEnabled(enabled);

    }

    private void manageState() {
    	Action action = actions.getCurrentAction();
    	if (action.notWorking() && !action.doEnd()) {
    		action = actions.getEndAction();
    		bookings.add(new Booking(EntitiesFactory.getInstance().getJanitor(), action));
    		bookings.sendLastBooking(webservice);
    		//actions.setCurrentAction(action);
    		//actions.changeMenu(menu);
    	}
    	this.action = actions.getNextWorkingStateAction(action);
    }
    
    private void choose() {
    	if (choosenText == null || choosenText.length()==0) {
    		return;
    	}
    	Intent chooseTaskIntent = new Intent(this, ChooseTaskView.class);
    	Project project = withIcons ? projects.getByName(choosenText) : projects.getByText(choosenText);
    	if (project == null) {
    		return;
    	}
    	this.project = project;
    	MyLog.d("ProjectsView", "text = " + choosenText +" project = "+project.toString());
    	chooseTaskIntent.putExtra("ProjectId", project.getId());
		chooseTaskIntent.putExtra("ProjectName", this.choosenText);
		chooseTaskIntent.putExtra("ActionShotcut", actions.getWorkingStateAction().getKonto());
		//chooseTaskIntent.putExtra("TextParam", action.getTextParam());
		startActivityForResult(chooseTaskIntent, MY_REQUEST);
		
		//startActivity(chooseTaskIntent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == MY_REQUEST) {
    		if (resultCode == RESULT_CANCELED) {
    			if (data == null) {
    				return;
    			}
    			Bundle extras = data.getExtras();
    			if (extras.getBoolean(EXIT)) {
    				finish();
    				return;
    			}
    		}
    		if (resultCode == RESULT_OK) {
    			Bundle extras = data.getExtras();
    			Integer taskId = extras.getInt(TASK_ID);
    			String comment = extras.getString(COMMENT);
    			Task task = EntitiesFactory.getInstance().getTasks().find(taskId);
            	Janitor janitor = Settings.getInstance().getJanitor();
            	manageState();
            	Booking booking = new Booking(janitor, project, action, task, comment);
            	EntitiesFactory.getInstance().getBookings().add(booking);
            	//EntitiesFactory.getInstance().sendBooking(booking);
            	EntitiesFactory.getInstance().sendBookings();
            	actions.setCurrentProject(project);
            	actions.setCurrentTask (task);
        		actions.setCurrentAction(action);
        		actions.changeMenu(menu);
        		
        		refreshWidget(project, task, action);
        		
        		MyLog.d("ProjectsView.onActivityResult", "Changed from "+action+" to "+actions.getCurrentAction());
        		MyLog.d("ProjectsView.onActivityResult", "Project was: "+project);
        		if (taskEndPanelVisible) {
        			btWaitTaskEnd.setText(task.getName()+" beenden");
        			waitTaskEndPanel.setVisibility(LinearLayout.VISIBLE);
        			buttonPanel.setVisibility(LinearLayout.GONE);
        		}
    		}
    	} else if (requestCode == FROM_CHOOSE_STANDIN) {
    		if (resultCode == RESULT_OK) {
        		lvProjects.setAdapter(getMyObjectsAdapter());
        		//EntitiesFactory.getInstance().resetStandIn();
        		//this.standInObjectsAdapter = null;
        		//this.bothObjectsAdapter = null;
        		miMyObjects.setChecked(true);
        		lastMenuItem.setChecked(false);
        		lastMenuItem = miMyObjects;
        		lastViewedList = ViewedList.MY_OBJECTS;
        		boolean enabled = Settings.getInstance().getStandIn() != null;
       			miStandInObjects.setEnabled(enabled);
       			miBothObjects.setEnabled(enabled);
    		}
    	}
    }
    
    private void refreshWidget(Project project, Task task, Action action) {
		if (project != null) {
			Intent widgetRefresh = new Intent(this, BookingsWidget.class);
			if (project != null) {
				widgetRefresh.putExtra(BookingsWidget.PROJECT_ID, project.getId());
			}
			widgetRefresh.putExtra(BookingsWidget.TASK_ID, task == null ? -1 : task.getId());
			widgetRefresh.putExtra(BookingsWidget.ACTION_ID, action.getKonto());
			widgetRefresh.setAction(BookingsWidget.ACTION_REFRESH);
			sendBroadcast(widgetRefresh);
		}
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	int id = item.getItemId();
    	
    	if (id<100) {
    		this.action = actions.getPossibleAction(id);
    		MyLog.i("ProjectsView.menu", "Action = "+action.toString());
    		if (this.action.doChoose()) { 
    			choose();
    			return true;
    		}  
    		if (action.doBreak() || action.doEnd() || action.doBreakEnd()) {
    			bookings.add(new Booking(Settings.getInstance().getJanitor(), action));
    			bookings.sendLastBooking(webservice);
    			if (action.doEnd()) {
    				saveAll();
    			}
    			refreshWidget(project, null, action);
    		}
    		
    		actions.setCurrentAction(action);
    		actions.changeMenu(menu);
    		return true;
    	}
        switch (id) {
        	case R.id.menu_show_bookings: {
        		try {
        			Intent k = new Intent(this, StandardBookingView.class);
        			startActivityForResult(k,MY_REQUEST);
        		} catch (Exception ex) {
        			ex.printStackTrace();
        		}
        		return true;
        	}
        	case R.id.menu_stand_in: {
        		try {
        			Intent k = new Intent(this, StandInView.class);
        			startActivityForResult(k, FROM_CHOOSE_STANDIN);
        		} catch (Exception ex) {
        			ex.printStackTrace();
        		}
        		return true;
        	}
        	case R.id.menu_settings : {
        		try {
        			showDialog();
        		} catch (Exception ex) {
        			throw new RuntimeException(ex);
        		}
        		return true;
        	}
        	case R.id.menu_show_logs : {
        		return showLogs();
        	}
        	case R.id.menu_shownObjects : { 
        		return onSubmenuEntry(item.getSubMenu().getItem());
        	}
        	case R.id.menu_choose_my_objects: {
        		//lvProjects.setAdapter(getMyObjectsAdapter());
        		projects = EntitiesFactory.getInstance().getMyProjects();
            	int rowLayout = withIcons ? R.layout.project_list_row_with_icon : R.layout.textview_project_list; 
            	ArrayAdapter<Project> adapter = projects.getAdapter(this, rowLayout);
            	lvProjects.setAdapter(adapter);
        		miMyObjects.setChecked(true);
        		lastMenuItem.setChecked(false);
        		lastMenuItem = miMyObjects;
        		lastViewedList = ViewedList.MY_OBJECTS;
        		break;
        	}
        	case R.id.menu_choose_all : {
        		//lvProjects.setAdapter(getAllObjectsAdapter());
        		projects = EntitiesFactory.getInstance().getProjects();
            	int rowLayout = withIcons ? R.layout.project_list_row_with_icon : R.layout.textview_project_list; 
            	ArrayAdapter<Project> adapter = projects.getAdapter(this, rowLayout);
            	lvProjects.setAdapter(adapter);
        		miAllObjects.setChecked(true);
        		lastMenuItem.setChecked(false);
        		lastMenuItem = miAllObjects;
        		lastViewedList = ViewedList.ALL_OBJECTS;
        		break;
        	}
        	case R.id.menu_choose_both : {
        		//ListAdapter adapter = getBothObjectsAdapter();
        		//if (adapter == null) {
        		//	break;
        		//}
        		//lvProjects.setAdapter(adapter);
        		Projects projects = EntitiesFactory.getInstance().getMergedProjects();
        		if (projects == null) {
        			break;
        		}
        		this.projects = projects;
            	int rowLayout = withIcons ? R.layout.project_list_row_with_icon : R.layout.textview_project_list; 
            	ArrayAdapter<Project> adapter = projects.getAdapter(this, rowLayout);
            	lvProjects.setAdapter(adapter);
        		miBothObjects.setChecked(true);
        		lastMenuItem.setChecked(false);
        		lastMenuItem = miBothObjects;
        		lastViewedList = ViewedList.BOTH_OBJECTS;
        		break;
        	}
        	case R.id.menu_choose_standins_objects : {
        		//ListAdapter adapter = getStandInObjectsAdapter();
        		//if (adapter == null) {
        		//	break;
        		//}
        		//lvProjects.setAdapter(adapter);
        		Projects projects = EntitiesFactory.getInstance().getStandInProjects();
        		if (projects == null) {
        			break;
        		}
        		this.projects = projects;
            	int rowLayout = withIcons ? R.layout.project_list_row_with_icon : R.layout.textview_project_list; 
            	ArrayAdapter<Project> adapter = projects.getAdapter(this, rowLayout);
            	lvProjects.setAdapter(adapter);
        		lastMenuItem.setChecked(false);
        		miStandInObjects.setChecked(true);
        		lastMenuItem = miStandInObjects;
        		lastViewedList = ViewedList.STANDINS_OBJECTS;
        		break;
        	}
        	case R.id.menu_app_end : {
        		finish();
        		break;
        	}
        	case R.id.menu_reload_settings : {
        		reloadSettings();
        		finish();
        		break;
        	}
        	case R.id.menu_about : {
        		Intent listRefreshIntent = new Intent(this, BookingsListWidget.class);
        		listRefreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        		this.sendBroadcast(listRefreshIntent);
        		Intent about = new Intent(this, AboutView.class);
        		startActivity(about);
        		break;
        	}
        	case R.id.menu_show_search_field : {
        		hideShowSearchField();
        		break;
        	}
            case de.cisoft.zeiterfassung.R.id.menu_test_k2soap:
                //NavUtils.navigateUpFromSameTask(this);
            	try {
            		Intent w = new Intent(this, BookingsWidget.class);
            		w.setAction(BookingsWidget.ACTION_REFRESH);
            		sendBroadcast(w);
                	Intent k = new Intent(this, K2SoapView.class);
                	startActivity(k);
                } catch (Exception ex) {
                	throw new RuntimeException(ex);
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private boolean showLogs() {
    	new Thread() {
    		@Override
    		public void run() {
    	    	try {
    				new FTPAccess().upload();
    			} catch (IOException e) {
    				throw new RuntimeException(e);
    			}
    		}
    	}.start();
	
		AlertDialog.Builder builder = new AlertDialog.Builder(ProjectsView.this);
		builder.setMessage("Logs und Daten hochgeladen");
		builder.setPositiveButton("Daten hochgeladen", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int button) {
				dialog.dismiss();
				
			}

			
		}).create().show();
    			
    	//Intent logs = new Intent(this, LogViewActivity.class);
    	//startActivity(logs);
    	return true;
    }
    
    private void reloadSettings() {
    	String serverAddress = Settings.getInstance().getServerPath();
		Webservice.init(
				Settings.getInstance().getWebServicePath(serverAddress),
				Settings.getInstance().getNamespace());
		Webservice service = Webservice.getInstance();
		String v = null;
		try {
			v = service.call("Version");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (v==null) {
			showAlertDialog("Server nicht gefunden");
			return;
		}
    	EntitiesFactory factory = EntitiesFactory.getInstance();
    	ProgressDialog progressDialog = new ProgressDialog(this);
    	factory.initFromWebservice(progressDialog);
    	factory.initPersonalData(Settings.getInstance().getUserMailAddress(), true);
    	if (factory.getJanitor()==null) {
    		showAlertDialog("Benutzer nicht gefunden");
    		Settings.getInstance().deInit();
    		return;
    	}
    	factory.save();
    }
    
    
    private void showDialog() {
    	final AlertDialog.Builder adb = new AlertDialog.Builder(this);
    	final EditText et = new EditText(this);
    	/*
    	DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == AlertDialog.BUTTON_POSITIVE && !"admin123".equals(et.getText())) {
					((AlertDialog)dialog).setMessage("Falsches Passwort. Bitte neu eingeben");
				} else {
					dialog.dismiss();
				}
			}
		};
    	*/
    	adb.setPositiveButton("OK", null);
    	adb.setNegativeButton("Abbruch", null);
    	final AlertDialog ad = adb.create();
    	ad.setCancelable(false);
    	ad.setCanceledOnTouchOutside(false);
    	ad.setMessage("Kennwort bitte!");
    	et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    	et.setText("");
    	ad.setView(et);
    	ad.show();
    	ad.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				MyLog.i("LoginDialog",et.getText().toString());
				if (!"admin123".equals(et.getText().toString())) {
					ad.setMessage("Falsches Passwort. Bitte neu eingeben");
				} else {
					ad.dismiss();
        			Intent k = new Intent(ProjectsView.this, SettingsView.class);
        			startActivityForResult(k, MY_REQUEST);
				}
				
			}
		});
    }
    
    private boolean onSubmenuEntry(MenuItem item) {
    	return true;
 	}

    private ListAdapter getMyObjectsAdapter() {
    	if (myObjectsAdapter == null) {
    		int rowLayout = withIcons ? R.layout.project_list_row_with_icon : R.layout.textview_project_list;
    		myObjectsAdapter = new ArrayAdapter<String>(this, rowLayout, EntitiesFactory.getInstance().getMyProjects().toSortedStringArray());
    	}
    	return this.myObjectsAdapter;
    }
    
    /*
	private ListAdapter getAllObjectsAdapter() {
    	if (allObjectsAdapter == null) {
    		int rowLayout = withIcons ? R.layout.project_list_row_with_icon : R.layout.textview_project_list;
    		allObjectsAdapter = new ArrayAdapter<String>(this, rowLayout, EntitiesFactory.getInstance().getProjects().toSortedStringArray());
    	}
    	return this.allObjectsAdapter;
	}

	private ListAdapter getBothObjectsAdapter() {
		if (Settings.getInstance().getStandIn() == null) {
			showStandinAlert();
			return null;
		}
    	if (bothObjectsAdapter == null) {
    		int rowLayout = withIcons ? R.layout.project_list_row_with_icon : R.layout.textview_project_list;
    		bothObjectsAdapter = new ArrayAdapter<String>(this, rowLayout, EntitiesFactory.getInstance().getMergedProjects().toSortedStringArray());
    	}
    	return this.bothObjectsAdapter;
	}

	private ListAdapter getStandInObjectsAdapter() {
		if (Settings.getInstance().getStandIn() == null) {
			showStandinAlert();
			return null;
		}
    	if (standInObjectsAdapter == null) {
    		int rowLayout = withIcons ? R.layout.project_list_row_with_icon : R.layout.textview_project_list;
    		standInObjectsAdapter = new ArrayAdapter<String>(this, rowLayout, EntitiesFactory.getInstance().getStandInProjects().toSortedStringArray());
    	}
    	return this.standInObjectsAdapter;
	}

	private void showStandinAlert() {
		AlertDialog ad = new AlertDialog.Builder(this).create();
		ad.setTitle("Fehlende Angaben");
		ad.setMessage("Eine Vertretung wurde noch nicht ausgewählt");
		ad.setButton("OK",new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		ad.setCancelable(false);
		ad.show();
	}
	*/
	
	private EditText createSearchView() {
		EditText etSearch = (EditText) this.findViewById(R.id.etSearch);
		etSearch.setVisibility(EditText.GONE);
		etSearch.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable arg0) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				filter(s);
			}
			
		});
		
		return etSearch;
	}
	
	private void filter(CharSequence s) {
		ListAdapter adapter = lvProjects.getAdapter();
		if (adapter == null || !(adapter instanceof ArrayAdapter)) {
			return;
		}
		Filter filter = ((ArrayAdapter<?>)adapter).getFilter();
		filter.filter(s);
	}
	
	private void hideShowSearchField() {
		if (etSearch.getVisibility()==EditText.VISIBLE) {
			etSearch.setVisibility(EditText.GONE);
			filter("");
		} else {
			etSearch.setVisibility(EditText.VISIBLE);
			etSearch.requestFocus();
			//filter(etSearch.getText());
		}
	}
	
	private TextView getTextView (View parentView) {
		RelativeLayout relativeParentView = (RelativeLayout) parentView;
		for (int i=0; i<relativeParentView.getChildCount(); i++) {
			View child = relativeParentView.getChildAt(i);
			if (child instanceof TextView) {
				return (TextView) child; 
			}
		}
		return null;
	}

	private ListView createProjectsList() {
    	ListView lvProjects = (ListView) this.findViewById(R.id.lvProjects);
    	int rowLayout = withIcons ? R.layout.project_list_row_with_icon : R.layout.textview_project_list; 
    	ArrayAdapter<Project> adapter = projects.getAdapter(this, rowLayout);
    	lvProjects.setAdapter(adapter);
    	lvProjects.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (Settings.getInstance().getProjectSelectBehavior().equals(ProjectSelectBehavior.LongClick)) {
					choose();
					return true;
				}
				return false;
			}
 		});
    	lvProjects.setOnItemClickListener(new OnItemClickListener() {
			private View selectedView;
			//private Color standardBackgroundColor;
			private long lastMs;

			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				/*
				if (selectedView!=null) {
					if (selectedView instanceof TextView) {
						((TextView)selectedView).setTextColor(Color.BLACK);
					} else {
						getTextView(selectedView).setTextColor(Color.BLACK); 
					}
					selectedView.setBackgroundDrawable(null); //Color(Color.GRAY);
				}
				view.setBackgroundColor(Color.LTGRAY);
				*/
				
				TextView textView = null;
				if (view instanceof TextView) {
					textView = (TextView) view;
				} else {
					textView = getTextView (view);					 
				}
				
				
				//textView.setTextColor(Color.BLUE);
				
				CharSequence text = textView.getText();
				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
				ProjectsView.this.choosenPosition = position;
				ProjectsView.this.choosenText = text;
				if (Settings.getInstance().getProjectSelectBehavior().equals(ProjectSelectBehavior.Click)) {
					choose();
				} 
				Date date = new Date();
				if (Settings.getInstance().getProjectSelectBehavior().equals(ProjectSelectBehavior.DoubleClick)) {
					if (selectedView.equals(view) && (date.getTime()-lastMs)<1000  ) {
						choose();
					}
				}
				
				this.selectedView = view;
				this.lastMs = date.getTime();
			}
		});
		return lvProjects;
	}
	
	
	private View getQuestionLayoutFromRes() {
		LayoutInflater li = LayoutInflater.from(this);
		final View layout = li.inflate(R.layout.dialog_choose_last_workend, lvProjects);
		layout.setBackgroundColor(Color.LTGRAY);
		return layout;
	}
	
	private void handleDialog(Dialog dialog) {
		final EditText etTimeHr = (EditText) dialog.findViewById(R.id.etTimeHr);
		final EditText etTimeMin = (EditText) dialog.findViewById(R.id.etTimeMin);
		final TextView tvColon = (TextView) dialog.findViewById(R.id.tvColon); 
		RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.rgChooseIfBookWorkend);
		//final RadioButton rbIsOk = (RadioButton) dialog.findViewById(R.id.rbIsOk);
		//final RadioButton rbEnter = (RadioButton) dialog.findViewById(R.id.rbEnter);
		etTimeHr.setText("");
		etTimeMin.setText("");
		/*
		OnFocusChangeListener fcl = new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				((EditText)v).setText("");
			}
		};
		etTimeHr.setOnFocusChangeListener(fcl);
		etTimeMin.setOnFocusChangeListener(fcl);
		*/
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int visibility = group.getCheckedRadioButtonId() == R.id.rbIsOk ? EditText.INVISIBLE : EditText.VISIBLE;
				etTimeHr.setVisibility(visibility);
				etTimeMin.setVisibility(visibility);
				tvColon.setVisibility(visibility);
				if (visibility == EditText.INVISIBLE) {
					etTimeHr.setText("");
					etTimeMin.setText("");
				}
			}
		});
	}
	
	private void sendEndBooking(Date d) {
		Booking booking = new Booking(Settings.getInstance().getJanitor(), Actions.ENDE);
		booking.setTimestamp(d);
		Booking lastBooking = EntitiesFactory.getInstance().getBookings().getLastBooking();
		if (lastBooking != null && lastBooking.getProject() != null) {
			booking.setProject(lastBooking.getProject());
		}
		bookings.add(booking);
		bookings.sendLastBooking(webservice);
	}
	
	private void sendUnconditionalEndBooking() {
		Booking lastBooking = bookings.getLastBooking();
		if (lastBooking == null) {
			if (bookings.getPreLastBooking() != null) {
				lastBooking = bookings.getPreLastBooking();
			} else {
				return;
			}
		}
		if (lastBooking.getAction() == Actions.ENDE) {
			return;
		}
		Date lastBookingsTimestamp = lastBooking.getTimestamp();
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(lastBookingsTimestamp);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour < 17) {
			calendar.set(Calendar.HOUR_OF_DAY, 17);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
		} else {
			calendar.add(Calendar.MINUTE, 1);
		}
		sendEndBooking(calendar.getTime());
		// if (actions.getCurrentAction()==Actions.PAUSE) 
	}
	
	private void resetBookingStatus() {
		// removed 16.10.2021 on request J.Aparisi
		// sendUnconditionalEndBooking();
		actions.setCurrentAction(Actions.ENDE);
		this.tvProjectTask.setText("======");
	}
	
	
	private void showQuestionDialog() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				final EditText etTimeHr = (EditText) ((Dialog)dialog).findViewById(R.id.etTimeHr);
				final EditText etTimeMin = (EditText) ((Dialog)dialog).findViewById(R.id.etTimeMin);
				//final RadioGroup rg = (RadioGroup) ((Dialog)dialog).findViewById(R.id.rgChooseIfBookWorkend);
				final RadioButton rb = (RadioButton) ((Dialog)dialog).findViewById(R.id.rbIsOk);
				if (rb.isChecked()) {
					return;
				}
				Integer h = null;
				Integer m = null;
				try {
					h = Integer.parseInt(etTimeHr.getText().toString());
					m = Integer.parseInt(etTimeMin.getText().toString());
				} catch (Exception e) {
					//swallow, it's only because the conversion fails
				}
				if (h!=null && m!=null && h<24 && m<60) {
					Date d = Settings.getInstance().getLastTimeStamp();
					Long t = d.getTime();
					t /= 1000*60*60*24;
					t *= 1000*60*60*24;
					t += (h*60+m)*60*1000;
					d.setTime(t);
					sendEndBooking(d);
					dialog.dismiss();
				} else {
					dialog.dismiss();
					showQuestionDialog();
				}
			}
		});
		dialogBuilder.setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				ProjectsView.this.finish();
			}
		});
		dialogBuilder.setView(getQuestionLayoutFromRes());
		AlertDialog dialog = dialogBuilder.create();
		//dialog.setCancelable(false);
		
		dialog.show();
		handleDialog(dialog);
		/*
		Dialog dialog = new Dialog(this);
		dialog.addContentView(getQuestionLayout(dialog), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		dialog.show();
		*/
	}
	
	
	private void endOfWork() {
		changeAction(Actions.ENDE);
	}
	
	private void doButtonFunction() {
		if (actions.getCurrentAction().doBreak()) {
			doBreakEnd();
			return;
		} 
		doStartBreak();
	}
	
	private void doBreakEnd() {
		changeAction(Actions.PAUSE_ENDE);
		if (taskEndPanelVisible && waitTaskEndPanel.getVisibility()!=LinearLayout.VISIBLE) {
			waitTaskEndPanel.setVisibility(LinearLayout.VISIBLE);
		}
		refreshWidget(project, null, Actions.PAUSE_ENDE);
	}
	
	private void doStartBreak() {
		changeAction(Actions.PAUSE);
		refreshWidget(project, null, Actions.PAUSE);
	}
	
	private void changeAction(Action action) {
		bookings.add(new Booking(Settings.getInstance().getJanitor(), action));
		bookings.sendLastBooking(webservice);
		actions.setCurrentAction(action);
		actions.changeMenu(menu);
	}

	
	private void createView() {
		//getWindow().setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, R.drawable.ic_action_search);
		etSearch = createSearchView();
		//instanceTitleBarListenener();
		lvProjects = createProjectsList();
		if (Settings.getInstance().getLastTimeStamp()==null) {
			return;
		}
		tvElapsedTime = (TextView) findViewById(R.id.tvTimeElapsed);
		tvProjectTask = (TextView) findViewById(R.id.tvProjectTask);
		actions.setTextView(tvProjectTask);
		btEndWork = (Button) findViewById(R.id.btWorkEnd);
		btEndWork.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				endOfWork();
			}
		});
		
		btFunction = (Button) findViewById(R.id.btFunction);
		btFunction.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doButtonFunction();
			}
		});
		actions.setFuctionButton(btFunction);
		actions.setEndWorkButton(btEndWork);
		if (MainActivity.BRANDING) {
			int orange = getResources().getColor(R.color.Orange);
			findViewById(R.id.rlHeadupPanel).setBackgroundColor(orange);
			findViewById(R.id.llButtonPanel).setBackgroundColor(orange);
		}

		buttonPanel = (LinearLayout) btFunction.getParent();
		
		tvWaitTaskEnd = (TextView) findViewById(R.id.tvWaitTaskEnd);
		btWaitTaskEnd = (Button) findViewById(R.id.btTaskEnd);
		tvProjectInWaitPanel = (TextView) findViewById(R.id.tvProjectInWaitPanel);
		actions.setProjectTextView (tvProjectInWaitPanel);
		btBreak = (Button) findViewById(R.id.btBreak);
		btWorkEndPanel = (Button) findViewById(R.id.btWorkEndPanel);
		waitTaskEndPanel = (LinearLayout) btWaitTaskEnd.getParent();
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		waitTaskEndPanel.setLayoutParams(layoutParams);
		if (initialBooking!=null && initialBooking.getProject()!=null && taskEndPanelVisible) {
			waitTaskEndPanel.setVisibility(LinearLayout.VISIBLE);
			buttonPanel.setVisibility(LinearLayout.GONE);
			lvProjects.setEnabled(false);
			btWaitTaskEnd.setText(initialBooking.getTask().getName()+" beenden");
		} else {
			waitTaskEndPanel.setVisibility(LinearLayout.INVISIBLE );
			buttonPanel.setVisibility(LinearLayout.VISIBLE);
			lvProjects.setEnabled(true);
		}
		btWaitTaskEnd.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				taskEndClicked(v);
			}
		});
		
		btBreak.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doButtonFunction();
				if (btWaitTaskEnd.getVisibility()==LinearLayout.VISIBLE) {
					btWaitTaskEnd.setVisibility(LinearLayout.GONE);
					btBreak.setText(R.string.breakEnd);
				} else {
					btWaitTaskEnd.setVisibility(LinearLayout.VISIBLE);
					btBreak.setText(R.string.breakEnd);
				}
			}
		});
		btWorkEndPanel.setVisibility(Button.GONE);
		btWorkEndPanel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				endOfWork();
				waitTaskEndPanel.setVisibility(LinearLayout.INVISIBLE);
			}
		});
		

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				timerTick();
			}
		}, 0, 1000);
	}
	

	private Date difference = new Date();

	private void timerTick() {
		Calendar calendar = Calendar.getInstance();
		//Date currentTime = new Date();
		Booking lastBooking = bookings == null ? null : bookings.getLastBooking();
		boolean showTime = false;
		final boolean panelVisible = waitTaskEndPanel.getVisibility()==LinearLayout.VISIBLE; 
		if (lastBooking == null || 
				lastBooking.getTimestamp() == null || 
				lastBooking.getAction() == Actions.ENDE || 
				panelVisible) {
			showTime = true;
		}
		
		if (lastBooking!=null) {
			difference.setTime(calendar.getTime().getTime()-lastBooking.getTimestamp().getTime()-OFFSET);
		}
		final String time = format.format(showTime ? calendar.getTime().getTime() : difference);
		tvElapsedTime.post(new Runnable() {
			public void run() {
				tvElapsedTime.setText(time);
				if (panelVisible) {
					tvWaitTaskEnd.setText(format.format(difference));
				}
			}
		});
	}
	
	
	public void checkForDateOverflow() {
		if (Settings.getInstance().getLastAction()!=null) { 
			Date now = new Date();

			long timeDiff = now.getTime()
					- Settings.getInstance().getLastTimeStamp().getTime();
			timeDiff /= 1000 * 60 * 60;

			if (Settings.getInstance().getLastAction() != Actions.ENDE
					&& timeDiff > 12
					|| (timeDiff > 8 && now.getDay() != Settings.getInstance()
							.getLastTimeStamp().getDay())) {

				resetBookingStatus();
				/* removed 16.10.2021
				if (Settings.getInstance().getBranding() == Branding.WUERZBURG) {
					resetBookingStatus();
				} else {
					showQuestionDialog();
				}
				*/
			}
		}
	}
	
	private void taskEndClicked(View v) {
		changeAction(Actions.TASK_END);
		waitTaskEndPanel.setVisibility(LinearLayout.INVISIBLE );
		buttonPanel.setVisibility(LinearLayout.VISIBLE);
		lvProjects.setEnabled(true);
	}
	
	private void saveAll() {
		Settings.getInstance().setLastViewedList(lastViewedList);
		if (bookings==null) {
			return;
		}
		
		Thread thread = new Thread() {
			@Override
			public void run() {
				MyLog.i("Main", "saving ....");
				synchronized(bookings) {
					Settings.getInstance().saveAtClose(bookings.getLastBooking());
					EntitiesFactory.getInstance().saveBookings();
				}
			}
		};
		thread.start();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		saveAll();
		MyLog.i("Projects", "onPause");
	}
	

	@Override
	public void onStop() {
		super.onStop();
		saveAll();
		MyLog.i("Main", "onStop");
		//super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		saveAll();
		ErrorNotifier.getInstance().removeHandler(this);
		MyLog.i("Main", "onDestroy");
	}

	
	private void showAlertDialog(String s) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(s);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
			
		AlertDialog dialog = builder.create();
		dialog.show();
	}


	private void checkForValidLicenseInBackground() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				checkForValidLicense();
			}
		}, 200);
	}
	
	private void checkForValidLicense() {
		Webservice ciSoftWs = Webservice.getCiSoftInstance();
		String settings = "";
		try {
			String suffix = Settings.getInstance().getSuffix();
			settings = ciSoftWs.call(Settings.GET_SETTINGS_BY_SUFFIX, 
					                 Settings.MAIL_SUFFIX, 
					                 suffix);
		} catch (Exception e) {
			return; //swallow for now
			//throw new RuntimeException(e);
		}
		if (settings != null && settings.length()>0) {
			String[] partSettings = settings.split("\\|");
			String dateString = partSettings[11];
			String[] dateParts = dateString.substring(0,10).split("\\.");
			Calendar c = GregorianCalendar.getInstance();
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[0]));
			c.set(Calendar.MONTH, Integer.parseInt(dateParts[1])-1);
			c.set(Calendar.YEAR, Integer.parseInt(dateParts[2]));
			
			Date date = c.getTime();
			if (date.before(new Date())) {
				showLicenseDialog(date);
			}
			
		} else {
			showLicenseDialog(null);
		}
		
	}
	
	private boolean shouldCancelApp(Date cancelDate) {
		Date dayBeforeOneWeek = new Date(new Date().getTime() - 7 * 24 * 60 * 60 * 1000);
		return dayBeforeOneWeek.after(cancelDate);
	}
	
	private void showLicenseDialog(Date licenseUntil) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final boolean cancelApp;
		if (licenseUntil != null) {
			cancelApp = shouldCancelApp(licenseUntil);
		} else {
			cancelApp = false;
		}
		builder.setTitle(
				licenseUntil != null ? 
					"Die Lizenz dieser App ist nicht mehr aktuell.\n"+ (
					cancelApp ? "Weitere Programmausführung nicht mehr möglich" :
						        "In maximal einer Woche wird das Programm blockiert")
					+"\nBitte an den IT-Verantwortlichen wenden" :
					"Für Ihre Anmeldedaten ist keine Lizenz vermerkt.\nBitte an den IT-Verantwortlichen wenden"
				);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (cancelApp) {
					ProjectsView.this.finish();
				}
			}
		});
		
		builder.setCancelable(false);

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public int getPanelId() {
		return R.id.chooseProjectWarningPanel;
	}

	@Override
	public int getLabelId() {
		return R.id.chooseProjectWarningLabel;
	}
}

