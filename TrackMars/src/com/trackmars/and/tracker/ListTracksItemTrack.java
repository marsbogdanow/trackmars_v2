package com.trackmars.and.tracker;

import java.text.DecimalFormat;

import org.w3c.dom.Text;

import com.trackmars.and.tracker.dataUtils.DataUtils;

import android.os.Bundle;
import android.os.Parcelable.Creator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ListTracksItemTrack extends Fragment {
	
	String title;
	Integer id;
	Long date;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		      Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.track_list_item,
		        container, false);		
		
		title = this.getArguments().getString("title");
		id = this.getArguments().getInt("id");
		date = this.getArguments().getLong("created");
		
		
		TextView textView = (TextView) view.findViewById(R.id.nameOfTheTrack);
		
		if (title != null) {
			textView.setText(title);
		}
		
		TextView dateView = (TextView) view.findViewById(R.id.trackDate);
		dateView.setText(DataUtils.getDataVisualRepresentaion(date, this.getActivity()));

		return view;
	}
}
