package de.tudarmstadt.informatik.tk.android.assistance.activity.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.SensorProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.01.2016
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        HarvesterServiceProvider.getInstance(getApplicationContext()).startSensingService();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
//        HarvesterServiceProvider.getInstance(getApplicationContext()).unbindService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscribeRequests();
        SensorProvider.getInstance(getApplicationContext()).synchronizeRunningSensorsWithDb();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unsubscribeRequests();
    }

    /**
     * Subscribes to reactive network requests
     */
    protected abstract void subscribeRequests();

    /**
     * Unsubscribes from reactive network requests
     */
    protected abstract void unsubscribeRequests();

    /**
     * Recreates to reactive network requests
     */
    protected abstract void recreateRequests();

    /**
     * Shows loading ProgressDialog
     */
    protected void showLoading() {
        progressDialog = ProgressDialog.show(this, "Loading wtf", "...", true);
    }

    /**
     * Hides/Dismisses ProgressDialog
     */
    protected void hideLoading() {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}