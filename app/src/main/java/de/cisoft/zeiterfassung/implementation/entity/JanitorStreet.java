package de.cisoft.zeiterfassung.implementation.entity;

import de.cisoft.framework.Entity;
import de.cisoft.framework.annotations.Field;
import de.cisoft.framework.annotations.ID;

public class JanitorStreet extends Entity {
	Integer id;
	Integer janitorId;
	Janitor janitor;
	Integer projectId;
	Project project;
	
	public JanitorStreet() {
		
	}
	
	public JanitorStreet(String row, String separator,
			de.cisoft.framework.Field[] assignments) {
		super(row, separator, assignments);
		// TODO Auto-generated constructor stub
	}



	public JanitorStreet(String[] fields,
			de.cisoft.framework.Field[] assignments) {
		super(fields, assignments);
		// TODO Auto-generated constructor stub
	}



	@ID
	@Field(name="id", alias="id", type=Integer.class) 
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	@Field(name="janitorId", alias="janitorId", type=Integer.class)
	public Integer getJanitorId() {
		return this.janitorId;
	}
	
	public void setJanitorId(Integer janitorId) {
		this.janitorId = janitorId;
	}
	
	@Field(name="projectId", alias="projectId", type=Integer.class)
	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public Janitor getJanitor() {
		return janitor;
	}

	public void setJanitor(Janitor janitor) {
		this.janitor = janitor;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
