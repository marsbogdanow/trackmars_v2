package com.trackmars.and.tracker.model;

import com.trackmars.and.tracker.dataUtils.EntityField;
import com.trackmars.and.tracker.dataUtils.IEntity;

public class History implements IEntity{

	@EntityField(type=EntityField.TYPE_INTEGER, primaryKey=true, autoIncrement=true)
	public Integer ID;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Integer ID_TRACK;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Integer ID_POINT;

	@EntityField(type=EntityField.TYPE_INTEGER)
	public Integer ID_TRACK_FOR_POINT;

	@EntityField
	public String TITLE;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Long CREATED;
	
	@EntityField
	public String GEOCODE;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Integer KIND;
	
}
