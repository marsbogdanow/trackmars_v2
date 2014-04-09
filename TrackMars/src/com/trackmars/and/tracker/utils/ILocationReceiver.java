package com.trackmars.and.tracker.utils;

import android.location.Location;

public interface ILocationReceiver {
	public void newLocation(Location location, Class listener, Float accuracy);

	public void askLocation();
}
