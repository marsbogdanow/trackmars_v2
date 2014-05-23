package com.trackmars.and.tracker;

import java.util.ArrayList;
import java.util.List;

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

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

public class PointsActivity extends FragmentActivity {

	private int rowId = 0;
	
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
				}
			
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
	
	private TableRow createTableRow() {
		
		TableRow tR = new TableRow(this);
        tR.setPadding(0,0,0,0);
        tR.setClickable(true);
        
        TableRow.LayoutParams flp1 = new TableRow.LayoutParams(
        		TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        tR.setLayoutParams(flp1);
        
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setId(++rowId);
        
        tR.addView(frameLayout);
        
        return tR;
	}
	
	Handler handler = new Handler() {
		
		int month = 0;
		int year = 0;
		String monthName;
		String geo = new String("");
		
		List<History> histories = new ArrayList<History>();
		
		@Override
		public void handleMessage(Message msg) {
    
			final Object entityRow = msg.obj;
			TableLayout tableLayout = (TableLayout) findViewById(R.id.listTable);
			
			/////////////////
						
				        final String title = ((History)entityRow).TITLE;
				        final Long created = ((History)entityRow).CREATED;
				        final Integer kind = ((History)entityRow).KIND;
				        final String geoCode = ((History)entityRow).GEOCODE;
				        final boolean isTrack = ((History)entityRow).ID_TRACK != null && !((History)entityRow).ID_TRACK.equals(0);
				        final boolean isPoint = ((History)entityRow).ID_POINT != null && !((History)entityRow).ID_POINT.equals(0);
				        final Integer id = ((History)entityRow).ID;
				        final Integer id_track = ((History)entityRow).ID_TRACK;
				        final Integer id_point = ((History)entityRow).ID_POINT;
				        
				        TableRow tRMonth;
				        Bundle args;
				        FragmentTransaction ftmonth;
				        
				        if ((year != DateUtils.getYearByDateLong(created)) || (month != DateUtils.getMonthByDateLong(created))) {
				        	
					        if (year != 0) {
					        	tRMonth = createTableRow();
		
						        // заталкиваем фрагмент в строку таблицы
						        ftmonth = getSupportFragmentManager().beginTransaction();
						        ActivitiesListGeo listPointsItemGeo = new ActivitiesListGeo();
						        args = new Bundle();
						        
						        args.putString("geo", getResources().getString(R.string.unknown_place));
						        
						        listPointsItemGeo.setArguments(args);
						        ftmonth.replace(rowId, listPointsItemGeo);
						        ftmonth.commit();         
		
						        tableLayout.addView(tRMonth);
					        }
					        
				        	for (final History historyItem : histories) {
					        	TableRow tR = createTableRow();
						        tR.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										selectPoint(v, historyItem);
									}
								});
						        
						        // заталкиваем фрагмент в строку таблицы
						        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
						        ActivitiesListPoint listPointsItemPoint = new ActivitiesListPoint();
						        args = new Bundle();
						        
						        args.putString("title", historyItem.ID.toString() + " id_point-" + historyItem.ID_POINT.toString() + " " +  historyItem.TITLE);
						        args.putLong("created", historyItem.CREATED);
						        args.putInt("kind", historyItem.KIND);
						        
						        listPointsItemPoint.setArguments(args);
						        ft.replace(rowId, listPointsItemPoint);
						        ft.commit();         
			
						        tableLayout.addView(tR);
				        		
				        	}
				        	
				        	histories.clear();
				        	
				        	year = DateUtils.getYearByDateLong(created);
				        	month = DateUtils.getMonthByDateLong(created);
				        	monthName = DateUtils.getMonthNameByDateLong(created);
				        	
					        tRMonth = createTableRow();
	
					        // заталкиваем фрагмент в строку таблицы
					        ftmonth = getSupportFragmentManager().beginTransaction();
					        ActivitiesListMonth listPointsItemMonth = new ActivitiesListMonth();
					        args = new Bundle();
					        
					        args.putString("month", monthName);
					        args.putInt("year", year);
					        
					        listPointsItemMonth.setArguments(args);
					        ftmonth.replace(rowId, listPointsItemMonth);
					        ftmonth.commit();         
	
					        tableLayout.addView(tRMonth);		
				        
				        }
				        
				        if (!isTrack && geoCode != null && !geo.equals(geoCode)) {
				        	geo = new String(geoCode);
					        TableRow tRMonth1 = createTableRow();
	
					        // заталкиваем фрагмент в строку таблицы
					        FragmentTransaction ftmonth1 = getSupportFragmentManager().beginTransaction();
					        ActivitiesListGeo listPointsItemGeo = new ActivitiesListGeo();
					        args = new Bundle();
					        
					        args.putString("geo", geo);
					        
					        listPointsItemGeo.setArguments(args);
					        ftmonth1.replace(rowId, listPointsItemGeo);
					        ftmonth1.commit();         
	
					        tableLayout.addView(tRMonth1);		
				        }
				        
				        if (isPoint) { // point
				        
					        if (geo != null && !geo.equals("")) {
					        	TableRow tR = createTableRow();
						        tR.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										selectPoint(v, (History)entityRow);
									}
								});
						        
						        // заталкиваем фрагмент в строку таблицы
						        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
						        ActivitiesListPoint listPointsItemPoint = new ActivitiesListPoint();
						        args = new Bundle();
						        
						        args.putString("title", id.toString() + " id_point-" + id_point.toString() + " " +  title);
						        args.putLong("created", created);
						        args.putInt("kind", kind);
						        
						        listPointsItemPoint.setArguments(args);
						        ft.replace(rowId, listPointsItemPoint);
						        ft.commit();         
			
						        tableLayout.addView(tR);
					        } else {
					        	histories.add((History) entityRow);
					        }
					        
				        }
				        
				        if (isTrack) {
					        
					        TableRow tR = createTableRow();
					        tR.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									selectTrack(v, (History)entityRow);
								}
							});
					        
					        // заталкиваем фрагмент в строку таблицы
					        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					        ActivitiesListTrack listPointsItemTrack = new ActivitiesListTrack();
					        args = new Bundle();
					        
					        args.putString("title", id.toString() + " id_track-" + id_track.toString() + " " +  title);
					        args.putLong("created", kind);
					        
					        listPointsItemTrack.setArguments(args);
					        ft.replace(rowId, listPointsItemTrack);
					        ft.commit();         
		
					        tableLayout.addView(tR);
					        
				        }
			
	        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		}
		
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_list);
		
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

        
		
		MyTask mt = new MyTask();
	    mt.execute();		
		
	}
	
}
