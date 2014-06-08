package com.trackmars.and.tracker;

import java.util.ArrayList;
import java.util.List;

import ru.elifantiev.android.roboerrorreporter.Logger;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.LayoutParams;

import com.google.android.gms.maps.SupportMapFragment;
import com.trackmars.and.tracker.dataUtils.EntityHelper;
import com.trackmars.and.tracker.model.Track;
import com.trackmars.and.tracker.model.TrackPointData;
import com.trackmars.and.tracker.utils.ILocationReceiver;
import com.trackmars.and.tracker.utils.LocationUtils;
import com.trackmars.and.tracker.utils.Tools;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends FragmentActivity implements ILocationReceiver {
	
	private GoogleMap map;	
	//private Boolean mapPositioned = false;
    private Marker myCurrentPositionMarker;
    private Location location;
    private Location lastSavedLocation;
    private Class listenerType;
    private Float accuracy;
    private TrackRecorderReceiver trackRecorderReceiver = new TrackRecorderReceiver();
    private TrackRecorderService trackRecorderService;
    Circle circle;
    
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
    	
    	
    	//this.deleteDatabase("trackmars.db");
    	
        Logger.log("Main activity onCreate started");
    	
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

        
		ImageButton menuBtn = (ImageButton)findViewById(R.id.imageButtonSettings);
		
		menuBtn.setOnClickListener(new ImageButton.OnClickListener(){

			@Override
			public void onClick(View v) {
				showSettings(v);
			}
			
		});
		
	    data = new String[]{"0.1", "1", "2", "5", "10", "30", "1" + getResources().getString(R.string.hour1)};		    

		Button intervalButton = (Button) findViewById(R.id.headerIntervalButton);
		intervalButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				showSettings(v);
			}
			
		});
        
        
        
    }


	class MyTask extends AsyncTask<Void, Void, Void> {

	    @Override
	    protected void onPreExecute() {
	      super.onPreExecute();
	      //tvInfo.setText("Begin");
	    }

	    @Override
	    protected Void doInBackground(Void... params) {
	      try {

	    	//EntityHelper histEntityHelper = new EntityHelper(getApplicationContext(), History.class);
			//History hist = (History) histEntityHelper.getRow(null);
			//hist = null;
			//histEntityHelper = null;
	    	  
	    	EntityHelper trackEntityHelper = new EntityHelper(getApplicationContext(), Track.class);
			Track track = (Track) trackEntityHelper.getRow(null);
			if (track.ID != null && (track.FINISHED == null || track.FINISHED == 0)) {
				if (!trackRecorderService.isRecording()) {
					trackRecorderService.startRecord(true);
					
				    //Intent intent = new Intent(MainActivity.this, TrackRecordActivity.class);
				    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				    //startActivity(intent);
				}
			}
			
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return null;
	    }

	    @Override
	    protected void onPostExecute(Void result) {
	      super.onPostExecute(result);
	      //tvInfo.setText("End");
	    }
	  }	
    
    
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
        	trackRecorderService = ((TrackRecorderService.ManagerBinder) binder).getMe();
            buttonsArrange();
            
            trackRecorderService.setInterval(-1); // real time;
            trackRecorderService.resume();
            
            if (trackRecorderService.getLocation() != null) {
                Logger.log( "Main activity onResume this.location != null");
                
          	  MainActivity.this.newLocation(trackRecorderService.getLocation(), 
          			  trackRecorderService.getLastPointProvider(), 
          			  trackRecorderService.getLocation().getAccuracy());
          	  //this.lastPoint = null;
          	  
            } else {
                Logger.log( "Main activity onResume this.location == null");
            }

    		MyTask mt = new MyTask();
    	    mt.execute();		

    		getSettings();
    		Button intervalButton = (Button) findViewById(R.id.headerIntervalButton);
    		intervalButton.setText(getResources().getString(R.string.interval) + " " + data[interval]);
    	    
    	    
        }

        public void onServiceDisconnected(ComponentName className) {
        }
        
    };    
    
    /* Request updates at startup */
    @Override
    protected void onResume() {
      super.onResume();
      
      //lastPoint = null;
      
      Logger.log( "Main activity onResume started");

      Intent intent = new Intent(this, TrackRecorderService.class);
      bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

      //mapPositioned = false;
      
      trackRecorderReceiver.setLocationReceiver(this);
      registerReceiver(trackRecorderReceiver, new IntentFilter(LocationUtils.LOCATION_RECEIVER_ACTION));
      
    }

    @Override
    protected void onPause() {
      super.onPause();
      
      if (trackRecorderService != null || trackRecorderService.isRecording()) {
    	  trackRecorderService.collectLocations(true);
      }
      
      //if (trackRecorderService == null || !trackRecorderService.isRecording()) {
    	  unregisterReceiver(trackRecorderReceiver);
      //}
      
      if (trackRecorderService != null) {
	      Logger.log( "Ready to unbind");
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

    private PolylineOptions polylineOptions;
    private Polyline polyline;
	List<LatLng> lngs = new ArrayList<LatLng>();
    
    private void showTrackOnTheMap (Location location) throws IllegalAccessException, InstantiationException {
    	
    	if (this.map != null) {
    		
    		boolean segmentsBegining = true;
    		
	    	polylineOptions = new PolylineOptions();
	    	polylineOptions.geodesic(true).color(0x400000ff);
	    	polylineOptions.width(LocationUtils.DEFAULT_ACCURACY);
	    	
	    	if (lastPoint == null) {
		    	for (TrackPointData data : trackRecorderService.getAllTrackPoint()) {
		    		
		    		if (segmentsBegining) {
		    			
		    		}
		    		
		    		lngs.add(new LatLng(data.LAT, data.LNG));
		    		if (data.paused) {
		    	    	polyline = this.map.addPolyline(polylineOptions);
		    	    	polyline.setPoints(lngs);
		    	    	lngs = new ArrayList<LatLng>();
		    	    	polyline = this.map.addPolyline(polylineOptions);
		    		}
		    	}
		    	polyline = this.map.addPolyline(polylineOptions);
		    	polyline.setPoints(lngs);
	    	}
	    	
	    	if (lastPoint != null) {
	    		
				//
	    		
	    		// если вернулись по кнопке бек, то в сервисе должны буть сохранены точки
	    		// которые не отрисовывались
	    		if (trackRecorderService.collected()) {
	    			
	    			List<LatLng> collectedLngs = new ArrayList<LatLng>();
	    			
			    	for (TrackPointData data : trackRecorderService.getCollectedLocations()) {
			    		
			    		if (segmentsBegining) {
			    			
			    		}
			    		
			    		collectedLngs.add(new LatLng(data.LAT, data.LNG));
			    		if (data.paused) {
			    	    	polyline = this.map.addPolyline(polylineOptions);
			    	    	polyline.setPoints(collectedLngs);
			    	    	collectedLngs = new ArrayList<LatLng>();
			    	    	polyline = this.map.addPolyline(polylineOptions);
			    		}
			    	}
			    	
			    	trackRecorderService.collectLocations(false);
			    	
			    	lngs.addAll(collectedLngs);
	    		} else {
	    			lngs.add(new LatLng(location.getLatitude(), location.getLongitude()));	    			
	    		}
	    		
	    		
	    		if (trackRecorderService.isPaused()) {
	    	    	lngs = new ArrayList<LatLng>();
	    	    	polyline = this.map.addPolyline(polylineOptions);
	    		}
	    		
	    	}
	    	
	    	lastPoint = new LatLng(location.getLatitude(), location.getLongitude());
	    	
	    	polyline.setPoints(lngs);
	    	
    	}
    	
    }
    
    @Override
	public void newLocation(Location location, Class listenerType, Float accuracy) {
        this.location = location;
        this.listenerType = listenerType;
        this.accuracy = accuracy;

    	if (trackRecorderService.isRecording()) {
    		if (LocationUtils.isDistantOutOfAccuracy(location, lastSavedLocation)) {
	    		try {
					showTrackOnTheMap(location);
					lastSavedLocation = location;
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
	            	
		            
		            //if (!mapPositioned) {
			            map.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentPosition, 16), 100, null);
	            	//}
		            //map.moveCamera(CameraUpdateFactory.newLatLng(myCurrentPosition));
		            
		            if (circle != null) {
		            	circle.remove();
		            }
		            
		            CircleOptions circleOptions = new CircleOptions();
		            circleOptions.center(new LatLng(location.getLatitude(), location.getLongitude()));
		            circleOptions.fillColor(Color.BLUE).strokeColor(0x2000ff00).strokeWidth(2).radius(this.accuracy / 2);
		            circleOptions.fillColor(0x10ff0000);
		            circle = map.addCircle(circleOptions);
		            
		            
		            //mapPositioned = true;
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
		ImageView antennaIndicator = (ImageView) this.findViewById(R.id.antennaIndicatorOnMap);

    	
    	if (trackRecorderService != null) {
	    	if (trackRecorderService.getLastPointProvider() == LocationUtils.GpsListener.class) {
	        	antennaIndicator.setImageResource(R.drawable.sattelite);
	    	} else if (trackRecorderService.getLastPointProvider() == LocationUtils.NetworkListener.class) {
	        	antennaIndicator.setImageResource(R.drawable.antenna);
	    	}
    	}
		
		
    }
	
    
    
    
	public void onClick(View view) throws IllegalAccessException, InstantiationException {
		if (view.getId() == R.id.imageButtonPoint) {
			
			  if (location != null) {
			
			      Intent intent = new Intent(this, DialogCreatePoint.class);
			      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			      
			      intent.putExtra("long", location.getLongitude());
			      intent.putExtra("lat", location.getLatitude());
			      
			      if (trackRecorderService.isRecording()) {
			    	  intent.putExtra("track_id", trackRecorderService.getCurrentRecordingTrackId());
			      } else {
			    	  intent.putExtra("track_id", (Integer)null);
			      }
			      
			      startActivity(intent);
		      
			  } else {
				  // <TODO> have to append null location proceed. Some sort of user notification
			  }
			  
		} else if (view.getId() == R.id.buttonPoints) {
			
		      Intent intent = new Intent(this, PointsActivity.class);
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
				trackRecorderService.trackStop(this);
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
			this.accuracy = trackRecorderService.getAccuracy();
			this.listenerType = trackRecorderService.getLastPointProvider();
			
			this.newLocation(this.location, this.listenerType, this.accuracy);
			
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

    
	//////////////////////////////////////////////////////////////////////////////
	////// Interval settings
	//////////////////////////////////////////////////////////////////////////////

	private String[] data;
	private Integer interval = 1 ; // default 1=1min
	
	
	private void getSettings() {
		SharedPreferences sPref = getSharedPreferences(Tools.PREFERENCES_NAME, Activity.MODE_PRIVATE);
	    interval = sPref.getInt(Tools.PREF_INTERVAL, 1);		
	}
	
	private void saveSettings() {
		SharedPreferences sPref = getSharedPreferences(Tools.PREFERENCES_NAME, Activity.MODE_PRIVATE);
	    Editor ed = sPref.edit();
	    ed.putInt(Tools.PREF_INTERVAL, this.interval);
	    ed.commit();
	}
	
	public void showSettings(View v) {

		getSettings();
		
		if (v.getId() == R.id.imageButtonSettings || v.getId() == R.id.headerIntervalButton) {
			
			// creating popup window from XML layout
			LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    View popupView = layoutInflater.inflate(R.layout.settings, null);
		    
		    final PopupWindow popupWindow = new PopupWindow(
		               popupView, 
		               LayoutParams.MATCH_PARENT,  
		               LayoutParams.WRAP_CONTENT);  

		    
		    
		    // adding popup window views processors
		    //////////////////////////////////////////////////////////////////////////////////
		     
		    // button "back"
		    ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.buttonBack);
		    btnDismiss.setOnClickListener(new Button.OnClickListener(){
			     @Override
			     public void onClick(View v) {
			    	 // TODO Auto-generated method stub
			    	 popupWindow.dismiss();
			     }
		     });
		    
		    // button "ok"
		    ImageButton btnOk = (ImageButton) popupView.findViewById(R.id.buttonOk);
		    btnOk.setOnClickListener(new Button.OnClickListener(){
			     @Override
			     public void onClick(View v) {
			    	 saveSettings();

			    	 Button intervalButton = (Button) findViewById(R.id.headerIntervalButton);
			    	 intervalButton.setText(getResources().getString(R.string.interval) + " " + data[interval]);
			 	     //Intent intent = new Intent(getActivity().getApplicationContext(), TrackRecorderService.class);
			 	     //getActivity().unbindService(mConnection);
				     //getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
				     
			    	 trackRecorderService.setInterval(interval);
			    	 
			    	 popupWindow.dismiss();
			     }
		     });

		    
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        
	        Spinner spinner = (Spinner) popupView.findViewById(R.id.SpinnerInterval);
	        spinner.setAdapter(adapter);
	        // заголовок
	        spinner.setPrompt("Title");
	        spinner.setSelection(interval);
	        
	        spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
	            @Override
	            public void onItemSelected(AdapterView<?> parent, View view,
	                int position, long id) {
	              interval = position;
	            }
	            @Override
	            public void onNothingSelected(AdapterView<?> arg0) {
	            }
	          });
	        
		    //////////////////////////////////////////////////////////////////////////////////
	        
		    /// show popup window
		    popupWindow.showAsDropDown(v, 50, 0);
		}
	}
    
}
