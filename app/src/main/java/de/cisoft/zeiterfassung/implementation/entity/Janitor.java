package de.cisoft.zeiterfassung.implementation.entity;

import de.cisoft.framework.Entity;
import de.cisoft.framework.annotations.Field;
import de.cisoft.framework.annotations.ID;

public class Janitor extends Entity {
	private int id;
	private String name;
	private String firstName;
	private String lastName;
	private int konto;
	private Integer counter;
	private Integer id1;
	private String temp;
	private String eMail;
	
	public Janitor() {
		super();
	}
	
	public Janitor(String line, String seperator, de.cisoft.framework.Field[] assignments) {
		super(line, seperator, assignments);
	}
	
	
	@ID
	@Field(name="id", alias="id", type=Integer.class)
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	@Field(name="name", alias="name", type=String.class)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Field(name="firstName", alias="firstName", type=String.class)
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	@Field(name="lastName", alias="lastName", type=String.class)
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	@Field(name="konto", alias="konto", type=Integer.class)
	public int getMainJanitorId() {
		return konto;
	}
	public void setMainJanitorId(int mainJanitorId) {
		this.konto = mainJanitorId;
	}
	
	@Field(name="counter", alias="counter", type=Integer.class)
	public Integer getCounter() {
		return this.counter;
	}
	
	public void setCounter(Integer counter) {
		this.counter = counter;
	}
	
	@Field(name="id1", alias="id1", type=Integer.class )
	public Integer getId1() {
		return this.id1;
	}
	
	public void setId1(Integer id1) {
		this.id1 = id1;
	}
	
	@Field(name="temp", alias="temp", type=String.class)
	public String getTemp() {
		return this.temp;
	}
	
	public void setTemp(String temp) {
		this.temp = temp;
	}
	
	@Field(name="email", alias="email", type=String.class)
	public String getEMail() {
		return this.eMail;
	}
	
	public void setEMail(String eMail) {
		this.eMail = eMail;
	}
	
	@Field(name="konto", alias="konto", type=Integer.class)
	public Integer getKonto() {
		return this.konto;
	}
	
	public void setKonto(Integer konto) {
		this.konto = konto;
	}
	
	@Override
	public String toString() {
		return "["+id+"] "+lastName+", "+firstName+" ("+konto+")";
	}
}
