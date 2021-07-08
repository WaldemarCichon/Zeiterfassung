package de.cisoft.zeiterfassung.implementation.entity;

import java.util.HashMap;
import java.util.Map;

import de.cisoft.utility.log.MyLog;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;
import de.cisoft.zeiterfassung.implementation.helpers.settings.enums.ProjectSelectBehavior;

import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;

public class Actions {
	private static Actions instance;
	private static Map<String, Action> actionMap;
	//private Map<Integer, Action> doingActionMap;
	public  static final Action BEGINN = new Action("Beginn", "KO", null, Action.DO_BEGIN | Action.DO_CHOOSE);
	public  static final Action ENDE = new Action("Arbeitsende", "GE", "***Arbeitsende", Action.DO_EXIT, false);
	public  static final Action WECHSEL = new Action("Wählen", "W", null, Action.DO_CHOOSE);
	public  static final Action PAUSE = new Action("Pause", "Pause B", "***Pausenbegin", Action.DO_BREAK, false);
	public  static final Action PAUSE_ENDE = new Action("Pausenende", "Pause E", "***Pausenende", Action.DO_BREAK_END);
	public  static final Action ARZT=new Action("Arztbesuch", "Arzt B", "**Arztbesuch Beginn", Action.DO_PHYSICIAN, false);
	public  static final Action ARZT_ENDE =new Action("Ende Arztbesuch", "Arzt E", "***Arztbesuch Ende", Action.DO_PHYSICIAN_END);
	public  static final Action BESORGUNG=new Action("Besorgung", "Besorgung B", "***Besorgung Beginn", Action.DO_SHOPPING, false);
	public  static final Action BESORGUNG_ENDE=new Action("Ende Besorgung","Besorgung E", "***Besorgung Ende", Action.DO_SHOPPING_END);
	public  static final Action TASK_END = new Action("Tätigkeitsende","Taetigkeit E", "***Tätigkeit Ende", Action.DO_TASK_END);
	private static final Action[] allActions = {
		BEGINN,
		ENDE,
		WECHSEL,
		PAUSE,
		PAUSE_ENDE,
		ARZT,
		ARZT_ENDE,
		BESORGUNG,
		BESORGUNG_ENDE,
		//TASK_ENDE
	};
	private Action currentAction;
	private Project project;
	private Task task;
	private TextView textView;
	private Button btFunction;
	private boolean outstandingUpdate;
	private TextView projectTextView;
	private Button btEndWork;
	private static boolean initialized = false;
	//private Action lastAction;
	
	private Actions() {
		if (!initialized) {
			init();
		}
		Settings settings = Settings.getInstance();
		if (settings != null) {
			currentAction = settings.getLastAction();
		}
		if (currentAction == null) {
			currentAction = getInitialAction();
		}
	}
	
	public static void init() {
		BEGINN.setPossibleActions( WECHSEL, PAUSE, ARZT, BESORGUNG, ENDE);
		ENDE.setPossibleActions (BEGINN);
		WECHSEL.setPossibleActions(WECHSEL, PAUSE, ARZT, BESORGUNG, ENDE);
		PAUSE.setPossibleActions( PAUSE_ENDE, ARZT, ENDE);
		PAUSE_ENDE.setPossibleActions( WECHSEL, PAUSE, ARZT, BESORGUNG, ENDE );
		ARZT.setPossibleActions(ARZT_ENDE, ENDE);
		ARZT.setVisible(false);
		ARZT_ENDE.setPossibleActions(WECHSEL, PAUSE, ARZT, BESORGUNG, ENDE);
		ARZT_ENDE.setVisible(false);
		BESORGUNG.setPossibleActions(BESORGUNG_ENDE, ENDE);
		BESORGUNG.setVisible(false);
		BESORGUNG_ENDE.setPossibleActions(WECHSEL, PAUSE, ARZT, BESORGUNG, ENDE);
		BESORGUNG_ENDE.setVisible(false);
		TASK_END.setPossibleActions(WECHSEL, PAUSE, ARZT, BESORGUNG, ENDE);
		TASK_END.setVisible(false);
		if (Settings.getInstance().getProjectSelectBehavior() != ProjectSelectBehavior.Menu) {
			BEGINN.setVisible(false);
			WECHSEL.setVisible(false);
		}


		actionMap = new HashMap<String, Action>();
		for (Action action : allActions) {
			actionMap.put(action.getKonto(), action);
		}
		initialized = true;
	}
	
	public static Actions getInstance() {
		if (instance==null) {
			instance = new Actions();
		}
		return instance;
	}
	
	public static Action[] getAllActions() {
		return allActions;
	}
	
	public static Action getInitialAction() {
		return ENDE;
	}

	public void addTo(Menu menu) {
		Action[] possibleActions = currentAction.getPossibleActions();
		int i = 0;
		for (Action possibleAction : possibleActions) {
			if (possibleAction.isVisible()) {
				menu.add(1, i,  i, possibleAction.getName());
			}
			i++;
		}
	}
	
	public void changeMenu(Menu menu) {
		if (menu==null) {
			return;
		}
		menu.removeGroup(1);
		addTo(menu);
	}
	
	public void setCurrentAction(Action action) {
		this.currentAction = action;
		updateDisplay();
	}
	
	public void setTextView(TextView textView) {
		this.textView = textView;
		if (outstandingUpdate) {
			updateDisplay();
			outstandingUpdate = false;
		}
	}
	
	public void setProjectTextView(TextView tvProjectInWaitPanel) {
		this.projectTextView = tvProjectInWaitPanel;
	}
	
	public void setFuctionButton(Button btFunction) {
		this.btFunction = btFunction;
	}
	
	public void setEndWorkButton(Button btEndWork) {
		this.btEndWork = btEndWork;
	}
	
	public void setCurrentAction(int index) {
		setCurrentAction(currentAction.getPossibleActions()[index]);
	}
	
	public void setCurrentProject(Project project) {
		this.project = project;
	}
	
	public void setCurrentTask(Task task) {
		this.task = task;
	}

	public  Action getPossibleAction(int actionId) {
		Action[] possibleActions = currentAction.getPossibleActions();
		return possibleActions[actionId];
	}
	
	public Action[] getPossibleActions() {
		return currentAction.getPossibleActions();
	}
	
	public Action getCurrentAction() {
		return this.currentAction;
	}
	
	public int getCurrentActionsIndex() {
		for (int i=0; i<allActions.length; i++) {
			if (allActions[i]==currentAction) {
				return i;
			}
		}
		return -1;
	}
	
	public Action getAction(String konto) {
		if (!initialized) {
			init();
		}
		return actionMap.get(konto);
	}
	
	public static Action getAction(int id) {
		if (id==-1) {
			return null;
		}
		return allActions[id];
	}
	
	public int getId(Action action) {
		for (int i=0; i<allActions.length; i++) {
			if (allActions[i]==action) {
				return i;
			}
		}
		return -1;
	}
	
	public Action getEndAction(Action action) {
		if (action == BEGINN || action == WECHSEL) {
			return ENDE;
		}
		if (action == PAUSE) {
			return PAUSE_ENDE;
		}
		if (action == ARZT) {
			return ARZT_ENDE;
		}
		if (action == BESORGUNG) {
			return BESORGUNG_ENDE;
		}
		return null;
	}
	
	public Action getEndAction() {
		return getEndAction(currentAction);
	}

	public Action getWorkingStateAction() {
		return WECHSEL;
	}

	public Action getNextWorkingStateAction(Action action) {	
		if (action == ENDE) {
			return BEGINN;
		}
		return WECHSEL;
	}

	public int getActionIndex(Action action) {
		for (int i=0; i<allActions.length; i++) {
			if (action == allActions[i]) {
				return i;
			}
		}
		return -1;
	}
	
	private String getProjectName() {
		if (project == null) {
			return "????";
		}
		return project.getName();
	}
	
	private String getTaskName() {
		if (task == null) {
			return "????";
		}
		return task.getName();
	}
	
	private String getActionText() {
		if (currentAction == ENDE) {
			return "Freizeit";
		}
		return currentAction.getName();
	}
	
	private void updateDisplay() {
		
		if (textView == null) {
			outstandingUpdate = true;
			return;
		}
		
		if (projectTextView == null) {
			outstandingUpdate = true;
		}
		
		Runnable updater = new Runnable() {
			public void run() {
				MyLog.e("--->", ""+currentAction);
				String txt = currentAction.needsProject() ? (getProjectName() + " - " + getTaskName()) : getActionText();
				if (currentAction == TASK_END) {
					txt += " wurde beendet";
				}
				textView.setText(txt);
				if (projectTextView != null) {
					projectTextView.setText("==> "+getProjectName()+" <==");
				}
				setButtons();
			}
		};
		if (Looper.getMainLooper().equals(Thread.currentThread())) {
			updater.run();
			return;
		}
		textView.post(updater);
	}

	public void setButtons() {
		setEndWorkButtonText();
		setFunctionButtonText();
	}
	
	private void setEndWorkButtonText() {
		if (currentAction.isPossible(ENDE)) {
			btEndWork.setText("Arbeitsende");
			btEndWork.setClickable(true);
			return;
		}
		btEndWork.setText("---");
		btEndWork.setClickable(false);
	}
	
	private void setFunctionButtonText() {
		if (currentAction.isPossible(PAUSE)) {
			btFunction.setText("Pause");
			btFunction.setClickable(true);
			return;
		}
		if (currentAction.isPossible(PAUSE_ENDE)) {
			btFunction.setText("Pause beenden");
			btFunction.setClickable(true);
			return;
		}
		btFunction.setText("---");
		btFunction.setClickable(false);
	}




}
