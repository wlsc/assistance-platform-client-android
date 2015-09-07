package de.tudarmstadt.informatik.tk.android.assistance;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

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

        // init of a memory leak finder library
        LeakCanary.install(this);
    }
}
