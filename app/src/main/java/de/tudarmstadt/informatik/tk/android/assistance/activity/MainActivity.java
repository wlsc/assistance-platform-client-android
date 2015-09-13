package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.common.DrawerActivity;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ToggleModuleRequest;
import de.tudarmstadt.informatik.tk.android.assistance.service.ModuleService;
import de.tudarmstadt.informatik.tk.android.assistance.service.ServiceGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.util.Snacker;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.db.DatabaseManager;
import de.tudarmstadt.informatik.tk.android.kraken.db.Module;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleDao;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleInstallation;
import de.tudarmstadt.informatik.tk.android.kraken.db.ModuleInstallationDao;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * User home
 */
public class MainActivity extends DrawerActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean userHasModulesInstalled;

    private ModuleDao moduleDao;

    private ModuleInstallationDao moduleInstallationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userHasModulesInstalled = UserUtils.isUserHasModules(getApplicationContext());

        if (userHasModulesInstalled) {

            getLayoutInflater().inflate(R.layout.activity_main, mFrameLayout);
            setTitle(R.string.main_activity_title);

            loadModules();
        } else {

            Intent intent = new Intent(this, AvailableModulesActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Loads all information about running assistance modules
     */
    private void loadModules() {

        if (moduleDao == null) {
            moduleDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getModuleDao();
        }

        if (moduleInstallationDao == null) {
            moduleInstallationDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getModuleInstallationDao();
        }


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
                moduleUninstall();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Enables or disables currently selected module
     *
     * @param moduleState
     */
    private void toggleModuleState(boolean moduleState) {

        if (moduleState) {

        } else {

        }
    }

    /**
     * Uninstalls currently selected module
     */
    private void moduleUninstall() {
//        uninstallModuleFromDb();
        unregisterModuleService();
    }

    /**
     * Removes module installation
     */
    private void uninstallModuleFromDb() {

        long currentModuleId = UserUtils.getCurrentModuleId(getApplicationContext());

        ModuleInstallation moduleInstallation = new ModuleInstallation();
        moduleInstallation.setModuleId(currentModuleId);

        moduleInstallationDao.delete(moduleInstallation);
    }

    /**
     * Unregisters current module on server
     */
    private void unregisterModuleService() {

        String userToken = UserUtils.getUserToken(getApplicationContext());
        long currentModuleId = UserUtils.getCurrentModuleId(getApplicationContext());

        Module module = moduleDao
                .queryBuilder()
                .where(ModuleDao.Properties.Id.eq(currentModuleId))
                .limit(1)
                .build()
                .unique();

        if (module == null) {
            Log.e(TAG, "Module DB entry is NULL! Cannot send deactivate request to module service!");
            return;
        }

        String modulePackageId = module.getPackageName();

        ToggleModuleRequest toggleModuleRequest = new ToggleModuleRequest();
        toggleModuleRequest.setModuleId(modulePackageId);

        ModuleService moduleService = ServiceGenerator.createService(ModuleService.class);

        moduleService.deactivateModule(userToken, toggleModuleRequest, new Callback<Void>() {

            @Override
            public void success(Void aVoid, Response response) {

                // deactivation successful
                if (response.getStatus() == 200) {
                    CoordinatorLayout snackbarCoordinatorLayout = ButterKnife.findById(MainActivity.this, R.id.snackbarCoordinatorLayout);
                    Snacker.showLong(snackbarCoordinatorLayout, R.string.about, R.string.button_ok_text, null);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorMessages(TAG, error);
            }
        });
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        moduleDao = null;
        moduleInstallationDao = null;
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
