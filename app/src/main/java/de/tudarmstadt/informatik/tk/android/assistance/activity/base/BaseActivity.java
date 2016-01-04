package de.tudarmstadt.informatik.tk.android.assistance.activity.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.SensorProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.01.2016
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorProvider.getInstance(getApplicationContext()).synchronizeRunningSensorsWithDb();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}