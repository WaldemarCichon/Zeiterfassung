package de.cisoft.zeiterfassung.implementation.entity;

public class Action {
	public static final int UNKNOWN = 0; 
	public static final int DO_BEGIN = 1;
	public static final int DO_CHOOSE = 2;
	public static final int DO_EXIT = 4;
	public static final int DO_BREAK = 256;
	public static final int DO_BREAK_END = 65536;
	public static final int DO_PHYSICIAN = 512;
	public static final int DO_PHYSICIAN_END = DO_BREAK_END*2;
	public static final int DO_SHOPPING = 1024;
	public static final int DO_SHOPPING_END = DO_PHYSICIAN_END*2;
	public static final int BREAKS_BEGIN = DO_BREAK;
	public static final int BREAK_ENDS_BEGIN = DO_BREAK_END;
	public static final int DO_TASK_END = DO_BREAK_END * 2;

	private String name;
	private String konto;
	private String textParam;
	private int    possibleDoings;
	private Action[] possibleActions;
	private boolean visible = true;
	private boolean needsProject = true;
	
	public Action(String name, String konto, String textParam, int possibleDoings) {
		this.name = name;
		this.konto = konto;
		this.textParam = textParam;
		this.possibleDoings = possibleDoings;
	}
	
	public Action(String name, String konto, String textParam, int possibleDoings, boolean needsProject) {
		this(name, konto, textParam, possibleDoings);
		this.needsProject = needsProject;
	}
	
	public Action(String name,  String konto, String textParam, int possibleDoings, Action...actions) {
		this(name, konto,  textParam, possibleDoings);
		this.possibleActions = actions;
	}
	
	public String getName() {
		return name;
	}
	
	public Action[] getPossibleActions() {
		return possibleActions;
	}
	
	public String getKonto() {
		return konto;
	}
	
	public String getTextParam() {
		return textParam;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setPossibleActions(Action... actions) {
		this.possibleActions = actions;
	}

	public boolean doChoose() {

		return (possibleDoings & DO_CHOOSE) == DO_CHOOSE;
	}

	public boolean doBegin() {
		return (possibleDoings & DO_BEGIN) == DO_BEGIN;
	}
	
	public boolean doBreak() {
		return (possibleDoings & DO_BREAK) == DO_BREAK;
	}
	
	public boolean doBreakEnd() {
		return (possibleDoings & DO_BREAK_END) == DO_BREAK_END;
	}
	
	public boolean doEnd() {
		return (possibleDoings & DO_EXIT) == DO_EXIT;
	}
	
	public boolean doExit() {
		return (possibleDoings & DO_EXIT) == DO_EXIT;
	}

	public boolean notWorking() {
		
		return (possibleDoings & (DO_BREAK | DO_EXIT | DO_PHYSICIAN | DO_SHOPPING))>0;
	}
	
	public boolean needsProject() {
		return needsProject;
	}
	
	public String toString() {
		return "<"+this.konto+"> "+this.name;
	}

	public boolean isPossible(Action action) {
		for (Action possibleAction : possibleActions) {
			//System.out.println(action.getName()+" - "+possibleAction.getName());
			if (possibleAction == action) {
				return true;
			}
		}
		return false;
	}
}
