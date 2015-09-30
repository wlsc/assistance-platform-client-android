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

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.common.DrawerActivity;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ToggleModuleRequest;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.DrawerItem;
import de.tudarmstadt.informatik.tk.android.assistance.service.ModuleService;
import de.tudarmstadt.informatik.tk.android.assistance.util.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.communication.ServiceGenerator;
import de.tudarmstadt.informatik.tk.android.kraken.db.DatabaseManager;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModule;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleDao;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleInstallation;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleInstallationDao;
import de.tudarmstadt.informatik.tk.android.kraken.KrakenServiceManager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Module information dashboard
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class MainActivity extends DrawerActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Menu menu;

    private DbModuleDao moduleDao;

    private DbModuleInstallationDao moduleInstallationDao;

    private List<DbModuleInstallation> dbModuleInstallations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startSensingService();

        long userId = UserUtils.getCurrentUserId(getApplicationContext());

        if (moduleDao == null) {
            moduleDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDbModuleDao();
        }

        if (moduleInstallationDao == null) {
            moduleInstallationDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDbModuleInstallationDao();
        }

        if (dbModuleInstallations == null) {
            dbModuleInstallations = moduleInstallationDao
                    .queryBuilder()
                    .where(DbModuleInstallationDao.Properties.UserId.eq(userId))
                    .build()
                    .list();

            // user has got some active modules -> activate module menu
            if (dbModuleInstallations != null && !dbModuleInstallations.isEmpty()) {

                getLayoutInflater().inflate(R.layout.activity_main, mFrameLayout);
                setTitle(R.string.main_activity_title);

                mDrawerFragment.updateDrawerBody(getApplicationContext());

            } else {

                stopSensingService();

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
     * Releases the Kraken.
     */
    private void startSensingService() {

        KrakenServiceManager service = KrakenServiceManager.getInstance(getApplicationContext());
        service.startKrakenService();
    }

    /**
     * Calms down the Kraken.
     */
    private void stopSensingService() {

        KrakenServiceManager service = KrakenServiceManager.getInstance(getApplicationContext());
        service.stopKrakenService();
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

        if (moduleInstallationDao == null) {
            moduleInstallationDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDbModuleInstallationDao();
        }

        DbModuleInstallation moduleInstallation = moduleInstallationDao
                .queryBuilder()
                .where(DbModuleInstallationDao.Properties.UserId.eq(currentModule.getUserId()))
                .where(DbModuleInstallationDao.Properties.ModuleId.eq(currentModule.getId()))
                .limit(1)
                .build()
                .unique();

        if (moduleInstallation == null) {
            Toaster.showLong(getApplicationContext(), R.string.error_module_not_installed);
        } else {

            moduleInstallation.setActive(moduleState);
            moduleInstallationDao.update(moduleInstallation);

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

        ModuleService moduleService = ServiceGenerator.createService(ModuleService.class);
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

        List<DbModuleInstallation> installedModules = moduleInstallationDao
                .queryBuilder()
                .where(DbModuleInstallationDao.Properties.ModuleId.eq(currentModuleId))
                .where(DbModuleInstallationDao.Properties.UserId.eq(currentUserId))
                .build()
                .list();

        moduleInstallationDao.deleteInTx(installedModules);

        Log.d(TAG, "Finished removing module from db!");
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);

        moduleDao = null;
        moduleInstallationDao = null;

        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
    }

}
