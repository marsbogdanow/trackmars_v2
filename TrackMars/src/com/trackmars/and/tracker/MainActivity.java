package com.trackmars.and.tracker;

import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
//import com.google.android.gms.maps.MapFragment;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
//import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
//import android.app.Activity;
//import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.trackmars.and.tracker.utils.ILocationReceiver;
import com.trackmars.and.tracker.utils.LocationUtils;

import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;

public class MainActivity extends FragmentActivity implements ILocationReceiver {
	
	private GoogleMap map;	
	private Boolean mapPositioned = false;
    private Marker myCurrentPositionMarker;
    private Location location;
    private TrackRecorderReceiver trackRecorderReceiver = new TrackRecorderReceiver();
    private TrackRecorderService trackRecorderService;
    
    private LatLng lastPoint;
    
      
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
          // Inflate the menu; this adds items to the action bar if it is present.
          getMenuInflater().inflate(R.menu.main, menu);
          return true;
    }
      
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        Log.d(MainActivity.class.getName(), "Main activity onCreate started");
    	
        lastPoint = null; 
        
        updateServices();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        

        try {
	        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
	                .getMap();
        } finally {}
        
        Intent intent = new Intent(this, TrackRecorderService.class);
        startService(intent);
        //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
    }

    
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
        	trackRecorderService = ((TrackRecorderService.ManagerBinder) binder).getMe();
            buttonsArrange();
            
            trackRecorderService.setInterval(-1); // real time;
            trackRecorderService.resume();
            
        }

        public void onServiceDisconnected(ComponentName className) {
        }
        
    };    
    
    /* Request updates at startup */
    @Override
    protected void onResume() {
      super.onResume();
      
      Log.d(MainActivity.class.getName(), "Main activity onResume started");

      Intent intent = new Intent(this, TrackRecorderService.class);
      bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

      mapPositioned = false;
      
      trackRecorderReceiver.setLocationReceiver(this);
      registerReceiver(trackRecorderReceiver, new IntentFilter(LocationUtils.LOCATION_RECEIVER_ACTION));
      
      
	  //locationUtils.onResume();
	    
    }

    @Override
    protected void onPause() {
      super.onPause();
      
      if (trackRecorderService == null || !trackRecorderService.isRecording()) {
    	  unregisterReceiver(trackRecorderReceiver);
      }
      
      Log.d(MainActivity.class.getName(), "Ready to unbind");
      trackRecorderService.setInterval(null); // real time;
      unbindService(mConnection);
      
      try {
		trackRecorderService.pause();
	  } catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	  } catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	  }
	      //locationUtils.onPause();
    }
    
    private void buttonsArrange () {
    	if (trackRecorderService != null) {
	    	if (trackRecorderService.isRecording()) {
	    		if (! trackRecorderService.isPaused()) {
		    		ImageButton recordButton = (ImageButton) findViewById(R.id.imageButtonRecord);
		    		recordButton.setImageResource(R.drawable.button_pause);
		    		ImageButton resumeButton = (ImageButton) findViewById(R.id.imageButtonResume);
		    		resumeButton.setVisibility(View.INVISIBLE);
	    		} else {
		    		ImageButton recordButton = (ImageButton) findViewById(R.id.imageButtonRecord);
		    		recordButton.setImageResource(R.drawable.button_record);
		    		ImageButton resumeButton = (ImageButton) findViewById(R.id.imageButtonResume);
		    		resumeButton.setVisibility(View.VISIBLE);
		    		resumeButton.setImageResource(R.drawable.button_stop);
	    		}
	    	} else {
	    		ImageButton recordButton = (ImageButton) findViewById(R.id.imageButtonRecord);
	    		recordButton.setImageResource(R.drawable.button_record);
	    		ImageButton resumeButton = (ImageButton) findViewById(R.id.imageButtonResume);
	    		resumeButton.setVisibility(View.VISIBLE);
	    		resumeButton.setImageResource(R.drawable.button_resume);
	    	}
    	}
    		
    }

    private void showTrackOnTheMap (Location location) throws IllegalAccessException, InstantiationException {
    	
    	Log.d(MainActivity.class.getName(), "call showTrackOnTheMap");
    	
    	PolylineOptions polylineOptions = new PolylineOptions();
    	polylineOptions.geodesic(true);
    	
    	
    	if (lastPoint == null) {
        	List<LatLng> latLngs =  trackRecorderService.getAllTrackPoint(); 
	    	for (LatLng latLng : latLngs) {
	    		Log.d(MainActivity.class.getName(), "latLng " + latLng.latitude + " " + latLng.longitude);
	    		polylineOptions.add(latLng);
	    	}
    	} else {
    		polylineOptions.add(lastPoint);
    		polylineOptions.add(new LatLng(location.getLatitude(), location.getLongitude()));
    	}
    	
		lastPoint = new LatLng(location.getLatitude(), location.getLongitude());

    	if (map != null) {
	    	this.map.addPolyline(polylineOptions);
    	}
    }
    
    @Override
	public void newLocation(Location location) {
        this.location = location;

    	if (trackRecorderService.isRecording()) {
    		try {
				showTrackOnTheMap(location);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
        
        if (map != null && location != null) {
            	
            	if (location != null) {
            		
            		if (myCurrentPositionMarker != null) {
            			myCurrentPositionMarker.remove();
            		}
	            	
	            	LatLng myCurrentPosition = new LatLng(location.getLatitude(), location.getLongitude()); 
	            	
	            	myCurrentPositionMarker = map.addMarker(new MarkerOptions().position(myCurrentPosition)
	                .title("You are here"));
	            	
		            
		            map.moveCamera(CameraUpdateFactory.newLatLng(myCurrentPosition));
		            if (!mapPositioned) {
			            map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
	            	}
		            
		            mapPositioned = true;
            	}
            	
            
        } else {
//              FragmentTest fragment = (FragmentTest) getSupportFragmentManager()
//              .findFragmentById(R.id.map);
//              
//	          if (fragment != null && fragment.isInLayout()) {
//	            fragment.setText("Here is the place for LIST");
//	          } 
        }
		buttonsArrange();
		
    }
	
    
    
    
	public void onClick(View view) throws IllegalAccessException, InstantiationException {
		if (view.getId() == R.id.imageButtonPoint) {
			
			  if (location != null) {
			
			      Intent intent = new Intent(this, DialogCreatePoint.class);
			      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			      
			      intent.putExtra("long", location.getLongitude());
			      intent.putExtra("lat", location.getLatitude());
			      startActivity(intent);
		      
			  } else {
				  // <TODO> have to append null location proceed. Some sort of user notification
			  }
			  
		} else if (view.getId() == R.id.buttonPoints) {
			
		      Intent intent = new Intent(this, PointsActivity.class);
		      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		      
		      startActivity(intent);
		      
		} else if (view.getId() == R.id.buttonTracks) {
			
		      Intent intent = new Intent(this, TracksActivity.class);
		      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		      
		      startActivity(intent);
		      
		} else if (view.getId() == R.id.imageButtonRecord) {
			
			if (!trackRecorderService.isRecording()) {
				trackRecorderService.startRecord(false);
				
			    Intent intent = new Intent(this, TrackRecordActivity.class);
			    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    startActivity(intent);
				
				
			} else if (trackRecorderService.isRecording() && !trackRecorderService.isPaused()) {
				trackRecorderService.trackPause();
			} else if (trackRecorderService.isRecording() && trackRecorderService.isPaused()) {
				trackRecorderService.startRecord(true);
			}
			
		} else if (view.getId() == R.id.imageButtonResume) {
			
			if (!trackRecorderService.isRecording()) {
				trackRecorderService.startRecord(true);
				
			    Intent intent = new Intent(this, TrackRecordActivity.class);
			    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    startActivity(intent);
			} else if (trackRecorderService.isRecording() && trackRecorderService.isPaused()) {
				trackRecorderService.trackStop();
			}
			
		} else if (view.getId() == R.id.buttonDetails) {
			
			if (trackRecorderService.isRecording()) {
			    Intent intent = new Intent(this, TrackRecordActivity.class);
			    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    startActivity(intent);
			}
			
		}
		
		
		buttonsArrange();
		
 	}

	
	@Override
	public void askLocation() {
		if (trackRecorderService != null ) {
			this.location = trackRecorderService.getLocation();
			this.newLocation(this.location);
		}
	}
	
	
    private void updateServices() {

    	
    	boolean services = false;
    	try
    	{
    		ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.gms", 0);
    		services = true;
    	}
    	catch(PackageManager.NameNotFoundException e)
    	{
    		services = false;
    	}

    	if (services)
    	{
    		// Ok, do whatever.
    		Toast.makeText(MainActivity.this, "Play Services are installed. Would start map now...", Toast.LENGTH_LONG).show();
    		return;
    	}
    	else
    	{
    		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

    		// set dialog message
    		alertDialogBuilder
    				.setTitle("Google Play Services")
    				.setMessage("The map requires Google Play Services to be installed.")
    				.setCancelable(true)
    				.setPositiveButton("Install", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog,int id) {
    						dialog.dismiss();
    						// Try the new HTTP method (I assume that is the official way now given that google uses it).
    						try
    						{
    							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms"));
    							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    							intent.setPackage("com.android.vending");
    							startActivity(intent);
    						}
    						catch (ActivityNotFoundException e)
    						{
    							// Ok that didn't work, try the market method.
    							try
    							{
    								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms"));
    								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    								intent.setPackage("com.android.vending");
    								startActivity(intent);
    							}
    							catch (ActivityNotFoundException f)
    							{
    								// Ok, weird. Maybe they don't have any market app. Just show the website.

    								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms"));
    								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    								startActivity(intent);
    							}
    						}
    					}
    				})
    				.setNegativeButton("No",new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog,int id) {
    						dialog.cancel();
    					}
    				})
    				.create()
    				.show();
    	}    	
    	
    }
	
}
