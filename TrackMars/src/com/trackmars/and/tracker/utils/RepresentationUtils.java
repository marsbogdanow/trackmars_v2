package com.trackmars.and.tracker.utils;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trackmars.and.tracker.R;
import com.trackmars.and.tracker.model.Point;

public class RepresentationUtils {

	public enum KindOfPoint {
		NONE {
			public Integer val() {return 0;};
		},
		ATTRACTIVE {
			public Integer val() {return 1;};
		},
		FOOD {
			public Integer val() {return 2;};
		},
		NIGHT {
			public Integer val() {return 3;};
		},
		NOTE {
			public Integer val() {return 4;};
		};
	
		public abstract Integer val();
	}

	public static void showPoint(GoogleMap map, Point pnt) {
		map.addCircle(new CircleOptions()
	    .center(new LatLng(pnt.COLUMN_LAT, pnt.COLUMN_LNG))
	    .radius(30)
	    .strokeColor(Color.RED)
	    .fillColor(Color.BLUE));
		// рисуем маркер
		if (pnt.COLUMN_KIND == RepresentationUtils.KindOfPoint.ATTRACTIVE.val()) {
			map.addMarker(new MarkerOptions().
					position(new LatLng(pnt.COLUMN_LAT, pnt.COLUMN_LNG)).
					icon(BitmapDescriptorFactory.fromResource(R.drawable.attractive)));
		} else if (pnt.COLUMN_KIND == RepresentationUtils.KindOfPoint.FOOD.val()) {
			map.addMarker(new MarkerOptions().
					position(new LatLng(pnt.COLUMN_LAT, pnt.COLUMN_LNG)).
					icon(BitmapDescriptorFactory.fromResource(R.drawable.food)));
		} else if (pnt.COLUMN_KIND == RepresentationUtils.KindOfPoint.NIGHT.val()) {
			map.addMarker(new MarkerOptions().
					position(new LatLng(pnt.COLUMN_LAT, pnt.COLUMN_LNG)).
					icon(BitmapDescriptorFactory.fromResource(R.drawable.night)));
		} else if (pnt.COLUMN_KIND == RepresentationUtils.KindOfPoint.NOTE.val()) {
			map.addMarker(new MarkerOptions().
					position(new LatLng(pnt.COLUMN_LAT, pnt.COLUMN_LNG)).
					icon(BitmapDescriptorFactory.fromResource(R.drawable.note)));
		}
	}
	
}
