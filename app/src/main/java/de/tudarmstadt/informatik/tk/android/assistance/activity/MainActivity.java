package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.common.DrawerActivity;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;


/**
 * User home
 */
public class MainActivity extends DrawerActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean userHasModulesInstalled = UserUtils.isUserHasModules(getApplicationContext());

        if (userHasModulesInstalled) {

            getLayoutInflater().inflate(R.layout.activity_main, mFrameLayout);
            setTitle(R.string.main_activity_title);

        } else {

            Intent intent = new Intent(this, AvailableModulesActivity.class);
            startActivity(intent);
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
