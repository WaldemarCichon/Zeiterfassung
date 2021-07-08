package de.cisoft.framework.android.ui;

import java.util.LinkedList;

import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;

import android.app.Activity;

public class ErrorNotifier {
  private LinkedList<ErrorPanelHandler> panelHandlerList;
  private static ErrorNotifier instance = new ErrorNotifier();
  private static boolean errorState;
	
  private ErrorNotifier() {
	  errorState = false;
	  panelHandlerList = new LinkedList<ErrorPanelHandler>();
	  if (EntitiesFactory.getInstance().getBookings().hasNotSentBookings()) {
		  errorState = true;
	  }
  }
  
  public static ErrorNotifier getInstance() {
	  return instance;
  }
  
  public void addHandler(ErrorPanelHandler handler) {
	  panelHandlerList.add(handler);
	  if (errorState) {
		  handler.warn();
	  } else {
		  handler.resetWarning();
	  }
  }
  
  public void warn() {
	  errorState = true;
	  for(ErrorPanelHandler handler: panelHandlerList) {
		  handler.warn();
	  }
  }
  
  public void resetWarning() {
	  errorState = false;
	  for (ErrorPanelHandler handler: panelHandlerList) {
		  handler.resetWarning();
	  }
  }
  
  public void addHandler(Activity activity) {
	  addHandler(new ErrorPanelHandler(activity));
  }
  
  public void removeHandler(ErrorPanelHandler handler) {
	  panelHandlerList.remove(handler);
  }
  
  public void removeHandler(Activity activity) {
	  if (activity == null) {
		  return;
	  }
	  for (ErrorPanelHandler handler : panelHandlerList) {
		  if (activity.equals(handler.getParent())) {
			  removeHandler(handler);
			  return;
		  }
	  }
  }
}
