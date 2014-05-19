package com.trackmars.and.tracker;

import java.util.Date;
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
import android.content.res.Resources;
//import android.app.Activity;
//import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.LayoutParams;
import android.text.Html;

import com.google.android.gms.maps.SupportMapFragment;
import com.trackmars.and.tracker.dataUtils.DateUtils;
import com.trackmars.and.tracker.utils.ILocationReceiver;
import com.trackmars.and.tracker.utils.LocationUtils;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class TrackRecordActivity extends FragmentActivity implements ILocationReceiver {
	
	private GoogleMap map;	
	private Boolean mapPositioned = false;
    private Marker myCurrentPositionMarker;
    private Location location;
    private Class listenerType;
    private float accuracy;
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
    	
        lastPoint = null; 
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_record);
        
    }

    
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
        	trackRecorderService = ((TrackRecorderService.ManagerBinder) binder).getMe();
            buttonsArrange();
            
            trackRecorderService.resume();
        }

        public void onServiceDisconnected(ComponentName className) {
        }
        
    };    
    
    /* Request updates at startup */
    @Override
    protected void onResume() {
      super.onResume();
      
      Intent intent = new Intent(this, TrackRecorderService.class);
      bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

      trackRecorderReceiver.setLocationReceiver(this);
      registerReceiver(trackRecorderReceiver, new IntentFilter(LocationUtils.LOCATION_RECEIVER_ACTION));
      
      
	  //locationUtils.onResume();
	    
    }

    @Override
    protected void onPause() {
      super.onPause();
      unregisterReceiver(trackRecorderReceiver);
      
      Log.d(TrackRecordActivity.class.getName(), "Ready to unbind");
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

    
    @Override
	public void newLocation(Location location, Class listenerType, Float accuracy) {
        this.location = location;
        this.listenerType = listenerType;
        this.accuracy = accuracy;

        final Long travelTime = this.trackRecorderService.getCurrentTravelTime();
        final Double distance = this.trackRecorderService.getCurrentDistance();
        final Long totalTime = (new Date()).getTime() - this.trackRecorderService.getTrackCreatedTime();
        
        Resources res = getResources();
        
        if (this.trackRecorderService.getTrackCreatedTime() != null) {
        	
	        final Integer hours = (int) (totalTime / DateUtils.MILLISECONDS_IN_HOUR);
	        final Integer minutes = (int)((totalTime - hours * DateUtils.MILLISECONDS_IN_HOUR) / DateUtils.MILLISECONDS_IN_MINUTE);
	        
	        
	        String fieldText = "<big><big><b>" + hours.toString() + "</b></big></big>";
	        fieldText += "<small>" + res.getString(R.string.hour) + ":</small>";
	        fieldText += "  <big><big><b>" + minutes.toString() + "</b></big></big>";
	        fieldText += "<small>" + res.getString(R.string.minute) + "</small>";
	        
	        ((TextView)findViewById(R.id.time)).setText(Html.fromHtml(fieldText));
        
        }

        
        if (travelTime != null) {
        	
	        final Integer hours = (int) (travelTime / DateUtils.MILLISECONDS_IN_HOUR);
	        final Integer minutes = (int)((travelTime - hours * DateUtils.MILLISECONDS_IN_HOUR) / DateUtils.MILLISECONDS_IN_MINUTE);
	        
	        String fieldText = "<big><big><b>" + hours.toString() + "</b></big></big>";
	        fieldText += "<small>" + res.getString(R.string.hour) + ":</small>";
	        fieldText += "  <big><big><b>" + minutes.toString() + "</b></big></big>";
	        fieldText += "<small>" + res.getString(R.string.minute) + "</small>";
	        
	        ((TextView)findViewById(R.id.in_motion)).setText(Html.fromHtml(fieldText));
        
        }
        
        if (distance != null) {
        	
	        final Integer km = (int) (distance / 1000);
	        final Integer meter = (int)(distance - km * 1000);
	        
	        
	        String fieldText = "<big><big><b>" + km.toString() + "</b></big></big>";
	        fieldText += "<small>" + res.getString(R.string.kilometer) + ", </small>";
	        fieldText += "  <big><big><b>" + meter.toString() + "</b></big></big>";
	        fieldText += "<small>" + res.getString(R.string.meter) + "</small>";
	        
	        ((TextView)findViewById(R.id.distance)).setText(Html.fromHtml(fieldText));
        
        }
        
        if (distance != null && travelTime != null && travelTime != 0l && distance != 0d) {
        	Integer speed = (int)  (distance / (double)travelTime / 1000d * (double)DateUtils.MILLISECONDS_IN_HOUR); 

	        String fieldText = "<big><big><b>" + speed.toString() + "</b></big></big>";
	        fieldText += "<small>" + res.getString(R.string.kmph) + ", </small>";
	        
	        ((TextView)findViewById(R.id.avg_speed)).setText(Html.fromHtml(fieldText));
        
        
        } else {
        	
	        String fieldText = "<big><big><b> --</b></big></big>";
	        fieldText += "<small>" + res.getString(R.string.kmph) + ", </small>";
	        
	        ((TextView)findViewById(R.id.avg_speed)).setText(Html.fromHtml(fieldText));
        	
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
			} else if (trackRecorderService.isRecording() && !trackRecorderService.isPaused()) {
				trackRecorderService.trackPause();
			} else if (trackRecorderService.isRecording() && trackRecorderService.isPaused()) {
				trackRecorderService.startRecord(true);
			}
			
		} else if (view.getId() == R.id.imageButtonResume) {
			
			if (!trackRecorderService.isRecording()) {
				trackRecorderService.startRecord(true);
			} else if (trackRecorderService.isRecording() && trackRecorderService.isPaused()) {
				trackRecorderService.trackStop(this);
			}
			
		} else if (view.getId() == R.id.buttonDetails) {
			
			    Intent intent = new Intent(this, MainActivity.class);
			    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    startActivity(intent);
			
		}
		
		buttonsArrange();
		
 	}

	
	public void showSettings(View view) {
		
		   //LayoutInflater layoutInflater  = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		   
		   View popupView = getLayoutInflater().inflate(R.layout.popup_settings, null);
		   
		   final PopupWindow popupWindow = new PopupWindow(
		               popupView, 
		               LayoutParams.WRAP_CONTENT,  
		                     LayoutParams.WRAP_CONTENT, true);  
		   
		   popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
		   //popupWindow.showAsDropDown(findViewById(R.id.imageButton1));
		   
		   ImageButton btnDismiss = (ImageButton)popupView.findViewById(R.id.buttonBack);
		   
		   btnDismiss.setOnClickListener(new ImageButton.OnClickListener() {
			   public void onClick(View v) {    
				   popupWindow.dismiss();
				  }
		   }
		 );
		               
		   
		         
		   }
	
	@Override
	public void askLocation() {
		if (trackRecorderService != null ) {
			this.location = trackRecorderService.getLocation();
			this.newLocation(this.location, this.listenerType, this.accuracy);
		}
	}
	
	
	
}
