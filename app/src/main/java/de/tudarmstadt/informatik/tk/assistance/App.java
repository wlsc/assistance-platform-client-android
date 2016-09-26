package de.tudarmstadt.informatik.tk.assistance;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import de.tudarmstadt.informatik.tk.assistance.R.xml;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.AppUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.LogWrapper;

/**
 * Main application
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 24.08.2015
 */
public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

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
    public static GoogleAnalytics getAnalytics() {
        return analytics;
    }

    /**
     * The default app tracker. If this method returns null you forgot to either set
     * android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.tracker field in onCreate method override.
     */
    public static Tracker getTracker() {
        return tracker;
    }

    /**
     * Initializes logging
     */
    public void initLogging() {

        boolean isDebugEnabled = AppUtils.isDebug(getApplicationContext());

        LogWrapper logWrapper = new LogWrapper();
        Log.setDebug(isDebugEnabled);
        Log.setLogNode(logWrapper);

        Log.i(TAG, "Ready");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initGoogleAnalytics();
        initLogging();

        // init of a memory leak finder library
//        LeakCanary.install(this);
    }

    /**
     * Initialize Google Analytics
     */
    private void initGoogleAnalytics() {

        // initialize Google Analytics
        analytics = GoogleAnalytics.getInstance(this);

        // load config from xml file
        tracker = analytics.newTracker(xml.analytics_global_config);

    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}