package com.trackmars.and.tracker.actListItems;

import com.trackmars.and.tracker.R;
import com.trackmars.and.tracker.utils.RepresentationUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivitiesListPoint extends Fragment{

	String title;
	Integer kind;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		      Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_activities_list_point,
		        container, false);		
		
		title = this.getArguments().getString("title");
		kind = this.getArguments().getInt("kind");
	
		if (kind == null || kind.equals(0)) {
			((ImageView)view.findViewById(R.id.buttonTrack)).setImageBitmap(null);
		} else {
			if (kind.equals(RepresentationUtils.KindOfPoint.ATTRACTIVE.val())) {
				((ImageView)view.findViewById(R.id.buttonTrack)).setImageResource(R.drawable.sm_attractive);
			} else if (kind.equals(RepresentationUtils.KindOfPoint.FOOD.val())) {
				((ImageView)view.findViewById(R.id.buttonTrack)).setImageResource(R.drawable.sm_food);
			} else if (kind.equals(RepresentationUtils.KindOfPoint.NIGHT.val())) {
				((ImageView)view.findViewById(R.id.buttonTrack)).setImageResource(R.drawable.sm_night);
			} else if (kind.equals(RepresentationUtils.KindOfPoint.NOTE.val())) {
				((ImageView)view.findViewById(R.id.buttonTrack)).setImageResource(R.drawable.sm_note);
			}
		}
		
		TextView textView = (TextView) view.findViewById(R.id.nameOfThePoint);
		
		if (title != null) {
			textView.setText(title);
		}
		
		return view;
	}
}
