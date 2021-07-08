package de.cisoft.framework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import de.cisoft.zeiterfassung.implementation.entity.Booking;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;	

public abstract class Entity implements Comparable<Entity> {
	public enum MethodKind {
		UNKNOWN,
		GETTER,
		SETTER,
		OTHER
	}
	private Field idField;

	public Entity() {
		//MyLog.i("Entity", "In default constructor");
	}
	
	public Entity (String[] fields, de.cisoft.framework.Field[] assignments) {
		if (this.getClass() == Booking.class) {
			//MyLog.i("--", "--");
		}
		int length = fields.length;
		if (length > assignments.length) {
			length = assignments.length;
		}
		for (int i=0; i<length; i++) {
			Field field = assignments[i];
			if (field.isId()) {
				this.idField = field;
			}
			if (field!=null) {
				field.set(this, fields[i]);
			}
		}
	}
	
	public Entity (String row, String separator, Field[] assignments) {
		this (row.split(separator), assignments);
		if (this.getClass() == Booking.class) {
			//MyLog.i("Ctor Booking)"," -> "+row);
		}

	}
	
	private MethodKind getMethodKind(Method method) {
		String name = method.getName();
		if (name.length()>3) {
			String l = name.substring(0,3);
			if (l.equals("set")) {
				return MethodKind.SETTER;
			}
			if (l.equals("get")) {
				return MethodKind.GETTER;
			}
		}
		return MethodKind.OTHER;
	}
	
	public Map<String, Field> selfExamine() {
		
		Method[] methods = this.getClass().getMethods();
		Map<String, Field> fields = new HashMap<String, Field>();
		for (Method method : methods) {
			MethodKind methodKind = getMethodKind(method);
			if (methodKind == MethodKind.GETTER || methodKind == MethodKind.SETTER) {
				String name = method.getName().substring(3).toLowerCase();
				Field field = fields.get(name);
				if (methodKind == MethodKind.GETTER) {
					if (field == null) {
						field = new Field(null, method);
						fields.put(name, field);
					} else {
						field.setGetter(method);
					}
				}
				if (methodKind == MethodKind.SETTER) {
					if (field == null) {
						field = new Field(method);
						fields.put(name, field);
					} else {
						field.setSetter(method);
					}
				}
				if (field.isId()) {
					this.idField = field;
				}
			}

		}
		return fields;
	}

	public String serialize(String fieldseparator, Field[] assignments) {
		StringBuilder sb = new StringBuilder();

		int length = assignments.length;
		for (int i=0; i<length; i++) {
			if (i != 0) {
				sb.append(fieldseparator);
			}
			Field field = assignments[i];
			if (field!=null) {
				sb.append(field.getString(this));
			}
		}
		return sb.toString();
	}

	public String getString(Field field) {
		return field.getString(this);
	}
	
	public Object get(Field field) {
		
		return field.get(this);
	}
	
	public void set(Field field, Object value) {
		if (field != null) {
			field.set(this, value);
		}
	}
	
	public Field getIdField() {
		return this.idField;
	}
	
	
	public Object getIdValue() {
		Field idField = getIdField();
		if (idField == null) {
			return null;
		}
		
		return get(idField);
	}
	
	public void setIdValue(Object value) {
		Field idField = getIdField();
		if (idField != null) {
			set(idField, value);
		}
	}

	public int compareTo(Entity another) {
		return this.toString().compareTo(another.toString());
	}
	
	protected void fill(View convertView, int position) {
		
	}
	
	protected View getView(Context context, int resource, View convertView, ViewGroup parent, int position) {
		
		if (convertView == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			convertView = inflater.inflate(resource, parent, false);
		}
		fill(convertView, position);
		return convertView;
	}

	public boolean shouldSave() {
		return true;
	}
}
