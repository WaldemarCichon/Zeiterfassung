package de.cisoft.zeiterfassung.implementation.helpers.settings.enums;

import de.cisoft.zeiterfassung.R;

public enum SendBehavior {
	Immediately (R.id.rbImmediately),
	AtEnd(R.id.rbAtEnd),
	Manual(R.id.rbManually);
	
	private int buttonId;

	SendBehavior(int buttonId) {
		this.buttonId = buttonId;
	}
	
	public int getButtonId() {
		return buttonId;
	}
	
	public static SendBehavior getById(int id) {
		for (SendBehavior value : values()) {
			if (id == value.buttonId || id == value.ordinal()) {
				return value;
			}
		}
		return null;
	}
}
