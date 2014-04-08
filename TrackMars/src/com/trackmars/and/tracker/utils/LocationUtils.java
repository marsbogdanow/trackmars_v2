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
import android.widget.Toast;

public class LocationUtils implements LocationListener {
	
	private ILocationReceiver locationReceiver;
    private LocationManager locationManager;
	private LocationProvider provider;
	private LocationProvider provider1;
	
	private Integer interval = -1;
	
	final public static String LOCATION_RECEIVER_ACTION = "com.trackmars.and.tracker.locationMessage"; 
	
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
	
	
//	private LocationListener dummyListener =  new LocationListener() {
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//        }
//        @Override
//        public void onProviderEnabled(String provider) {
//        }
//        @Override
//        public void onProviderDisabled(String provider) {
//        }
//        @Override
//        public void onLocationChanged(final Location location) {
//        }
//	};
	
	
	
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
		locationManager.removeUpdates(this);
		this.onResume();
	}
	
	public LocationUtils(ILocationReceiver locationReceiver, FragmentActivity activity) {
		this.locationReceiver = locationReceiver;

		Context context = activity;
		
	    locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
	    
	    
	    
//	    locationManager.requestLocationUpdates(
//	    	    LocationManager.GPS_PROVIDER, 0, 0, dummyListener 
//	    	    );	    
	    
	
	}
	
	public LocationUtils(ILocationReceiver locationReceiver, Service service) {
		this.locationReceiver = locationReceiver;

	    locationManager = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
	
	}

	
	
	public void onResume() {
		Location location = null;
    	provider1 = locationManager.getProvider(locationManager.NETWORK_PROVIDER);
    	if (provider1 != null) {
	    	location = locationManager.getLastKnownLocation(provider1.getName());
	    	
	        if (location != null) {
	        	
	          //Toast.makeText(, "gps", Toast.LENGTH_LONG);
	        	
	          onLocationChanged(location);
	        }    	
	        
	        if (locationManager != null) {
	      	  locationManager.requestLocationUpdates(provider1.getName(), this.getIntervalTime(), 0, this);
	        }
    	}
		
		//if (location == null) {
	    	//provider = locationManager.getProvider(this.getBestProvider(locationManager));
	    	provider = locationManager.getProvider(locationManager.GPS_PROVIDER);
	    	
	    	if (provider != null) {
		    	
		    	//locationManager.req
		    	location = locationManager.getLastKnownLocation(provider.getName());
		
		    	
		        if (location != null) {
		          onLocationChanged(location);
		        }    	
		        
		        if (locationManager != null) {
		      	  locationManager.requestLocationUpdates(provider.getName(), this.getIntervalTime(), 0, this);
		        }
	    	}
		//}
    	
	}
	
	public void onPause() { 
		locationManager.removeUpdates(this);
		//locationManager.removeUpdates(dummyListener);
		provider = null;
	}	
	
	@Override
	public void onLocationChanged(Location location) {
		this.locationReceiver.newLocation(location);
	}


	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}


	public LocationManager getLocationManager() {
		return locationManager;
	}

	public LocationProvider getProvider() {
		return provider;
	}
	
}
