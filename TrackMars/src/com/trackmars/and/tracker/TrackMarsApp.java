package com.trackmars.and.tracker;

import ru.elifantiev.android.roboerrorreporter.RoboErrorReporter;
import android.app.Application;

public class TrackMarsApp extends Application {

	@Override
    public void onCreate() {
		RoboErrorReporter.bindReporter(this.getBaseContext());
        super.onCreate();
    }	
	
}
