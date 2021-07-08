package de.cisoft.zeiterfassung.implementation.helpers.settings.enums;

import de.cisoft.zeiterfassung.R;

public enum TaskSelectBehavior {
	Menu(R.id.rbMenuTask),
	Click(R.id.rbClickTask),
	Button(R.id.rbButtonTask);
	
	
	private int buttonId;

	TaskSelectBehavior(int buttonId) {
		this.buttonId = buttonId;
	}
	
	public int getButtonId() {
		return buttonId;
	}
	
	public static TaskSelectBehavior getById(int id) {
		for (TaskSelectBehavior value : values()) {
			if (id == value.buttonId || id == value.ordinal()) {
				return value;
			}
		}
		return null;
	}
}
