package com.trackmars.and.tracker.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trackmars.and.tracker.DialogCreatePoint;
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

	// показывает на карте отмеченную точку
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
	
	// Достаем название города/райна/страны для последующей групировки точек/треков
	// сейчас это будет браться из Goole geocoder, но у них есть серьезные ограничения на количество запросов в день
	// Есть на мой взгляд, гораздо более перспективное API - foursquare API. Помле запуска сайта надо
	// пользоваться им.
	public static String getGeoCodingInfo (LatLng latLng, Context context) {
		String string = new String();
		
		//Далее идет достаточно временный код

	    Geocoder geoCoder = new Geocoder(context, Locale.getDefault());    
	    try {
	            List<Address> addresses = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
	            if (addresses.size() > 0) {
	            	
	            	String adminArea = new String();
	            	String country = new String();
	            	
	            	for (Address address : addresses) {
	            		
	                	String addressLine = new String("");
	                	String addressLineFull = new String("");
	                	//AddressObj addressObj = new DialogCreatePoint.AddressObj();
	            		
	            		
	            		// эта переменная нужна только для анализа. Потом удалить
	                	addressLineFull += "#";
	            		addressLineFull += "getAdminArea " + address.getAdminArea();
	            		addressLineFull += ", getAddressLine(0) " + address.getAddressLine(0);
	            		addressLineFull += ", getLocality " + address.getLocality();
	            		addressLineFull += ", getFeatureName " + address.getFeatureName();
	            		addressLineFull += ", getPremises " + address.getPremises();
	            		addressLineFull += ", getSubAdminArea " + address.getSubAdminArea();
	            		addressLineFull += ", getSubLocality " + address.getSubLocality();
	            		addressLineFull += ", getThoroughfare " + address.getThoroughfare();
	            		addressLineFull += ", getSubThoroughfare " + address.getSubThoroughfare();
	            		addressLineFull += ", getCountryName " + address.getCountryName();
	            		
	            		Log.d("", addressLineFull);
	            		
	            		if (address.getCountryName() != null) {
	            			country = address.getCountryName();
	            		}
	            		
	            		if (address.getAdminArea() != null) {
	            			adminArea = new String(address.getAdminArea());
	            			
	            			if (address.getLocality() != null) {
	            				string = (country.length()!=0?country:address.getCountryName()) + ", " + adminArea + ", " + address.getLocality();
	            				return string;
	            			} else if (address.getSubAdminArea() != null) {
	            				string = (country.length()!=0?country:address.getCountryName()) + ", " + adminArea + ", " + address.getSubAdminArea();
	            				return string;
	            			}
	            			
	            		}
	            	}
	            	
            		if (adminArea.length() != 0) {
            			return country.length()!=0?(country + ", "):"" + adminArea;
            		}
            		
            		if (country.length() != 0) {
            			return country;
            		}
	            	
	            }    
	    } catch (IOException e) {
	            e.printStackTrace();
	    }		
		
		
		
		return string;
	}
	
}
