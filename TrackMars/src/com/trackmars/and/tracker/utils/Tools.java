package com.trackmars.and.tracker.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.trackmars.and.tracker.model.TrackPointData;

public class Tools {

	static public List<LatLng> toLatLng(List<TrackPointData> datas) {
		List<LatLng> locations = new ArrayList<LatLng>();
		for (TrackPointData data : datas) {
			
			LatLng location = new LatLng(data.LAT, data.LNG);
			locations.add(location);
		}
		return locations;
	}


}
