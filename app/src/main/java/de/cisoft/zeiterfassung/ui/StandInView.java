package de.cisoft.zeiterfassung.ui;

import de.cisoft.framework.webservice.Webservice;
import de.cisoft.zeiterfassung.R;
import de.cisoft.zeiterfassung.R.layout;
import de.cisoft.zeiterfassung.R.menu;
import de.cisoft.zeiterfassung.implementation.entity.Action;
import de.cisoft.zeiterfassung.implementation.entity.Actions;
import de.cisoft.zeiterfassung.implementation.entity.Janitor;
import de.cisoft.zeiterfassung.implementation.entity.Janitors;
import de.cisoft.zeiterfassung.implementation.entity.Project;
import de.cisoft.zeiterfassung.implementation.entity.Projects;
import de.cisoft.zeiterfassung.implementation.entity.Tasks;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StandInView extends Activity {

	protected static int selectedPosition = -1;
	protected static CharSequence selectedText;
	private ListView lvUsers;
	private ArrayAdapter<String> adapter;
	private Janitors janitors;
	private Menu menu;
	private Settings settings;
	private Janitor selected;
	private View  selectedView;
	private TextView tvStandInName;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_stand_in);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        janitors = EntitiesFactory.getInstance().getJanitors();
        settings = Settings.getInstance();
        createUiew();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_users_view, menu);
        this.menu = menu;
        return true;
    }

    public View getSelectedView() {
    	return selectedView;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int id = item.getItemId();
        switch (id) {
        	case R.id.choose : {
        		if (selectedPosition!=-1) {
    				settings.setStandIn(selected);
    				settings.persistStandIn();
    				if (selectedPosition!=-2) {
    					EntitiesFactory factory = EntitiesFactory.getInstance();
    					factory.initPersonalData(selected, false);
    					factory.saveStandInProjects();
    				}
    				setResult(RESULT_OK);
    				finish();
        		}
        		return true;
        	}
            case de.cisoft.zeiterfassung.R.id.cancel: {
            	Intent i = new Intent();
            	i.putExtra("QUIT", true);
            	setResult(RESULT_CANCELED);
            	finish();
            	return true;
            }
            case de.cisoft.zeiterfassung.R.id.exit: {
            	Intent i = new Intent();
            	i.putExtra("QUIT", true);
            	setResult(RESULT_CANCELED);
            	finish();
            	return true;
            }
            case de.cisoft.zeiterfassung.R.id.clear: {
            	selected = null;
            	selectedPosition = -2;
            	tvStandInName.setText(R.string.none_user_choosen);
            }
            
            
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void createUiew() {
    	tvStandInName = (TextView) this.findViewById(R.id.tvChoosenUser);
    	Janitor standIn = settings.getStandIn();
    	if (standIn!=null) {
    		String name = standIn.toString();
    		tvStandInName.setText(name);
    	}
    	lvUsers = (ListView) this.findViewById(R.id.lvStandIn);
    	adapter = new ArrayAdapter<String>(this, R.layout.textview_project_list, /*FRUITS*/janitors.toSortedStringArray());
    	lvUsers.setAdapter(adapter);
    	
    	lvUsers.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				StandInView.selectedPosition = position;
				StandInView.selectedText = ((TextView) view).getText();
				selected = janitors.getFromSortedById(selectedPosition);
				tvStandInName.setText(selected.toString());
				selectedView = view;
				Toast.makeText(getApplicationContext(), selectedText, Toast.LENGTH_SHORT).show();
			}
		});
    }

}
