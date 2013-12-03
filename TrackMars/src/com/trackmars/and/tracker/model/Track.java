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
	
}
