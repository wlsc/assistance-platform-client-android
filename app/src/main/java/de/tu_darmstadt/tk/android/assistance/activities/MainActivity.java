package de.tu_darmstadt.tk.android.assistance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.DrawerActivity;
import de.tu_darmstadt.tk.android.assistance.utils.Constants;
import de.tu_darmstadt.tk.android.assistance.utils.UserUtils;


/**
 * User home
 */
public class MainActivity extends DrawerActivity {

    private String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean userHasModulesInstalled = UserUtils.isUserHasModules(getApplicationContext());

        if (userHasModulesInstalled) {

            getLayoutInflater().inflate(R.layout.activity_main, mFrameLayout);
            setTitle(R.string.main_activity_title);

        } else {

            Intent parentIntent = getIntent();
            long currentDeviceId = parentIntent.getLongExtra(Constants.INTENT_CURRENT_DEVICE_ID, -1);

            Intent intent = new Intent(this, AvailableModulesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.INTENT_CURRENT_DEVICE_ID, currentDeviceId);
            setResult(Constants.INTENT_CURRENT_DEVICE_ID_RESULT, intent);
            finish();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // drawer item was select

    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
