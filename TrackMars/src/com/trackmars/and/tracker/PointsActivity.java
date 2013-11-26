package com.trackmars.and.tracker;

import java.util.List;

import com.trackmars.and.tracker.dataUtils.EntityHelper;
import com.trackmars.and.tracker.dataUtils.IEntity;
import com.trackmars.and.tracker.model.Point;

import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class PointsActivity extends FragmentActivity {

	public void selectPoint(View v, Point point) {
		
	      Intent intent = new Intent(this, PointViewActivity.class);
	      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	      
	      intent.putExtra("lng", point.COLUMN_LNG);
	      intent.putExtra("lat", point.COLUMN_LAT);
	      intent.putExtra("title", point.COLUMN_TITLE);
	      intent.putExtra("created", point.COLUMN_CREATED);
	      intent.putExtra("id", point.COLUMN_ID);
	      
	      startActivity(intent);
	      
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_list);
		
		TableLayout tableLayout = (TableLayout) findViewById(R.id.listTable);
		

		
		EntityHelper entityHelper = new EntityHelper(getApplicationContext(), Point.class);
		
		try {
			List<IEntity> points = entityHelper.getAllRows(0, 50, "column_created DESC");

			for (final IEntity entityRow : points) {
				
				TableRow tR = new TableRow(this);
		        tR.setPadding(1,2,1,2);
		        tR.setClickable(true);
		        
		        final String title = ((Point)entityRow).COLUMN_TITLE;
		        final Double lng = ((Point)entityRow).COLUMN_LNG;
		        final Double lat = ((Point)entityRow).COLUMN_LAT;
		        final Long created = ((Point)entityRow).COLUMN_CREATED;
		        final Integer id = ((Point)entityRow).COLUMN_ID;
		        
		        tR.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						selectPoint(v, (Point)entityRow);
					}
					
				});
		        
		        TableRow.LayoutParams flp1 = new TableRow.LayoutParams(
		        		TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.FILL_HORIZONTAL);
		        tR.setLayoutParams(flp1);
		        
		        FrameLayout frameLayout = new FrameLayout(this);
		        frameLayout.setId(((Point)entityRow).COLUMN_ID);
		        
		        tR.addView(frameLayout);
		        
		        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		        
		        ListPointsItemPoint listPointsItemPoint = new ListPointsItemPoint();
		        
		        Bundle args = new Bundle();
		        
		        args.putString("title", title);
		        args.putDouble("lng", lng);
		        args.putDouble("lat", lat);
		        args.putLong("created", created);
		        args.putInt("id", id);
		        
		        listPointsItemPoint.setArguments(args);
		        
		        ft.replace(((Point)entityRow).COLUMN_ID, listPointsItemPoint);
		        
		        ft.commit();         

		        tableLayout.addView(tR);		
			}
		
		
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
//		TableRow tR = new TableRow(this);
//        tR.setPadding(5,5,5,5);
//
//        FrameLayout frameLayout = new FrameLayout(this);
//        frameLayout.setId(1);
//        
//        tR.addView(frameLayout);
//        
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.replace(frameLayout.getId(), new ListPointsItemPoint());
//        ft.commit();         
//        
//        TableRow tR1 = new TableRow(this);
//        tR1.setPadding(5,5,5,5);
//
//        frameLayout = new FrameLayout(this);
//        frameLayout.setId(2);
//        
//        tR1.addView(frameLayout);
//        
//        ft = getSupportFragmentManager().beginTransaction();
//        ft.replace(frameLayout.getId(), new ListPointsItemPoint());
//        ft.commit();         
//
//        tableLayout.addView(tR);		
//        tableLayout.addView(tR1);		
		
	}
	
	public void onClick(View view) {
		
		if (view.getId() == R.id.buttonBack) {
			
		    Intent intent = new Intent(this, MainActivity.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		      
		    startActivity(intent);
		    
		}
	}
}
