package de.cisoft.zeiterfassung.implementation.enums;

public enum ViewedList {
	NONE(-1),
	MY_OBJECTS(0),
	STANDINS_OBJECTS(1),
	BOTH_OBJECTS(2),
	ALL_OBJECTS(3);
	
	private int value;
	
	private ViewedList (int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static ViewedList getViewedList(int value) {
		for (ViewedList vl : values()) {
			if (vl.getValue() == value) {
				return vl;
			}
		}
		return null;
	}
}
