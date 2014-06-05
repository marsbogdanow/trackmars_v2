package com.trackmars.and.tracker.model;

import com.trackmars.and.tracker.dataUtils.EntityField;

public class TrackPointData{
	
	@EntityField(type=EntityField.TYPE_INTEGER)
	public Long CREATED;
	
	@EntityField(type=EntityField.TYPE_REAL)
	public Double LNG;
	
	@EntityField(type=EntityField.TYPE_REAL)
	public Double LAT;
	
	public Boolean paused;
	
	public Float accuracy;
	
}
