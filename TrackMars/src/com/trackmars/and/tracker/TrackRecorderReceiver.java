package com.trackmars.and.tracker;

import com.trackmars.and.tracker.utils.ILocationReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TrackRecorderReceiver extends BroadcastReceiver {

	ILocationReceiver locationReceiver;
	
	public void setLocationReceiver(ILocationReceiver locationReceiver) {
		this.locationReceiver = locationReceiver;
	}
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		this.locationReceiver.askLocation();
		
	}

}
