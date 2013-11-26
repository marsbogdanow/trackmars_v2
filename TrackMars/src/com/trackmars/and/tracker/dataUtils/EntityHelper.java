package com.trackmars.and.tracker.dataUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.Package;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EntityHelper extends SQLiteOpenHelper {
	
	static final private String DATABASE_NAME = "trackmars.db";
	static final private int DATABASE_VERSION = 1;
	
	private Class entityClass; 
	
	public EntityHelper(Context context, Class entity) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		entityClass = entity;
	}
	
	private String createStatement() {
		
		String statement = new String("");
		String tableName = new String("");
		
		for (Field field  : entityClass.getDeclaredFields())	{
			
			if (field.isAnnotationPresent(EntityField.class)) {
				
				if (! statement.equals("")) {
					statement += ", ";
				}
					
				statement += " " + field.getName();
				
				EntityField entityField = field.getAnnotation(EntityField.class);
				
				statement += (" " + entityField.type());
				
				if (entityField.primaryKey()) {
					statement += (" " + "primary key");
				}
				
				if (entityField.autoIncrement()) {
					statement += (" " + "autoincrement");
				}
			} 
			
		}
		
		statement = "create table " + entityClass.getSimpleName() + " (" + statement + ");";
		
		
		return statement;
	}
	
	public List<IEntity> getAllRows(Integer startingRow, Integer count, String orderBy) throws IllegalArgumentException, IllegalAccessException, InstantiationException{
		
		List<String> fields = new ArrayList<String>();
		String[] fielsStrings;
		Integer counter = 0; 
		
		// getting fields list
		for (Field field  : entityClass.getDeclaredFields())	{
			
			if (field.isAnnotationPresent(EntityField.class)) {
				
				EntityField entityField = field.getAnnotation(EntityField.class);
				
				fields.add(field.getName());
				
			} 
			
		}

		fielsStrings = new String[fields.size()];
		
		// filling fields list
		for (String fieldName : fields) {
			fielsStrings[counter] = fieldName;
			counter++;
		}
		
		SQLiteDatabase database;
		database = this.getWritableDatabase();
		Cursor cursor = database.query(entityClass.getSimpleName(),
		        fielsStrings, null, null, null, null, orderBy);
		
		List<IEntity> listToReturn = new ArrayList<IEntity>();
		
		Boolean toExit = false;
		cursor.moveToPosition(startingRow);
		
		while(!toExit) {
			IEntity row;
			row = (IEntity)entityClass.newInstance();
			
			
			if (cursor.isAfterLast() || cursor.getPosition() > (startingRow + count)) {
				toExit = true;
			} else {
			
				Integer fieldCounter = 0;
				for (Field field  : entityClass.getDeclaredFields())	{
					
					if (field.isAnnotationPresent(EntityField.class)) {
						if (field.getAnnotation(EntityField.class).type()==EntityField.TYPE_TEXT) {
							field.set(row, cursor.getString(fieldCounter));
						} else if (field.getAnnotation(EntityField.class).type()==EntityField.TYPE_INTEGER) {
							if (field.getType() == Long.class){
								field.set(row, cursor.getLong(fieldCounter));
							} else {
								field.set(row, cursor.getInt(fieldCounter));
							}
						} else if (field.getAnnotation(EntityField.class).type()==EntityField.TYPE_REAL) {
							field.set(row, cursor.getDouble(fieldCounter));
						}
						fieldCounter++;
					} 
					
				}
				
				listToReturn.add(row);
				cursor.moveToNext();
			}
		}
		
		return listToReturn;
	}
	
	public void save(IEntity entity) throws IllegalArgumentException, IllegalAccessException, InstantiationException {

		String statement = new String("");
		String subStatement = new String("");
		String firstField = new String("");
		String primaryKeyFieldName = new String("");
		String primaryKeyValue = new String("");
		String primaryKeyType = new String("");
		
		Boolean isUpdate = false;
		
		
		for (Field field  : entityClass.getDeclaredFields())	{
			
			if (field.isAnnotationPresent(EntityField.class)) {
				
				EntityField entityField = field.getAnnotation(EntityField.class);
				
				if (entityField.primaryKey()) {
					
					primaryKeyValue = (String)field.get(entityClass.newInstance());
					isUpdate = primaryKeyValue != null;
					primaryKeyType = entityField.type();
					
				}
				
			} 
			
		}
		
		if (isUpdate) {
			statement = "update " + entityClass.getSimpleName() + " set ";
		} else {
			statement = "insert into " + entityClass.getSimpleName() + " (";
		}
		
		for (Field field  : entityClass.getDeclaredFields())	{
			
			Object fieldValue = new String();
			fieldValue = field.get(entity);
			
			if (field.isAnnotationPresent(EntityField.class)) {
				
				if (isUpdate) {
					
					statement += (firstField + field.getName() + " = " +
							(field.getAnnotation(EntityField.class).type()==EntityField.TYPE_TEXT?"\"":"") +
							fieldValue +
							(field.getAnnotation(EntityField.class).type()==EntityField.TYPE_TEXT?"\" ":" ")
						); 
					
					firstField = ",";
					
				} else {
					if (fieldValue != null) {
						
						statement += (firstField + " " + field.getName());
						subStatement += (firstField + " " + 
								(field.getAnnotation(EntityField.class).type()==EntityField.TYPE_TEXT?"\"":"") +
								fieldValue +
								(field.getAnnotation(EntityField.class).type()==EntityField.TYPE_TEXT?"\" ":" ")
							); 
						
						firstField = ",";
						
					}
				}
			}
			
		}
		
		String primaryKeyValueBounder = new String();
		if (primaryKeyType.equals(EntityField.TYPE_TEXT)) {
			primaryKeyValueBounder = "\"";
		} else {
			primaryKeyValueBounder = "";
		}
				
				
		if (isUpdate) {
			
			statement += " where " + 
							primaryKeyFieldName + " = " + primaryKeyValueBounder + primaryKeyValue + primaryKeyValueBounder + ";";

		} else {
			
			statement += ") values (" + subStatement + ");";
		}
		
		SQLiteDatabase database;
		database = this.getWritableDatabase();
		database.execSQL(statement);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(createStatement());
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(entityClass.getName(),
	            "Upgrading database from version " + oldVersion + " to "
	                + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + entityClass.getSimpleName());
	    onCreate(db);	
	}
}
