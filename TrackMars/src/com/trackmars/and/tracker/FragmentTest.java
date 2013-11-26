package com.trackmars.and.tracker;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentTest extends Fragment {
	  @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.fragment_test,
	        container, false);
	    return view;
	  }

	  public void setText(String item) {
	    TextView view = (TextView) getView().findViewById(R.id.detailsText);
	    view.setText(item);
	  }
}
