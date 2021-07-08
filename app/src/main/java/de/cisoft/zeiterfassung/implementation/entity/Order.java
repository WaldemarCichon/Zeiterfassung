package de.cisoft.zeiterfassung.implementation.entity;

import java.util.Date;

import de.cisoft.zeiterfassung.implementation.enums.OrderState;

public class Order extends Project {
	private String manualOrder;
	private String comment;
	private Date firstBegin;
	private Date end;
	private OrderState orderState = OrderState.NotStarted;


	public String getManualOrder() {
		return manualOrder;
	}
	public void setManualOrder(String manualOrder) {
		this.manualOrder = manualOrder;
	}
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
	public Date getFirstBegin() {
		return firstBegin;
	}
	public void setFirstBegin(Date firstBegin) {
		this.firstBegin = firstBegin;
	}
	
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	public OrderState getOrderState() {
		return orderState;
	}
	public void setOrderState(OrderState orderState) {
		this.orderState = orderState;
	}
	
	
	
}
