package de.cisoft.zeiterfassung.implementation.entity;

import java.io.FileNotFoundException;

import de.cisoft.framework.Entity;
import de.cisoft.framework.EntityTable;
import de.cisoft.framework.webservice.Webservice;

public class JanitorStreetLinks extends EntityTable<JanitorStreet> {
	private static final String titleLine = "id|janitorId|projectId";
	private static final String serviceName = "SGetLinks";
	private static final String FILE_NAME = "links.dat";
	
	public JanitorStreetLinks() throws FileNotFoundException {
		super(JanitorStreet.class, getInputStream(FILE_NAME), titleLine);
	}
	
	public JanitorStreetLinks(Webservice webservice) throws Exception {
		super(JanitorStreet.class, webservice, serviceName, titleLine);
	}
	
	public JanitorStreetLinks(Class<JanitorStreet> clazz) {
		super(clazz);
	}
	
	protected JanitorStreetLinks getInstance() {
		return new JanitorStreetLinks(JanitorStreet.class);
	}
	
	public Class<JanitorStreet> getEntityClass() {
		return JanitorStreet.class;
	}
	
	@Override
	protected String getDefaultFileName() {
		return (FILE_NAME);
	}
	
	protected String getServiceName() {
		return serviceName;
	}
	
	protected String getDefaultTitle() {
		return titleLine;
	}

}
