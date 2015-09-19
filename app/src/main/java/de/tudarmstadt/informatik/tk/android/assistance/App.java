package de.tudarmstadt.informatik.tk.android.assistance;

import android.app.Application;

import de.tudarmstadt.informatik.tk.android.kraken.utils.KrakenServiceManager;

/**
 * Main application
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 24.08.2015
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        KrakenServiceManager.getInstance(getApplicationContext()).startService();

        // init of a memory leak finder library
//        LeakCanary.install(this);
    }
}
