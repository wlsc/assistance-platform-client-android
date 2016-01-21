package de.tudarmstadt.informatik.tk.assistance.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.google.gson.Gson;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.activity.base.BaseActivity;
import de.tudarmstadt.informatik.tk.assistance.adapter.ModuleGlobalCapsAdapter;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleAllowedPermissionStateChangedEvent;
import de.tudarmstadt.informatik.tk.assistance.model.item.ModuleAllowedTypeItem;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.sdk.Config;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbFacebookSensor;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleAllowedCapabilities;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbTucanSensor;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ToggleModuleRequestDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.sensing.SensorApiType;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.ApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.SensorProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.SocialProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.api.ModuleApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.DateUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.RxUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.StringUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import rx.Subscriber;
import rx.Subscription;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.01.2016
 */
public class ModuleCapabilitiesPermissionActivity extends BaseActivity {

    private static final String TAG = ModuleCapabilitiesPermissionActivity.class.getSimpleName();

    private DaoProvider daoProvider;

    private ApiProvider apiProvider;

    private SocialProvider socialProvider;

    // facebook stuff
    private CallbackManager callbackManager;

    private PermissionUtil.PermissionRequestObject mRequestObject;

    private Subscription subModuleDeactivation;

    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;

    @Bind(R.id.permissionRecyclerView)
    protected RecyclerView mPermissionsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_types_permission);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void subscribeRequests() {

    }

    @Override
    protected void unsubscribeRequests() {

    }

    @Override
    protected void recreateRequests() {

    }

    /**
     * Paints the view
     */
    private void initView() {

        apiProvider = ApiProvider.getInstance(getApplicationContext());
        daoProvider = DaoProvider.getInstance(getApplicationContext());
        socialProvider = SocialProvider.getInstance(getApplicationContext());

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        ButterKnife.bind(this);

        try {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (Exception e) {
            // fix for Samsung Android 4.2.2 AppCompat ClassNotFoundException
        }

        setTitle(R.string.settings_module_allowed_capability_title);

        Resources resources = getResources();

        String userToken = PreferenceUtils.getUserToken(getApplicationContext());
        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            return;
        }

        List<DbModule> activeModules = daoProvider.getModuleDao().getAllActive(user.getId());

        List<DbModuleAllowedCapabilities> allAllowedModuleCapsDb = daoProvider
                .getModuleAllowedCapsDao()
                .getAll();

        // calculate numbers
        SparseIntArray usageCounters = new SparseIntArray();

        for (DbModule dbModule : activeModules) {

            List<DbModuleCapability> caps = dbModule.getDbModuleCapabilityList();

            for (DbModuleCapability cap : caps) {

                // not active or not required capability will not be counted
                if (!cap.getActive() || !cap.getRequired()) {
                    continue;
                }

                String type = cap.getType();

                for (DbModuleAllowedCapabilities dbAllowedCap : allAllowedModuleCapsDb) {

                    if (dbAllowedCap.getType().equals(type)) {

                        int intType = SensorApiType.getDtoType(type);

                        if (usageCounters.get(intType) == 0) {
                            usageCounters.put(intType, 1);
                        } else {
                            int oldNumber = usageCounters.get(intType);
                            oldNumber++;
                            usageCounters.put(intType, oldNumber);
                        }
                    }
                }
            }
        }

        PermissionUtils permUtils = PermissionUtils.getInstance(getApplicationContext());
        Map<String, String[]> dangerousPerms = permUtils.getDangerousPermissionsToDtoMapping();
        List<ModuleAllowedTypeItem> allAllowedModuleCaps = new ArrayList<>(allAllowedModuleCapsDb.size());

        for (DbModuleAllowedCapabilities dbAllowedCap : allAllowedModuleCapsDb) {

            boolean isAllowed = dbAllowedCap.getIsAllowed();
            int type = SensorApiType.getDtoType(dbAllowedCap.getType());

            if (dangerousPerms.get(dbAllowedCap.getType()) != null) {
                String[] perms = dangerousPerms.get(dbAllowedCap.getType());

                if (permUtils.isGranted(perms)) {
                    isAllowed = true;
                }
            }

            allAllowedModuleCaps.add(
                    new ModuleAllowedTypeItem(
                            type,
                            SensorApiType.getName(type, resources),
                            isAllowed,
                            usageCounters.get(type)));
        }

        mPermissionsRecyclerView.setHasFixedSize(true);
        mPermissionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPermissionsRecyclerView.setAdapter(new ModuleGlobalCapsAdapter(allAllowedModuleCaps));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.d(TAG, "Back pressed");
        finish();
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        RxUtils.unsubscribe(subModuleDeactivation);
        super.onDestroy();
    }

    /**
     * Allowed module permission state message changed handler
     *
     * @param event
     */
    public void onEvent(ModuleAllowedPermissionStateChangedEvent event) {

        Log.d(TAG, "ModuleAllowedPermissionStateChangedEvent invoked");

        if (event.getNumReqModules() > 0) {
            // some modules are using in their required capabilities

            handleDisablingCapabilityWithRestriction(event.getCapType(), event.isChecked(), event.getNumReqModules());

        } else {
            // no other module uses required capability that will be modified

            String type = SensorApiType.getApiName(event.getCapType());

            if (event.isChecked() && isSpecialCapability(event.getCapType())) {
                Log.d(TAG, "Handling special capability type: " + type);
                handleSpecialCapType(event.getCapType());
                return;
            }

            Map<String, String[]> dangerousGroup = PermissionUtils
                    .getInstance(getApplicationContext())
                    .getDangerousPermissionsToDtoMapping();

            String[] perms = dangerousGroup.get(type);

            if (perms == null) {
                Log.d(TAG, "Do not need perm for the type: " + type);
                updateModuleAllowedCapabilityDbEntry(event.getCapType(), event.isChecked());
                return;
            }

            mRequestObject = PermissionUtil.with(this)
                    .request(perms)
                    .onAnyDenied(new Func() {

                        @Override
                        protected void call() {
                            Log.d(TAG, "Permission was denied");
                            updateModuleAllowedCapabilityDbEntry(event.getCapType(), false);
                            updateModuleAllowedCapabilitySwitcher(event.getCapType(), false);
                        }
                    })
                    .onAllGranted(new Func() {

                        @Override
                        protected void call() {
                            Log.d(TAG, "Permission was granted");
                            updateModuleAllowedCapabilityDbEntry(event.getCapType(), event.isChecked());
                            updateModuleAllowedCapabilitySwitcher(event.getCapType(), event.isChecked());
                        }
                    })
                    .ask(Config.PERM_MODULE_ALLOWED_CAPABILITY);

        }
    }

    /**
     * Handles types like social logins and another one time sensors
     *
     * @param type
     */
    private void handleSpecialCapType(int type) {

        switch (type) {
            case SensorApiType.UNI_TUCAN:
                showTucanDialog();
                break;
            case SensorApiType.SOCIAL_FACEBOOK:
                showFacebookDialog();
                break;
        }
    }

    /**
     * Shows dialog with credential information
     */
    private void showTucanDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAppCompatAlertDialog);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_enter_credentials, null);

        builder.setView(dialogView);

        EditText usernameET = ButterKnife.findById(dialogView, R.id.username);
        EditText passwordET = ButterKnife.findById(dialogView, R.id.password);

        usernameET.setHint(R.string.dialog_social_tucan_username);
        passwordET.setHint(R.string.password);

        builder.setPositiveButton(R.string.button_ok, (dialog, which) -> {
            // dummy
        });

        builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> {
            updateModuleAllowedCapabilitySwitcher(SensorApiType.UNI_TUCAN, false);
            dialog.cancel();
        });

        builder.setCancelable(false);

        builder.setTitle(R.string.sensor_uni_tucan);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        /*
         * Modifying behavior of buttons
         */
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {

                    boolean dialogShouldClose = false;

                    Log.d(TAG, "User confirmed TUCAN credentials");

                    String username = usernameET.getText().toString().trim();
                    String password = passwordET.getText().toString().trim();

                    if (isInputOk(usernameET, passwordET)) {

                        showLoading();
                        storeTucanCredentials(username, password);
                        updateModuleAllowedCapabilityDbEntry(SensorApiType.UNI_TUCAN, true);
                        updateModuleAllowedCapabilitySwitcher(SensorApiType.UNI_TUCAN, true);
                        hideLoading();

                        dialogShouldClose = true;

                    } else {
                        updateModuleAllowedCapabilitySwitcher(SensorApiType.UNI_TUCAN, false);
                    }

                    if (dialogShouldClose) {
                        Toaster.showLong(getApplicationContext(), R.string.changes_were_saved);
                        alertDialog.dismiss();
                    }
                });
    }

    private void storeTucanCredentials(String username, String password) {

        String userToken = PreferenceUtils.getUserToken(getApplicationContext());
        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            Log.d(TAG, "storeTucanCredentials: User is NULL");
            return;
        }

        Log.d(TAG, "storeTucanCredentials: Storing...");

        DbTucanSensor sensorEntry = daoProvider.getTucanSensorDao().getForUserId(user.getId());

        // we have here entry already -> update it
        if (sensorEntry != null) {

            sensorEntry.setUsername(username);
            sensorEntry.setPassword(password);
            sensorEntry.setWasChanged(Boolean.TRUE);

            daoProvider.getTucanSensorDao().update(sensorEntry);

        } else {

            // create new entry
            DbTucanSensor sensor = new DbTucanSensor();

            sensor.setUsername(username);
            sensor.setPassword(password);
            sensor.setWasChanged(Boolean.TRUE);
            sensor.setUserId(user.getId());
            sensor.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

            daoProvider.getTucanSensorDao().insert(sensor);
        }

        Log.d(TAG, "storeTucanCredentials: Stored.");
    }

    private boolean isInputOk(EditText usernameET, EditText passwordET) {

        Log.d(TAG, "Checking user data...");

        String username = usernameET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        usernameET.setError(null);
        passwordET.setError(null);

        if (StringUtils.isNullOrEmpty(username)) {
            usernameET.setError(getString(R.string.error_field_required));
            usernameET.requestFocus();
            return false;
        }

        if (StringUtils.isNullOrEmpty(password)) {
            passwordET.setError(getString(R.string.error_field_required));
            passwordET.requestFocus();
            return false;
        }

        Log.d(TAG, "User data OK!");

        return true;
    }

    /**
     * Shows dialog with credential information
     */
    private void showFacebookDialog() {

        Log.d(TAG, "User wants FACEBOOK credentials");

        showLoading();

        // register callback
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        Log.d(TAG, "Facebook Access Token: " + loginResult.getAccessToken().getToken());

                        storeFacebookCredentials(
                                loginResult.getAccessToken().getToken(),
                                loginResult.getRecentlyGrantedPermissions(),
                                loginResult.getRecentlyDeniedPermissions());
                        updateModuleAllowedCapabilityDbEntry(SensorApiType.SOCIAL_FACEBOOK, true);
                        updateModuleAllowedCapabilitySwitcher(SensorApiType.SOCIAL_FACEBOOK, true);

                        hideLoading();
                    }

                    @Override
                    public void onCancel() {
                        Toaster.showShort(getApplicationContext(), R.string.error_unknown);
                        updateModuleAllowedCapabilitySwitcher(SensorApiType.SOCIAL_FACEBOOK, false);
                        hideLoading();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toaster.showShort(getApplicationContext(), R.string.error_service_not_available);
                        updateModuleAllowedCapabilitySwitcher(SensorApiType.SOCIAL_FACEBOOK, false);
                        hideLoading();
                    }
                });

        // do a call
        LoginManager.getInstance()
                .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));

    }

    private void storeFacebookCredentials(String oauthToken,
                                          Set<String> permissions,
                                          Set<String> deniedPermissions) {

        String userToken = PreferenceUtils.getUserToken(getApplicationContext());
        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            Log.d(TAG, "storeFacebookCredentials: User is NULL");
            return;
        }

        Log.d(TAG, "storeFacebookCredentials: Storing...");

        DbFacebookSensor sensorEntry = daoProvider.getFacebookSensorDao().getForUserId(user.getId());

        // we have here entry already -> update it
        if (sensorEntry != null) {

            sensorEntry.setOauthToken(oauthToken);
            sensorEntry.setPermissions((new Gson().toJson(permissions)));
            sensorEntry.setPermissionsDeclined((new Gson().toJson(deniedPermissions)));
            sensorEntry.setWasChanged(Boolean.TRUE);

            daoProvider.getFacebookSensorDao().update(sensorEntry);

        } else {

            // create new entry
            DbFacebookSensor sensor = new DbFacebookSensor();

            sensor.setOauthToken(oauthToken);
            sensor.setPermissions((new Gson().toJson(permissions)));
            sensor.setPermissionsDeclined((new Gson().toJson(deniedPermissions)));
            sensor.setUserId(user.getId());
            sensor.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));
            sensor.setWasChanged(Boolean.TRUE);

            daoProvider.getFacebookSensorDao().insert(sensor);
        }

        Log.d(TAG, "storeFacebookCredentials: Stored.");
    }

    /**
     * Handles types like social login and another one time sensors
     *
     * @param type
     * @return
     */
    private boolean isSpecialCapability(int type) {

        boolean result = true;

        switch (type) {
            case SensorApiType.UNI_TUCAN:
            case SensorApiType.SOCIAL_FACEBOOK:
                break;
            default:
                result = false;
                break;
        }

        return result;
    }

    private void updateModuleAllowedCapabilityDbEntry(int capType, boolean isChecked) {

        Log.d(TAG, "UpdateModuleAllowedCapabilityStateEvent invoked");

        String userToken = PreferenceUtils.getUserToken(getApplicationContext());
        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            Log.d(TAG, "User is NULL");
            return;
        }

        String type = SensorApiType.getApiName(capType);
        DbModuleAllowedCapabilities allowedCapability = daoProvider.getModuleAllowedCapsDao()
                .get(type, user.getId());

        if (allowedCapability == null) {
            Log.d(TAG, "allowedCapability is NULL");
            return;
        }

        allowedCapability.setIsAllowed(isChecked);

        daoProvider.getModuleAllowedCapsDao().update(allowedCapability);

        Log.d(TAG, "Allowed capability was refreshed: " + isChecked);
    }

    /**
     * If module capability is used by another module
     *
     * @param capType
     * @param isChecked
     * @param numReqModules
     */
    private void handleDisablingCapabilityWithRestriction(int capType, boolean isChecked, int numReqModules) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAppCompatAlertDialog);

        builder.setPositiveButton(R.string.button_disable, (dialog, which) -> {
            Log.d(TAG, "User tapped positive button");
            disableActiveModulesForType(capType);
        });

        builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> {

            Log.d(TAG, "User tapped negative button");
            updateModuleAllowedCapabilitySwitcher(capType, true);
            dialog.cancel();
        });

        builder.setOnCancelListener(dialog -> {

            Log.d(TAG, "Negative dialog event invoked");
            updateModuleAllowedCapabilitySwitcher(capType, true);
            dialog.cancel();
        });

        builder.setTitle(R.string.settings_module_allowed_capability_disable_header);
        builder.setMessage(R.string.settings_module_allowed_capability_disable_message2);
//        builder.setMessage(getResources()
//                .getQuantityString(
//                        R.plurals.settings_module_allowed_capability_disable_message,
//                        numReqModules));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Disables active modules that required this capability type
     *
     * @param capType
     */
    private void disableActiveModulesForType(int capType) {

        Log.d(TAG, "disableActiveModulesForType: " + capType);

        String userToken = PreferenceUtils.getUserToken(getApplicationContext());
        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            Log.d(TAG, "User is NULL");
            return;
        }

        ToggleModuleRequestDto request = new ToggleModuleRequestDto();

        String typeStr = SensorApiType.getApiName(capType);

        List<DbModule> activeModules = daoProvider.getModuleDao().getAllActive(user.getId());

        ModuleApiProvider moduleApiProvider = apiProvider.getModuleApiProvider();

        for (DbModule dbModule : activeModules) {

            request.setModulePackageName(dbModule.getPackageName());

            // deactivate module
            subModuleDeactivation = moduleApiProvider
                    .deactivateModule(userToken, request)
                    .subscribe(new ModuleDeactivationSubscriber());

            List<DbModuleCapability> caps = dbModule.getDbModuleCapabilityList();

            for (DbModuleCapability cap : caps) {

                // we have a match -> disable that module
                if (typeStr.equals(cap.getType()) && cap.getRequired() && cap.getActive()) {
                    dbModule.setActive(false);
                    break;
                }
            }
        }

        // update modules state info
        daoProvider.getModuleDao().update(activeModules);

        // update global capability permission state
        updateModuleAllowedCapabilityDbEntry(capType, false);

        // synchronize db active module capabilities with running caps
        SensorProvider.getInstance(getApplicationContext()).synchronizeRunningSensorsWithDb();
    }

    /**
     * Set switcher checked or not ;)
     *
     * @param capType
     * @param isChecked
     */
    private void updateModuleAllowedCapabilitySwitcher(int capType, boolean isChecked) {

        ModuleGlobalCapsAdapter adapter = (ModuleGlobalCapsAdapter) mPermissionsRecyclerView.getAdapter();

        if (adapter == null) {
            Log.d(TAG, "Adapter is null");
            return;
        }

        List<ModuleAllowedTypeItem> allowedPermItems = adapter.getData();

        for (ModuleAllowedTypeItem item : allowedPermItems) {
            if (item.getType() == capType) {
                item.setAllowed(isChecked);
                break;
            }
        }

        adapter.swapData(allowedPermItems);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        mRequestObject.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class ModuleDeactivationSubscriber extends Subscriber<Void> {

        @Override
        public void onCompleted() {

            Log.d(TAG, "subModuleDeactivation has been completed");
        }

        @Override
        public void onError(Throwable e) {
            Toaster.showLong(getApplicationContext(), R.string.error_unknown);
        }

        @Override
        public void onNext(Void aVoid) {
            Log.d(TAG, "subModuleDeactivation successfully called");
        }
    }
}