package com.trackmars.and.tracker.model;

import com.trackmars.and.tracker.dataUtils.EntityField;
import com.trackmars.and.tracker.dataUtils.IEntity;

public class Track implements IEntity {

	@EntityField(type=EntityField.TYPE_INTEGER, primaryKey=true, autoIncrement=true)
	public Integer ID;
	
	@EntityField
	public String TITLE;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Long CREATED;

	@EntityField(type=EntityField.TYPE_REAL)
	public Double LEFT;
	
	@EntityField(type=EntityField.TYPE_REAL)
	public Double RIGHT;

	@EntityField(type=EntityField.TYPE_REAL)
	public Double TOP;

	@EntityField(type=EntityField.TYPE_REAL)
	public Double BOTTOM;

	@EntityField(type=EntityField.TYPE_REAL)
	public Double DISTANCE;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Long TRAVEL_TIME;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Long FINISHED;
}
