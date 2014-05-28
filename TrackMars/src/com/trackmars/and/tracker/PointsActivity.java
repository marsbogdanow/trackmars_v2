package com.trackmars.and.tracker;

import java.util.ArrayList;
import java.util.List;

import ru.elifantiev.android.roboerrorreporter.Logger;

import com.trackmars.and.tracker.TracksActivity.MyTask;
import com.trackmars.and.tracker.actListItems.ActivitiesListGeo;
import com.trackmars.and.tracker.actListItems.ActivitiesListMonth;
import com.trackmars.and.tracker.actListItems.ActivitiesListPoint;
import com.trackmars.and.tracker.actListItems.ActivitiesListTrack;
import com.trackmars.and.tracker.dataUtils.DateUtils;
import com.trackmars.and.tracker.dataUtils.EntityHelper;
import com.trackmars.and.tracker.dataUtils.IEntity;
import com.trackmars.and.tracker.model.History;
import com.trackmars.and.tracker.model.Point;
import com.trackmars.and.tracker.model.Track;
import com.trackmars.and.tracker.utils.LocationUtils;
import com.trackmars.and.tracker.utils.RepresentationUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PointsActivity extends FragmentActivity {

	private int rowId = 0;
	private boolean anyItemFound = false;
	
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
	
	private void createList() {
		
		EntityHelper entityHelper;
		try {
			entityHelper = new EntityHelper(getApplicationContext(), History.class);
		
			try {
				List<IEntity> historyRecords = entityHelper.getAllRows(0, null, "created DESC");

				
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
	      
	}
	
	public void selectPoint(View v, History point) {
		
	      Intent intent = new Intent(this, PointViewActivity.class);
	      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	      
	      intent.putExtra("geocode", point.GEOCODE);
	      intent.putExtra("id", point.ID_POINT);
	      
	      startActivity(intent);
	      
	}

	public void selectTrack(View v, History track) {
		
	      Intent intent = new Intent(this, TrackViewActivity.class);
	      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	      
	      intent.putExtra("title", track.TITLE);
	      intent.putExtra("created", track.CREATED);
	      intent.putExtra("id", track.ID_TRACK);
	      
	      startActivity(intent);
	      
	}
	
	
	Handler handler = new Handler() {
		
		int month = 0;
		int year = 0;
		String monthName;
		String geo = new String("");
		
		List<History> histories = new ArrayList<History>();
		
		private void showUnknownPlaces (TableLayout tableLayout) {
			
			Bundle args;
			TableRow tRMonth;
			FragmentTransaction ftmonth;
			
			tRMonth = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_geo, null);

			((TextView)tRMonth.findViewById(R.id.nameOfTheGeo)).setText(getResources().getString(R.string.unknown_place));

			tableLayout.addView(tRMonth);

			
			for (final History historyItem : histories) {
        		
	        	TableRow tR = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_point, null);
		        tR.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						selectPoint(v, historyItem);
					}
				});
		        
		        
				String title = historyItem.ID.toString() + " id_point-" + historyItem.ID_POINT.toString() + " " +  historyItem.TITLE;
				Integer kind = historyItem.KIND;
			
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
		        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		        
		        if (!anyItemFound) {
			        findViewById(R.id.no_points).setVisibility(View.VISIBLE);
		        } else {
		        	if (histories != null && histories.size() > 0) {
		        		showUnknownPlaces(tableLayout);
		        	}
		        }
		        
		        return;
			}
    
			final Object entityRow = msg.obj;
			
						
			final String title = ((History) entityRow).TITLE;
			final Long created = ((History) entityRow).CREATED;
			final Integer kind = ((History) entityRow).KIND;
			final String geoCode = ((History) entityRow).GEOCODE;
			final boolean isTrack = ((History) entityRow).ID_TRACK != null
					&& !((History) entityRow).ID_TRACK.equals(0);
			final boolean isPoint = ((History) entityRow).ID_POINT != null
					&& !((History) entityRow).ID_POINT.equals(0);
			final Integer id = ((History) entityRow).ID;
			final Integer id_track = ((History) entityRow).ID_TRACK;
			final Integer id_point = ((History) entityRow).ID_POINT;

			TableRow tRMonth;
			Bundle args;
			FragmentTransaction ftmonth;

			if ((year != DateUtils.getYearByDateLong(created))
					|| (month != DateUtils.getMonthByDateLong(created))) {

				if (year != 0) {
					
					showUnknownPlaces(tableLayout);
				}


				year = DateUtils.getYearByDateLong(created);
				month = DateUtils.getMonthByDateLong(created);
				monthName = DateUtils.getMonthNameByDateLong(created);

				tRMonth = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_month, null);

				String mnTitle = monthName.concat(", ").concat(new Integer(year).toString());
				((TextView)tRMonth.findViewById(R.id.nameOfTheMonth)).setText(mnTitle);
				
				// заталкиваем фрагмент в строку таблицы
				//ftmonth = getSupportFragmentManager().beginTransaction();
				//ActivitiesListMonth listPointsItemMonth = new ActivitiesListMonth();
				//args = new Bundle();

				//args.putString("month", monthName);
				//args.putInt("year", year);

				//listPointsItemMonth.setArguments(args);
				//ftmonth.replace(rowId, listPointsItemMonth);
				//ftmonth.commit();

				tableLayout.addView(tRMonth);

			}

			if (!isTrack && geoCode != null && !geo.equals(geoCode)) {
				geo = new String(geoCode);
				
				tRMonth = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_geo, null);

				((TextView)tRMonth.findViewById(R.id.nameOfTheGeo)).setText(geo);

				tableLayout.addView(tRMonth);
			}

			if (isPoint) { // point

				if (geoCode != null && !geoCode.equals("")) {
					
		        	TableRow tR = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_point, null);
			        tR.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							selectPoint(v, (History) entityRow);
						}
					});
			        
			        
					String titleToShow = id.toString() + " id_point-" + id_point.toString() + " " +  title;
				
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
					histories.add((History) entityRow);
				}

			}

			if (isTrack) {

	        	TableRow tR = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_track, null);
				tR.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						selectTrack(v, (History) entityRow);
					}
				});
				

				TextView textView = (TextView) tR.findViewById(R.id.nameOfTheTrack);
				
				textView.setText(id.toString() + " id_track-" + id_track.toString()
							+ " " + title);
				
				tableLayout.addView(tR);

			}
			
		}
		
	};

	int parentWidth;

    @Override
    protected void onResume() {
      super.onResume();
      
    }
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_list);
		
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        
		MyTask mt = new MyTask();
	    mt.execute();		
		
	}
	
}
