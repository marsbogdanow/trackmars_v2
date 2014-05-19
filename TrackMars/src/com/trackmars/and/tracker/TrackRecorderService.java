package com.trackmars.and.tracker;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trackmars.and.tracker.dataUtils.DataOperation;
import com.trackmars.and.tracker.dataUtils.EntityHelper;
import com.trackmars.and.tracker.dataUtils.IEntity;
import com.trackmars.and.tracker.model.Track;
import com.trackmars.and.tracker.model.TrackPoint;
import com.trackmars.and.tracker.model.TrackPointData;
import com.trackmars.and.tracker.utils.ILocationReceiver;
import com.trackmars.and.tracker.utils.LocationUtils;
import com.trackmars.and.tracker.utils.Tools;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.EditText;

public class TrackRecorderService extends Service implements ILocationReceiver{

	private final long MAX_SERIA_TIME = 1000 * 60 * 1;
	private final Integer MAX_SERIA_QUANTITY = 50;
	
	private LocationUtils locationUtils;
	private ManagerBinder binder = new ManagerBinder();
	
	private Location location;
	private Location lastSavedLocation;
	private Location lastSavedNetworkLocation;
	
	private Float accuracy;
	private Boolean isRecording = false;
	private Boolean isPaused = false;
	private int currentRecordingTrackId;
	private Class lastPointProvider;
	
	
	private TrackPoint trackPoint = new TrackPoint();
    private List<TrackPointData> trackPointsToSave = new ArrayList<TrackPointData>();
	
    private Track track;
    private Rectangle rectangle;
    private Long lastSavePointTime;
    private LatLng lastSavePointLatLng; 
    

    private Long currentTravelTime = 0l;
    private Long trackCreatedTime = 0l;
    private Double currentDistance = 0d;
    
    private Integer interval = 1;
    
    
    
	public Float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Float accuracy) {
		this.accuracy = accuracy;
	}

	public void setLastPointProvider(Class lastPointProvider) {
		this.lastPointProvider = lastPointProvider;
	}

	public Long getTrackCreatedTime() {
		return trackCreatedTime;
	}

	public Long getCurrentTravelTime() {
		return currentTravelTime;
	}

	public Double getCurrentDistance() {
		return currentDistance;
	}

	public void setInterval(Integer intvl) {
		interval = intvl;
		
		if (intvl == null) {
			SharedPreferences sPref = getSharedPreferences(Tools.PREFERENCES_NAME, MODE_PRIVATE);
		    interval = sPref.getInt(Tools.PREF_INTERVAL, 1);
		}
		
		locationUtils.setInterval(interval);
		if (!this.isRecording || this.isPaused) {
			locationUtils.restartWithNewInterval();
		}
		
	}
	
	public boolean isRecording() {
		return this.isRecording;
	}
	
	public boolean isPaused() {
		return this.isPaused;
	}
	
	// нажата кнопка "пауза"
	public void trackPause() throws IllegalAccessException, InstantiationException {
		
		this.isPaused = true;
		
		if (isRecording) {
			saveTrackPoint(location);
		
		}
		
		this.lastSavePointTime = null;
		this.lastSavePointLatLng = null;
		
	}
	
	public void trackStop(Context winContext) throws IllegalAccessException, InstantiationException {
		

////////////////////////////////////////////////////////////
		AlertDialog.Builder alert = new AlertDialog.Builder(winContext);

		alert.setTitle(winContext.getResources().getString(R.string.end_of_trip));
		alert.setMessage(winContext.getResources().getString(R.string.name_the_trip));

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {

			TrackRecorderService.this.isPaused = false;
			TrackRecorderService.this.isRecording = false;

			saveTrackPoint(location);
			
			try {
				EntityHelper trackHelper = new EntityHelper(getApplicationContext(), Track.class);
				trackHelper.getRow(track.ID);
				track.TITLE = input.getText().toString();
				
				DataOperation.saveTrack(getApplicationContext(), track);
				
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			TrackRecorderService.this.track = null;
			TrackRecorderService.this.rectangle = null;
			
			TrackRecorderService.this.lastSavePointTime = null;
			TrackRecorderService.this.lastSavePointLatLng = null;
			
		    currentTravelTime = 0l;
		    currentDistance = 0d;
		    
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();////////////////////////////////////////////////////////////		
		
	}
	
	public void resume() {
		locationUtils.onResume();
	}
	
	// пауза при потере фокуса главного окна
	public void pause() throws IllegalAccessException, InstantiationException {
		if (!this.isRecording || this.isPaused) {
			//saveTrackPoint(location);
			locationUtils.onPause();
		}
	}
	
	public Location getLocation () {
		return location;
	}
	
	public List<TrackPointData> getAllTrackPoint () throws IllegalAccessException, InstantiationException {
		return getAllTrackPoint(null);
	}
	
	public List<TrackPointData> getAllTrackPoint (Integer trackId) throws IllegalAccessException, InstantiationException {
		EntityHelper entityHelper = new EntityHelper(getApplicationContext(), TrackPoint.class);
		
		List<TrackPointData> locations = new ArrayList<TrackPointData>();
		
		for (IEntity trackPoint : entityHelper.getAllRowsWhere("id_track", trackId!=null?trackId.toString():track.ID.toString(), 0, null, "created")) {
			String pointData = ((TrackPoint)trackPoint).POINTS_DATA;
			
			List<TrackPointData> datas = new ArrayList<TrackPointData>();
			
			Gson gson = new Gson();
			
			datas = gson.fromJson(pointData, new TypeToken<ArrayList<TrackPointData>>(){}.getType());
			
			locations.addAll(datas);
			
			//for (TrackPointData data : datas) {
			//	
			//	LatLng location = new LatLng(data.LAT, data.LNG);
			//	locations.add(location);
			//}
			
		}

		if (trackId == null) {
			
			for (TrackPointData data : trackPointsToSave) {
				
				locations.add(data);
				
			}
			
		}
		
		return locations;
	}
	
	public Integer startRecord(Boolean resumeLastTrack) throws IllegalAccessException, InstantiationException{
		
		this.isRecording = true;
		this.isPaused = false;
		
		this.rectangle = new Rectangle();
		
		
		if (resumeLastTrack) {
			
			EntityHelper entityHelper = new EntityHelper(getApplicationContext(), Track.class);
			track = (Track)entityHelper.getRow(null);
			
			entityHelper = null;
			
			this.currentRecordingTrackId = track.ID;
			
			this.rectangle.create(track.LEFT, track.RIGHT, track.TOP, track.BOTTOM); 
			
		} else {
		
			track = new Track();
			track.CREATED = new Date().getTime();
			
			
			
			try {
				
				track = DataOperation.saveTrack(getApplicationContext(), track);
					
				this.currentRecordingTrackId = track.ID;
			
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		trackCreatedTime = track.CREATED;
		
		return track.ID;
		
	}
	
	public int getCurrentRecordingTrackId() {
		return currentRecordingTrackId;
	}

	private void saveTrackPoint(Location location) {
		
		Long curDate = new Date().getTime();
		
		// время в пути
		if (this.lastSavePointTime == null) {// если например поставили паузу в время записи трека или начали писать новый трек
			 // отметим время самой первой точки, чтобы потом от него продолжить считать
			this.lastSavePointTime = curDate;
		}

		// общее расстояние
		if (this.lastSavePointLatLng == null) {// если например поставили паузу в время записи трека или начали писать новый трек
			 // отметим координаты самой первой точки, чтобы потом от него продолжить считать
			this.lastSavePointLatLng = new LatLng(location.getLatitude(), location.getLongitude());
		}
		
		if (trackPoint.ID_TRACK == null) { // Track point was just saved. Use ID_TRACK == null to determine this event
			trackPoint.ID_TRACK = this.currentRecordingTrackId;
			trackPoint.CREATED = curDate;
			trackPoint.LAT = location.getLatitude();
			trackPoint.LNG = location.getLongitude();
		}
		
		TrackPointData trackPointData = new TrackPointData();
		trackPointData.CREATED = curDate;
		trackPointData.LAT = location.getLatitude();
		trackPointData.LNG = location.getLongitude();
		trackPointData.paused = this.isPaused;
		trackPointData.accuracy = location.getAccuracy();
		
		
		this.track.TRAVEL_TIME = (this.track.TRAVEL_TIME != null ? this.track.TRAVEL_TIME
				: 0l)
				+ curDate - lastSavePointTime;

		this.currentTravelTime = this.track.TRAVEL_TIME;

		this.track.DISTANCE = (this.track.DISTANCE != null ? this.track.DISTANCE
				: 0)
				+ LocationUtils.distFrom(location.getLatitude(),
						location.getLongitude(),
						lastSavePointLatLng.latitude,
						lastSavePointLatLng.longitude);

		this.currentDistance = this.track.DISTANCE;
		
	    if (this.MAX_SERIA_QUANTITY <= trackPointsToSave.size() || (trackPointsToSave.size() > 0 && (curDate >= this.MAX_SERIA_TIME + trackPointsToSave.get(0).CREATED))
	    		|| this.isPaused || !this.isRecording) {
	    	
	    	Gson gson = new Gson();
	    	trackPoint.POINTS_DATA = gson.toJson(trackPointsToSave);
	    	
	    	Log.d(TrackRecorderService.class.getName(), "saving trackPoint");
			Log.d(TrackRecorderService.class.getName(), "trackPoint.ID_TRACK " + trackPoint.ID_TRACK.toString());
			Log.d(TrackRecorderService.class.getName(), "trackPoint.CREATED " + trackPoint.CREATED.toString());
			Log.d(TrackRecorderService.class.getName(), "trackPoint.LAT " + trackPoint.LAT.toString());
			Log.d(TrackRecorderService.class.getName(), "trackPoint.LNG " + trackPoint.LNG.toString());
			Log.d(TrackRecorderService.class.getName(), "trackPoint.POINTS_DATA " + trackPoint.POINTS_DATA);

			try {
				EntityHelper entityHelper = new EntityHelper(getApplicationContext(), TrackPoint.class);
				entityHelper.save(trackPoint);
			    Log.d(TrackRecorderService.class.getName(), "trackPoint saved");
			    
			    trackPoint.ID_TRACK = null;
			    trackPointsToSave.clear();
			    
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			try {

				if (this.rectangle.isAltered()) {
					this.track.BOTTOM = rectangle.getBottom();
					this.track.LEFT = rectangle.getLeft();
					this.track.RIGHT = rectangle.getRight();
					this.track.TOP = rectangle.getTop();
				}


				this.track = DataOperation.saveTrack(getApplicationContext(), this.track);

				// текущие заначения
				this.lastSavePointTime = curDate;
				this.lastSavePointLatLng = new LatLng(location.getLatitude(),
						location.getLongitude());

				this.rectangle.setAltered(false);

			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
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
	public void newLocation(Location location, Class listenerType, Float accuracy) {
	    
		this.location = location;
		
		Intent intent = new Intent(LocationUtils.LOCATION_RECEIVER_ACTION);
		sendBroadcast(intent);
		
		if (isRecording && !isPaused && LocationUtils.isDistantOutOfAccuracy(this.location, this.lastSavedLocation)) {
			rectangle.shape(location);
			saveTrackPoint(location);
			this.lastSavedLocation = location;
		}
		
		this.lastPointProvider = listenerType;
		this.accuracy = accuracy;
	}

	@Override
	public void askLocation() {
		
	}

	public Class getLastPointProvider() {
		return lastPointProvider;
	}
	
}


