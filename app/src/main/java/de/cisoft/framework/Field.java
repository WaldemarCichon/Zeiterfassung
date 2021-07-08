package de.cisoft.framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Date;

import de.cisoft.framework.annotations.Filter;
import de.cisoft.framework.annotations.Name;
import de.cisoft.zeiterfassung.implementation.entity.Action;
import de.cisoft.zeiterfassung.implementation.entity.Actions;
import de.cisoft.zeiterfassung.implementation.helpers.EntitiesFactory;


public class Field {
	private String fieldName;
	private String name;
	private String alias;
	private Class type;
	private int   maxLen;
	private int   decimal;
	private int   sequenceId=-1;
	//private String asString; //TODO can be removed after beta phase
	
	private Method getter;
	private Method setter;
	
	private de.cisoft.framework.annotations.Field fieldAnnotation;
	private de.cisoft.framework.annotations.ID idAnnotation;
	private Type setterType;
	private Type getterType;
	private boolean isIdField;
	private boolean isFilterField;
	private boolean isNameField;
	private Filter filterAnnotation;
	private Name nameAnnotation;
	
	public Field(String name, Class<?> type) {
		this(name, type, 0);
	}
	
	public Field (String name, Class<?> type, int maxLen) {
		this(name, type, maxLen, 0);
	}
	
	public Field (String name, Class<?> type, int maxLen, int decimal) {
		this (name, null, type, maxLen, decimal);
	}
	
	public Field (String name,  String alias, Class<?> type, int maxLen, int decimal) {
		this.name = name;
		this.alias = alias;
		this.type = type;
		this.maxLen = maxLen;
		this.decimal = decimal;
	}
	
	public Field (String name, String alias, Class<?> type, int maxLen, int decimal, int sequenceId) {
		this(name, alias, type, maxLen, decimal);
		this.sequenceId = sequenceId;
	}
	
	public Field(de.cisoft.framework.annotations.Field anno) {
		this(anno.name(), anno.type());
	}
	
	public Field(Method setter) {
		setSetter(setter);
	}
	
	public Field(Method setter, Method getter) {
		setSetter(setter);
		setGetter(getter);
	}
	
	public void setSetter(Method setter) {
		if (setter == null) {
			return;
		}
		tryInitialize(setter);
		this.setter = setter;
	}

	
	private void tryInitAnnotations(Method method) {
		Annotation[] annotations =  method.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof de.cisoft.framework.annotations.Field) {
				this.fieldAnnotation = (de.cisoft.framework.annotations.Field) annotation;
				this.name=this.fieldAnnotation.name();
				this.alias=this.fieldAnnotation.alias();
				this.type=this.fieldAnnotation.type();
			}
			
			if (annotation instanceof de.cisoft.framework.annotations.ID) {
				this.idAnnotation = (de.cisoft.framework.annotations.ID) annotation;
				isIdField = true;
			}
			
			if (annotation instanceof de.cisoft.framework.annotations.Name) {
				this.nameAnnotation = (de.cisoft.framework.annotations.Name) annotation;
				isNameField = true;
			}
			if (annotation instanceof de.cisoft.framework.annotations.Filter) {
				this.filterAnnotation = (de.cisoft.framework.annotations.Filter) annotation;
				isFilterField = true;
			}
		}
		
	}
	
	public void setAnnotation(de.cisoft.framework.annotations.Field annotation) {
		this.fieldAnnotation = annotation;
		this.name = annotation.name();
		this.type = annotation.type();
	}
	
	private void init(Method method) {
		String name = method.getName();
		if (fieldName == null && name.length()>3) {		
			fieldName = name.substring(3);
		} 
		String prefix = (name.length()>3 ? name.substring(0,3) : name);
		
		if (name.length()>3 && (
				prefix.equals("set") || prefix.equals("get"))) {
			
			if (prefix.equals("set")) {
				setterType = method.getGenericParameterTypes()[0];
			} else {
				getterType = method.getGenericReturnType();
			}
			
		}
	}
	
	private void tryInitialize(Method method) {
		
		init(method);
		tryInitAnnotations(method);
	}
	
	public void setGetter(Method getter) {
		if (getter == null) {
			return;
		}
		tryInitialize(getter);
		this.getter = getter;
	}
	
	
	
	

	public int getSequenceId() {
		return sequenceId;
	}
	
	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public int getMaxLen() {
		return maxLen;
	}

	public void setMaxLen(int maxLen) {
		this.maxLen = maxLen;
	}

	public int getDecimal() {
		return decimal;
	}

	public void setDecimal(int decimal) {
		this.decimal = decimal;
	}
	
	@Override
	public String toString() {
		return name+"/"+fieldName+" ("+alias+") : "+type;
	}

	public Object get(Entity e) {
		if (getter==null) {
			return null;
		}
		
		try {
			return getter.invoke(e);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public String getString(Entity e) {
		Object o = get(e);
		if (o==null) {
			return null;
		}
		//if (o instanceof Date) improve date handling by getting time() 
		if (o instanceof Entity) {
			return ((Entity) o).getIdValue().toString();
		}
		if (o instanceof Date) {
			return Long.toString(((Date) o).getTime());
		}
		if (o instanceof Action) {
			return ((Action) o).getKonto();
		}
		return o.toString();
	}
	
	@SuppressWarnings("unchecked")
	public void set(Entity e, Object o) {
		if (setter==null) {
			return; // could be, we should throw an exception?
		}
		if (o==null ||
				type.equals(o.getClass()) ||
				type.isAssignableFrom(o.getClass())) {
			try {
				setter.invoke(e, o);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void set(Entity e, String string) {
		if (setter==null) {
			return; // could be, we should throw an exception?
		}
		

		try {
			if (type == String.class) {
				setter.invoke(e, string);
				return;
			}
			
			if (type == Integer.class) {
				
				Integer i=null;
				if (string != null && string.length()>0 && !string.equals("null")) {
					try {
						i = Integer.parseInt(string);
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				}
				setter.invoke(e, i);
				return;
			}
			
			if (type == Date.class) {
				
				Date d = null;
				if (string.contains(":")) {
					try {
						d = new Date(Date.parse(string));
					} catch (Exception ex) {
						ex.printStackTrace();
					} 
				} else {
					try {
						Long l = string.equals("null") ? null : Long.parseLong(string);
						if (l!=null) {
							d = new Date(l);
						}
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				}
				setter.invoke(e, d);
				return;
			}
			
			if (type == Double.class) {
				
				Double d = null;
				try {
					d = string.equals("null") ? null : Double.parseDouble(string);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				
				setter.invoke(e, d);
				return;
			}
			if (type == Long.class) {
				Long l = string.equals("null") ? null : Long.parseLong(string);
				setter.invoke(e, l);
				return;
			}
			
			if (type == Float.class) {
				Float f = string.equals("null") ? null : Float.parseFloat(string);
				setter.invoke(e, f);
				return;
			}
			
			if (Entity.class.isAssignableFrom(type)) {
				Entity instance = EntitiesFactory.getInstance().getEntity(type, string);
				setter.invoke(e, instance);
			}
			
			if (type == Action.class) {
				Action a = Actions.getInstance().getAction(string);
				setter.invoke(e, a);
			}
			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public boolean isId() {
		
		return isIdField;
	}

	public boolean isFilterField() {
		
		return isFilterField;
	}

	public boolean isNameField() {
		
		return isNameField;
	}
}
