package com.trackmars.and.tracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trackmars.and.tracker.dataUtils.EntityHelper;
import com.trackmars.and.tracker.dataUtils.IEntity;
import com.trackmars.and.tracker.model.Track;
import com.trackmars.and.tracker.model.TrackPoint;
import com.trackmars.and.tracker.model.TrackPointData;
import com.trackmars.and.tracker.utils.ILocationReceiver;
import com.trackmars.and.tracker.utils.LocationUtils;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class TrackRecorderService extends Service implements ILocationReceiver{

	private final long MAX_SERIA_TIME = 1000 * 60 * 1;
	private final Integer MAX_SERIA_QUANTITY = 50;
	
	private LocationUtils locationUtils;
	private ManagerBinder binder = new ManagerBinder();
	private Location location;
	private Boolean isRecording = false;
	private Boolean isPaused = false;
	private int currentRecordingTrackId;
	private Integer id = null;

	
	private TrackPoint trackPoint = new TrackPoint();
    private List<TrackPointData> trackPointsToSave = new ArrayList<TrackPointData>();
	
	
	public void setInterval(Integer interval) {
		
	}
	
	public boolean isRecording() {
		return this.isRecording;
	}
	
	public boolean isPaused() {
		return this.isPaused;
	}
	
	public void trackPause() {
		this.isPaused = true;
	}
	
	public void trackStop() {
		this.isPaused = false;
		this.isRecording = false;
	}
	
	public void resume() {
		//locationUtils.onResume();
	}
	
	public void pause() {
		locationUtils.onPause();
	}
	
	public Location getLocation () {
		return location;
	}
	
	public List<LatLng> getAllTrackPoint () throws IllegalAccessException, InstantiationException {
		EntityHelper entityHelper = new EntityHelper(getApplicationContext(), TrackPoint.class);
		
		List<LatLng> locations = new ArrayList<LatLng>();
		
		for (IEntity trackPoint : entityHelper.getAllRowsWhere("id_track", id.toString(), 0, null, "created")) {
			String pointData = ((TrackPoint)trackPoint).POINTS_DATA;
			
			List<TrackPointData> datas = new ArrayList<TrackPointData>();
			
			Gson gson = new Gson();
			
			datas = gson.fromJson(pointData, new TypeToken<ArrayList<TrackPointData>>(){}.getType());
			
			for (TrackPointData data : datas) {
				
				LatLng location = new LatLng(data.LAT, data.LNG);
				locations.add(location);
			}
			
		}

		for (TrackPointData data : trackPointsToSave) {
			
			LatLng location = new LatLng(data.LAT, data.LNG);
			locations.add(location);
		}
		
		
		return locations;
	}
	
	public Integer startRecord(Boolean resumeLastTrack) throws IllegalAccessException, InstantiationException{
		
		this.isRecording = true;
		this.isPaused = false;
		
		EntityHelper entityHelper = new EntityHelper(getApplicationContext(), Track.class);
		
		if (resumeLastTrack) {
			
			id = ((Track)entityHelper.getRow(null)).ID;
			this.currentRecordingTrackId = id;
			
		} else {
		
			Track track = new Track();
			track.CREATED = new Date().getTime();
			
			
			try {
				entityHelper.save(track);
	
				try {
					id = ((Track)entityHelper.getRow(null)).ID;
					
					this.currentRecordingTrackId = id;
					
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return id;
		
	}
	
	private void saveTrackPoint(Location location) throws IllegalAccessException, InstantiationException {
		EntityHelper entityHelper = new EntityHelper(getApplicationContext(), TrackPoint.class);
		
		Long curDate = new Date().getTime();
		
		if (trackPoint.ID_TRACK == null) {
			trackPoint.ID_TRACK = this.currentRecordingTrackId;
			trackPoint.CREATED = curDate;
			trackPoint.LAT = location.getLatitude();
			trackPoint.LNG = location.getLongitude();
		}
		
		TrackPointData trackPointData = new TrackPointData();
		trackPointData.CREATED = curDate;
		trackPointData.LAT = location.getLatitude();
		trackPointData.LNG = location.getLongitude();
		
	    if (this.MAX_SERIA_QUANTITY <= trackPointsToSave.size() || (trackPointsToSave.size() > 0 && (curDate >= this.MAX_SERIA_TIME + trackPointsToSave.get(0).CREATED))) {
	    	
	    	Gson gson = new Gson();
	    	trackPoint.POINTS_DATA = gson.toJson(trackPointsToSave);
	    	
	    	Log.d(TrackRecorderService.class.getName(), "saving trackPoint");
			Log.d(TrackRecorderService.class.getName(), "trackPoint.ID_TRACK " + trackPoint.ID_TRACK.toString());
			Log.d(TrackRecorderService.class.getName(), "trackPoint.CREATED " + trackPoint.CREATED.toString());
			Log.d(TrackRecorderService.class.getName(), "trackPoint.LAT " + trackPoint.LAT.toString());
			Log.d(TrackRecorderService.class.getName(), "trackPoint.LNG " + trackPoint.LNG.toString());
			Log.d(TrackRecorderService.class.getName(), "trackPoint.POINTS_DATA " + trackPoint.POINTS_DATA);

			try {
				entityHelper.save(trackPoint);
			    Log.d(TrackRecorderService.class.getName(), "trackPoint saved");
			    
			    trackPoint.ID_TRACK = null;
			    trackPointsToSave.clear();
			    
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    }
		
	    trackPointsToSave.add(trackPointData);
	    
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	public void onCreate() {
	    super.onCreate();

	    Log.d(TrackRecorderService.class.getName(), "service onCreate");
	    
	    locationUtils = new LocationUtils(this, this);
	    locationUtils.onResume();
	}

	
	class ManagerBinder extends Binder {
		public TrackRecorderService getMe() {
			return TrackRecorderService.this;
		}
	}

	@Override
	public void newLocation(Location location) {
		// TODO Add Broadcast
	    Log.d(TrackRecorderService.class.getName(), "newLocation");
	    
		this.location = location;
		Intent intent = new Intent(LocationUtils.LOCATION_RECEIVER_ACTION);
		sendBroadcast(intent);
		
		if (isRecording) {
			// TODO call record the point of the recording track
			try {
				saveTrackPoint(location);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void askLocation() {
		// TODO Auto-generated method stub
		
	}
	
}
