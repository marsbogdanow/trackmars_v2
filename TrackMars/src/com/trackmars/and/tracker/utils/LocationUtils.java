package com.trackmars.and.tracker.utils;

import ru.elifantiev.android.roboerrorreporter.Logger;

import com.google.android.gms.internal.ar;
import com.trackmars.and.tracker.dataUtils.DateUtils;

import android.app.Service;
import android.content.Context;
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
	private Class lastNetworkProviderClass;
	
	
	private NetworkListener networkListener = new NetworkListener();
	private GpsListener gpsListener = new GpsListener();
	
	private Integer interval = -1;
	
	final public static String LOCATION_RECEIVER_ACTION = "com.trackmars.and.tracker.locationMessage"; 
	
	public class NetworkListener implements LocationListener {

		@Override
		public void onLocationChanged(Location arg0) {
			//networkLocation = arg0;
			Logger.log(this.toString() + " " + "onLocationChanged ");
			// позовем изменение локации только есть нетворк провайдер был вызван второй раз подряд
			// это сделано чтобы свести к кминимуму дурацкие точки по сети между точками по gps
			// (это условие пока отлючено)
			// if (LocationUtils.this.lastNetworkProviderClass == NetworkListener.class) {
				//если никуда не двигаемся, то не зовем изменения локации
				if (LocationUtils.this.lastNetworkProviderLocation != null)  {
					Logger.log("----ANTENNA");
					Logger.log("arg0.getLatitude() " + arg0.getLatitude());
					Logger.log("arg0.getLongitude() " + arg0.getLongitude());
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
					
					
					if (distBetweenNetworkPoint > LocationUtils.this.lastNetworkProviderLocation.getAccuracy()) {
						//arg0.getLatitude() != LocationUtils.this.lastNetworkProviderLocation.getLatitude() ||
						//arg0.getLongitude() != LocationUtils.this.lastNetworkProviderLocation.getLongitude()) 
						LocationUtils.this.onLocationChanged(this.getClass(), arg0);
					}
				}
				LocationUtils.this.lastNetworkProviderLocation = arg0;
			//} 
			// установим признак, что последнее изменение локации было на основе сети
			LocationUtils.this.lastNetworkProviderClass = NetworkListener.class;
			
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
			// установим признак, что последнее изменение локации ьыло вызвано именно gps
			LocationUtils.this.lastNetworkProviderClass = this.getClass();
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
	    	} else
	    	if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
	    		Logger.log("GpsStatus.GPS_EVENT_SATELLITE_STATUS");
	    	} else
	    	if (event == GpsStatus.GPS_EVENT_STARTED) {
	    		Logger.log("GpsStatus.GPS_EVENT_STARTED");
	    	} else 
	    	if (event == GpsStatus.GPS_EVENT_STOPPED) {
	    		Logger.log("GpsStatus.GPS_EVENT_STOPPED");
	    	} else {
	    		Logger.log("GpsStatus UNKNOWN " + event);
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

	    locationManager = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
		locationManager.addGpsStatusListener(listener);
	
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
	
	public void onLocationChanged(Class listenerProvider, Location location) {
		// теперь поймем, с какого ресивера взять точку
		
		//Float gpsAccuracy = null;
		//Float networkAccuracy = null;
		//Float accuracy = null;
		//Location location = null;
		//Class listener = null;
		/*
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
		*/
		
		Logger.log(this.toString() + " " + "#############################" );
		Logger.log(this.toString() + " " + "onLocationChanged() " + listenerProvider.getName());
		Logger.log(this.toString() + " " + "onLocationChanged() --- accuracy" + location.getAccuracy());
		Logger.log(this.toString() + " " + "#############################" );
		
		this.locationReceiver.newLocation(location, listenerProvider, location.getAccuracy());
	}

	public LocationManager getLocationManager() {
		return locationManager;
	}

	public LocationProvider getProvider() {
		return provider;
	}
	
}
