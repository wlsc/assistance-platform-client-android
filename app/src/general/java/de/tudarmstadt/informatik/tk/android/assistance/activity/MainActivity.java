package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.ModuleEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.UserEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ToggleModuleRequest;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.profile.ProfileResponse;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferencesUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbModuleInstallation;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbUser;
import de.tudarmstadt.informatik.tk.android.kraken.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.kraken.provider.DbProvider;
import de.tudarmstadt.informatik.tk.android.kraken.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.kraken.provider.PreferenceProvider;
import de.tudarmstadt.informatik.tk.android.kraken.service.GcmRegistrationIntentService;
import de.tudarmstadt.informatik.tk.android.kraken.util.DateUtils;
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
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DbProvider dbProvider;

    private Toolbar mToolbar;

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

        long userId = UserUtils.getCurrentUserId(getApplicationContext());

        if (dbModuleInstallations == null) {

            dbModuleInstallations = dbProvider.getModuleInstallationsByUserId(userId);

            // user has got some active modules -> activate module menu
            if (dbModuleInstallations != null && !dbModuleInstallations.isEmpty()) {

                HarvesterServiceProvider.getInstance(getApplicationContext()).startSensingService();

                setContentView(R.layout.activity_main);
                setTitle(R.string.main_activity_title);

                mToolbar = ButterKnife.findById(this, R.id.toolbar);
//                setSupportActionBar(mToolbar);
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//                getSupportActionBar().setDisplayShowHomeEnabled(true);

                CollapsingToolbarLayout collapsingToolbar = ButterKnife.findById(this, R.id.collapsing_toolbar);
                collapsingToolbar.setTitle("yey");

            } else {

                HarvesterServiceProvider.getInstance(getApplicationContext()).stopSensingService();

                Intent intent = new Intent(this, AvailableModulesActivity.class);
                startActivity(intent);
                finish();
            }

        } else {

            HarvesterServiceProvider.getInstance(getApplicationContext()).startSensingService();

            setContentView(R.layout.activity_main);
            setTitle(R.string.main_activity_title);

            mToolbar = ButterKnife.findById(this, R.id.toolbar);
//            setSupportActionBar(mToolbar);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);

            CollapsingToolbarLayout collapsingToolbar = ButterKnife.findById(this, R.id.collapsing_toolbar);
            collapsingToolbar.setTitle("yey");
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

        getMenuInflater().inflate(R.menu.news_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_settings:
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
            // TODO:
//            DbModule currentModule = getCurrentActiveModuleFromDrawer().getDbModule();
//            List<DbModuleInstallation> moduleInstallations = currentModule.getDbModuleInstallationList();
//
//            MenuItem toggleModuleStateItem = menu.findItem(R.id.menu_module_toggle_state);
//
//            for (DbModuleInstallation installedModule : moduleInstallations) {
//
//                if (installedModule.getModuleId().equals(currentModule.getId())) {
//                    toggleModuleStateItem.setChecked(installedModule.getActive());
//                }
//            }
        }
    }

    /**
     * Enables or disables currently selected module
     *
     * @param moduleState
     */
    private int toggleModuleState(boolean moduleState) {

        // TODO:
//        DbModule currentModule = getCurrentActiveModuleFromDrawer().getDbModule();
//
//        DbModuleInstallation moduleInstallation = dbProvider.getModuleInstallationForModuleByUserId(
//                currentModule.getUserId(),
//                currentModule.getId());
//
//        if (moduleInstallation == null) {
//            Toaster.showLong(getApplicationContext(), R.string.error_module_not_installed);
//        } else {
//
//            moduleInstallation.setActive(moduleState);
//
//            dbProvider.updateModuleInstallation(moduleInstallation);
//
//            if (moduleState) {
//                Log.d(TAG, "User DISABLED a module");
//                return 0;
//            } else {
//                Log.d(TAG, "User ENABLED a module");
//                return 1;
//            }
//
//        }

        return -1;
    }

    /**
     * Uninstalls currently selected module
     */
    private void moduleUninstall() {

        String userToken = UserUtils.getUserToken(getApplicationContext());

        // TODO:
//        DbModule currentModule = getCurrentActiveModuleFromDrawer().getDbModule();
//
//        Log.d(TAG, "Uninstall module. ModuleId: " + currentModule.getId() + " package: " + currentModule.getPackageName());

        ToggleModuleRequest toggleModuleRequest = new ToggleModuleRequest();
//        toggleModuleRequest.setModuleId(currentModule.getPackageName());

        ModuleEndpoint moduleEndpoint = EndpointGenerator.create(ModuleEndpoint.class);
        moduleEndpoint.deactivateModule(userToken, toggleModuleRequest, new Callback<Void>() {

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
                }
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorMessages(TAG, error);

                // no such installed module -> remove it immediately
                if (error.getResponse() == null || error.getResponse().getStatus() == 400) {
                    uninstallModuleFromDb();
                }
            }
        });
    }

    /**
     * Removes module installation for user and module ids
     */
    private void uninstallModuleFromDb() {

        // TODO:
//        DbModuleInstallation currentModule = getCurrentActiveModuleFromDrawer();
//
//        long currentUserId = currentModule.getUserId();
//        long currentModuleId = currentModule.getModuleId();
//
//        Log.d(TAG, "Current user id: " + currentUserId);
//        Log.d(TAG, "Current module id: " + currentModuleId);

//        Log.d(TAG, "Removing module from db...");
//
//        List<DbModuleInstallation> installedModules = dbProvider.getModuleInstallationsByUserId(
//                currentUserId,
//                currentModuleId);
//
//        dbProvider.removeInstalledModules(installedModules);
//
//        Log.d(TAG, "Finished removing module from db!");
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

    /**
     * Requests user profile information
     */
    private void requestUserProfile() {

        String userToken = UserUtils.getUserToken(getApplicationContext());

        UserEndpoint userservice = EndpointGenerator.create(UserEndpoint.class);
        userservice.getUserProfileShort(userToken, new Callback<ProfileResponse>() {

            @Override
            public void success(ProfileResponse profileResponse, Response response) {

                if (profileResponse == null) {
                    return;
                }

                UserUtils.saveUserFirstname(getApplicationContext(), profileResponse.getFirstname());
                UserUtils.saveUserLastname(getApplicationContext(), profileResponse.getLastname());
                UserUtils.saveUserEmail(getApplicationContext(), profileResponse.getPrimaryEmail());

                persistLogin(profileResponse);
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorMessages(TAG, error);
            }
        });
    }

    /**
     * Updates existent user login or creates one in db
     *
     * @param profileResponse
     */
    private void persistLogin(ProfileResponse profileResponse) {

        // check already available user in db
        DbUser user = dbProvider.getUserByEmail(profileResponse.getPrimaryEmail());

        // check for user existence in the db
        if (user == null) {
            // no user found -> create one

            user = new DbUser();

            user.setFirstname(profileResponse.getFirstname());
            user.setLastname(profileResponse.getLastname());
            user.setPrimaryEmail(profileResponse.getPrimaryEmail());

            if (profileResponse.getJoinedSince() != null) {
                user.setJoinedSince(DateUtils.dateToISO8601String(new Date(profileResponse.getJoinedSince()), Locale.getDefault()));
            }

            if (profileResponse.getLastLogin() != null) {
                user.setLastLogin(DateUtils.dateToISO8601String(new Date(profileResponse.getLastLogin()), Locale.getDefault()));
            }

            user.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

            dbProvider.insertUser(user);

        } else {
            // found a user -> update for device and user information

            user.setFirstname(profileResponse.getFirstname());
            user.setLastname(profileResponse.getLastname());
            user.setPrimaryEmail(profileResponse.getPrimaryEmail());

            if (profileResponse.getJoinedSince() != null) {
                user.setJoinedSince(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));
            }

            if (profileResponse.getLastLogin() != null) {
                user.setLastLogin(DateUtils.dateToISO8601String(new Date(profileResponse.getLastLogin()), Locale.getDefault()));
            }

            dbProvider.updateUser(user);
        }
    }

    /**
     * Processes error response from server
     *
     * @param TAG
     * @param retrofitError
     */
    protected void showErrorMessages(String TAG, RetrofitError retrofitError) {

        Response response = retrofitError.getResponse();

        if (response != null) {

            int httpCode = response.getStatus();

            switch (httpCode) {
                case 400:
                    Toaster.showLong(getApplicationContext(), R.string.error_service_bad_request);
                    break;
                case 401:
                    Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
                    PreferencesUtils.clearUserCredentials(getApplicationContext());
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
                case 404:
                    Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
                    break;
                case 503:
                    Toaster.showLong(getApplicationContext(), R.string.error_server_temporary_unavailable);
                    break;
                default:
                    Toaster.showLong(getApplicationContext(), R.string.error_unknown);
                    break;
            }
        } else {
            Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
        }
    }
}
