package de.tudarmstadt.informatik.tk.android.assistance;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Main application
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 24.08.2015
 */
public class App extends Application {

    /**
     * The Analytics singleton. The field is set in onCreate method override when the application
     * class is initially created.
     */
    private static GoogleAnalytics analytics;

    /**
     * The default app tracker. The field is from onCreate callback when the application is
     * initially created.
     */
    private static Tracker tracker;

    /**
     * Access to the global Analytics singleton. If this method returns null you forgot to either
     * set android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.analytics field in onCreate method override.
     */
    public static GoogleAnalytics analytics() {
        return analytics;
    }

    /**
     * The default app tracker. If this method returns null you forgot to either set
     * android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.tracker field in onCreate method override.
     */
    public static Tracker tracker() {
        return tracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initGoogleAnalytics();

        // init of a memory leak finder library
//        LeakCanary.install(this);
    }

    /**
     * Initialize Google Analytics
     */
    private void initGoogleAnalytics() {

        // initialize Google Analytics
        analytics = GoogleAnalytics.getInstance(this);

        tracker = analytics.newTracker(Config.GOOGLE_ANALYTICS_TRACKING_ID);
        tracker.setAppName(getString(R.string.app_name));
        tracker.enableExceptionReporting(Config.GOOGLE_ANALYTICS_EXCEPTION_REPORTING);
        tracker.enableAdvertisingIdCollection(Config.GOOGLE_ANALYTICS_ADVERTISING_COLLECTION);
        tracker.enableAutoActivityTracking(Config.GOOGLE_ANALYTICS_AUTO_ACTIVITY_TRACKING);

    }


}
