package com.trackmars.and.tracker.utils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Service;
import android.content.Context;
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
	
	final public static String LOCATION_RECEIVER_ACTION = "com.trackmars.and.tracker.locationMessage"; 
	
	
	private LocationListener dummyListener =  new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        @Override
        public void onLocationChanged(final Location location) {
        }
	};
	
	
	
	private String getBestProvider(LocationManager locationManager) {
		
		String bestProvider = new String();
		Float bestAccuracy = null;
		String bestPrivider = new String();
		Location bestLocation;
		
		for (String currentProvider : locationManager.getAllProviders() ) {
			Location location = locationManager.getLastKnownLocation(currentProvider);
			
			if (currentProvider == locationManager.GPS_PROVIDER) {
				return locationManager.GPS_PROVIDER;
			}
			
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
	}
	
	
	public LocationUtils(ILocationReceiver locationReceiver, FragmentActivity activity) {
		this.locationReceiver = locationReceiver;

	    locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
	    
	    
	    
	    locationManager.requestLocationUpdates(
	    	    LocationManager.GPS_PROVIDER, 0, 0, dummyListener 
	    	    );	    
	    
	
	}
	
	public LocationUtils(ILocationReceiver locationReceiver, Service service) {
		this.locationReceiver = locationReceiver;

	    locationManager = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
	    
	    
	    
	    locationManager.requestLocationUpdates(
	    	    LocationManager.GPS_PROVIDER, 0, 0, dummyListener 
	    	    );	    
	    
	
	}

	
	public void onResume() {
		
    	provider = locationManager.getProvider(this.getBestProvider(locationManager));
    	
    	if (provider != null) {
	    	
	    	//locationManager.req
	    	Location location = locationManager.getLastKnownLocation(provider.getName());
	
	    	
	        if (location != null) {
	          onLocationChanged(location);
	        }    	
	        
	        if (locationManager != null) {
	      	  locationManager.requestLocationUpdates(provider.getName(), 4000, 1, this);
	        }
    	}
    	
	}
	
	public void onPause() { 
		locationManager.removeUpdates(this);
		locationManager.removeUpdates(dummyListener);
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
