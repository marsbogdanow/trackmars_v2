package com.trackmars.and.tracker.dataUtils;
import java.lang.annotation.*;

@Target(value=ElementType.FIELD)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface EntityField {
	
	static final String TYPE_INTEGER = "INTEGER"; 
	static final String TYPE_TEXT = "TEXT"; 
	static final String TYPE_REAL = "REAL";
	
	String type() default EntityField.TYPE_TEXT;
	boolean primaryKey() default false;
	boolean autoIncrement() default false;
}
