package de.cisoft.framework.android.ui;

import java.util.List;

import de.cisoft.zeiterfassung.R;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class FilteringArrayAdapter extends ArrayAdapter<String> {

	private ArrayAdapter<String> adapter;
	private String filter;

	public FilteringArrayAdapter(Context context, int resource,
			int textViewResourceId, List<String> objects) {
		super(context, resource, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

	public FilteringArrayAdapter(Context context, int resource,
			int textViewResourceId, String[] objects) {
		super(context, resource, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

	public FilteringArrayAdapter(Context context, int resource,
			int textViewResourceId) {
		super(context, resource, textViewResourceId);
		// TODO Auto-generated constructor stub
	}

	public FilteringArrayAdapter(Context context, int textViewResourceId,
			List<String> objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

	public FilteringArrayAdapter(Context context, int textViewResourceId,
			String[] objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

	public FilteringArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		// TODO Auto-generated constructor stub
	}
	
	public FilteringArrayAdapter(ArrayAdapter<String> adapter) {
		super(adapter.getContext(), R.layout.textview_project_list);
		this.adapter = adapter;
		fillAdapter();
	}
	
	private void fillAdapter() {
		this.clear();
		for (int i=0; i<adapter.getCount(); i++) {
			this.add(adapter.getItem(i));
		}
	}
	
	private void fillAdapter(String filter) {
		this.clear();
		for (int i=0; i<adapter.getCount(); i++) {
			String s = adapter.getItem(i);
			if (s.contains(filter)) {
				this.add(s);
			}
		}
	}
	
	public void setFilter(String filter) {
		if (filter == null || filter.length()==0) {
			clearFilter();
		}
		this.filter = filter;
		fillAdapter(filter);
	}
	
	public void clearFilter() {
		this.filter = null;
		fillAdapter();
	}
	
	public String getFilterString() {
		return this.filter;
	}
}
