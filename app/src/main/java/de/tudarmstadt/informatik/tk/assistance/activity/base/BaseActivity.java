package de.tudarmstadt.informatik.tk.assistance.activity.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.SensorProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.CommonUtils;

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
        SensorProvider.getInstance(getApplicationContext()).synchronizeRunningSensorsWithDb();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Shows loading ProgressDialog
     */
    protected void showLoading() {
        progressDialog = ProgressDialog.show(this,
                getString(R.string.loading_header),
                getString(R.string.loading_message),
                true);

        CommonUtils.hideKeyboard(this, progressDialog.getCurrentFocus());
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