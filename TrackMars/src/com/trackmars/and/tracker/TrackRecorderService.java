package com.trackmars.and.tracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.elifantiev.android.roboerrorreporter.Logger;

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
	private final long MIN_INTERNAL_BETWEEN_POINTS = 10;
	private final int SPEED_OF_SOUND = 330;
	
	private LocationUtils locationUtils;
	private ManagerBinder binder = new ManagerBinder();
	
	private Location location;
	
	
	private class LocationWithTime {
		private Location location;
		private Long time;
		
		public LocationWithTime(Location location, Long time) {
			this.location = location;
			this.time = time;
		}

		public Location getLocation() {
			return location;
		}

		public Long getTime() {
			return time;
		}
		
		
	}
	
	
	private Float accuracy;
	private Boolean isRecording = false;
	private Boolean isPaused = false;
	private Integer currentRecordingTrackId;
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
		
		this.lastMatterLocationWithTime = null;
		
	}
	
	private List<TrackPointData> collectedLocations = new ArrayList<TrackPointData>();
	private boolean collect = false;
	
	public void collectLocations(boolean on) {
		collectedLocations.clear();
		collect = on;
	}
	
	public boolean collected () {
		return this.collect && this.collectedLocations.size() > 0;
	}
	public List<TrackPointData> getCollectedLocations() {
		
		return collectedLocations;
		
	}
	
	public void trackStop(Context winContext) throws IllegalAccessException, InstantiationException {
		

////////////////////////////////////////////////////////////
		AlertDialog.Builder alert = new AlertDialog.Builder(winContext);

		alert.setTitle(winContext.getResources().getString(R.string.end_of_trip));
		alert.setMessage(winContext.getResources().getString(R.string.name_the_trip));

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		if (this.track.TITLE != null && !this.track.TITLE.equals("")) {
			input.setText(this.track.TITLE);
		}
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
				track.FINISHED = new Date().getTime();
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
	
	public static List<TrackPointData> getAllTrackPointByTrack (Integer trackId, Context context) throws IllegalAccessException, InstantiationException {
		EntityHelper entityHelper = new EntityHelper(context, TrackPoint.class);
		
		List<TrackPointData> locations = new ArrayList<TrackPointData>();
		
			for (IEntity trackPoint : entityHelper.getAllRowsWhere("id_track", trackId.toString(), 0, null, "created")) {
				String pointData = ((TrackPoint)trackPoint).POINTS_DATA;
				
				List<TrackPointData> datas = new ArrayList<TrackPointData>();
				
				Gson gson = new Gson();
				
				datas = gson.fromJson(pointData, new TypeToken<ArrayList<TrackPointData>>(){}.getType());
				
				locations.addAll(datas);
				
			}

		return locations;
	}
	
	public List<TrackPointData> getAllTrackPoint (Integer trackId) throws IllegalAccessException, InstantiationException {
		EntityHelper entityHelper = new EntityHelper(getApplicationContext(), TrackPoint.class);
		
		List<TrackPointData> locations;
		
		if (trackId != null || track != null) {
			
			locations = TrackRecorderService.getAllTrackPointByTrack(trackId!=null?trackId:track.ID, getApplicationContext());
			
			if (trackId == null) {
				
				for (TrackPointData data : trackPointsToSave) {
					
					locations.add(data);
					
				}
				
			}
			
			return locations;
		}
		
		return new ArrayList<TrackPointData>(); // ничего не нашли

	}
	
	public Integer startRecord(Boolean resumeLastTrack) throws IllegalAccessException, InstantiationException{
		
		this.isRecording = true;
		this.isPaused = false;
		
		this.rectangle = new Rectangle();
		
		pauseMonitor.start();
		
		if (resumeLastTrack) {
			
			EntityHelper entityHelper = new EntityHelper(getApplicationContext(), Track.class);
			track = (Track)entityHelper.getRow(null);
			
			entityHelper = null;
			
			if (track.ID == null) {
				resumeLastTrack = false;
			} else {
				this.currentRecordingTrackId = track.ID;
				this.rectangle.create(track.LEFT, track.RIGHT, track.TOP, track.BOTTOM);
				
				this.currentDistance = track.DISTANCE;
				this.currentTravelTime = track.TRAVEL_TIME;
				
			}
			
		} 
		if (!resumeLastTrack) {
		
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

	
	private LocationWithTime lastMatterLocationWithTime;
	
	
	private void saveTrackPoint(Location location) {
		
		if (location == null) {
			return;
		}
		
		Long curDate = new Date().getTime();
		
		// время в пути
		if (this.lastSavePointTime == null) {// если например поставили паузу в время записи трека или начали писать новый трек
			 // отметим время самой первой точки, чтобы потом от него продолжить считать
			this.lastSavePointTime = curDate;
		}

		// общее расстояние
		if (this.lastSavePointLatLng == null && location != null) {// если например поставили паузу в время записи трека или начали писать новый трек
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
		
		
		if (this.collect) {
			this.collectedLocations.add(trackPointData);
		}
	
		
		//////
		if (lastMatterLocationWithTime != null && this.currentTravelTime != null && this.currentDistance !=null && this.track != null) {
			
			this.track.TRAVEL_TIME = this.currentTravelTime;
			this.track.DISTANCE = this.currentDistance;
			
		}
		///////
		
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

	private LocationWithTime lastSavedLocation;
	
	private boolean dontCheckAutoPause = false;
	
	@Override
	public void newLocation(Location location, Class listenerType, Float accuracy) {
	    
		this.location = location;
		
		if (isRecording && 
				!isPaused &&
				!dontCheckAutoPause &&
				this.lastSavedLocation != null && 
				(new Date().getTime() - this.lastSavedLocation.getTime()) > (MIN_INTERNAL_BETWEEN_POINTS * 1000)
			) {
			pauseMonitor.add(new LocationWithTime(location, new Date().getTime()));
			
			if (pauseMonitor.getIsStaying()) {
				this.lastSavedLocation = new LocationWithTime(location, new Date().getTime());
				return ;
			}
		}
		
		Intent intent = new Intent(LocationUtils.LOCATION_RECEIVER_ACTION);
		sendBroadcast(intent);
		
		// проверка на резкий возврат в точку, где последний раз видел сеть
		// попробую делать так: если скорось превышает скорость звука, то не буду ставить точку
		int instanteSpeed = 0;
		
		if (lastSavedLocation != null) {
			instanteSpeed = (int) (LocationUtils.distFrom(this.location, lastSavedLocation.getLocation())  /
			(new Date().getTime() - this.lastSavedLocation.getTime()) * 1000);
		}
		
		if (instanteSpeed > 330) {
			Logger.log("GAP!");
		}
		
		if (	instanteSpeed < SPEED_OF_SOUND &&
				isRecording && 
				!isPaused && 
				(this.lastSavedLocation == null || LocationUtils.isDistantOutOfAccuracy(this.location, this.lastSavedLocation.getLocation()) &&
				(new Date().getTime() - this.lastSavedLocation.getTime()) > (MIN_INTERNAL_BETWEEN_POINTS * 1000))
			) {

			Long curDate = new Date().getTime();
			
			//////
			if (lastMatterLocationWithTime != null) {
				
				this.currentTravelTime = (this.track.TRAVEL_TIME != null ? this.track.TRAVEL_TIME
						: 0l)
						+ curDate - lastMatterLocationWithTime.getTime();
		
		
				this.currentDistance = (this.track.DISTANCE != null ? this.track.DISTANCE
						: 0)
						+ LocationUtils.distFrom(location.getLatitude(),
								location.getLongitude(),
								lastMatterLocationWithTime.location.getLatitude(),
								lastMatterLocationWithTime.location.getLongitude());
		
				
			}
			///////
			
			lastMatterLocationWithTime = new LocationWithTime(location, curDate);
			
			rectangle.shape(location);
			saveTrackPoint(location);
			
			this.lastSavedLocation = new LocationWithTime(location, new Date().getTime());
			
			
		}
		
		this.lastPointProvider = listenerType;
		this.accuracy = accuracy;
	}

	
	class PauseMonitor{
		
		List<LocationWithTime> locationsWithTime = new ArrayList<TrackRecorderService.LocationWithTime>();
		boolean isStaying = false;
		
		final private double SPEED_THRESHOLD = 2d;
		final private double DISTANCE_THRESOLD = 100d;

		int follow = 0;
		LocationWithTime firsFollowedLocation;
		LocationWithTime lastFollowedLocation;
		
		public void start() {
			follow = 0;
			firsFollowedLocation = null;
			lastFollowedLocation = null;
			isStaying = false;
			locationsWithTime.clear();
		}
		
		public void add(LocationWithTime location) {
			if (locationsWithTime.size() > 2) {
				List<LocationWithTime> withTimes = new ArrayList<LocationWithTime>();
				withTimes.add(locationsWithTime.get(1));
				withTimes.add(locationsWithTime.get(2));
				withTimes.add(location);
				
				locationsWithTime.clear();
				locationsWithTime.addAll(withTimes);
			} else {
				locationsWithTime.add(location);
			}
			determinate(location);
		}
		
		public boolean getIsStaying() {
			return isStaying;
		}
		
		private boolean toFollow(LocationWithTime locationWithTime) {

			
			if (locationsWithTime.size() < 3) {
				return false;
			}
			
			LocationWithTime prevLocationWithTime;
			
			prevLocationWithTime = locationsWithTime
					.get(locationsWithTime.size() - 2);
			
			if (LocationUtils.distFrom(
					prevLocationWithTime.location,
					locationWithTime.location) < locationWithTime.location
					.getAccuracy() * 1.5) {

				if (firsFollowedLocation == null) {
					firsFollowedLocation = locationWithTime;
				}
				
				lastFollowedLocation = locationWithTime;
				
				return true;

			} else if (locationsWithTime.size() > 2) {
				
				double dist = LocationUtils.distFrom(locationWithTime.location, 
						locationsWithTime.get(0).location);

				long time = locationWithTime.time - locationsWithTime.get(0).getTime();
				
				double speed = (dist / time) / 1000 * 3600 * 1000;
				
				if (speed <= SPEED_THRESHOLD) {
					if (firsFollowedLocation == null) {
						firsFollowedLocation = locationWithTime;
					}
					
					lastFollowedLocation = locationWithTime;
					
					return true;
				}
					
			}
			
			return false;
			
		}
		
		private void determinate(LocationWithTime locationWithTime) {
			
			
			if (locationsWithTime.size() > 2) {
					
						if (this.isStaying) {
							if (LocationUtils.distFrom(
									locationWithTime.getLocation(),
									firsFollowedLocation.getLocation()) > DISTANCE_THRESOLD) {
								
								
								for (int n = 1; n < locationsWithTime.size(); n++) {
									dontCheckAutoPause = true;
									newLocation(locationsWithTime.get(n).location,
											lastPointProvider,
											locationsWithTime.get(n).location.getAccuracy());
									dontCheckAutoPause = false;
								}
								
								follow = 0;
								isStaying = false;
								firsFollowedLocation = null;
								lastFollowedLocation = null;
							}
						}
					//}


				if (follow > 1) {
					
					if (LocationUtils.distFrom(locationWithTime.getLocation(), firsFollowedLocation.getLocation()) < 
							LocationUtils.distFrom(lastFollowedLocation.getLocation(), firsFollowedLocation.getLocation())) {
						
						this.isStaying = true;
						
					}					
				}
				
				if (toFollow(locationWithTime)) {
					if (follow < 3) {
						follow++;
					}
				}			
				}
		}
	}
	
	PauseMonitor pauseMonitor = new PauseMonitor();
	
	@Override
	public void askLocation() {
		
	}

	public Class getLastPointProvider() {
		return lastPointProvider;
	}
	
}


