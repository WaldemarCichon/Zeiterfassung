package de.cisoft.framework.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Target({ElementType.METHOD, ElementType.FIELD} ) 
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {
	String name();
	String alias();
	Class<?> type();
	int    maxLength() default 0;
	int    decimal() default 0;
	int    sequenceId() default -1;

}
