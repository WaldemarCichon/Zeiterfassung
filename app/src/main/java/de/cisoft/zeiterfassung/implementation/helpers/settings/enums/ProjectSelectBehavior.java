package de.cisoft.zeiterfassung.implementation.helpers.settings.enums;

import de.cisoft.zeiterfassung.R;

public enum ProjectSelectBehavior {
	Click(R.id.rbClick),
	Menu(R.id.rbMenu), 
	LongClick(R.id.rbLongClick), 
	DoubleClick(R.id.rbDoubleClick);
	
	
	private int buttonId;

	ProjectSelectBehavior(int buttonId) {
		this.buttonId = buttonId;
	}
	
	public int getButtonId() {
		return buttonId;
	}
	
	public static ProjectSelectBehavior getById(int id) {
		for (ProjectSelectBehavior value : values()) {
			if (id == value.buttonId || id == value.ordinal()) {
				return value;
			}
		}
		return null;
	}
}
