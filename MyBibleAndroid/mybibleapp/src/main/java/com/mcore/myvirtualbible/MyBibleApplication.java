package com.mcore.myvirtualbible;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Mario on 19/07/2015.
 */
public class MyBibleApplication  extends Application {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    public void onCreate() {
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);
        tracker = analytics.newTracker("UA-44632455-1");
        tracker.enableExceptionReporting(false);
        tracker.enableAdvertisingIdCollection(false);
        tracker.enableAutoActivityTracking(true);
    }
}