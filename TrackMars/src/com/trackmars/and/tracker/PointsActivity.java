package com.trackmars.and.tracker;

import java.util.ArrayList;
import java.util.List;

import com.trackmars.and.tracker.dataUtils.DateUtils;
import com.trackmars.and.tracker.dataUtils.EntityHelper;
import com.trackmars.and.tracker.dataUtils.IEntity;
import com.trackmars.and.tracker.model.History;
import com.trackmars.and.tracker.utils.RepresentationUtils;

import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PointsActivity extends FragmentActivity {

	//private int rowId = 0;
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
	

	public void selectTrackById(View v, int trackId) {
		
	      Intent intent = new Intent(this, TrackViewActivity.class);
	      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	      
	      intent.putExtra("id", trackId);
	      
	      startActivity(intent);
	      
	}
	
	
	public void selectTrackById(View v, History track) {
		
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
		
		private void showTrackPlaces (TableLayout tableLayout) {
			
			
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
					//((ImageButton)tR.findViewById(R.id.buttonTrack)).setImageBitmap(null);
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
		
		
		private boolean prevItemIsPointOfTrack = false;
		private boolean prevItemIstrack = false;
		private ImageView trackBodyToRemove;
		private ImageView trackBodyToRemove1;
		private RelativeLayout boxToRemove;
		
		
		@Override
		public void handleMessage(Message msg) {
			
			TableLayout tableLayout = (TableLayout) findViewById(R.id.listTable);
			
			if ((msg.obj).equals(Integer.valueOf(0))) {
		        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		        
		        if (!anyItemFound) {
			        findViewById(R.id.no_points).setVisibility(View.VISIBLE);
		        } /*else {
		        	if (histories != null && histories.size() > 0) {
		        		showUnknownPlaces(tableLayout);
		        	}
		        }*/
		        
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
			final Integer id_track_for_point = ((History) entityRow).ID_TRACK_FOR_POINT;
			final Integer id_point = ((History) entityRow).ID_POINT;

			if (isTrack) {
				int i = 1;
			}
			
			
			TableRow tRMonth;

			if ((year != DateUtils.getYearByDateLong(created))
					|| (month != DateUtils.getMonthByDateLong(created))) {

				year = DateUtils.getYearByDateLong(created);
				month = DateUtils.getMonthByDateLong(created);
				monthName = DateUtils.getMonthNameByDateLong(created);

				tRMonth = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_month, null);

				String mnTitle = monthName.concat(", ").concat(Integer.valueOf(year).toString());
				((TextView)tRMonth.findViewById(R.id.nameOfTheMonth)).setText(mnTitle);

				tableLayout.addView(tRMonth);

			}

			if (isPoint) { // point
				
					if (id_track_for_point != null && !id_track_for_point.equals(0)) {
						// если у точки есть ссылка на трек, то создаим этот самы трек, поскольку имЯ трека как замыслено
						// должно быть перед именами точек, которые были созданы в рамках данного трека (путешествиЯ)
						
						if (findViewById(id_track_for_point) == null) {
							
				        	TableRow tR = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_track, null);
				        	tR.setId(id_track_for_point);
					        tableLayout.addView(tR);
							
						}
					} else {
						
						// если предыдущаЯ точка была точкой трека, то надо убрать у нее хвос картинки трека
						if (prevItemIsPointOfTrack) {
							if (trackBodyToRemove != null) {
								trackBodyToRemove.setVisibility(View.INVISIBLE);
							}
							trackBodyToRemove = null;
							
							if (boxToRemove != null) {
								boxToRemove.setVisibility(View.INVISIBLE);
							}
							boxToRemove = null;

				        	TableRow tR = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_end_of_track, null);
				        	tR.setId(id_track_for_point);
				        	
					        tableLayout.addView(tR);
						
						}
						prevItemIsPointOfTrack = false;
						
					}

					if (!geo.equals(geoCode!=null?geoCode:new String(""))) {
						
						geo = geoCode!=null?new String(geoCode):new String("");
						
						tRMonth = (id_track_for_point != null && !id_track_for_point.equals(0))?
								((TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_geo_of_track, null)):
								((TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_geo, null));

						if (geo.equals("")) {
							((TextView)tRMonth.findViewById(R.id.nameOfTheGeo)).setText(getResources().getString(R.string.unknown_place));
						} else {
							((TextView)tRMonth.findViewById(R.id.nameOfTheGeo)).setText(geo);
						}

						tableLayout.addView(tRMonth);
					}
					
		        	TableRow tR = (id_track_for_point != null && !id_track_for_point.equals(0))? 
		        			((TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_point_of_track, null)):
		        				((TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_point, null));
		        			
			        tR.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							selectPoint(v, (History) entityRow);
						}
					});
			        

					if (id_track_for_point != null && !id_track_for_point.equals(0)) {
						// эти переменные длЯ следующей этерации цикла по точкам
						prevItemIsPointOfTrack = true;
						trackBodyToRemove = (ImageView) tR.findViewById(R.id.removableTrackBody);
						boxToRemove = (RelativeLayout) tR.findViewById(R.id.removableTerm);
						
					} else {
						
						// предыдущий айтем был треком без точек. “берет хвост.
						if (prevItemIstrack) {
							if (trackBodyToRemove != null) {
								trackBodyToRemove.setVisibility(View.INVISIBLE);
							}
							trackBodyToRemove = null;
							
							if (trackBodyToRemove1 != null) {
								trackBodyToRemove1.setVisibility(View.INVISIBLE);
							}
							trackBodyToRemove1 = null;
						}
						
					}
			        
					String titleToShow = /*id.toString() + " id_point-" + id_point.toString() + " " +  */title;
					// отладочный if Ќадо убрать
					//if (id_track_for_point != null && !id_track_for_point.equals(0)) {
					//	titleToShow += " id_track_for_point " + id_track_for_point.toString();
					//}				
					if (kind == null || kind.equals(0)) {
						//((ImageButton)tR.findViewById(R.id.buttonTrack)).setImageBitmap(null);
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
						if (titleToShow.length() > 60) {
							titleToShow = titleToShow.substring(0, 59) + "...";
						}
						textView.setText(titleToShow);
					}
					
					textView = (TextView) tR.findViewById(R.id.datesOfTheTrack);
					textView.setText(DateUtils.getDateVisualRepresentaion(created, PointsActivity.this));
					

					//textView.invalidate();
					//tR.invalidate();
			        tableLayout.addView(tR);
			        tableLayout.invalidate();
			        textView.invalidate();
			        tR.invalidate();
					
				/*
				} else {
					histories.add((History) entityRow);
				}
				*/
				prevItemIstrack = false;

			}

			if (isTrack) {
				
				

				if (prevItemIsPointOfTrack) {
					if (trackBodyToRemove != null) {
						trackBodyToRemove.setVisibility(View.INVISIBLE);
					}
					trackBodyToRemove = null;
					
					if (boxToRemove != null) {
						boxToRemove.setVisibility(View.INVISIBLE);
					}
					boxToRemove = null;

		        	TableRow tR = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_end_of_track, null);
		        	//tR.setId(id_track_for_point);
		        	
			        tableLayout.addView(tR);
				
				} else 
				// предыдущий элемент так же был треком
				// причем треком, в котором не было точек
				if (prevItemIstrack) {
					if (trackBodyToRemove != null) {
						trackBodyToRemove.setVisibility(View.INVISIBLE);
					}
					
					if (trackBodyToRemove1 != null) {
						trackBodyToRemove1.setVisibility(View.INVISIBLE);
					}
				}
				
				TableRow tR;
				boolean toAdd = true;
				
				if (findViewById(id_track) == null) { // не нашли элемента с таким ID 
					// это значит, что это трек без точек
					// точки идут перед треком, поскольку точки с треком добавлЯютсЯ по id_track_for_point из первой
					// попавшейсЯ точки пренадлежащей этому треку

					tR = (TableRow)LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.fragment_activities_list_track, null);
					tR.setId(id_track);
					tR.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							selectTrack(v, (History) entityRow);
						}
					});
				
					// “ трека без точек картинка будет в виде точки
					((ImageView) tR.findViewById(R.id.removableTrackBd1)).setVisibility(View.INVISIBLE);
					((ImageView) tR.findViewById(R.id.removableTrackBd2)).setVisibility(View.INVISIBLE);
					prevItemIstrack = true;
					prevItemIsPointOfTrack = false;
				
				} else {
					tR = (TableRow)findViewById(id_track);
					toAdd = false;
				}

				tR.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						selectTrack(v, (History) entityRow);
					}
				});
				

				TextView textView = (TextView) tR.findViewById(R.id.nameOfTheTrack);
				
				if (title != null && !title.equals("null")) {
					String titleToShow = new String(title);
					if (titleToShow.length() > 60) {
						titleToShow = titleToShow.substring(0, 59) + "...";
					}
					
					textView.setText(/*id.toString() + " id_track-" + id_track.toString()
							+ " " + */titleToShow);
				} else {
					
					//textView.setText(id.toString() + " id_track-" + id_track.toString());
					textView.setText(getResources().getString(R.string.new_trip));
					
				}
				
				textView = (TextView) tR.findViewById(R.id.datesOfTheTrack);
				textView.setText(DateUtils.getDateVisualRepresentaion(created, PointsActivity.this));
				
				if (toAdd) {
					tableLayout.addView(tR);
				}

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
