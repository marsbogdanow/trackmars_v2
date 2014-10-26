package com.trackmars.and.tracker.utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trackmars.and.tracker.R;
import com.trackmars.and.tracker.dataUtils.DateUtils;
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

	
	public static int getZoom (double distance) {
		if (distance > 1000000d) {
			return 3;
		} else if (distance > 500000d) {
			return 4;
		} else if (distance > 200000d) {
			return 6;
		} else if (distance > 100000d) {
			return 7;
		}  else if (distance > 50000d) {
			return 8;
		}  else if (distance > 20000d) {
			return 9;
		}  else if (distance > 10000d) {
			return 10;
		}  else if (distance > 5000d) {
			return 11;
		}  else if (distance > 1000d) {
			return 13;
		}  else if (distance > 500d) {
			return 15;
		}
		return 16;
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
	            		
	                	//String addressLine = new String("");
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
	            //e.printStackTrace();
	    }		
		
		
		
		return string;
	}
	
	public static String getDistanceHTMLView(Double distance, Resources res) {
		
        final Integer km = (int) (distance / 1000);
        final Integer meter = (int)(distance - km * 1000);
        
        
        String fieldText = "<big><big><b>" + km.toString() + "</b></big></big>";
        fieldText += "<small>" + res.getString(R.string.kilometer) + ", </small>";
        fieldText += "  <big><big><b>" + meter.toString() + "</b></big></big>";
        fieldText += "<small>" + res.getString(R.string.meter) + "</small>";
        
        return fieldText;
        
	}
	
	public static String getSpeedHTMLView(Double distance, Long msDuration, Resources res ) {
		
        String fieldText = new String();
		
		if (distance != null && msDuration != null && msDuration != 0l && distance != 0d) {
			
        	Integer speed = (int)  (distance / (double)msDuration / 1000d * (double)DateUtils.MILLISECONDS_IN_HOUR); 

	        fieldText = "<big><big><b>" + speed.toString() + "</b></big></big>";
	        fieldText += "<small>" + res.getString(R.string.kmph) + ", </small>";
        
        } else {
        	
	        fieldText = "<big><big><b> --</b></big></big>";
	        fieldText += "<small>" + res.getString(R.string.kmph) + ", </small>";
        	
        }
        
        return fieldText;
		
	}
	
	public static String getDurationHTMLView (Long msDuration, Resources res) {
        Integer hours = (int) (msDuration / DateUtils.MILLISECONDS_IN_HOUR);
        final Integer minutes = (int)((msDuration - hours * DateUtils.MILLISECONDS_IN_HOUR) / DateUtils.MILLISECONDS_IN_MINUTE);
        
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
        
        return fieldText;
		
	}
	
}
