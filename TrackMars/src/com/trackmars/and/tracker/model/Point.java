package com.trackmars.and.tracker.model;

import com.trackmars.and.tracker.dataUtils.EntityField;
import com.trackmars.and.tracker.dataUtils.IEntity;

public class Point implements IEntity{

	@EntityField(type=EntityField.TYPE_INTEGER, primaryKey=true, autoIncrement=true)
	public Integer COLUMN_ID;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Integer COLUMN_ID_TRACK;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Integer COLUMN_ID_TYPE;
	
	@EntityField
	public String COLUMN_TITLE;
	
	@EntityField
	public String COLUMN_DESC;
	
	@EntityField(type=EntityField.TYPE_REAL)
	public Double COLUMN_LNG;
	
	@EntityField(type=EntityField.TYPE_REAL)
	public Double COLUMN_LAT;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Long COLUMN_CREATED;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Integer COLUMN_LIKE;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Integer COLUMN_KIND;
	
}
