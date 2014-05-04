package com.trackmars.and.tracker;

import java.text.DecimalFormat;

import org.w3c.dom.Text;

import com.trackmars.and.tracker.dataUtils.DateUtils;

import android.os.Bundle;
import android.os.Parcelable.Creator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ListPointsItemPoint extends Fragment {
	
	String title;
	Integer id;
	Long date;
	Double lng;
	Double lat;
	String geocode;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		      Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.point_list_item,
		        container, false);		
		
		title = this.getArguments().getString("title");
		geocode = this.getArguments().getString("geocode");
		id = this.getArguments().getInt("id");
		date = this.getArguments().getLong("created");
		lat = this.getArguments().getDouble("lat");
		lng = this.getArguments().getDouble("lng");
		
		
		TextView textView = (TextView) view.findViewById(R.id.nameOfThePoint);
		
		if (title != null) {
			textView.setText(title);
		}
		
		TextView dateView = (TextView) view.findViewById(R.id.pointDate);
		dateView.setText(DateUtils.getDateVisualRepresentaion(date, this.getActivity()));
		
		TextView coord = (TextView) view.findViewById(R.id.coord);
		coord.setTextSize(10);
		coord.setText(new DecimalFormat("#.######").format(lat) + " " + new DecimalFormat("#.######").format(lng));

		TextView gc = (TextView) view.findViewById(R.id.geocode);
		if (geocode != null) {
			gc.setText(geocode);
		}
		
		return view;
	}
}
