package de.cisoft.zeiterfassung.ui;

import java.util.logging.Logger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import de.cisoft.framework.android.ui.ActivityWithErrorhandling;
import de.cisoft.framework.android.ui.ErrorNotifier;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.implementation.entity.Task;
import de.cisoft.zeiterfassung.implementation.entity.Tasks;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;
import de.cisoft.zeiterfassung.implementation.helpers.settings.enums.TaskSelectBehavior;

public class ChooseTaskView extends ActivityWithErrorhandling implements ActivityWithErrorNotifier {


	private EditText etComment;
	private TextView tvProject;
	private RadioGroup rgTasks;
	private ListView lvTasks;
	//private int projectId;
	private Tasks tasks;
	protected boolean dialogResult;
	protected boolean useListView;
	protected int selectedPosition; 

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //MyLog.i("ProjectsView", "Vor Projects");
        
        useListView = Settings.getInstance().isTaskChoiceIcons();
        setContentView(useListView ? R.layout.activity_choose_task_list : R.layout.activity_choose_task_button);
        //MyLog.i("ProjectsView", "before creating projects");

        
        //MyLog.i("Main","after setContentView");
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        createView();
        
        ErrorNotifier.getInstance().addHandler(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_choose_task, menu);
        return true;
    }

    @SuppressWarnings("deprecation")
	private void showAlert(String message) {
    	AlertDialog ad = new AlertDialog.Builder(this).create();
    	ad.setCancelable(false);
    	ad.setMessage(message);
    	ad.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	ad.show();
    }
    
    private void askQuestion(final Task task) {
    	AlertDialog ad = new AlertDialog.Builder(this).create();
    	if (ad==null) {
    		return;
    	}
    	ad.setCancelable(false);
    	String message = this.getString(R.string.chooseAction, task.toString());
    	ad.setMessage(message);
    	ad.setButton(AlertDialog.BUTTON_POSITIVE, "Ja", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				ChooseTaskView.this.dialogResult = true;
				chooseTask(task);
			}
		});
    	ad.setButton(AlertDialog.BUTTON_NEGATIVE, "Nein", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				ChooseTaskView.this.dialogResult = false;
			}
		});
    	ad.show();
    }
    
    private void chooseTask(boolean showQuestion) {
    	Task task = useListView ? tasks.find(selectedPosition) : tasks.find(rgTasks);
    	if (task==null) {
    		showAlert(getString(R.string.taskNotFound, this.selectedPosition)); 
    		return;
    	}
    	if (showQuestion) {
    		askQuestion(task);
    		return;
    	} else if (task == null) {
    		showAlert(getString(R.string.chooseAction));
    		return;
    	}
    	chooseTask(task);
    }
    
    private void chooseTask(Task task) {
    	Intent i = new Intent();
    	i.putExtra("taskId", task.getId());
    	Editable s = etComment.getText();
    	i.putExtra("comment", s==null ? (String)"" : s.toString());
    	setResult(RESULT_OK, i);
        finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case de.cisoft.zeiterfassung.R.id.menu_choose : {
            	chooseTask(false);
                return true;
            }
            case de.cisoft.zeiterfassung.R.id.menu_exit : {
            	Intent i = new Intent();
            	i.putExtra("exit", false);
            	setResult(RESULT_CANCELED);
            	finish();
            	return true;
            }
            case de.cisoft.zeiterfassung.R.id.menu_app_end:
            	Intent i = new Intent();
            	i.putExtra("exit", true);
            	setResult(RESULT_CANCELED, i);
            	finish();
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void createRadioGroup(boolean selectSingleClick) {
    	
    	rgTasks = (RadioGroup) this.findViewById(R.id.rgTask);
        tasks.fill(rgTasks, this);
        rgTasks.setFocusable(true);
        rgTasks.setFocusableInTouchMode(true);
        rgTasks.requestFocus();
        if (selectSingleClick) {
	        rgTasks.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					chooseTask(true);
				}
			});
        }
    }
    
    private void createListView(final boolean selectSingleClick) {
    	lvTasks = (ListView) this.findViewById(R.id.lvTasks);
    	lvTasks.setAdapter(tasks.getAdapter(this, R.layout.task_list_row_with_icon));
   		lvTasks.setOnItemClickListener(new OnItemClickListener() {

   			public void onItemClick(AdapterView<?> listView, View view,
					int position, long id) {
   				lvTasks.setSelection(position);
   				ChooseTaskView.this.selectedPosition = view.getId();
   				Logger.getLogger(getClass().getName()).info("Position = "+position);
   		    	if (selectSingleClick) {
   		    		chooseTask(true);
   		    	}
			}
		});
    }
    
    private void createView() {
    	
    	tasks=EntitiesFactory.getInstance().getTasks();
    	etComment = (EditText) this.findViewById(R.id.etComment);
        tvProject = (TextView) this.findViewById(R.id.tvProjectInWaitPanel);
        Button button = (Button) this.findViewById(R.id.btChoose);
        TaskSelectBehavior taskSelectBehavior = Settings.getInstance().getTaskSelectBehavior();
        if (taskSelectBehavior == TaskSelectBehavior.Button) {
        	button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					chooseTask(false);
				}
			});
        } else {
        	button.setVisibility(Button.GONE);
        }

        boolean selectSingleClick = TaskSelectBehavior.Click == Settings.getInstance().getTaskSelectBehavior();
        if (useListView) {
        	createListView(selectSingleClick);
        } else {
        	createRadioGroup(selectSingleClick);
        }
        if (!Settings.getInstance().showComment()) {
        	etComment.setVisibility(EditText.GONE);
        }
    	Bundle extras = this.getIntent().getExtras();
    	if (extras!=null) {
    		String projectName = extras.getString("ProjectName");
    		//projectId = extras.getInt("ProjectId", -1);
    		if (projectName!=null &&projectName.length()>0)
    			tvProject.setText(projectName);
    		else
    			tvProject.setText("-------------------------");
    	}
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	ErrorNotifier.getInstance().removeHandler(this);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK &&
    			event.getRepeatCount() == 0) {
    		Intent i = new Intent();
    		i.putExtra("exit", false);
    		setResult(RESULT_CANCELED, i);
    	}
    	return super.onKeyDown(keyCode, event);
    }

	@Override
	public int getPanelId() {
		return useListView ? R.id.chooseTaskListWarningPanel : R.id.chooseTaskButtonWarningPanel;
	}

	@Override
	public int getLabelId() {
		return useListView ?  R.id.chooseTaskListWarningLabel : R.id.chooseTaskButtonWarningLabel;
	}
}
	