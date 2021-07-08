package de.cisoft.zeiterfassung.ui.enums;

public enum Branding {
	EMPTY(-1),
	NONE(0),
	WUERZBURG(1),
	SCHWEINFURT(2),
	CISOFT(3),
	HERRMANN(4),
	GKN(5);
	
	int i;
	
	Branding(int i) {
		this.i = i;
	}
	
	public static Branding getBranding(int i) {
		return Branding.values()[i+1];
	}
	
	public int getValue() {
		return i;
	}
}
