package com.trackmars.and.tracker.actListItems;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trackmars.and.tracker.R;
import com.trackmars.and.tracker.utils.RepresentationUtils.KindOfPoint;

public class ActivitiesListGeo extends Fragment{

	String title;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		      Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_activities_list_geo,
		        container, false);		
		
		title = this.getArguments().getString("geo");
		
		TextView textView = (TextView) view.findViewById(R.id.nameOfTheGeo);
		
		if (title != null) {
			textView.setText(title);
		}
		
		return view;
	}
}
