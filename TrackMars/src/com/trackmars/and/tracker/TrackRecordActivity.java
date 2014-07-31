package com.trackmars.and.tracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.location.Location;
//import com.google.android.gms.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.LayoutParams;
import android.text.Html;

import com.trackmars.and.tracker.dataUtils.DateUtils;
import com.trackmars.and.tracker.dataUtils.EntityHelper;
import com.trackmars.and.tracker.dataUtils.IEntity;
import com.trackmars.and.tracker.model.Point;
import com.trackmars.and.tracker.utils.ILocationReceiver;
import com.trackmars.and.tracker.utils.LocationUtils;
import com.trackmars.and.tracker.utils.RepresentationUtils;
import com.trackmars.and.tracker.utils.Tools;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.util.Log;

public class TrackRecordActivity extends FragmentActivity implements ILocationReceiver {
	
    private Location location;
    private Class listenerType;
    private float accuracy;
    private TrackRecorderReceiver trackRecorderReceiver = new TrackRecorderReceiver();
    private TrackRecorderService trackRecorderService;
    
    private Boolean listCreated = false;
    
      
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
          // Inflate the menu; this adds items to the action bar if it is present.
          getMenuInflater().inflate(R.menu.main, menu);
          return true;
    }
      

    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_record);
        
		MyTask mt = new MyTask();
	    mt.execute();		

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

    
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
        	trackRecorderService = ((TrackRecorderService.ManagerBinder) binder).getMe();
            buttonsArrange();
            
            trackRecorderService.resume();
            TrackRecordActivity.this.newLocation(trackRecorderService.getLocation(), 
            		trackRecorderService.getLastPointProvider(), 
            		0f);
            
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
      
		if (trackRecorderService != null) {

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
        	
	        Integer hours = (int) (totalTime / DateUtils.MILLISECONDS_IN_HOUR);
	        final Integer minutes = (int)((totalTime - hours * DateUtils.MILLISECONDS_IN_HOUR) / DateUtils.MILLISECONDS_IN_MINUTE);
	        
	        String fieldText = new String();
	        
	        if (hours > 23) {
	        	int dayCount = (int) Math.floor(hours / 24);
	        	int dayCountType = dayCount;
	        	String dayWord = new String();
	        	
	        	if (dayCount > 10) {
	        		dayCountType = dayCount % 10;
	        	}
	        	
	        	if (dayCountType == 1) {
	        		dayWord = res.getString(R.string.day1);
	        	} else if (dayCountType > 1 && dayCountType < 5) {
	        		dayWord = res.getString(R.string.day2_4);
	        	} else {
	        		dayWord = res.getString(R.string.day5_0);
	        	}
	        	
	        	hours = hours % 24;
	        	
	        	
	        	fieldText = "<big><big><b>" + String.valueOf(dayCount) + "</b></big></big>";
	        	fieldText += "<small>" + dayWord + ":</small>";
	        	fieldText += "<big><big><b>" + hours.toString() + "</b></big></big>";
	        	fieldText += "<small>" + res.getString(R.string.hour) + ":</small>";
	        	fieldText += "  <big><big><b>" + minutes.toString() + "</b></big></big>";
	        	fieldText += "<small>" + res.getString(R.string.minute) + "</small>";
	        } else {
	        	fieldText = "<big><big><b>" + hours.toString() + "</b></big></big>";
	        	fieldText += "<small>" + res.getString(R.string.hour) + ":</small>";
	        	fieldText += "  <big><big><b>" + minutes.toString() + "</b></big></big>";
	        	fieldText += "<small>" + res.getString(R.string.minute) + "</small>";
	        }
	        
	        ((TextView)findViewById(R.id.startDate)).setText(DateUtils.getDateVisualRepresentaion(this.trackRecorderService.getTrackCreatedTime(), this));
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
        
        if (!listCreated) {
        	createList();
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
	
	@Override
	public void askLocation() {
		if (trackRecorderService != null ) {
			this.location = trackRecorderService.getLocation();
			this.newLocation(this.location, this.listenerType, this.accuracy);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	/// 	дальше методы дла рисованиЯ точек в списке ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	
	class MyTask extends AsyncTask<Void, Void, Void> {

	    @Override
	    protected void onPreExecute() {
	      super.onPreExecute();
	      //tvInfo.setText("Begin");
	    }

	    @Override
	    protected Void doInBackground(Void... params) {
	      //TimeUnit.SECONDS.sleep(2);
		  createList();
	      return null;
	    }

	    @Override
	    protected void onPostExecute(Void result) {
	      super.onPostExecute(result);
	      //tvInfo.setText("End");
	    }
	  }	
	
	private boolean anyItemFound = false;
	
	private void createList() {
		
		EntityHelper entityHelper;
		
		if (trackRecorderService == null) {
			return;
		}
		
		try {
			entityHelper = new EntityHelper(getApplicationContext(), Point.class);
			
			try {
				
				List<IEntity> historyRecords = entityHelper.getAllRowsWhere("COLUMN_ID_TRACK", 
						Integer.valueOf(trackRecorderService.getCurrentRecordingTrackId()).toString(), 
						0, null, "COLUMN_CREATED DESC");

				
				/////////////////////
				for (final IEntity entityRow : historyRecords) {

			        Message msg = new Message();
			        msg.obj = entityRow;
			        
			        handler.sendMessage(msg);
			        
			        anyItemFound = true;
				}
				
		        Message msg = new Message();
		        msg.obj = Integer.valueOf(0);
		        handler.sendMessage(msg);
				
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	      
		listCreated = true;
	}
	
	public void selectPoint(View v, Point point) {
		
	      Intent intent = new Intent(this, PointViewActivity.class);
	      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	      
	      intent.putExtra("geocode", point.COLUMN_GEOCODE);
	      intent.putExtra("id", point.COLUMN_ID);
	      startActivity(intent);
	      
	}
	

	Handler handler = new Handler() {
		
		int month = 0;
		int year = 0;
		String monthName;
		String geo = new String("");
		
		List<Point> histories = new ArrayList<Point>();
		
		public void showUnknownPlaces (TableLayout tableLayout) {
			
			TableRow tRMonth;
			
			tRMonth = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_geo, null);

			((TextView)tRMonth.findViewById(R.id.nameOfTheGeo)).setText(getResources().getString(R.string.unknown_place));

			tableLayout.addView(tRMonth);

			
			for (final Point historyItem : histories) {
        		
	        	TableRow tR = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_point, null);
		        tR.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						selectPoint(v, historyItem);
					}
				});
		        
		        
				String title = historyItem.COLUMN_ID.toString() + " id_point-" + historyItem.COLUMN_ID.toString() + " " +  historyItem.COLUMN_TITLE;
				Integer kind = historyItem.COLUMN_KIND;
			
				if (kind == null || kind.equals(0)) {
					((ImageView)tR.findViewById(R.id.buttonTrack)).setImageBitmap(null);
				} else {
					if (kind.equals(RepresentationUtils.KindOfPoint.ATTRACTIVE.val())) {
						((ImageView)tR.findViewById(R.id.buttonTrack)).setImageResource(R.drawable.sm_attractive);
					} else if (kind.equals(RepresentationUtils.KindOfPoint.FOOD.val())) {
						((ImageView)tR.findViewById(R.id.buttonTrack)).setImageResource(R.drawable.sm_food);
					} else if (kind.equals(RepresentationUtils.KindOfPoint.NIGHT.val())) {
						((ImageView)tR.findViewById(R.id.buttonTrack)).setImageResource(R.drawable.sm_night);
					} else if (kind.equals(RepresentationUtils.KindOfPoint.NOTE.val())) {
						((ImageView)tR.findViewById(R.id.buttonTrack)).setImageResource(R.drawable.sm_note);
					}
				}
				
				TextView textView = (TextView) tR.findViewById(R.id.nameOfThePoint);
				
				if (title != null) {
					textView.setText(title);
				}

		        tableLayout.addView(tR);
        		
        	}
        	
        	histories.clear();
			
		}
		
		
		@Override
		public void handleMessage(Message msg) {
			
			TableLayout tableLayout = (TableLayout) findViewById(R.id.listTable);
			
			if ((msg.obj).equals(Integer.valueOf(0))) {
		        
		        if (histories != null && histories.size() > 0) {
		        		showUnknownPlaces(tableLayout);
		        }
		        
		        return;
			}
    
			final Object entityRow = msg.obj;
			
						
			final String title = ((Point) entityRow).COLUMN_TITLE;
			final Long created = ((Point) entityRow).COLUMN_CREATED;
			final Integer kind = ((Point) entityRow).COLUMN_KIND;
			final String geoCode = ((Point) entityRow).COLUMN_GEOCODE;
			final Integer id_point = ((Point) entityRow).COLUMN_ID;

			TableRow tRMonth;

			if ((year != DateUtils.getYearByDateLong(created))
					|| (month != DateUtils.getMonthByDateLong(created))) {

				if (year != 0) {
					
					showUnknownPlaces(tableLayout);
				}


				year = DateUtils.getYearByDateLong(created);
				month = DateUtils.getMonthByDateLong(created);
				monthName = DateUtils.getMonthNameByDateLong(created);

				tRMonth = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_month, null);

				String mnTitle = monthName.concat(", ").concat(Integer.valueOf(year).toString());
				((TextView)tRMonth.findViewById(R.id.nameOfTheMonth)).setText(mnTitle);
				

				tableLayout.addView(tRMonth);

			}

			if (geoCode != null && !geo.equals(geoCode)) {
				geo = new String(geoCode);
				
				tRMonth = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_geo, null);

				((TextView)tRMonth.findViewById(R.id.nameOfTheGeo)).setText(geo);

				tableLayout.addView(tRMonth);
			}

			if (geoCode != null && !geoCode.equals("")) {
					
		        	TableRow tR = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_point, null);
			        tR.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							selectPoint(v, (Point) entityRow);
						}
					});
			        
			        
					String titleToShow = " id_point-" + id_point.toString() + " " +  title;
				
					if (kind == null || kind.equals(0)) {
						((ImageButton)tR.findViewById(R.id.buttonTrack)).setImageBitmap(null);
					} else {
						if (kind.equals(RepresentationUtils.KindOfPoint.ATTRACTIVE.val())) {
							((ImageButton)tR.findViewById(R.id.buttonTrack)).setImageResource(R.drawable.sm_attractive);
						} else if (kind.equals(RepresentationUtils.KindOfPoint.FOOD.val())) {
							((ImageButton)tR.findViewById(R.id.buttonTrack)).setImageResource(R.drawable.sm_food);
						} else if (kind.equals(RepresentationUtils.KindOfPoint.NIGHT.val())) {
							((ImageButton)tR.findViewById(R.id.buttonTrack)).setImageResource(R.drawable.sm_night);
						} else if (kind.equals(RepresentationUtils.KindOfPoint.NOTE.val())) {
							((ImageButton)tR.findViewById(R.id.buttonTrack)).setImageResource(R.drawable.sm_note);
						}
					}
					
					TextView textView = (TextView) tR.findViewById(R.id.nameOfThePoint);
					
					if (titleToShow != null) {
						textView.setText(titleToShow);
					}

					//textView.invalidate();
					//tR.invalidate();
			        tableLayout.addView(tR);
			        tableLayout.invalidate();
			        textView.invalidate();
					
			} else {
					histories.add((Point) entityRow);
			}
			
		}
		
	};
	
	//////////////////////////////////////////////////////////////////////////////
	////// Interval settings
	//////////////////////////////////////////////////////////////////////////////

    // spinner to interval select
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
