package de.cisoft.zeiterfassung.implementation.entity;

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import android.content.Context;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import de.cisoft.framework.EntityTable;
import de.cisoft.framework.webservice.Webservice;

public class Tasks extends EntityTable<Task>{
	private static final String titleLine = "id|name|konto|iconName";
	private static final String serviceName = "SGetTasks";
	private static final String FILE_NAME = "tasks.dat";
	
	public Tasks() throws FileNotFoundException {
		super(Task.class, getInputStream(FILE_NAME), titleLine);
	}
	
	public Tasks(Webservice webservice) throws Exception {
		super(Task.class, webservice, serviceName, titleLine);
	}
	
	public Tasks(Class<Task> clazz) {
		super(clazz);
	}
	
	protected Tasks getInstance() {
		return new Tasks(Task.class);
	}

	public void fill(RadioGroup rgTasks, Context context) {
		rgTasks.removeAllViews();
		int i = 0;
		for (Task task : this.getSortedValues()) {
			RadioButton rb =new RadioButton(context);
			rb.setText(task.toString());
			rb.setId(task.getId());
			rgTasks.addView(rb, i++);			
		}
	}
	
	public Class<Task> getEntityClass() {
		return Task.class;
	}
	
	@Override
	protected String getDefaultFileName() {
		return (FILE_NAME);
	}
	
	protected String getServiceName() {
		return serviceName;
	}
	
	protected String getDefaultTitle() {
		return titleLine;
	}

	public Task find(ListView lvTasks) {
		int checkId = lvTasks.getSelectedItemPosition();
		Logger.getLogger(getClass().getName()).info("Finding task at id"+checkId);
		if (checkId < 0) {
			return null;
		}
		return find(checkId);
	}
	
	public Task find(RadioGroup rgTasks) {
		int btId = rgTasks.getCheckedRadioButtonId();
		if (btId<0) {
			return null;
		}
		return find(btId);
	}
	
	
}
