package com.trackmars.and.tracker.actListItems;

import com.trackmars.and.tracker.R;
import com.trackmars.and.tracker.dataUtils.DateUtils;
import com.trackmars.and.tracker.utils.RepresentationUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class ActivitiesListTrack extends Fragment{

	String title;
	Integer kind;
	Long created;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		      Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_activities_list_track,
		        container, false);		
		
		title = this.getArguments().getString("title");
		created = this.getArguments().getLong("created");
		
		TextView textView = (TextView) view.findViewById(R.id.nameOfTheTrack);
		
		if (title != null) {
			textView.setText(title);
		} else {
			textView.setText(DateUtils.getDateVisualRepresentaion(created, this.getActivity()));
		}
		
		return view;
	}
}
