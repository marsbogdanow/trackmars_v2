package com.trackmars.and.tracker;

import java.util.List;

import com.trackmars.and.tracker.actListItems.ActivitiesListMonth;
import com.trackmars.and.tracker.actListItems.ActivitiesListPoint;
import com.trackmars.and.tracker.dataUtils.DateUtils;
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
	
	private TableRow createTableRow(Integer id) {
		
		TableRow tR = new TableRow(this);
        tR.setPadding(0,0,0,0);
        tR.setClickable(true);
        
        TableRow.LayoutParams flp1 = new TableRow.LayoutParams(
        		TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        tR.setLayoutParams(flp1);
        
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setId(id);
        
        tR.addView(frameLayout);
        
        return tR;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_list);
		
		TableLayout tableLayout = (TableLayout) findViewById(R.id.listTable);
		

		
		EntityHelper entityHelper;
		try {
			entityHelper = new EntityHelper(getApplicationContext(), Point.class);

		
			try {
				List<IEntity> points = entityHelper.getAllRows(0, 50, "column_created DESC");

				int month = 0;
				int year = 0;
				String monthName;
				
				for (final IEntity entityRow : points) {
			        
			        final String title = ((Point)entityRow).COLUMN_TITLE;
			        final Double lng = ((Point)entityRow).COLUMN_LNG;
			        final Double lat = ((Point)entityRow).COLUMN_LAT;
			        final Long created = ((Point)entityRow).COLUMN_CREATED;
			        final Integer id = ((Point)entityRow).COLUMN_ID;
			        final Integer kind = ((Point)entityRow).COLUMN_KIND;
			        
			        if ((year != DateUtils.getYearByDateLong(created)) || (month != DateUtils.getMonthByDateLong(created))) {
			        	
			        	year = DateUtils.getYearByDateLong(created);
			        	month = DateUtils.getMonthByDateLong(created);
			        	monthName = DateUtils.getMonthNameByDateLong(created);
			        	
				        TableRow tRMonth = createTableRow(year*12 + month);

				        // заталкиваем фрагмент в строку таблицы
				        FragmentTransaction ftmonth = getSupportFragmentManager().beginTransaction();
				        ActivitiesListMonth listPointsItemMonth = new ActivitiesListMonth();
				        Bundle args = new Bundle();
				        
				        args.putString("month", monthName);
				        
				        listPointsItemMonth.setArguments(args);
				        ftmonth.replace(year*12 + month, listPointsItemMonth);
				        ftmonth.commit();         

				        tableLayout.addView(tRMonth);		
			        
			        }
			        
			        TableRow tR = createTableRow(((Point)entityRow).COLUMN_ID);
			        tR.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							selectPoint(v, (Point)entityRow);
						}
					});
			        
			        // заталкиваем фрагмент в строку таблицы
			        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			        ActivitiesListPoint listPointsItemPoint = new ActivitiesListPoint();
			        Bundle args = new Bundle();
			        
			        args.putString("title", title);
			        args.putInt("kind", kind);
			        
			        listPointsItemPoint.setArguments(args);
			        ft.replace(((Point)entityRow).COLUMN_ID, listPointsItemPoint);
			        ft.commit();         

			        tableLayout.addView(tR);		
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
	
	public void onClick(View view) {
		
		if (view.getId() == R.id.buttonBack) {
			
		    Intent intent = new Intent(this, MainActivity.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		      
		    startActivity(intent);
		    
		}
	}
}
