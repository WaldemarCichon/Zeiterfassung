package de.cisoft.framework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.ObjectInputStream.GetField;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.ReflectPermission;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.cisoft.framework.webservice.Webservice;
import de.cisoft.utility.log.MyLog;
import de.cisoft.zeiterfassung.MainActivity;
import de.cisoft.zeiterfassung.implementation.entity.Booking;
import de.cisoft.zeiterfassung.implementation.entity.JanitorStreetLinks;
import de.cisoft.zeiterfassung.implementation.entity.Project;
import de.cisoft.zeiterfassung.implementation.entity.Projects;
import de.cisoft.zeiterfassung.implementation.enums.SaveMode;
import de.cisoft.zeiterfassung.implementation.helpers.settings.Settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public abstract class EntityTable<T extends Entity> {
	private SparseArray<T>  entities;
	protected Map<String,Field>  fields;
	private Class<T> clazz;
	private String   title;
	private List<String> notAssigned;
	protected Field[] assignments;
	private de.cisoft.framework.Field[] fieldDescriptions;
	private de.cisoft.framework.Field[] sortedFieldDescriptions;
	private static final String fieldSeparator = "\\|";
	private static final String rowSeparator = "~";
	private Constructor<T> constr;
	private int externalCount;
	private List<T> sortedValues;
	private String[] sortedStringArray;
	private Comparator<T> comparator;
	private int defaultId = 0;
	private int rowCount;
	private boolean saved = false;

	
	public EntityTable(Class<T> clazz) { 
		MyLog.i("EntityTable", "Before getting obj");
		this.clazz = clazz;
		T obj = (T)getInstance(clazz);
		fields = obj.selfExamine();
		entities = new SparseArray<T>();
	}
	
	
	public EntityTable(Class<T> clazz, FileInputStream fis, String title) {
		this(clazz, getString(fis), title);
	}
	
	public EntityTable(Class<T> clazz, String[] rows, String title) {
		this(clazz);
		MyLog.i("EntityTable "+clazz.getCanonicalName(), "Here I am");
		boolean skip = false;
		
		if (title!=null) {
			this.title = title;
		    assignFields(title.split(fieldSeparator));
		    if (!rows[0].contains(fieldSeparator)) {
		    	try {
		    		this.externalCount = Integer.parseInt(rows[0]);
		    		skip = true;
		        } catch (Exception ex) {
		        	
		        }
		    }
		}
		int i=0; 
		//TODO Remove for 
		rowCount = rows.length-(skip ? 2 : 1);
		storingStarted(rowCount);
		T instance = null;
		for (String row : rows) {
			if (skip) {
				skip = false;
				continue;
			}
			if (title == null) {
				title = row;
				this.title = title;
				assignFields(title.split(fieldSeparator));
			} else {
				if (row.length()>0) {
					instance = (T)getInstance(clazz, row, fieldSeparator, assignments);
					if (instance instanceof Booking) {
					  MyLog.i(clazz.toString(), instance.toString());
					}
					
					if (instance != null && shouldStore(i, instance)) {
						beforeStore(i, instance);
						Integer id = (Integer)instance.getIdValue();
						if (id==null) {
							id = preIncId();
						} else {
							if (id > defaultId) {
								defaultId = id;
							}
						}
						entities.put(id, instance);
						afterStore(i, instance);
					}
				}
			}
		}
		storingCompleted(instance, rowCount);
	}
	
	public int getRowCount() {
		return rowCount;
	}
	
	public int getExternalRowCount() {
		return externalCount;
	}
	
	public int getInternalRowCount() {
		return entities.size();
	}
	
	public void setFieldDesriptions(de.cisoft.framework.Field[] fieldDescriptions) {
		this.fieldDescriptions = fieldDescriptions;
	}
	
	public de.cisoft.framework.Field[] getFieldDescriptions() {
		return fieldDescriptions;
	}
	
	public void setComparator(Comparator<T> comparator) {
		if (comparator != this.comparator)  {
			this.comparator = comparator;
		}
	}
	
	public Comparator<T> getComparator() {
		return comparator;
	}
	
	/*
	private void sortFieldDescriptions(String[]fieldList) {
		sortedFieldDescriptions = new de.cisoft.framework.Field[fieldList.length];
		for (int i=0; i<fieldList.length; i++) {
			for (int ii=0; ii<fieldDescriptions.length; ii++) {
				if (fieldDescriptions[ii].getAlias() == fieldList[i]) {
					sortedFieldDescriptions[i] = fieldDescriptions[ii];
					break;
				}
			}
		}
	}
	
	protected static de.cisoft.framework.Field[] getSortedFieldDescriptions() {
		return sortedFieldDescriptions;
	}
	*/
	
	@SuppressLint("DefaultLocale")
	private void assignFields(String[] fieldNames) {
		int i = 0;
		assignments = new Field[fieldNames.length];
		for (String fieldName : fieldNames) {
			String lowerFieldName = fieldName.toLowerCase().trim();
			if (fields.containsKey(lowerFieldName)) {
				assignments[i] = fields.get(lowerFieldName);
			} else {
				if (notAssigned == null) {
					notAssigned = new LinkedList<String>();
				}
				notAssigned.add(fieldName);
			}
			i++;
		}
	}
	

	protected void afterStore(int count, T	instance) {
		// could be overwritten
	}
	
	protected void beforeStore(int count, T	instance) {
		// could be overwritten
	}
	
	protected boolean shouldStore(int count, T instance) {
		return true; // could be also overwritten
	}
	
	protected void storingStarted(int rowCount) {
		
	}
	
	protected void storingCompleted(T lastRow, int rowCount) {
		
	}
	
	public EntityTable (Class<T> clazz, String line, String title) {
		this(clazz, line.split(rowSeparator), title);
	}
	
	
	public EntityTable(Class<T> clazz, Webservice webservice, String serviceName, String titleLine) throws Exception {
		this(clazz, webservice.call(serviceName), titleLine);
	}

	public EntityTable(Class<T> clazz, int i) {
		this(clazz);
		defaultId  = i;
	}

	private T getInstance(Class<T> clazz, String row, String fieldSeparator, Field[] sortedFieldDescriptions) {
		try {
			if (constr == null) {
				constr = clazz.getConstructor(String.class, String.class, Field[].class);
			}
			return constr.newInstance(row, fieldSeparator, sortedFieldDescriptions);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	
	private T getInstance(Class<T> clazz) {
		T obj = null;
		try {
			obj = clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return obj;
	}

	private List<T> toList(SparseArray<T> a) {
		List<T> list = new LinkedList<T>();
		int size = a.size();
		for (int i=0; i<size; i++) {
			list.add(a.valueAt(i));
		}
		return list;
	}

	private T[] toArray(SparseArray<T> a) {
		T[] array = getArray(a.size());
		for (int i=0; i<array.length; i++) {
			array[i] = a.valueAt(i);
		}
		return array;
	}
	
	@SuppressWarnings("unchecked")
	public T[] toArray() {
		return toArray(entities);
	}
	
	public String[] toStringArray() {
		String[] array = new String[entities.size()];
		{
			for (int i=0; i<entities.size(); i++) {
				array[i++]=entities.valueAt(i).toString();
			}
		}
		return array;
	}
	
	@SuppressWarnings("unchecked")
	public T[] toSafeArray(boolean sort) {
		T[] array = getArray (entities.size());
		int i=0;
		List<T> list = toList(entities);
		if (sort) {
			Collections.sort(list);
		}
		for (Entity entity : list) {
			array[i++] = (T)entity;
		}
		return array;
	}
	
	public String[] toSortedStringArray() {
		if (this.sortedStringArray == null || this.sortedValues == null) {
			Collection<T> c = this.getSortedValues();
			String[] array = new String[c.size()];
			int i=0;
			for (T t : c) {
				array[i++] = t.toString();
			}
			this.sortedStringArray = array;
		}
		return this.sortedStringArray;
	}
	
	public List<T> getValues() {
		return toList(entities);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<T> getSortedValues() {
		if (this.sortedValues == null) {
			List<T> values = toList(entities);
			if (comparator == null) {
			    Collections.sort(values);
			} else {
				Collections.sort(values, comparator);
			}
				
			sortedValues=values;
		}
		return this.sortedValues;
	}
	
	protected void setTitle(String title) {
		this.title = title;
	}
	
	protected String getTitle() {
		return title;
	}
	
	protected String getDefaultTitle() {
		return null;
	}
	
	protected boolean isTitleUsed() {
		return false;
	}
	
	protected String getDefaultFileName() {
		return null;
	}
	
	public abstract Class <T> getEntityClass();
		
	protected String getServiceName() {
		return null;
	}
	
	public FileInputStream getDefaultInputStream() throws FileNotFoundException {
		return getInputStream(getDefaultFileName());
	}
	
	
	public void save() throws IOException {
		saveTo(getDefaultFileName());
	}
	
	public boolean isSaved() {
		return saved;
	}
	
	public FileOutputStream getDefaultOutputStream() throws FileNotFoundException {
		return getOutputStream(getDefaultFileName());
	}
	
	private static FileOutputStream getOutputStream (String filename) throws FileNotFoundException {
		return MainActivity.getLastInstance().openFileOutput  (filename, Context.MODE_PRIVATE);
	}
	
	public void saveTo(String filename) throws IOException {
		if (isSaved()) {
			return;
		}
		FileOutputStream fos = getOutputStream(filename);
		saveTo(fos);
		fos.close();
		saved = true;
	}
	
	public void saveTo(FileOutputStream fos) throws IOException {
		if (isSaved()) {
			return;
		}
		String s = serialize();
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(s);
		bw.close();
		osw.close();
		saved = true;
	}
	
	public String serialize() {
		String rowSeparator = EntityTable.rowSeparator.substring(EntityTable.rowSeparator.length()-1); // get only last char;
		String fieldSeparator = EntityTable.fieldSeparator.substring(EntityTable.fieldSeparator.length()-1); // remove all escapes
		StringBuilder sb = new StringBuilder();
		//boolean start = true;
		int size = entities.size(); 
		sb.append(size); //.append(rowSeparator);
		for (int i=0; i<size; i++) {
			Entity entity = entities.valueAt(i);
			if (entity.shouldSave()) {
				sb.append(rowSeparator);
				sb.append(entity.serialize(fieldSeparator, assignments));
			}
		}
		return sb.toString();
	}
	
	
	
	protected static FileInputStream getInputStream(String filename) throws FileNotFoundException {
		return Settings.getCurrentContext().openFileInput(filename);
	}
	
	private static String getString(FileInputStream fis) {
		
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		int blockLength=1024;  
		char[] buffer = new char[blockLength];
		StringBuffer sb = new StringBuffer();
		int charCount=blockLength+1;
		while (charCount>=blockLength) {
			try {
				charCount = br.read(buffer);
				if (charCount>=blockLength) {
					sb.append(new String(buffer));
				} else { if (charCount>0)
					sb.append(new String(buffer,0,charCount));
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	
	}
	
	@SuppressLint("DefaultLocale")
	public T find(String fieldName, String what) {
		Field field = fields.get(fieldName.toLowerCase());
		if (field == null || field.getType() != String.class) {
			return null;
		}
		int size = entities.size();
		for (int i=0; i<size; i++) {
			T entity = entities.valueAt(i);
			if (((String)entity.get(field)).equalsIgnoreCase(what)) {
				return entity;
			}
		}
		return null;
	}
	
	public T find(int id) {
		return entities.get(id);
	}
	
	protected abstract EntityTable<T> getInstance();
	
	@SuppressLint("DefaultLocale")
	public EntityTable<T> filter(String fieldName, Object what) {

		Field field = fields.get(fieldName.toLowerCase());
		if (field == null) {
			return null;
		}
		EntityTable<T> table = getInstance();	
		table.assignments = this.assignments;
		int size = entities.size();
		for (int i=0; i<size; i++) {
			T entity = entities.valueAt(i);
			if (entity.get(field).equals(what)) {
				table.add(entity);
			}
		}
		return table;
	}
	
	public void add(T entity) {
		Integer idValue = (Integer) entity.getIdValue();
		if (idValue == null || idValue<=0) {
			idValue = preIncId();
		}
		this.entities.put(idValue, entity);
		this.sortedValues = null;
		saved = false;
	}
	
	public void add(EntityTable<T> second) {
		if (second == null || second.getCount() == 0) {
			return; // do nothing to avoid exception and speed up
		}
		int size = second.getCount();
		for (int i=0; i<size; i++) {
			T entity = second.getItemAt(i);
			this.add(entity);
		}
	}
	
	public Field getField(String fieldName) {
		return fields.get(fieldName);
	}
	
	public EntityTable<T> in(EntityTable<? extends Entity> source, String fieldName) {
		Collection<? extends Entity> entities = source.getValues();
		Field field = source.getField(fieldName.toLowerCase());
		if (field == null) {
			return null;
		}
		EntityTable<T> table = getInstance();
		table.assignments = this.assignments;
		for (Entity entity : entities) {
			Integer key = (Integer) entity.get(field);
			T value = this.entities.get(key);
			if (value!=null) {
				table.add(value);
			}
		}
		return table;
	}
	
	public EntityTable<T> in(String myFieldName, EntityTable<? extends Entity> source, String fieldName) {
		//TODO Implement like above, finding each row by linear search or build a hashtable first or both according to number of rows
		return null;
	}
	
	public T getFromSortedById(int id) {
		return ((List<T>) getSortedValues()).get(id);
	}
	
	public int getCount() {
		return entities==null ? -1 : entities.size(); 
	}
	
	public int getDefaultId() {
		return defaultId;
	}
	
	public void setDefaultId(int id) {
		this.defaultId = id;
	}
	
	public int preIncId() {
		return ++defaultId;
	}
	
	public int postIncId() {
		return defaultId++;
	}
	
	protected String getFieldSeparator() {
		return fieldSeparator;
	}
	
	protected String getRowSeparator() {
		return rowSeparator;
	}
	
	public Field[] getAssignments() {
		return assignments;
	}
	
	public T getItemAt(int position) {
		if (position>entities.size()) {
			return null;
		}
		return entities.valueAt(position);
		//int key = entities.keyAt(position);
		//return entities.get(key);
	}
	
	protected View getView(T item, View convertView, ViewGroup parent, Context context, int resource, int position) {
		//TODO should make a generic implementation using some 
		//callbacks in implementation classes but now it should be overwritten
		return item.getView(context, resource, convertView, parent, position); 
	}
	
	protected View getView(int position, View convertView, ViewGroup parent, Context context, int resource) {
		T item = getItemAt(position);
		convertView.setId(position);
		return getView(item, convertView, parent, context, resource, position); 
	}
	
	protected Class<? extends Entity[]> getArrayClass() {
		return Entity[].class;
	}
	
	@SuppressWarnings("unchecked")
	public T[] getArray(int size) {
		Class<? extends Entity> arrayClass = getEntityClass();
		
		T[] objects = (T[]) Array.newInstance(arrayClass, size);
		return objects;
	}
	
	
	
	public ArrayAdapter<T> getAdapter(final Context context, final int resource) {
		
		T[] val =  getArray(entities.size());//(T[]) new Object[entities.size()];
		List<T> list = toList(entities);
		Collections.sort(list);
		/*
		for (T v : entities.values()) {
			val[i++]=v;
		}
	
		T[] values =  list.toArray(val);
		*/
		ArrayAdapter<T> adapter = new ArrayAdapter<T>(context, resource, list) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView!=null) {
					convertView.setId(position);
				}
				return EntityTable.this.getView(getItem(position), convertView, parent, context, resource, position);
			}
			
		};
		return adapter;
	}
	
	protected Collection<T> getEntites() {
		return getValues();
	}
	
	public T getItemWithId(int id) {
		return entities.get(id);
	}


	public void backup() {
		File filesDir = Settings.getCurrentContext().getFilesDir();
		String fileName = getDefaultFileName();
		File file = new File(filesDir, fileName);
		File newPath = new File(filesDir, fileName+".bak");
		if (file.exists()) {
			if (newPath.exists()) {
				newPath.delete();
			}
			file.renameTo(newPath);
		}
	}
}
