package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.common.DrawerActivity;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;


/**
 * User home
 */
public class MainActivity extends DrawerActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean userHasModulesInstalled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userHasModulesInstalled = UserUtils.isUserHasModules(getApplicationContext());

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
    public boolean onCreateOptionsMenu(Menu menu) {

        // if we have no modules installed -> no module menu will be showed
        if (!userHasModulesInstalled) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.module_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_module_toggle_state:
                boolean moduleState = item.isChecked();
                if (moduleState) {
                    item.setChecked(false);
                    Log.d(TAG, "User DISABLED a module");
                } else {
                    item.setChecked(true);
                    Log.d(TAG, "User ENABLED a module");
                }
                toggleModuleState(moduleState);
                return true;
            case R.id.menu_module_uninstall:
                Log.d(TAG, "User clicked module uninstall");
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Enables or disables particular module
     *
     * @param moduleState
     */
    private void toggleModuleState(boolean moduleState) {

        if (moduleState) {
            
        } else {

        }
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
