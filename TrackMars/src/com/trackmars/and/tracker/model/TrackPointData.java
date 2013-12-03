package com.trackmars.and.tracker.model;

import com.trackmars.and.tracker.dataUtils.EntityField;
import com.trackmars.and.tracker.dataUtils.IEntity;

public class TrackPointData{
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Long CREATED;
	
	@EntityField(type=EntityField.TYPE_REAL)
	public Double LNG;
	
	@EntityField(type=EntityField.TYPE_REAL)
	public Double LAT;
	
}
