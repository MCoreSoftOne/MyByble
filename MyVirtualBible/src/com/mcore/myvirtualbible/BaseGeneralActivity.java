package com.mcore.myvirtualbible;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public abstract class BaseGeneralActivity extends SherlockActivity {
	
	@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			trackMe();
		}

	protected void trackMe() {
		Tracker t = getTracker();
		t.setScreenName(this.getClass().getSimpleName());
		t.send(new HitBuilders.AppViewBuilder().build());
	}

	synchronized Tracker getTracker() {
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
		Tracker t = analytics.newTracker(R.xml.global_tracker);
		return t;
	}

}
