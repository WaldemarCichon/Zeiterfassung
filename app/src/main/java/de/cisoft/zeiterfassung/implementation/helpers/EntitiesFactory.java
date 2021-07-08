package de.cisoft.zeiterfassung.implementation.helpers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import de.cisoft.framework.Entity;
import de.cisoft.framework.EntityTable;
import de.cisoft.framework.Informer.Informer;
import de.cisoft.framework.webservice.Webservice;
import de.cisoft.utility.Saveable;
import de.cisoft.utility.ftp.FtpClient;
import de.cisoft.utility.log.MyLog;
import de.cisoft.zeiterfassung.MainActivity;
import de.cisoft.zeiterfassung.implementation.entity.Booking;
import de.cisoft.zeiterfassung.implementation.entity.Bookings;
import de.cisoft.zeiterfassung.implementation.entity.Janitor;
import de.cisoft.zeiterfassung.implementation.entity.JanitorStreetLinks;
import de.cisoft.zeiterfassung.implementation.entity.Janitors;
import de.cisoft.zeiterfassung.implementation.entity.Project;
import de.cisoft.zeiterfassung.implementation.entity.Projects;
import de.cisoft.zeiterfassung.implementation.entity.Task;
import de.cisoft.zeiterfassung.implementation.entity.Tasks;
import de.cisoft.zeiterfassung.implementation.enums.ViewedList;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;

public class EntitiesFactory {
	private Webservice webservice;
	private Janitors janitors;
	private Projects projects;
	private Projects standInProjects;
	private Projects myProjects;
	// private Projects mixedProjects;
	// private Janitor me;
	// private Janitor standIn;
	private Tasks tasks;
	private JanitorStreetLinks links;
	private static EntitiesFactory instance;// , contextInstance;
	private Settings settings;
	private boolean initialized;
	private boolean saved;
	private Janitor janitor;
	private Projects mergedProjects;
	private Bookings bookings;
	//private String errorMessage;

	private EntitiesFactory() {
		this(MainActivity.getLastInstance());
	}

	private EntitiesFactory(Context context) {
		settings = Settings.getInstance(context);
		webservice = settings.getWebservice();
	}

	public static synchronized EntitiesFactory getInstance() {
		if (instance == null) {
			instance = new EntitiesFactory();
		}
		return instance;
	}

	public static synchronized EntitiesFactory getInstance(Context context) {
		/*
		if (contextInstance == null) {
			contextInstance = new EntitiesFactory(context);
		}
		return contextInstance;
		*/
		if (instance == null) {
			instance = new EntitiesFactory(context);
		}
		return instance;
	}

	public synchronized boolean initFromFilesystem() {
		try {
			this.initialized = false;
			this.myProjects = new Projects(Projects.MY_PROJECTS);
			try {
				this.standInProjects = new Projects(Projects.STANDIN_PROJECTS);
			} catch (FileNotFoundException ex) {
				// let standInProjects as null;
			}
			Projects projects = new Projects();
			this.projects = projects;
			Janitors janitors = new Janitors();
			this.janitors = janitors;
			Tasks tasks = new Tasks();
			this.tasks = tasks;
			JanitorStreetLinks links = new JanitorStreetLinks();
			this.links = links;
			if (standInProjects != null && standInProjects.getCount() > 0) {
				this.mergeProjects();
			} else {
				mergedProjects = myProjects;
			}
			try {
				bookings = new Bookings();
				MyLog.i("EntitiyFactory", "Bookings loaded from file");
			} catch (FileNotFoundException ex) {
				bookings = new Bookings(0);
			}
			this.initialized = true;			
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex); //ex.printStackTrace();
		}
		return true;
	}

	private boolean checkIfEmpty(EntityTable<?> table, String errorRegion) {
		if (table == null) {
			return true;
			//errorMessage = errorRegion + " konnte nicht geladen werden";
		}
		return false;

	}

	public boolean initFromWebservice(ProgressDialog progressDialog) {
		try {
			this.initialized = false;
			//this.errorMessage = null;

			// progressdialog.setMessage("Lade Projekte");
			// progressdialog.setProgress(20);
			Projects projects = new Projects(webservice);
			if (checkIfEmpty(projects, "Projekte")) {
				throw new RuntimeException("Projects are empty");
			}

			// progressdialog.setMessage("Lade Mitarbeiter");
			// progressdialog.setProgress(40);
			Janitors janitors = new Janitors(webservice);
			if (checkIfEmpty(janitors, "Mitarbeiter")) {
				throw new RuntimeException("Janitors are empty");
			}

			// progressdialog.setMessage("Lade Zuordnungen");
			// progressdialog.setProgress(60);
			JanitorStreetLinks links = new JanitorStreetLinks(webservice);
			if (checkIfEmpty(links, "Zuordnungen")) {
				throw new RuntimeException("Links are empty");
			}

			// progressdialog.setMessage("Lade T√§tigkeiten");
			// progressdialog.setProgress(80);
			Tasks tasks = new Tasks(webservice);
			if (checkIfEmpty(tasks, "T√§tigkeiten")) {
				throw new RuntimeException("Tasks are empty");
			}
			
			//if (true)
			//throw new RuntimeException("Shit");

			this.projects = projects;
			this.janitors = janitors;
			this.links = links;
			this.tasks = tasks;
			
			this.bookings = new Bookings(0);

			this.initialized = true;
			// progressdialog.setMessage("Laden beendet");
			// progressdialog.setProgress(100);
			return true;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
			/*
			throw new RuntimeException(ex);
			//errorMessage = "Fehler beim Laden von Daten: " + ex.getMessage();
			//return false;
			*/
		}
	}

	public void save() {
		saveToFilesystem();
	}

	public void saveToFilesystem() {
		try {
			this.saved = false;
			if (!initialized) {
				return;
			}
			this.projects.save();
			if (myProjects == null) {
				this.myProjects = new Projects();
			}
			
			this.myProjects.save();
			
			if (standInProjects != null) {
				this.standInProjects.save();
			}
			this.janitors.save();
			this.tasks.save();
			this.links.save();
			this.saved = true;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public Janitors getJanitors() {
		return janitors;
	}

	public Projects getProjects() {
		return projects;
	}

	public Tasks getTasks() {
		return tasks;
	}

	public JanitorStreetLinks getLinks() {
		return links;
	}

	public Projects getMyProjects() {
		return myProjects;
	}

	public Projects getStandInProjects() {
		return standInProjects;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isSaved() {
		return this.saved;
	}

	public Projects getProjectsFor(Janitor janitor) {
		JanitorStreetLinks links = (JanitorStreetLinks) this.links.filter(
				"janitorId", janitor.getId());
		Projects projects = (Projects) this.projects.in(links, "projectId");
		return projects;
	}

	public void initPersonalData(String mailAddress, boolean me) {
		Janitor janitor = janitors.find("email", mailAddress);
		if (janitor != null) {
			initPersonalData(janitor, me);
		} else {
			if (me) {
				janitor = null;
			}
		}
		
	}

	public void initPersonalData(Janitor janitor, boolean me) {
		Projects projects = getProjectsFor(janitor);
		setProjects(projects, me);
		if (me) {
			this.janitor = janitor;
		}
	}

	public void initPersonalData(int userNumber, boolean me) {
		Janitor janitor = janitors.find(userNumber);
		initPersonalData(janitor, me);
	}

	public void setProjects(Projects projects, boolean me) {
		if (me) {
			projects.setFileName(Projects.MY_PROJECTS);
			myProjects = projects;
		} else {
			projects.setFileName(Projects.STANDIN_PROJECTS);
			standInProjects = projects;
		}
		mergeProjects();
	}

	public void mergeProjects() {
		Projects bothProjects = new Projects(Project.class);
		bothProjects.add(myProjects);
		bothProjects.add(standInProjects);
		mergedProjects = bothProjects;
	}

	public Projects getMergedProjects() {
		if (mergedProjects == null) {
			mergeProjects();
		}
		return mergedProjects;
	}

	public void saveStandInProjects() {
		try {
			standInProjects.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Janitor getJanitor() {
		if (this.janitor == null) {
			if (this.janitors == null) {
				return null;
			}
			String mailAddress = Settings.getInstance().getUserMailAddress();
			this.janitor = janitors.find("email", mailAddress);
		}
		return this.janitor;
	}

	public Bookings getBookings() {

		if (bookings == null) {
			bookings = new Bookings(Booking.class);
		}
		return bookings;
	}

	public void saveBookings() {
		try {
			this.bookings.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendBookings() {
		bookings.send(webservice);
	}

	public void sendBooking(Booking booking) {
		bookings.send(webservice, booking);
	}

	public Entity getEntity(Class<?> type, int id) {
		if (type == Bookings.class) {
			return bookings.find(id);
		}

		if (type == Janitor.class) {
			return janitors.find(id);
		}

		if (type == Project.class) {
			return projects.find(id);
		}

		if (type == Task.class) {
			return tasks.find(id);
		}
		return null;
	}

	public Entity getEntity(Class<?> type, String sId) {
		int id = 0;
		if (sId == null || sId.length() == 0 || sId.equals("null")) {
			return getNullEntity(type);
		}

		try {
			id = Integer.parseInt(sId);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		return getEntity(type, id);
	}

	public Entity getNullEntity(Class<?> type) {
		if (type == Project.class) {
			return (Project) null;
		}
		if (type == Task.class) {
			return (Task) null;
		}
		if (type == Janitor.class) {
			return (Janitor) null;
		}
		if (type == Booking.class) {
			return (Booking) null;
		}

		return null;
	}

	public Projects getProjects(ViewedList listToView) {
		switch (listToView) {
		case NONE:
			return null;
		case MY_OBJECTS:
			return getMyProjects();
		case STANDINS_OBJECTS:
			return getStandInProjects();
		case BOTH_OBJECTS:
			return getMergedProjects();
		case ALL_OBJECTS:
			return getProjects();
		}
		return null;
	}

	public static boolean loadData(final Context context,
			final String serverAddress, final String mailAddress) {
		final ProgressDialog progressDialog = new ProgressDialog(context);

		/*
		 * progressdialog.setTitle("Daten werden geladen");
		 * progressdialog.setCancelable(false); progressdialog.setMax(100);
		 * progressdialog.setMessage("Datenverbindung wird initialisiert");
		 * progressdialog.setProgressStyle(Progressdialog.STYLE_HORIZONTAL);
		 * ((Activity) context).runOnUiThread(new Runnable() {
		 * 
		 * @Override public void run() { progressdialog.show(); } });
		 */

		Webservice.init(
				Settings.getInstance().getWebServicePath(
						serverAddress.toString()), Settings.getInstance()
						.getNamespace());
		Webservice service = Webservice.getInstance();
		String v = null;
		try {
			v = service.call("Version");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (v == null) {
			showAlertDialog(context, "Server nicht gefunden", false);
			progressDialog.dismiss();
			return false;
		}
		try {
			v = service.call("ClearCache");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (context instanceof Saveable) {
			((Saveable) context).save();
		}
		EntitiesFactory factory = EntitiesFactory.getInstance();
		factory.initFromWebservice(progressDialog);
		factory.initPersonalData(mailAddress, true);
		// progressdialog.dismiss();
		if (factory.getJanitor() == null) {
			showAlertDialog(context, "Benutzer nicht gefunden", false);
			Settings.getInstance().deInit();
			return false;
		}
		factory.save();
		Informer.getInstance().inform();
		showAlertDialog(
				context,
				"Daten wurden erfolgreich geladen\nApplikation wird beendet. Bitte neu starten!",
				true);
		return true;
	}

	private static void showAlertDialog(final Context context, String s,
			final boolean finish) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(s);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (finish) {
					((Activity) context).finish();
				}
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void upload(FtpClient client, EntityTable<?> entity) {
		if (entity == null) {
			return;
		}
		String simpleName = entity.getClass().getSimpleName();
		try {
			FileInputStream is = entity.getDefaultInputStream();
			client.upload(is, simpleName);
		} catch (IOException ioex) {
			MyLog.d("Entities factory", "Can´t upload " + simpleName);
		}
	}
	
	private void download(FtpClient client, EntityTable<?> entity) {
		if (entity == null) {
			return;
		}
		String simpleName = entity.getClass().getSimpleName();
		entity.backup();

		try {
			FileOutputStream os = entity.getDefaultOutputStream();
			client.download(simpleName, os);
			os.flush();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void uploadLog(FtpClient client) throws IOException {
		Process process;
		try {
			process = Runtime.getRuntime().exec("logcat -d -v time");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		client.upload(process.getInputStream(), "logs");
	}
	
	private void uploadMyLog(FtpClient client) throws IOException {
		client.upload(MyLog.getLogStream(), "mylogs");
	}
	
	public void upload(FtpClient client) throws IOException {
		upload(client, projects);
		upload(client, janitors);
		upload(client, tasks);
		upload(client, standInProjects); // Vorsicht 
		upload(client, myProjects);
		upload(client, bookings);
		uploadLog(client);
		uploadMyLog(client);
	}
	
	public void download(FtpClient client) {
		download (client, projects);
		download(client, janitors);
		download(client, tasks);
		download(client, standInProjects); // Vorsicht, kein Standardname
		download(client, myProjects);
		download(client, bookings);
		EntitiesFactory.clear();
	}

	public static void clear() {
		// TODO Auto-generated method stub
		EntitiesFactory.instance = null;
	}

}
