package com.trackmars.and.tracker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.internal.IPolylineDelegate;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.Html;

import com.google.android.gms.maps.SupportMapFragment;
import com.trackmars.and.tracker.dataUtils.DateUtils;
import com.trackmars.and.tracker.dataUtils.EntityHelper;
import com.trackmars.and.tracker.dataUtils.IEntity;
import com.trackmars.and.tracker.dataUtils.XMLFile;
import com.trackmars.and.tracker.model.Point;
import com.trackmars.and.tracker.model.Track;
import com.trackmars.and.tracker.model.TrackPointData;
import com.trackmars.and.tracker.utils.LocationUtils;
import com.trackmars.and.tracker.utils.Tools;
import com.trackmars.and.tracker.utils.RepresentationUtils;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

public class TrackViewActivity extends FragmentActivity {
	
	private GoogleMap map;
	Integer id;
	Long created;
	String title;
    private TrackRecorderService trackRecorderService;
    private Rectangle shapeRectangle;

	EntityHelper entityHelper;
	Track track;
    
	//final private float DEFAULT_ACCURACY = 30;
    
	class MyTask extends AsyncTask<Void, Void, Void> {

	    @Override
	    protected void onPreExecute() {
	      super.onPreExecute();
	      //tvInfo.setText("Begin");
	    }

	    @Override
	    protected Void doInBackground(Void... params) {
	      //TimeUnit.SECONDS.sleep(2);
	      setLocation();
	      return null;
	    }

	    @Override
	    protected void onPostExecute(Void result) {
	      super.onPostExecute(result);
	      //tvInfo.setText("End");
	    }
	  }	
	
	
	SupportMapFragment mapFragment;
	
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_track);

        
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        
        
		Bundle extras = getIntent().getExtras();
		id = extras.getInt("id");
		created = extras.getLong("created");
		title = extras.getString("title");

        try {
        	mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
	        map = mapFragment.getMap();
	        
        } finally {}
        
        
        
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ListTracksItemTrack listTracksItemTrack = new ListTracksItemTrack();
        
        Bundle args = new Bundle();
        
        args.putString("title", title);
        args.putLong("created", created);
        args.putInt("id", id);
        
        listTracksItemTrack.setArguments(args);
     
        ft.replace(R.id.intoFrame, listTracksItemTrack);
        ft.commit();         
        
    }
    
    
    private Rectangle getRectangle(Track track) {
    	Rectangle rectangle = new Rectangle();
    	
    	if (track.LEFT != null && track.RIGHT != null && track.TOP != null && track.BOTTOM != null) {
    		rectangle.create(track.LEFT, track.RIGHT, track.TOP, track.BOTTOM);
    	} else {
    		rectangle = null;
    	}
    	
    	return rectangle;
    }
    
    
    private void setLocation() {
    	try {
    		
    		entityHelper = new EntityHelper(getApplicationContext(), Track.class);
    		track = (Track) entityHelper.getRow(id);
    		
    		this.shapeRectangle = getRectangle(track);
    		
			List<TrackPointData> latLngs = trackRecorderService.getAllTrackPoint(id);

			EntityHelper entityHelperPoint = new EntityHelper(getApplicationContext(), Point.class);
			List<IEntity> points = entityHelperPoint.getAllRowsWhere("COLUMN_ID_TRACK", track.ID.toString(), 0, null, null);
			
			TrackWithPoinsToShow trackWithPoinsToShow = new TrackWithPoinsToShow();
			trackWithPoinsToShow.setPoints(points);
			trackWithPoinsToShow.setTrackPoints(latLngs);
			
	        Message msg = new Message();
	        msg.obj = trackWithPoinsToShow;
	        
	        handler.sendMessage(msg);
	        
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
        	trackRecorderService = ((TrackRecorderService.ManagerBinder) binder).getMe();
    		MyTask mt = new MyTask();
    	    mt.execute();		
        }

        public void onServiceDisconnected(ComponentName className) {
        }
    };    
    
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
		    try {
		    	unbindService(mConnection);
		    } catch (Exception e) {
		    	
		    }
			
			if (map != null) {
			
	    		Toast.makeText(TrackViewActivity.this, "Start thread...", Toast.LENGTH_LONG).show();
	    		
				final TrackWithPoinsToShow trackWithPoinsToShow = (TrackWithPoinsToShow) msg.obj;
				final List<TrackPointData> latLngs = trackWithPoinsToShow.getTrackPoints();
				final List<IEntity> points = trackWithPoinsToShow.getPoints();
				
				Boolean updateShape = TrackViewActivity.this.shapeRectangle == null;
				
				if (updateShape) {
					TrackViewActivity.this.shapeRectangle = new Rectangle();
				}
	
		    	PolylineOptions polylineOptions = new PolylineOptions();
		    	polylineOptions.geodesic(true);
		    	
		    	boolean startOfTrck = true;
		    	boolean startOfSeg = false;
		    	

	    		Toast.makeText(TrackViewActivity.this, "Start for...", Toast.LENGTH_LONG).show();
		    	
		    	for (TrackPointData latLng : latLngs) {
		    		
		    		if (startOfTrck) {
		    			map.addMarker(new MarkerOptions().anchor(0.5f, 0.5f).
		    					position(new LatLng(latLng.LAT, latLng.LNG)).
		    					icon(BitmapDescriptorFactory.fromResource(R.drawable.segment_end)));
		    			
		    			startOfTrck = false;
		    			
		    		}
	
		    		if (startOfSeg) {
		    			map.addMarker(new MarkerOptions().anchor(0.5f, 0.5f).
		    					position(new LatLng(latLng.LAT, latLng.LNG)).
		    					icon(BitmapDescriptorFactory.fromResource(R.drawable.segment_sub_start)));
		    			
		    			startOfSeg = false;
		    			
		    		}
		    		
		    		
		    		polylineOptions.add(new LatLng(latLng.LAT, latLng.LNG));
			    	polylineOptions.geodesic(true).color(0x400000ff);
		    		
			    	polylineOptions.width(LocationUtils.DEFAULT_ACCURACY);
		    		
		    		if (latLng.paused != null && latLng.paused) {
		    	    	if (map != null) {
		    		    	map.addPolyline(polylineOptions);
		    		    	polylineOptions = null;
		    		    	polylineOptions = new PolylineOptions();
		    		    	
			    			map.addMarker(new MarkerOptions().anchor(0.5f, 0.5f).
			    					position(new LatLng(latLng.LAT, latLng.LNG)).
			    					icon(BitmapDescriptorFactory.fromResource(R.drawable.segment_pause)));
			    			startOfSeg = true;
		    	    	}
		    		}
		    		
		    		if (updateShape) {
		    			TrackViewActivity.this.shapeRectangle.shape(new LatLng(latLng.LAT, latLng.LNG));
		    		}
		    		
		    	}
	
	    		Toast.makeText(TrackViewActivity.this, "End for...", Toast.LENGTH_LONG).show();
		    	
		    	
		    	if (map != null) {
			    	map.addPolyline(polylineOptions);
		    	}
				
		    	if (updateShape) {
		    		track.TOP = shapeRectangle.getTop();
		    		track.BOTTOM = shapeRectangle.getBottom();
		    		track.LEFT = shapeRectangle.getLeft();
		    		track.RIGHT = shapeRectangle.getRight();
		    		entityHelper.save(track);
		    	}
		    	
		    	if (map != null) {
			    	for(IEntity point: points) {
			    		RepresentationUtils.showPoint(map, (Point) point);
			    	}
		    	}
				
		    	int zoom;
		    	if (track.BOTTOM != null && track.LEFT != null) {
			    	double distHorisontal = LocationUtils.distFrom(track.BOTTOM, track.LEFT, track.BOTTOM, track.RIGHT);
			    	double distVertical = LocationUtils.distFrom(track.TOP , 0, track.BOTTOM, 0);
			    	zoom = RepresentationUtils.getZoom(distHorisontal>distVertical?distHorisontal:distVertical);
		    	} else {
		    		zoom = 16;
		    	}
		    	
		    	if (map != null && track != null && track.TOP != null && track.BOTTOM != null && track.LEFT != null && track.RIGHT != null) {
		        	LatLng myCurrentPosition = new LatLng((track.TOP + track.BOTTOM) / 2, (track.LEFT + track.RIGHT) / 2); 
		            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myCurrentPosition, zoom));
		    	}
	    	
			}
	    	
	        // заполним детали трека
	        final Long travelTime = track.TRAVEL_TIME;
	        final Double distance = track.DISTANCE;
	        
	        final Long totalTime = track.FINISHED != null?(track.FINISHED - track.CREATED):
	        	new Date().getTime() - track.CREATED;
	        
	        Resources res = getResources();
	        
	        if (track.CREATED != null) {
		        
		        ((TextView)findViewById(R.id.startDate)).setText(DateUtils.getDateVisualRepresentaion(track.CREATED, TrackViewActivity.this));
		        
		        ((TextView)findViewById(R.id.time)).setText(Html.fromHtml(
		        		RepresentationUtils.getDurationHTMLView(totalTime, res)
		        ));
	        
	        }
	        
	        if (travelTime != null) {
	        	
		        ((TextView)findViewById(R.id.in_motion)).setText(Html.fromHtml(
		        		RepresentationUtils.getDurationHTMLView(travelTime, res)
		        ));
	        
	        }
	        
	        if (distance != null) {
		        
		        ((TextView)findViewById(R.id.distance)).setText(Html.fromHtml(
		        		RepresentationUtils.getDistanceHTMLView(distance, res)
		        ));
	        
	        }
	        
	        ((TextView)findViewById(R.id.avg_speed)).setText(Html.fromHtml(
	        		RepresentationUtils.getSpeedHTMLView(distance, travelTime, res)
	        ));
			
			// Установим размер карты
	        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
	        
	        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
	        params.height = (int)(TrackViewActivity.this.getWindow().getDecorView().getHeight() / 100 * 30);
	        mapFragment.getView().setLayoutParams(params);
			
	        
	        // выгрузка .gpx файла на карту памяти
	        // временный код
	        /*
	        XMLFile xmlFile = new XMLFile(TrackViewActivity.this.getApplicationContext());	  
	        try {
				xmlFile.addTrack(track);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
			try {
				xmlFile.serialize(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
	    	
	    	
		}
	};		
    
    @Override
    protected void onResume() {
        super.onResume();
        
        Intent intent = new Intent(this, TrackRecorderService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    	
    }
    
    boolean expanded = false;
    
	public void onClick(View view) {
		if (view.getId() == R.id.expandMap) {
			
	        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
	        
			ImageButton antennaIndicator = (ImageButton) this.findViewById(R.id.expandMap);
			
			if (!expanded) {
		        params.height = (int)(TrackViewActivity.this.getWindow().getDecorView().getHeight() / 100 * 90);
	        	antennaIndicator.setImageResource(R.drawable.collapce);
			} else {
		        params.height = (int)(TrackViewActivity.this.getWindow().getDecorView().getHeight() / 100 * 30);
	        	antennaIndicator.setImageResource(R.drawable.expand);
			}
			
			expanded = !expanded;
			
	        mapFragment.getView().setLayoutParams(params);
		}
	}

	
}
