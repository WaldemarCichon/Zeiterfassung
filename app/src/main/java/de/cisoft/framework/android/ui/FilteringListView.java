package de.cisoft.framework.android.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class FilteringListView extends ListView {
	
	private String filter;


	public FilteringListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public FilteringListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public FilteringListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void setFilter(String filter) {
		this.filter = filter;
		ListAdapter adapter = getAdapter();
		if (adapter instanceof ArrayAdapter<?>) {
			Filter ffilter = ((ArrayAdapter<?>)adapter).getFilter();
			ffilter.filter(filter);
		}		
	}
	
	public String getFilter() {
		return this.filter;
	}
	
	public Filter getAdapterFilter() {
		ListAdapter adapter = getAdapter();
		if (adapter instanceof ArrayAdapter<?>) {
			return ((ArrayAdapter<?>)adapter).getFilter();
		}
		return null;
	}
	
	
	public void removeFilter() {
		setFilter(null);
	}
}
