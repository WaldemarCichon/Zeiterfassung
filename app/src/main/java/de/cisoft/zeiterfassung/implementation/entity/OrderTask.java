package de.cisoft.zeiterfassung.implementation.entity;

import de.cisoft.zeiterfassung.implementation.enums.OrderState;

public class OrderTask extends Task {
	private String manualTask;
	private OrderState taskState;
	public String getManualTask() {
		return manualTask;
	}
	public void setManualTask(String manualTask) {
		this.manualTask = manualTask;
	}
	public OrderState getTaskState() {
		return taskState;
	}
	public void setTaskState(OrderState taskState) {
		this.taskState = taskState;
	}

}
