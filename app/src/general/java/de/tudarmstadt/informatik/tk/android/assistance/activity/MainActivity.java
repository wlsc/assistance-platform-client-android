package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.common.DrawerActivity;
import de.tudarmstadt.informatik.tk.android.assistance.handler.DrawerClickHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ToggleModuleRequest;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.DrawerItem;
import de.tudarmstadt.informatik.tk.android.assistance.service.ModuleService;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModule;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleInstallation;
import de.tudarmstadt.informatik.tk.android.kraken.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.kraken.provider.DbProvider;
import de.tudarmstadt.informatik.tk.android.kraken.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.kraken.provider.PreferenceProvider;
import de.tudarmstadt.informatik.tk.android.kraken.service.GcmRegistrationIntentService;
import de.tudarmstadt.informatik.tk.android.kraken.util.GcmUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Module information dashboard
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class MainActivity extends DrawerActivity implements DrawerClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DbProvider dbProvider;

    private Menu menu;

    private List<DbModuleInstallation> dbModuleInstallations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean accessibilityServiceActivated = PreferenceProvider.getInstance(getApplicationContext()).getActivated();

        if (accessibilityServiceActivated) {
            initView();
        } else {

            Log.d(TAG, "Accessibility Service is NOT active! Showing tutorial...");

            Intent intent = new Intent(this, AccessibilityTutorialActivity.class);
            startActivityForResult(intent, Constants.INTENT_ACCESSIBILITY_SERVICE_IGNORED_RESULT);
        }
    }

    /**
     * Initializes this activity
     */
    private void initView() {

        if (dbProvider == null) {
            dbProvider = DbProvider.getInstance(getApplicationContext());
        }

        registerForPush();

        HarvesterServiceProvider.getInstance(getApplicationContext()).startSensingService();

        long userId = UserUtils.getCurrentUserId(getApplicationContext());

        if (dbModuleInstallations == null) {

            dbModuleInstallations = dbProvider.getModuleInstallationsByUserId(userId);

            // user has got some active modules -> activate module menu
            if (dbModuleInstallations != null && !dbModuleInstallations.isEmpty()) {

                getLayoutInflater().inflate(R.layout.activity_main, mFrameLayout);
                setTitle(R.string.main_activity_title);

                mDrawerFragment.updateDrawerBody(getApplicationContext());

            } else {

                HarvesterServiceProvider.getInstance(getApplicationContext()).stopSensingService();

                Intent intent = new Intent(this, AvailableModulesActivity.class);
                startActivity(intent);
                finish();
            }

        } else {

            getLayoutInflater().inflate(R.layout.activity_main, mFrameLayout);
            setTitle(R.string.main_activity_title);

            mDrawerFragment.updateDrawerBody(getApplicationContext());
        }
    }

    /**
     * Registers for GCM push notifications
     */
    private void registerForPush() {

        boolean isTokenWasSent = UserUtils.isGcmTokenWasSent(getApplicationContext());

        if (isTokenWasSent) {
            return;
        }

        // check for play services installation
        if (GcmUtils.isPlayServicesInstalled(this)) {

            Log.d(TAG, "Google Play Services are installed.");

            // starting registration GCM service
            Intent intent = new Intent(this, GcmRegistrationIntentService.class);
            startService(intent);

            UserUtils.saveGcmTokenWasSent(getApplicationContext(), true);

        } else {
            Log.d(TAG, "Google Play Services NOT installed.");

            UserUtils.saveGcmTokenWasSent(getApplicationContext(), false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // if we have no modules installed -> no menu will be visible
        if (dbModuleInstallations != null && !dbModuleInstallations.isEmpty()) {

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.module_menu, menu);

            this.menu = menu;

            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_module_toggle_state:

                int result = toggleModuleState(item.isChecked());

                switch (result) {
                    case 0:
                        item.setChecked(false);
                        break;
                    case 1:
                        item.setChecked(true);
                        break;
                    default:
                        break;
                }

                return true;

            case R.id.menu_module_uninstall:

                Log.d(TAG, "User clicked module uninstall");

                moduleUninstall();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        updateMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Updates module menu
     */
    private void updateMenu() {

        Log.d(TAG, "Updating menu..");

        if (menu != null) {

            DbModule currentModule = getCurrentActiveModuleFromDrawer().getDbModule();
            List<DbModuleInstallation> moduleInstallations = currentModule.getDbModuleInstallationList();

            MenuItem toggleModuleStateItem = menu.findItem(R.id.menu_module_toggle_state);

            for (DbModuleInstallation installedModule : moduleInstallations) {

                if (installedModule.getModuleId().equals(currentModule.getId())) {
                    toggleModuleStateItem.setChecked(installedModule.getActive());
                }
            }
        }
    }

    /**
     * Enables or disables currently selected module
     *
     * @param moduleState
     */
    private int toggleModuleState(boolean moduleState) {

        DbModule currentModule = getCurrentActiveModuleFromDrawer().getDbModule();

        DbModuleInstallation moduleInstallation = dbProvider.getModuleInstallationForModuleByUserId(
                currentModule.getUserId(),
                currentModule.getId());

        if (moduleInstallation == null) {
            Toaster.showLong(getApplicationContext(), R.string.error_module_not_installed);
        } else {

            moduleInstallation.setActive(moduleState);

            dbProvider.updateModuleInstallation(moduleInstallation);

            if (moduleState) {
                Log.d(TAG, "User DISABLED a module");
                return 0;
            } else {
                Log.d(TAG, "User ENABLED a module");
                return 1;
            }

        }

        return -1;
    }

    /**
     * Gives current selected module by user via navigation drawer
     *
     * @return
     */
    private DbModuleInstallation getCurrentActiveModuleFromDrawer() {

        DrawerItem item = mDrawerFragment.getNavigationItems().get(mDrawerFragment.getCurrentSelectedPosition());
        return item.getModule();
    }

    /**
     * Uninstalls currently selected module
     */
    private void moduleUninstall() {

        String userToken = UserUtils.getUserToken(getApplicationContext());

        DbModule currentModule = getCurrentActiveModuleFromDrawer().getDbModule();

        Log.d(TAG, "Uninstall module. ModuleId: " + currentModule.getId() + " package: " + currentModule.getPackageName());

        ToggleModuleRequest toggleModuleRequest = new ToggleModuleRequest();
        toggleModuleRequest.setModuleId(currentModule.getPackageName());

        ModuleService moduleService = EndpointGenerator.create(ModuleService.class);
        moduleService.deactivateModule(userToken, toggleModuleRequest, new Callback<Void>() {

            @Override
            public void success(Void aVoid, Response response) {

                // deactivation successful
                if (response.getStatus() == 200) {

                    uninstallModuleFromDb();

                    Snackbar
                            .make(findViewById(android.R.id.content), R.string.main_activity_undo_uninstall, Snackbar.LENGTH_LONG)
                            .setAction(R.string.main_activity_undo_uninstall_button_title, new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG, "User tapped UNDO uninstall of a module.");
                                }
                            })
                            .setActionTextColor(Color.RED)
                            .show();

                    mDrawerFragment.updateDrawerBody(getApplicationContext());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorMessages(TAG, error);

                // no such installed module -> remove it immediately
                if (error.getResponse() == null || error.getResponse().getStatus() == 400) {
                    uninstallModuleFromDb();

                    mDrawerFragment.updateDrawerBody(getApplicationContext());
                }
            }
        });
    }

    /**
     * Removes module installation for user and module ids
     */
    private void uninstallModuleFromDb() {

        DbModuleInstallation currentModule = getCurrentActiveModuleFromDrawer();

        long currentUserId = currentModule.getUserId();
        long currentModuleId = currentModule.getModuleId();

        Log.d(TAG, "Current user id: " + currentUserId);
        Log.d(TAG, "Current module id: " + currentModuleId);

        Log.d(TAG, "Removing module from db...");

        List<DbModuleInstallation> installedModules = dbProvider.getModuleInstallationsByUserId(
                currentUserId,
                currentModuleId);

        dbProvider.removeInstalledModules(installedModules);

        Log.d(TAG, "Finished removing module from db!");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case Constants.INTENT_ACCESSIBILITY_SERVICE_IGNORED_RESULT:
                initView();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(View v, int position) {
        // TODO
    }
}
