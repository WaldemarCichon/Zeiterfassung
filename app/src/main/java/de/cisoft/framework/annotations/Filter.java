package de.cisoft.framework.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD} ) 
@Retention(RetentionPolicy.RUNTIME)
public @interface Filter  {
	int precedence() default -1;
}
