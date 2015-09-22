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
import de.tudarmstadt.informatik.tk.android.assistance.adapter.DrawerAdapter;
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
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Module information dashboard
 */
public class MainActivity extends DrawerActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DbModuleDao moduleDao;

    private DbModuleInstallationDao moduleInstallationDao;

    private List<DbModuleInstallation> dbModuleInstallations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

                ((DrawerAdapter) mDrawerFragment.getDrawerList().getAdapter()).selectPosition(2);
                mDrawerFragment.updateDrawerBody(getApplicationContext());

            } else {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // if we have no modules installed -> no module menu will be showed
        if (dbModuleInstallations != null && !dbModuleInstallations.isEmpty()) {
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

        long moduleId = UserUtils.getCurrentModuleId(getApplicationContext());
        long userId = UserUtils.getCurrentUserId(getApplicationContext());

        if (moduleInstallationDao == null) {
            moduleInstallationDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDbModuleInstallationDao();
        }

        DbModuleInstallation moduleInstallation = moduleInstallationDao
                .queryBuilder()
                .where(DbModuleInstallationDao.Properties.UserId.eq(userId))
                .where(DbModuleInstallationDao.Properties.ModuleId.eq(moduleId))
                .limit(1)
                .build()
                .unique();

        if (moduleInstallation == null) {
            Toaster.showLong(getApplicationContext(), R.string.error_module_not_installed);
        } else {
            moduleInstallation.setActive(moduleState);
            moduleInstallationDao.update(moduleInstallation);
        }
    }

    /**
     * Uninstalls currently selected module
     */
    private void moduleUninstall() {

        String userToken = UserUtils.getUserToken(getApplicationContext());

        DrawerItem item = mDrawerFragment.getNavigationItems().get(mDrawerFragment.getCurrentSelectedPosition());
        DbModule currentModule = item.getModule();

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

        DrawerItem item = mDrawerFragment.getNavigationItems().get(mDrawerFragment.getCurrentSelectedPosition());
        DbModule currentModule = item.getModule();
        long currentUserId = currentModule.getUserId();
        long currentModuleId = currentModule.getId();

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
