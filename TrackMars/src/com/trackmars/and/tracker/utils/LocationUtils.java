package com.trackmars.and.tracker.utils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trackmars.and.tracker.dataUtils.DateUtils;

import android.app.Service;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
//import android.widget.Toast;

public class LocationUtils{
	
	private ILocationReceiver locationReceiver;
    private LocationManager locationManager;
	private LocationProvider provider;
	private LocationProvider provider1;
	
	
	private NetworkListener networkListener = new NetworkListener();
	private GpsListener gpsListener = new GpsListener();
	
	private Location gpsLocation;
	private Location networkLocation;
	
	private Integer interval = -1;
	
	final public static String LOCATION_RECEIVER_ACTION = "com.trackmars.and.tracker.locationMessage"; 
	
	public class NetworkListener implements LocationListener {

		@Override
		public void onLocationChanged(Location arg0) {
			networkLocation = arg0;
			LocationUtils.this.onLocationChanged();
			
		}

		@Override
		public void onProviderDisabled(String arg0) {
			
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class GpsListener implements LocationListener {

		@Override
		public void onLocationChanged(Location arg0) {
			gpsLocation = arg0;
			LocationUtils.this.onLocationChanged();
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
	
	}
	
	
	private Integer getIntervalTime() {
		Integer intervatTime = 0;
		if (interval == 0) {
			intervatTime = 30 * DateUtils.MILLISECONDS_IN_SECOND;
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
	
	private String getBestProvider(LocationManager locationManager) {
		
		String bestProvider = new String();
		Float bestAccuracy = null;
		String bestPrivider = new String();
		Location bestLocation;
		
		Criteria criteria = new Criteria();
		return locationManager.getBestProvider(criteria, true);
		
		/*
		for (String currentProvider : locationManager.getAllProviders() ) {
			Location location = locationManager.getLastKnownLocation(currentProvider);
			
			//if (currentProvider.equals(locationManager.GPS_PROVIDER)) {
			//	return locationManager.GPS_PROVIDER;
			//}
			
			if (location != null) {
				float accuracy = location.getAccuracy();
				if (bestAccuracy == null) {
					bestAccuracy = accuracy;
					bestProvider = currentProvider;
					bestLocation = location;
				}
				
				if (accuracy < bestAccuracy) {
					bestAccuracy = accuracy;
					bestProvider = currentProvider;
					bestLocation = location;
				}
			}
		}
		
		return bestProvider;
		*/
	}
	
	
	public void setInterval (Integer interval) {
		this.interval = interval;
	}
	
	public void restartWithNewInterval() {
		locationManager.removeUpdates(this.gpsListener);
		locationManager.removeUpdates(this.networkListener);
		this.onResume();
	}
	
	public LocationUtils(ILocationReceiver locationReceiver, FragmentActivity activity) {
		this.locationReceiver = locationReceiver;

		locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
	
	}
	
	public LocationUtils(ILocationReceiver locationReceiver, Service service) {
		this.locationReceiver = locationReceiver;

	    locationManager = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
	
	}

	
	
	public void onResume() {
		
		// Пробуем подключиться сразу к двум провайдерам к gps и network
		
		
		provider1 = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);
		
    	if (provider1 != null) {
	    	Location location = locationManager.getLastKnownLocation(provider1.getName());
	        
	        if (locationManager != null) {
	      	  locationManager.requestLocationUpdates(provider1.getName(), this.getIntervalTime(), 0, this.networkListener);
	        }
    	}
		
	    provider = locationManager.getProvider(locationManager.GPS_PROVIDER);
	    	
	    if (provider != null) {
		    	
		    	Location location = locationManager.getLastKnownLocation(provider.getName());
		    	
		        if (locationManager != null) {
		      	  locationManager.requestLocationUpdates(provider.getName(), this.getIntervalTime(), 0, this.gpsListener);
		        }
	    }
    	
	}
	
	public void onPause() { 
		locationManager.removeUpdates(this.gpsListener);
		locationManager.removeUpdates(this.networkListener);
		//locationManager.removeUpdates(dummyListener);
		provider = null;
	}	
	
	public void onLocationChanged() {
		// теперь поймем, с какого ресивера взять точку
		
		Float gpsAccuracy = null;
		Float networkAccuracy = null;
		Float accuracy = null;
		Location location = null;
		Class listener = null;
		
		if (gpsLocation != null) {
			gpsAccuracy = gpsLocation.getAccuracy();
		}
		
		if (networkLocation != null) {
			networkAccuracy = networkLocation.getAccuracy();
			
			if (gpsAccuracy != null) {
				if (networkAccuracy < gpsAccuracy) {
					listener = NetworkListener.class;
					location = networkLocation;
					accuracy = networkAccuracy;
				} else {
					listener = GpsListener.class;
					location = gpsLocation;
					accuracy = gpsAccuracy;
				}
			} else {
				listener = NetworkListener.class;
				location = networkLocation;
				accuracy = networkAccuracy;
			}
		} else {
			
			if (gpsAccuracy != null) {
				listener = GpsListener.class;
				location = gpsLocation;
				accuracy = gpsAccuracy;
			}
		}

		
		
		this.locationReceiver.newLocation(location, listener, accuracy);
	}

	public LocationManager getLocationManager() {
		return locationManager;
	}

	public LocationProvider getProvider() {
		return provider;
	}
	
}
