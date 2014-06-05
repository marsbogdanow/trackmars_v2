package com.trackmars.and.tracker;

import java.util.List;

import com.trackmars.and.tracker.dataUtils.IEntity;
import com.trackmars.and.tracker.model.TrackPointData;

public class TrackWithPoinsToShow {
	private List<TrackPointData> trackPoints;
	private List<IEntity> points;
	
	public List<TrackPointData> getTrackPoints() {
		return trackPoints;
	}
	public void setTrackPoints(List<TrackPointData> trackPoints) {
		this.trackPoints = trackPoints;
	}
	public List<IEntity> getPoints() {
		return points;
	}
	public void setPoints(List<IEntity> points) {
		this.points = points;
	}
	
	
	
}
