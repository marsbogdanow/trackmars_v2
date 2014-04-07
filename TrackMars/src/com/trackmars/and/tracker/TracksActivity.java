package com.trackmars.and.tracker;

import java.util.List;

import com.trackmars.and.tracker.dataUtils.EntityHelper;
import com.trackmars.and.tracker.dataUtils.IEntity;
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

public class TracksActivity extends FragmentActivity {

	private Handler handler;
	
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
	
	
	public void selectTrack(View v, Track track) {
	      Intent intent = new Intent(this, TrackViewActivity.class);
	      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	      
	      intent.putExtra("title", track.TITLE);
	      intent.putExtra("created", track.CREATED);
	      intent.putExtra("id", track.ID);
	      
	      startActivity(intent);
	}
	
	private void createList() {
		
		EntityHelper entityHelper;
		try {
			entityHelper = new EntityHelper(getApplicationContext(), Track.class);

		
			try {
				List<IEntity> tracks = entityHelper.getAllRows(0, 50, "created DESC");

				for (final IEntity entityRow : tracks) {
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

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_track_list);
		
		//на всякий случай создадим таблицу
		EntityHelper entityHelper;
		try {
			entityHelper = new EntityHelper(getApplicationContext(), Track.class);
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				
				final IEntity entityRow = (Track)msg.obj;
				
				TableLayout tableLayout = (TableLayout) findViewById(R.id.listTable);
				
				
				TableRow tR = new TableRow(TracksActivity.this);
		        tR.setPadding(1,2,1,2);
		        tR.setClickable(true);
		        
		        final Integer id = ((Track)entityRow).ID;
		        final String title = ((Track)entityRow).TITLE;
		        final Long created = ((Track)entityRow).CREATED;
		        
		        tR.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						selectTrack(v, (Track)entityRow);
					}
					
				});
		        
		        TableRow.LayoutParams flp1 = new TableRow.LayoutParams(
		        		TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.FILL_HORIZONTAL);
		        tR.setLayoutParams(flp1);
		        
		        FrameLayout frameLayout = new FrameLayout(TracksActivity.this);
		        frameLayout.setId(((Track)entityRow).ID);
		        
		        tR.addView(frameLayout);
		        
		        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		        
		        ListTracksItemTrack listTracksItemTrack = new ListTracksItemTrack();
		        
		        Bundle args = new Bundle();
		        
		        args.putString("title", title);
		        args.putLong("created", created);
		        args.putInt("id", id);
		        
		        listTracksItemTrack.setArguments(args);
		        
		        ft.replace(((Track)entityRow).ID, listTracksItemTrack);
		        
		        ft.commit();         

		        
		        tableLayout.addView(tR);		
				
				
				//tableLayout.addView((View)msg.obj);
				
			}
		};		
		
		MyTask mt = new MyTask();
	    mt.execute();		
	}
	
	public void onClick(View view) {
		
		if (view.getId() == R.id.buttonBack) {
			
		    Intent intent = new Intent(this, MainActivity.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		      
		    startActivity(intent);
		    
		}
	}
}
