package com.trackmars.and.tracker.utils;

import ru.elifantiev.android.roboerrorreporter.Logger;

import com.trackmars.and.tracker.dataUtils.DateUtils;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class LocationUtils{
	
	private ILocationReceiver locationReceiver;
    private LocationManager locationManager;
	private LocationProvider provider;
	private LocationProvider provider1;
	
	private Location lastNetworkProviderLocation;
	private Class lastLocationProviderClass;
	
	
	private NetworkListener networkListener = new NetworkListener();
	private GpsListener gpsListener = new GpsListener();
	
	private Integer interval = -1;
	
	private Service trackRecorderService;
	
	final public static String LOCATION_RECEIVER_ACTION = "com.trackmars.and.tracker.locationMessage";
	final public static float DEFAULT_ACCURACY = 15;
	
	public class NetworkListener implements LocationListener {

		@Override
		public void onLocationChanged(Location arg0) {
			//networkLocation = arg0;
			Logger.log(this.toString() + " " + "onLocationChanged ");
			// ������� ��������� ������� ������ ���� ������� ��������� ��� ������ ������ ��� ������
			// ��� ������� ����� ������ � ��������� �������� ����� �� ���� ����� ������� �� gps
			// (��� ������� ���� ��������)
			 if (LocationUtils.this.lastLocationProviderClass == NetworkListener.class
					 || LocationUtils.this.lastNetworkProviderLocation == null // ��� �� ���� �������� �� ��������
				) {
				//���� ������ �� ���������, �� �� ����� ��������� �������
				if (LocationUtils.this.lastNetworkProviderLocation != null)  {
					Logger.log("----ANTENNA");
					Logger.log("arg0.getLatitude() " + arg0.getLatitude());
					Logger.log("arg0.getLongitude() " + arg0.getLongitude());
					Logger.log("LocationUtils.this.lastNetworkProviderLocation.getLatitude() " +
							LocationUtils.this.lastNetworkProviderLocation.getLatitude());
					Logger.log("LocationUtils.this.lastNetworkProviderLocation.getLongitude() " +
							LocationUtils.this.lastNetworkProviderLocation.getLongitude());
					
					
					double distBetweenNetworkPoint = LocationUtils.distFrom(arg0.getLatitude(), 
								arg0.getLongitude(), 
								LocationUtils.this.lastNetworkProviderLocation.getLatitude(), 
								LocationUtils.this.lastNetworkProviderLocation.getLongitude());
					Logger.log("distBetweenNetworkPoint " + distBetweenNetworkPoint);
					Logger.log("LocationUtils.this.lastNetworkProviderLocation.getAccuracy() " + 
							LocationUtils.this.lastNetworkProviderLocation.getAccuracy());
					if (distBetweenNetworkPoint > LocationUtils.this.lastNetworkProviderLocation.getAccuracy()) {
						//arg0.getLatitude() != LocationUtils.this.lastNetworkProviderLocation.getLatitude() ||
						//arg0.getLongitude() != LocationUtils.this.lastNetworkProviderLocation.getLongitude()) 
						LocationUtils.this.onLocationChanged(this.getClass(), arg0);
						LocationUtils.this.lastNetworkProviderLocation = arg0;
					}
				} else {
					Logger.log("----ANTENNA VERY FIRST onChange");
					Logger.log("arg0.getLatitude() " + arg0.getLatitude());
					Logger.log("arg0.getLongitude() " + arg0.getLongitude());
					
					// ����� ������.
					// ��� LG Optimus �������� ���:
					// ���� ������������ � ������� ����������, �� �� ������ ����������� ���������� �� ����,
					// ���� ����� � �������/������� ��� ���
					// � ���� ����� ���, �� ���� ������ ���� ��������� ��������� ��� �������!!!
					LocationUtils.this.onLocationChanged(this.getClass(), arg0);
					LocationUtils.this.lastNetworkProviderLocation = arg0;
				}
			} 
			// ��������� �������, ��� ��������� ��������� ������� ���� �� ������ ����
			LocationUtils.this.lastLocationProviderClass = NetworkListener.class;
			
		}

		@Override
		public void onProviderDisabled(String arg0) {
			Logger.log(this.toString() + " " + "onProviderDisabled ");
			
		}

		@Override
		public void onProviderEnabled(String arg0) {
			Logger.log(this.toString() + " " + "onProviderEnabled ");
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			Logger.log(this.toString() + " " + " onStatusChanged arg0" + arg0 + " arg1 " + arg1);
		}
	}
	
	public class GpsListener implements LocationListener {
		
		int listenerAvailable = LocationProvider.AVAILABLE;
		
		@Override
		public void onLocationChanged(Location arg0) {
			Logger.log(this.toString() + " " + "onLocationChanged ");

			LocationUtils.this.onLocationChanged(this.getClass(), arg0);
			
			// ��������� �������, ��� ��������� ��������� ������� ���� ������� ������ gps
			LocationUtils.this.lastLocationProviderClass = this.getClass();
		}

		@Override
		public void onProviderDisabled(String arg0) {
			Logger.log(this.toString() + " " + "onProviderDisabled ");
		}

		@Override
		public void onProviderEnabled(String arg0) {
			Logger.log(this.toString() + " " + "onProviderEnabled ");
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			Logger.log(this.toString() + " " + " onStatusChanged arg0" + arg0 + " arg1 " + arg1);
			this.listenerAvailable = (arg1 == LocationProvider.AVAILABLE)?1:0;
		}
	
	}
	
	static public boolean isDistantOutOfAccuracy(Location location1, Location location2) {
		
		if (location1 == null || location2 == null) {
			return true;
		}
		
		double distance = LocationUtils.distFrom(location1.getLatitude(), 
				location1.getLongitude(), 
				location2.getLatitude(), 
				location2.getLongitude()) * 2;
		
		if (distance > location2.getAccuracy() && distance > location1.getAccuracy()) {
			return true;
		}
		
		return false;
		
	}
	
	private Integer getIntervalTime() {
		Integer intervatTime = 0;
		
		
		SharedPreferences sPref = trackRecorderService.getSharedPreferences(Tools.PREFERENCES_NAME, Service.MODE_PRIVATE);
	    interval = sPref.getInt(Tools.PREF_INTERVAL, 1);		
		
		
		if (interval == 0) {
			intervatTime = 0; // realTime
		} else if (interval == 1 || interval == 2) {
			intervatTime = interval * DateUtils.MILLISECONDS_IN_MINUTE;
		}  else if (interval == 3) {
			intervatTime = 5 * DateUtils.MILLISECONDS_IN_MINUTE;
		}  else if (interval == 4) {
			intervatTime = 10 * DateUtils.MILLISECONDS_IN_MINUTE;
		}  else if (interval == 5) {
			intervatTime = 30 * DateUtils.MILLISECONDS_IN_MINUTE;
		}  else if (interval == 6) {
			intervatTime = 60 * DateUtils.MILLISECONDS_IN_MINUTE;
		} else if (interval == -1) {
			intervatTime = 10 * DateUtils.MILLISECONDS_IN_SECOND;
		}
		
		return intervatTime;
	}
	
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
	    double earthRadius = 3958.75;
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    int meterConversion = 1609;

	    return dist * meterConversion;
	    }
	
	public static double distFrom(Location location1, Location location2) {
		
		return LocationUtils.distFrom(
				location1.getLatitude(),  
				location1.getLongitude(), 
				location2.getLatitude(), 
				location2.getLongitude()
			);
		
	}

	
	public void setInterval (Integer interval) {
		this.interval = interval;
	}
	
	public void restartWithNewInterval() {
		locationManager.removeUpdates(this.gpsListener);
		locationManager.removeUpdates(this.networkListener);
		this.onResume();
	}
	
	private GpsStatus.Listener listener = new GpsStatus.Listener() {
	    public void onGpsStatusChanged(int event) {
	    	if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
	    		Logger.log("GpsStatus.GPS_EVENT_FIRST_FIX");
	    	}
	    }
	};	
	
	public LocationUtils(ILocationReceiver locationReceiver, FragmentActivity activity) {
		this.locationReceiver = locationReceiver;
		
		locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		locationManager.addGpsStatusListener(listener);
	
	}
	
	public LocationUtils(ILocationReceiver locationReceiver, Service service) {
		this.locationReceiver = locationReceiver;
		this.trackRecorderService = service;

	    locationManager = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
		locationManager.addGpsStatusListener(listener);
	
	}

	
	
	public void onResume() {
		
		// ������� ������������ ����� � ���� ����������� � gps � network
		
		// LocationUtils.this.lastNetworkProviderLocation = null;
		
		provider1 = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);
		
    	if (provider1 != null) {
	    	//Location location = locationManager.getLastKnownLocation(provider1.getName());
	        
	        if (locationManager != null) {
	      	  locationManager.requestLocationUpdates(provider1.getName(), this.getIntervalTime(), 0, this.networkListener);
	        }
    	}
		
	    provider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
	    	
	    if (provider != null) {
		        if (locationManager != null) {
		      	  locationManager.requestLocationUpdates(provider.getName(), this.getIntervalTime(), 0, this.gpsListener);
		        }
	    }
    	
	}
	
	public void onPause() { 
		locationManager.removeUpdates(this.gpsListener);
		locationManager.removeUpdates(this.networkListener);
		provider = null;
	}	
	
	public void onLocationChanged(Class listenerProvider, Location location) {
		this.locationReceiver.newLocation(location, listenerProvider, location.getAccuracy());
	}

	public LocationManager getLocationManager() {
		return locationManager;
	}

	public LocationProvider getProvider() {
		return provider;
	}
	
}
