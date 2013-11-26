package com.trackmars.and.tracker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.MapFragment;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
//import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ILocationReceiver {
	
	private GoogleMap map;	
	private LocationUtils locationUtils;
	private Boolean mapPositioned = false;
    Marker myCurrentPositionMarker;
    private Location location;
      
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
          // Inflate the menu; this adds items to the action bar if it is present.
          getMenuInflater().inflate(R.menu.main, menu);
          return true;
    }
      
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        updateServices();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        

        try {
	        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
	                .getMap();
        } finally {}
        
        locationUtils = new LocationUtils(this, this);
        
    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
      super.onResume();
      
	  Toast.makeText(this, "onResume",
		        Toast.LENGTH_SHORT).show();
      mapPositioned = false;
	  locationUtils.onResume();
	    
    }

    @Override
    protected void onPause() {
      super.onPause();
      locationUtils.onPause();
    }

	@Override
	public void newLocation(Location location) {
        this.location = location;
        if (map != null) {
            	
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
	}
	
    
    
    
	public void onClick(View view) {
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
