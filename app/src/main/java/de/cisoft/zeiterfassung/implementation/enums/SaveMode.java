package de.cisoft.zeiterfassung.implementation.enums;

public enum SaveMode {
	Main("bookings.dat"),
	Backup("bookings.bak"),
	SubBackup("bookngs.sbak");
	
	private final String fileName;
	
	private SaveMode(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
}
