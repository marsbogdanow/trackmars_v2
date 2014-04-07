package com.trackmars.and.tracker.dataUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.Package;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.trackmars.and.tracker.MainActivity;
import com.trackmars.and.tracker.TrackRecorderService;
import com.trackmars.and.tracker.model.Point;
import com.trackmars.and.tracker.model.Track;
import com.trackmars.and.tracker.model.TrackPoint;

public class EntityHelper extends SQLiteOpenHelper {
	
	static final private String DATABASE_NAME = "trackmars.db";
	static final private Integer DATABASE_VERSION = 14;
	
	private Class entityClass; 
	private Context context;
	
	public EntityHelper(Context context, Class entity) throws IllegalAccessException, InstantiationException {
		
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		entityClass = entity;
	}
	
	private String createStatement(Class entityToCreate) {
		
		String statement = new String("");
		String tableName = new String("");
		
		for (Field field  : entityToCreate.getDeclaredFields())	{
			
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
		
		statement = "create table " + entityToCreate.getSimpleName() + " (" + statement + ");";
		
		Log.d(EntityHelper.class.getName(), "db update statement " + statement);
		
		return statement;
	}
	

	public List<IEntity> getAllRowsWhere(String field, String equals, Integer startingRow, Integer count, String orderBy){
		
		
		Log.d(EntityHelper.class.getName(), "getAllRowsWhere start");
		
		List<String> fields = new ArrayList<String>();
		String fielsStrings = new String();
		Integer counter = 0; 
		
		// getting fields list
		for (Field tabfield  : entityClass.getDeclaredFields())	{
			
			if (tabfield.isAnnotationPresent(EntityField.class)) {
				
				EntityField entityField = tabfield.getAnnotation(EntityField.class);
				
				fields.add(tabfield.getName());
				
			} 
			
		}
		
		// filling fields list
		for (String fieldName : fields) {
			fielsStrings += (((fielsStrings.length() > 0)?", ":"") + fieldName);
			counter++;
		}
		
		SQLiteDatabase database;
		database = this.getWritableDatabase();
		
		Cursor cursor;
		cursor = database.rawQuery("SELECT " + fielsStrings + " FROM " + entityClass.getSimpleName() + " WHERE " + field + " = ? order by " + orderBy, new String[]{equals});

		
		List<IEntity> listToReturn = new ArrayList<IEntity>();
		
		Boolean toExit = false;
		cursor.moveToPosition(startingRow);

		if (count == null) {
			count = cursor.getCount();
		}
		
		
		try {
			while(!toExit) {
				IEntity row;
				row = (IEntity)entityClass.newInstance();
				
				
				if (cursor.isAfterLast() || cursor.getPosition() > (startingRow + count)) {
					toExit = true;
				} else {
				
					Integer fieldCounter = 0;
					for (Field tabfield  : entityClass.getDeclaredFields())	{
						
						if (tabfield.isAnnotationPresent(EntityField.class)) {
							if (tabfield.getAnnotation(EntityField.class).type()==EntityField.TYPE_TEXT) {
								tabfield.set(row, cursor.getString(fieldCounter));
							} else if (tabfield.getAnnotation(EntityField.class).type()==EntityField.TYPE_INTEGER) {
								if (tabfield.getType() == Long.class){
									tabfield.set(row, cursor.getLong(fieldCounter));
								} else {
									tabfield.set(row, cursor.getInt(fieldCounter));
								}
							} else if (tabfield.getAnnotation(EntityField.class).type()==EntityField.TYPE_REAL) {
								tabfield.set(row, cursor.getDouble(fieldCounter));
							}
							fieldCounter++;
						} 
						
					}
					
					listToReturn.add(row);
					cursor.moveToNext();
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} finally {
			cursor.close();
			database.close();
		}
		
		return listToReturn;
		
		
	}
	
	public List<IEntity> getAllRows(Integer startingRow, Integer count, String orderBy) {
		
		Log.d(EntityHelper.class.getName(), "getAllRows start");
		
		List<String> fields = new ArrayList<String>();
		String[] fielsStrings;
		Integer counter = 0; 
		
		// getting fields list
		for (Field field  : entityClass.getDeclaredFields())	{
			if (field.isAnnotationPresent(EntityField.class)) {
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
		
		if (count == null) {
			count = cursor.getCount();
		}
		
		try {
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
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} finally {
			cursor.close();
			database.close();
		}
		
		return listToReturn;
	}
	

	public IEntity getRow(Integer id) {

		Log.d(EntityHelper.class.getName(), "getRow start");
		
		List<String> fields = new ArrayList<String>();
		String fielsStrings = new String();
		Integer counter = 0; 
		
		// getting fields list
		for (Field field  : entityClass.getDeclaredFields())	{
			
			if (field.isAnnotationPresent(EntityField.class)) {
				
				EntityField entityField = field.getAnnotation(EntityField.class);
				
				fields.add(field.getName());
				
			} 
			
		}
		
		// filling fields list
		for (String fieldName : fields) {
			fielsStrings += (((fielsStrings.length() > 0)?", ":"") + fieldName);
			counter++;
		}
		
		SQLiteDatabase database;
		database = this.getWritableDatabase();
		
		Cursor cursor;
		if (id == null) {
			cursor = database.rawQuery("SELECT " + fielsStrings + " FROM " + entityClass.getSimpleName() + " WHERE ID = (SELECT MAX(ID) FROM  " + entityClass.getSimpleName() + " );", null);
		} else { 
			cursor = database.rawQuery("SELECT " + fielsStrings + " FROM " + entityClass.getSimpleName() + " WHERE ID = ?;", new String[]{id.toString()});
		}
		
		
		cursor.moveToPosition(0);
		
		IEntity row = null;

		try {
			row = (IEntity)entityClass.newInstance();
				
				
			if (cursor.isAfterLast()) {
					
			} else {
				
					Integer fieldCounter = 0;
					for (Field field  : entityClass.getDeclaredFields())	{
						
						if (field.isAnnotationPresent(EntityField.class)) {
							if (field.getAnnotation(EntityField.class).type()==EntityField.TYPE_TEXT) {
								field.set(row, cursor.getString(fieldCounter));
							} else if (field.getAnnotation(EntityField.class).type()==EntityField.TYPE_INTEGER) {
								
								if (cursor.getString(fieldCounter) != null) {
								
									if (field.getType() == Long.class){
										field.set(row, cursor.getLong(fieldCounter));
									} else {
										field.set(row, cursor.getInt(fieldCounter));
									}
								
								} else {
									field.set(row, null);
								}
								
							} else if (field.getAnnotation(EntityField.class).type()==EntityField.TYPE_REAL) {
								
								if (cursor.getString(fieldCounter) != null) {
									field.set(row, cursor.getDouble(fieldCounter));
								} else {
									field.set(row, null);
								}
								
							}
							
							fieldCounter++;
						} 
						
					}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} finally {
			cursor.close();
			database.close();
		}
		
		return row;
	}
	
	
	public void save(IEntity entity) {

		Log.d(EntityHelper.class.getName(), "save start");
		
		String statement = new String("");
		String subStatement = new String("");
		String firstField = new String("");
		String primaryKeyFieldName = new String("");
		Object primaryKeyValue = new String("");
		String primaryKeyType = new String("");
		
		Boolean isUpdate = false;
		
		
		try {
			for (Field field  : entityClass.getDeclaredFields())	{
				
				if (field.isAnnotationPresent(EntityField.class)) {
					
					EntityField entityField = field.getAnnotation(EntityField.class);
					
					if (entityField.primaryKey()) {
						
						Object fieldValue = new String();
						fieldValue = field.get(entity);
						
						primaryKeyValue = fieldValue;
						primaryKeyFieldName = field.getName();
						
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
								(field.getAnnotation(EntityField.class).type()==EntityField.TYPE_TEXT?"\'":"") +
								fieldValue +
								(field.getAnnotation(EntityField.class).type()==EntityField.TYPE_TEXT?"\' ":" ")
							); 
						
						firstField = ",";
						
					} else {
						if (fieldValue != null) {
							
							statement += (firstField + " " + field.getName());
							subStatement += (firstField + " " + 
									(field.getAnnotation(EntityField.class).type()==EntityField.TYPE_TEXT?"\'":"") +
									fieldValue +
									(field.getAnnotation(EntityField.class).type()==EntityField.TYPE_TEXT?"\' ":" ")
								); 
							
							firstField = ",";
							
						}
					}
				}
				
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		String primaryKeyValueBounder = new String();
		if (primaryKeyType.equals(EntityField.TYPE_TEXT)) {
			primaryKeyValueBounder = "\'";
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
		database.close();
		
	}

	
	private Class tableToUpdate;
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
				this.modifyData(db, 0, EntityHelper.DATABASE_VERSION);
		} catch (SQLiteException e) {
			// do nothing
		}
		
	}

	private void modifyData(SQLiteDatabase db, int oldVersion, int newVersion) {
		
    	for(int n=oldVersion+1;n<=newVersion;n++) {
        	
			if (n == 11) {
				this.tableToUpdate = Point.class;
				db.execSQL("DROP TABLE IF EXISTS " + this.tableToUpdate.getSimpleName());
				db.execSQL(createStatement(this.tableToUpdate));	
		    	
				this.tableToUpdate = Track.class;
				db.execSQL("DROP TABLE IF EXISTS " + this.tableToUpdate.getSimpleName());
				db.execSQL(createStatement(this.tableToUpdate));
				
				this.tableToUpdate = TrackPoint.class;
				db.execSQL("DROP TABLE IF EXISTS " + this.tableToUpdate.getSimpleName());
				db.execSQL(createStatement(this.tableToUpdate));
			}
			
			if (n == 12) {
				db.execSQL("ALTER TABLE " + Track.class.getSimpleName() + " ADD COLUMN LEFT REAL");
				db.execSQL("ALTER TABLE " + Track.class.getSimpleName() + " ADD COLUMN RIGHT REAL");
				db.execSQL("ALTER TABLE " + Track.class.getSimpleName() + " ADD COLUMN TOP REAL");
				db.execSQL("ALTER TABLE " + Track.class.getSimpleName() + " ADD COLUMN BOTTOM REAL");
			}
			
			if (n == 13) {
				String sqlS = "CREATE INDEX IF NOT EXISTS IDXTrackPoint_Created ON TrackPoint (created DESC)";
				db.execSQL(sqlS);
				sqlS = "CREATE INDEX IF NOT EXISTS IDXTrackPoint_id_track ON TrackPoint (id_track DESC)";
				db.execSQL(sqlS);
			}
			
			if (n == 14) {
				db.execSQL("ALTER TABLE " + Track.class.getSimpleName() + " ADD COLUMN DISTANCE REAL");
				db.execSQL("ALTER TABLE " + Track.class.getSimpleName() + " ADD COLUMN TRAVEL_TIME INTEGER");
			}
			
    	}
	}
	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    	Log.d(EntityHelper.class.getName(), "Update database from " + new Integer(oldVersion) + " " + new Integer(newVersion));
		this.modifyData(db, oldVersion, newVersion);
		
	}
}
