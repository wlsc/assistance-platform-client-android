package de.tudarmstadt.informatik.tk.android.assistance;

import android.app.Application;

import de.tudarmstadt.informatik.tk.android.kraken.service.KrakenServiceManager;

/**
 * Main application
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 24.08.2015
 */
public class App extends Application {

    private KrakenServiceManager service;

    @Override
    public void onCreate() {
        super.onCreate();

        service = KrakenServiceManager.getInstance(getApplicationContext());
        service.startKrakenService();

        // init of a memory leak finder library
//        LeakCanary.install(this);
    }
}
