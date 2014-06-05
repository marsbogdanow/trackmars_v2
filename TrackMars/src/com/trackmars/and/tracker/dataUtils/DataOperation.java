package com.trackmars.and.tracker.dataUtils;

import java.util.List;

import android.content.Context;

import com.trackmars.and.tracker.model.History;
import com.trackmars.and.tracker.model.Point;
import com.trackmars.and.tracker.model.Track;

public class DataOperation {
	static public Point savePoint(Context context, Point point) throws IllegalAccessException, InstantiationException {
		
		EntityHelper entityHelper = new EntityHelper(context, Point.class);
		entityHelper.save(point);
		
		point = (Point) entityHelper.getRow(point.COLUMN_ID);
		
		History history = new  History();
		history.CREATED = point.COLUMN_CREATED;
		history.GEOCODE = point.COLUMN_GEOCODE;
		history.ID_POINT = point.COLUMN_ID;
		history.KIND = point.COLUMN_KIND;
		history.TITLE = point.COLUMN_TITLE;
		
		entityHelper = new EntityHelper(context, History.class);
		
		List<IEntity> histories = entityHelper.getAllRowsWhere("ID_POINT", point.COLUMN_ID.toString(), 0, null, null);
		int counter = 0;
		for (IEntity hist : histories) {
			if (counter > 0) {
				entityHelper.deleteRow(((History)hist).ID);
			} else {
				history.ID = ((History)hist).ID;
			}
			counter++;
		}
		
		entityHelper.save(history);
		
		return point;
	}
	
	static public Track saveTrack(Context context, Track track) throws IllegalAccessException, InstantiationException {
		
		EntityHelper entityHelper = new EntityHelper(context, Track.class);
		entityHelper.save(track);
		track = (Track) entityHelper.getRow(track.ID);
		
		History history = new History();
		history.CREATED = track.CREATED;
		history.GEOCODE = "";
		history.ID_TRACK = track.ID;
		history.TITLE = track.TITLE;
		
		entityHelper = new EntityHelper(context, History.class);
		
		if (track.ID != null) {
			List<IEntity> histories = entityHelper.getAllRowsWhere("ID_TRACK", track.ID.toString(), 0, null, null);
			int counter = 0;
			for (IEntity hist : histories) {
				if (counter > 0) {
					entityHelper.deleteRow(((History)hist).ID);
				} else {
					history.ID = ((History)hist).ID;
				}
				counter++;
			}
		};
		
		entityHelper.save(history);
		
		return track;
	}
}
