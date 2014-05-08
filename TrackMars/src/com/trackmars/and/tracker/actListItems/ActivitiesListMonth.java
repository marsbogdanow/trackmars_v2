package com.trackmars.and.tracker.actListItems;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trackmars.and.tracker.R;
import com.trackmars.and.tracker.utils.RepresentationUtils.KindOfPoint;

public class ActivitiesListMonth extends Fragment{

	String title;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		      Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_activities_list_month,
		        container, false);		
		
		title = this.getArguments().getString("month");
		
		TextView textView = (TextView) view.findViewById(R.id.nameOfTheMonth);
		
		if (title != null) {
			textView.setText(title);
		}
		
		return view;
	}
}
