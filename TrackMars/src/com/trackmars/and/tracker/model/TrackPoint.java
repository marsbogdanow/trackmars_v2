package com.trackmars.and.tracker.model;

import com.trackmars.and.tracker.dataUtils.EntityField;
import com.trackmars.and.tracker.dataUtils.IEntity;

public class TrackPoint implements IEntity {

	@EntityField(type=EntityField.TYPE_INTEGER, primaryKey=true, autoIncrement=true)
	public Integer ID;

	@EntityField(type=EntityField.TYPE_INTEGER)
	public Integer ID_TRACK;
	
	//@EntityField
	//public String TITLE;
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Long CREATED;
	
	@EntityField(type=EntityField.TYPE_REAL)
	public Double LNG;
	
	@EntityField(type=EntityField.TYPE_REAL)
	public Double LAT;
	
	@EntityField(type=EntityField.TYPE_TEXT)
	public String POINTS_DATA;
	
}
