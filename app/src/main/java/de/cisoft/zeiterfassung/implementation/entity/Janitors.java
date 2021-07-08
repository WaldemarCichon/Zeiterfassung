package de.cisoft.zeiterfassung.implementation.entity;

import java.io.FileNotFoundException;

import de.cisoft.framework.Entity;
import de.cisoft.framework.EntityTable;
import de.cisoft.framework.webservice.Webservice;

public class Janitors extends EntityTable<Janitor> {
	
	//private static String testData="";
	private static String serviceName = "SGetUsers";
	private static String titleLine = "id|temp|firstname|lastname|counter|id1|email|temp|temp|konto";
	private static String FILE_NAME = "janitors.dat";
	
	
	public Janitors() throws FileNotFoundException {
		super(Janitor.class, getInputStream(FILE_NAME), titleLine);
	}
	
	public Janitors(Webservice webservice) throws Exception {
		super(Janitor.class, webservice, serviceName, titleLine);
	}
	
	public Janitors(Class<Janitor> clazz) {
		super(clazz);
	}
	
	protected Janitors getInstance() {
		return new Janitors(Janitor.class);
	}
	
	@Override
	protected boolean shouldStore(int count, Janitor instance) {
		return instance.getKonto()!=0; 
	}
	
	public Class<Janitor> getEntityClass() {
		return Janitor.class;
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
