package com.mcore.myvirtualbible;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public abstract class BaseGeneralActivity extends SherlockActivity {
	
	private Tracker gatracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gatracker = getTracker();
		trackMe();
	}

	protected void trackMe() {
		if (gatracker != null) {			
			gatracker.setScreenName(getScreenName());
			gatracker.send(new HitBuilders.ScreenViewBuilder().build());
		}
	}

	synchronized Tracker getTracker() {
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
		Tracker t = analytics.newTracker(R.xml.global_tracker);
		return t;
	}
	
	protected String getScreenName() {
		return this.getClass().getSimpleName();
	}
	
	public void sendEvent(String category, String action) {
		if (gatracker != null) {			
			gatracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).build());
		}
	}
	
	public void sendEvent(String category, String action, String label) {
		if (gatracker != null) {			
			gatracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
		}
	}
	
	public void sendEvent(String category, String action, String param, String value) {
		if (gatracker != null) {			
			gatracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).set(param, value).build());
		}
	}

}
