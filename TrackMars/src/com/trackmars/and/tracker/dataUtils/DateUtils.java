package com.trackmars.and.tracker.dataUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.trackmars.and.tracker.R;

import android.app.Activity;
import android.content.res.Resources;

public class DateUtils {

	public static final Integer MILLISECONDS_IN_SECOND = 1000;
	public static final Integer MILLISECONDS_IN_MINUTE = 1000 * 60;
	public static final Integer MILLISECONDS_IN_HOUR = 1000 * 60 * 60;
	
	public static Integer getYearByDateLong(Long date) {
		String iDate = new SimpleDateFormat("yyyy").format(new Date(date));
		return Integer.valueOf(iDate);
	}
	
	public static String getMonthNameByDateLong(Long date) {
		String iDate = new SimpleDateFormat("MMMMMMMMM").format(new Date(date));
		return iDate;
	}

	public static Integer getMonthByDateLong(Long date) {
		String iDate = new SimpleDateFormat("MM").format(new Date(date));
		return Integer.valueOf(iDate);
	}

	
	public static String getDateVisualRepresentaion(Long dataInt, Activity activity) {
		
		Date date = new Date(dataInt);
		Date now = new Date();
		
		Integer minutesToShow;
		Integer hoursToShow;
		Boolean yesterday = false;
		Boolean today = false;
		Boolean showHowManyDayAgo = false;
		Boolean showDate = false;
		
		String toReturn = new String();

		Resources resources = activity.getResources();
		SimpleDateFormat simpleDate = new SimpleDateFormat(resources.getString(R.string.dateFormatWOTime));
		
		
		if (now.getTime() - date.getTime() < 24 * DateUtils.MILLISECONDS_IN_HOUR) {
			if (now.getTime() - date.getTime() < DateUtils.MILLISECONDS_IN_HOUR) {
				minutesToShow = Math.round((now.getTime() - date.getTime()) / DateUtils.MILLISECONDS_IN_MINUTE); 

				if(minutesToShow == 1) {
					toReturn = resources.getString(R.string.min1) + " " + resources.getString(R.string.ago);
				} else 	if ("ru".equals(resources.getString(R.string.locale))) {	
					
					if (minutesToShow > 1 && minutesToShow < 5) {
						toReturn = minutesToShow.toString() + " " + resources.getString(R.string.min2_4) + " " + resources.getString(R.string.ago);
					} else if (minutesToShow > 4 && minutesToShow < 21) {
						toReturn = minutesToShow.toString() + " " + resources.getString(R.string.min5_0) + " " + resources.getString(R.string.ago);
					} else {
						int reminder = minutesToShow % 10;
						if (reminder == 0 || reminder > 4) {
							toReturn = minutesToShow.toString() + " " + resources.getString(R.string.min5_0) + " " + resources.getString(R.string.ago);
						} else if (reminder == 1) {
							toReturn = resources.getString(R.string.min1) + " " + resources.getString(R.string.ago);
						} else {
							toReturn = minutesToShow.toString() + " " + resources.getString(R.string.min2_4) + " " + resources.getString(R.string.ago);
						}
					}
					
				} else {
					toReturn = minutesToShow.toString() + " " + resources.getString(R.string.min2_4) + " " + resources.getString(R.string.ago);
				}
			
			} else if (now.getTime() - date.getTime() < (3 * DateUtils.MILLISECONDS_IN_HOUR)) {
				hoursToShow = Math.round((now.getTime() - date.getTime()) / DateUtils.MILLISECONDS_IN_HOUR);
				
				if(hoursToShow == 1) {
					toReturn = resources.getString(R.string.hour1) + " " + resources.getString(R.string.ago);
				} else {
					toReturn = hoursToShow.toString() + " " + resources.getString(R.string.hour2_4) + " " + resources.getString(R.string.ago);
				}
				
			} else if (now.getDate() > date.getDate()) {
				yesterday = true;
				toReturn = resources.getString(R.string.yesterday);
				SimpleDateFormat simpleTime = new SimpleDateFormat(resources.getString(R.string.timeFormat));
				toReturn += " " + simpleTime.format(date);
			} else if (now.getDate() == date.getDate()) {
				today = true;
				toReturn = resources.getString(R.string.today);
				SimpleDateFormat simpleTime = new SimpleDateFormat(resources.getString(R.string.timeFormat));
				toReturn += " " + simpleTime.format(date);
			}
		} else if (now.getTime() - date.getTime() < (4 * DateUtils.MILLISECONDS_IN_HOUR * 24)) {
				showHowManyDayAgo = true;
				Integer days = Math.round(now.getTime() - date.getTime()) / (DateUtils.MILLISECONDS_IN_HOUR * 24); 
				if (days <= 1) {
					toReturn = resources.getString(R.string.day1) + " " +resources.getString(R.string.ago); 
				} else {
					toReturn = days.toString() + " " + resources.getString(R.string.day_more_then1) + " " + resources.getString(R.string.ago); 
				}
		} else {
				showDate = true;
				toReturn = simpleDate.format(date);
		}
		
		return toReturn;
	}
	
}
