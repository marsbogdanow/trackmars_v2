package com.trackmars.and.tracker;

import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.MapFragment;

import com.google.android.gms.maps.model.PolylineOptions;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
//import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.trackmars.and.tracker.TrackViewActivity.MyTask;
import com.trackmars.and.tracker.dataUtils.EntityHelper;
import com.trackmars.and.tracker.dataUtils.IEntity;
import com.trackmars.and.tracker.model.Point;
import com.trackmars.and.tracker.model.Track;
import com.trackmars.and.tracker.model.TrackPointData;
import com.trackmars.and.tracker.utils.ILocationReceiver;
import com.trackmars.and.tracker.utils.LocationUtils;
import com.trackmars.and.tracker.utils.RepresentationUtils;

import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

public class PointViewActivity extends FragmentActivity {
	
	private GoogleMap map;
	//Double longitude;
	//Double latitude;
	Integer id;
	Point point;
	//Long created;
	//String title;

	class MyTask extends AsyncTask<Void, Void, Void> {

	    @Override
	    protected void onPreExecute() {
	      super.onPreExecute();
	      //tvInfo.setText("Begin");
	    }

	    @Override
	    protected Void doInBackground(Void... params) {
	      //TimeUnit.SECONDS.sleep(2);
	      try {
			setLocation();
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
	
	class GeocodingTask extends AsyncTask<Void, Void, Void> {

	    @Override
	    protected void onPreExecute() {
	      super.onPreExecute();
	    }

	    @Override
	    protected Void doInBackground(Void... params) {
	      LatLng latLng = new LatLng(point.COLUMN_LAT, point.COLUMN_LNG);
	      
	      if (point.COLUMN_GEOCODE == null) {
	    	  String addresses = RepresentationUtils.getGeoCodingInfo(latLng, PointViewActivity.this);
	    	  point.COLUMN_GEOCODE = addresses;
	    	  
	  		  try {
				EntityHelper entityHelper = new EntityHelper(getApplicationContext(), Point.class);
				entityHelper.save(point);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      }
	      
	      Message msg = new Message();
	      msg.obj = point;
	      geocodeHandler.sendMessage(msg);
	      
	      return null;
	    }

	    @Override
	    protected void onPostExecute(Void result) {
	      super.onPostExecute(result);
	      //tvInfo.setText("End");
	    }
	  }	
	
	
	
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_point);

		Bundle extras = getIntent().getExtras();
		//longitude = extras.getDouble("lng");
		//latitude = extras.getDouble("lat");
		id = extras.getInt("id");
		//created = extras.getLong("created");
		//title = extras.getString("title");

        try {
	        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
	                .getMap();
	        
    		MyTask mt = new MyTask();
    	    mt.execute();		
	        
        } finally {}
        
        
    }
  
    private Handler geocodeHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
	        //FrameLayout frameLayout = (FrameLayout) findViewById(R.id.intoFrame);
	        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	
	        ListPointsItemPoint listPointsItemPoint = new ListPointsItemPoint();
	        
	        Bundle args = new Bundle();
	        
	        args.putString("title", point.COLUMN_TITLE);
	        args.putDouble("lng", point.COLUMN_LNG);
	        args.putDouble("lat", point.COLUMN_LAT);
	        args.putLong("created", point.COLUMN_CREATED);
	        args.putString("geocode", point.COLUMN_GEOCODE);
	        args.putInt("id", id);
	        
	        listPointsItemPoint.setArguments(args);
	     
	        ft.replace(R.id.intoFrame, listPointsItemPoint);
	        ft.commit();         
			
		}
    };
    
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			point = (Point) msg.obj;
			
			if (map != null) {
            	
				RepresentationUtils.showPoint(map, point);
	            
	            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(point.COLUMN_LAT, point.COLUMN_LNG), 16), 2000, null);
        
	        }    	
	
			
			//if (point.COLUMN_GEOCODE == null) {
				// запуск задачи геокодинга
				GeocodingTask gt = new GeocodingTask();
				gt.execute();
			//}
			
		}
	};		
    
    
    private void setLocation() throws IllegalAccessException, InstantiationException {
    	
		EntityHelper entityHelper = new EntityHelper(getApplicationContext(), Point.class);
		//Point point = (Point) entityHelper.getRow(id);
		List<IEntity> points = entityHelper.getAllRowsWhere("COLUMN_ID", id.toString(), 0, null, null);
        
        for (IEntity pt : points) { // будет только одна этерациЯ
        	
        	Point point = (Point)pt;
        	
	        Message msg = new Message();
	        msg.obj = point;
	        
	        handler.sendMessage(msg);
        	
        	
        }
    
    
    }
    
	public void onClick(View view) {
 	}

	
}
