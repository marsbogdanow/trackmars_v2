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
import android.support.v4.app.FragmentTransaction;

import com.google.android.gms.maps.SupportMapFragment;
import com.trackmars.and.tracker.model.Point;
import com.trackmars.and.tracker.utils.ILocationReceiver;
import com.trackmars.and.tracker.utils.LocationUtils;

import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

public class PointViewActivity extends FragmentActivity {
	
	private GoogleMap map;
	Double longitude;
	Double latitude;
	Integer id;
	Long created;
	String title;
      
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_point);

		Bundle extras = getIntent().getExtras();
		longitude = extras.getDouble("lng");
		latitude = extras.getDouble("lat");
		id = extras.getInt("id");
		created = extras.getLong("created");
		title = extras.getString("title");

        try {
	        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
	                .getMap();
	        
	        setLocation();
	        
        } finally {}
        
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.intoFrame);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ListPointsItemPoint listPointsItemPoint = new ListPointsItemPoint();
        
        Bundle args = new Bundle();
        
        args.putString("title", title);
        args.putDouble("lng", longitude);
        args.putDouble("lat", latitude);
        args.putLong("created", created);
        args.putInt("id", id);
        
        listPointsItemPoint.setArguments(args);
     
        ft.replace(R.id.intoFrame, listPointsItemPoint);
        ft.commit();         
        
    }
    
    private void setLocation() {
    	
        Marker myCurrentPositionMarker;
        
        if (map != null) {
            	
            	LatLng myCurrentPosition = new LatLng(latitude, longitude); 
            	
            	myCurrentPositionMarker = map.addMarker(new MarkerOptions().position(myCurrentPosition)
                .title(title));
            	
	            
		        map.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
	            map.moveCamera(CameraUpdateFactory.newLatLng(myCurrentPosition));
        
        }    	
    }

    
	public void onClick(View view) {
 	}

	
}
