package com.trackmars.and.tracker;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.trackmars.and.tracker.dataUtils.IEntity;

public class TrackWithPoinsToShow {
	private List<LatLng> trackPoints;
	private List<IEntity> points;
	
	public List<LatLng> getTrackPoints() {
		return trackPoints;
	}
	public void setTrackPoints(List<LatLng> trackPoints) {
		this.trackPoints = trackPoints;
	}
	public List<IEntity> getPoints() {
		return points;
	}
	public void setPoints(List<IEntity> points) {
		this.points = points;
	}
	
	
	
}
