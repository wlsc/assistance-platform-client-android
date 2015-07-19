package de.tu_darmstadt.tk.android.assistance.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.DrawerActivity;
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

            Intent intent = new Intent(this, AvailableModulesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // drawer item was select

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        Log.d(TAG, "onDestroy -> unbound resources");
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
