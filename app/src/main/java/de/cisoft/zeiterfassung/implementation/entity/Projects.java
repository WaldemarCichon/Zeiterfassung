package de.cisoft.zeiterfassung.implementation.entity;

import java.io.FileNotFoundException;

import android.util.Log;

import de.cisoft.framework.Entity;
import de.cisoft.framework.EntityTable;
import de.cisoft.framework.webservice.Webservice;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;

public class Projects extends EntityTable<Project> {
	//private static final String testData = "id|name|strasse|ort~1|Wiedenkamp 45|Wiedenkamp 45|Vechelde~2|Ilmweg30|Omas Wohnung|Braunschweig~3|Th. Heuss-Str. 3|Arbeit|Braunschweg~4|Na Zakrecie 18/9|Stare mieszkanie|Gliwice";
	private static final String title = "id|land|plz|ort|strasse|name|konto|counter|iconName";
	private static final String serviceName = "SGetHouses";
	private static final String FILE_NAME="projects.dat";
	public static final String MY_PROJECTS = "myProjects.dat";
	public static final String STANDIN_PROJECTS = "standinProjects.dat";
	private String filename=null;

	public Projects() throws FileNotFoundException {
		this(FILE_NAME);
	}
	
	public Projects(String filename) throws FileNotFoundException {
		super(Project.class, getInputStream(filename), title);
		this.filename = filename;
	}

	public Projects(Webservice webservice) throws Exception {
		super(Project.class, webservice, serviceName, title);
	}
	
	public Projects(Class<Project> clazz) {
		super(clazz);
	}
	
	protected Projects getInstance() {
		return new Projects(Project.class);
	}

	public Class<Project> getEntityClass() {
		return Project.class;
	}
	
	public String getDefaultFileName() {
		return filename == null ? FILE_NAME : filename;
	}
	
	public void setFileName(String filename) {
		this.filename = filename;
	}
	
	protected String getServiceName() {
		return serviceName;
	}
	
	protected String getDefaultTitle() {
		return title;
	}
	
	@Override
	protected void beforeStore(int count, Project instance) {
		instance.setName(instance.getStrasse());
	}

	public Project getByName(CharSequence text) {
		if (text==null || text.equals("")) {
			return null;
		}
		for (Project project : getEntites()) {
			if (text.equals(project.getName())) {
				return project;
			}
		}
		return null;
	}

	
	public Project getByText(CharSequence text) {
		if (text==null || text.equals("")) {
			return null;
		}
		for (Project project : getEntites()) {
			if (text.equals(project.getDescription())) {
				return project;
			}
		}
		return null;
	}

}
