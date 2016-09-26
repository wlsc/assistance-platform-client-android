package de.tudarmstadt.informatik.tk.assistance.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.facebook.CallbackManager;
import com.facebook.CallbackManager.Factory;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.github.kayvannj.permission_utils.PermissionUtil.PermissionRequestObject;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.assistance.R.id;
import de.tudarmstadt.informatik.tk.assistance.R.layout;
import de.tudarmstadt.informatik.tk.assistance.R.string;
import de.tudarmstadt.informatik.tk.assistance.R.style;
import de.tudarmstadt.informatik.tk.assistance.adapter.ModuleRunningSensorsAdapter;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleAllowedPermissionStateChangedEvent;
import de.tudarmstadt.informatik.tk.assistance.model.item.ModuleRunningSensorTypeItem;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.sdk.Config;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbFacebookSensor;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbTucanSensor;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ToggleModuleRequestDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.sensing.SensorApiType;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.ApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.SensorProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.SocialProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.api.ModuleApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.sensing.ISensor;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.DateUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.RxUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.StringUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.view.DividerItemDecoration;
import rx.Subscriber;
import rx.Subscription;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.01.2016
 */
public class ModuleRunningSensorsActivity extends AppCompatActivity {

    private static final String TAG = ModuleRunningSensorsActivity.class.getSimpleName();

    private DaoProvider daoProvider;

    private ApiProvider apiProvider;

    private SocialProvider socialProvider;

    // facebook stuff
    private CallbackManager callbackManager;

    private PermissionRequestObject mRequestObject;

    private Subscription subModuleDeactivation;

    private Unbinder unbinder;

    @BindView(id.toolbar)
    protected Toolbar mToolbar;

    @BindView(id.permissionRecyclerView)
    protected RecyclerView mPermissionsRecyclerView;

    @BindView(id.noData)
    protected AppCompatTextView mNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_module_types_permission);

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

    /**
     * Paints the view
     */
    private void initView() {

        apiProvider = ApiProvider.getInstance(getApplicationContext());
        daoProvider = DaoProvider.getInstance(getApplicationContext());
        socialProvider = SocialProvider.getInstance(getApplicationContext());

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = Factory.create();

        unbinder = ButterKnife.bind(this);

        try {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (Exception e) {
            // fix for Samsung Android 4.2.2 AppCompat ClassNotFoundException
        }

        setTitle(string.settings_module_allowed_capability_title);

        Resources resources = getResources();

        String userToken = PreferenceUtils.getUserToken(getApplicationContext());
        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            return;
        }

        List<DbModule> activeModules = daoProvider.getModuleDao().getAllActive(user.getId());
        SparseArray<ISensor> runningSensors = SensorProvider.getInstance(this).getRunningSensors();

        // calculate numbers
        SparseIntArray usageCounters = new SparseIntArray();

        for (DbModule activeModule : activeModules) {

            List<DbModuleCapability> caps = activeModule.getDbModuleCapabilityList();

            for (DbModuleCapability cap : caps) {

                // not active or not required capability will not be counted
                if (!cap.getActive() || !cap.getRequired()) {
                    continue;
                }

                String type = cap.getType();

                for (int i = 0, size = runningSensors.size(); i < size; i++) {

                    ISensor runningSensor = runningSensors.valueAt(i);
                    int intType = SensorApiType.getDtoType(type);

                    if (runningSensor.getType() == intType) {

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

        final List<Integer> existingTypes = new ArrayList<>();
        final List<ModuleRunningSensorTypeItem> runningSensorToBeDisplayed = new ArrayList<>();

        for (DbModule activeModule : activeModules) {

            List<DbModuleCapability> caps = activeModule.getDbModuleCapabilityList();

            for (DbModuleCapability cap : caps) {

                int capType = SensorApiType.getDtoType(cap.getType());

                if (!existingTypes.contains(capType)) {
                    runningSensorToBeDisplayed.add(
                            new ModuleRunningSensorTypeItem(
                                    capType,
                                    SensorApiType.getName(capType, resources),
                                    cap.getActive(),
                                    usageCounters.get(capType)));

                    existingTypes.add(capType);
                }
            }
        }

        mPermissionsRecyclerView.setHasFixedSize(true);
        mPermissionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPermissionsRecyclerView.setAdapter(new ModuleRunningSensorsAdapter(runningSensorToBeDisplayed));
        mPermissionsRecyclerView.addItemDecoration(new DividerItemDecoration(this, null));

        if (runningSensorToBeDisplayed.isEmpty()) {
            mNoData.setVisibility(View.VISIBLE);
            mPermissionsRecyclerView.setVisibility(View.GONE);
        } else {
            mNoData.setVisibility(View.GONE);
            mPermissionsRecyclerView.setVisibility(View.VISIBLE);
        }
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
        unbinder.unbind();
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
                updateModuleSensorState(type, event.isChecked());
                SensorProvider.getInstance(this).synchronizeRunningSensorsWithDb();
                return;
            }

            mRequestObject = PermissionUtil.with(this)
                    .request(perms)
                    .onAnyDenied(new Func() {

                        @Override
                        protected void call() {
                            Log.d(TAG, "Permission was denied");
                            updateModuleSensorState(SensorApiType.getApiName(event.getCapType()), false);
                            updateModuleSensorSwitcher(event.getCapType(), false);
                            SensorProvider.getInstance(ModuleRunningSensorsActivity.this).synchronizeRunningSensorsWithDb();
                        }
                    })
                    .onAllGranted(new Func() {

                        @Override
                        protected void call() {
                            Log.d(TAG, "Permission was granted");
                            updateModuleSensorState(SensorApiType.getApiName(event.getCapType()), event.isChecked());
                            updateModuleSensorSwitcher(event.getCapType(), event.isChecked());
                            SensorProvider.getInstance(ModuleRunningSensorsActivity.this).synchronizeRunningSensorsWithDb();

                        }
                    })
                    .ask(Config.PERM_MODULE_ALLOWED_CAPABILITY);

        }
    }

    /**
     * Without restriction (no other module uses perms of that module)
     *
     * @param type
     * @param isActive
     */
    private void updateModuleSensorState(String type, boolean isActive) {

        if (type == null) {
            return;
        }

        Log.d(TAG, "Updating module cap state...");

        String userEmail = PreferenceUtils.getUserEmail(this);
        DbUser user = daoProvider.getUserDao().getByEmail(userEmail);

        List<DbModule> activeModules = daoProvider.getModuleDao().getAllActive(user.getId());

        for (DbModule module : activeModules) {

            List<DbModuleCapability> caps = module.getDbModuleCapabilityList();

            for (DbModuleCapability cap : caps) {

                if (!cap.getRequired() && type.equals(cap.getType())) {
                    cap.setActive(isActive);
                }
            }
        }

        daoProvider.getModuleDao().update(activeModules);

        Log.d(TAG, "Updated!");
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

        Builder builder = new Builder(this, style.MyAppCompatAlertDialog);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(layout.dialog_enter_credentials, null);

        builder.setView(dialogView);

        AppCompatEditText usernameET = ButterKnife.findById(dialogView, id.username);
        AppCompatEditText passwordET = ButterKnife.findById(dialogView, id.password);

        usernameET.setHint(string.dialog_social_tucan_username);
        passwordET.setHint(string.password);

        builder.setPositiveButton(string.button_ok, (dialog, which) -> {
            // dummy
        });

        builder.setNegativeButton(string.button_cancel, (dialog, which) -> {
            updateModuleSensorSwitcher(SensorApiType.UNI_TUCAN, false);
            dialog.cancel();
        });

        builder.setCancelable(false);

        builder.setTitle(string.sensor_uni_tucan);

        AlertDialog alertDialog = builder.create();

        if (!isFinishing()) {
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

                            storeTucanCredentials(username, password);
                            updateModuleSensorState(SensorApiType.getApiName(SensorApiType.UNI_TUCAN), true);
                            updateModuleSensorSwitcher(SensorApiType.UNI_TUCAN, true);
                            SensorProvider.getInstance(ModuleRunningSensorsActivity.this).synchronizeRunningSensorsWithDb();

                            dialogShouldClose = true;

                        } else {
                            updateModuleSensorSwitcher(SensorApiType.UNI_TUCAN, false);
                        }

                        if (dialogShouldClose) {
                            Toaster.showLong(getApplicationContext(), string.changes_were_saved);
                            alertDialog.dismiss();
                        }
                    });
        }
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
            usernameET.setError(getString(string.error_field_required));
            usernameET.requestFocus();
            return false;
        }

        if (StringUtils.isNullOrEmpty(password)) {
            passwordET.setError(getString(string.error_field_required));
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

                        updateModuleSensorState(SensorApiType.getApiName(SensorApiType.SOCIAL_FACEBOOK), true);
                        updateModuleSensorSwitcher(SensorApiType.SOCIAL_FACEBOOK, true);
                        SensorProvider.getInstance(ModuleRunningSensorsActivity.this).synchronizeRunningSensorsWithDb();
                    }

                    @Override
                    public void onCancel() {
                        Toaster.showShort(getApplicationContext(), string.error_unknown);
                        updateModuleSensorSwitcher(SensorApiType.SOCIAL_FACEBOOK, false);
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toaster.showShort(getApplicationContext(), string.error_service_not_available);
                        updateModuleSensorSwitcher(SensorApiType.SOCIAL_FACEBOOK, false);
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

    /**
     * If module capability is used by another module
     *
     * @param capType
     * @param isChecked
     * @param numReqModules
     */
    private void handleDisablingCapabilityWithRestriction(int capType, boolean isChecked, int numReqModules) {

        Builder builder = new Builder(this, style.MyAppCompatAlertDialog);

        builder.setPositiveButton(string.button_disable, (dialog, which) -> {
            Log.d(TAG, "User tapped positive button");
            disableActiveModulesForType(capType);
        });

        builder.setNegativeButton(string.button_cancel, (dialog, which) -> {

            Log.d(TAG, "User tapped negative button");
            updateModuleSensorSwitcher(capType, true);
            dialog.cancel();
        });

        builder.setOnCancelListener(dialog -> {

            Log.d(TAG, "Negative dialog event invoked");
            updateModuleSensorSwitcher(capType, true);
            dialog.cancel();
        });

        builder.setTitle(string.settings_module_allowed_capability_disable_header);
        builder.setMessage(string.settings_module_allowed_capability_disable_message2);

        AlertDialog alertDialog = builder.create();

        if (!isFinishing()) {
            alertDialog.show();
        }
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

        String typeStr = SensorApiType.getApiName(capType);

        List<DbModule> activeModules = daoProvider.getModuleDao().getAllActive(user.getId());

        ModuleApiProvider moduleApiProvider = apiProvider.getModuleApiProvider();

        ModuleRunningSensorsAdapter adapter = (ModuleRunningSensorsAdapter) mPermissionsRecyclerView.getAdapter();

        List<ModuleRunningSensorTypeItem> items = adapter.getData();
        List<ModuleRunningSensorTypeItem> newItemsList = new ArrayList<>(items.size());
        newItemsList.addAll(items);

        for (DbModule dbModule : activeModules) {

            ToggleModuleRequestDto deactivationRequest = new ToggleModuleRequestDto();
            deactivationRequest.setModulePackageName(dbModule.getPackageName());

            // deactivate module
            subModuleDeactivation = moduleApiProvider
                    .deactivateModule(userToken, deactivationRequest)
                    .subscribe(new ModuleDeactivationSubscriber());

            List<DbModuleCapability> caps = dbModule.getDbModuleCapabilityList();

            for (DbModuleCapability cap : caps) {

                // removing disabled perms
                for (ModuleRunningSensorTypeItem item : items) {
                    if (typeStr.equals(cap.getType()) && cap.getRequired() && cap.getActive()) {
                        newItemsList.remove(item);
                    }
                }

                // we have a match -> disable that module
                if (typeStr.equals(cap.getType()) && cap.getRequired() && cap.getActive()) {
                    dbModule.setActive(false);
                    break;
                }
            }
        }

        adapter.swapData(newItemsList);

        if (newItemsList.isEmpty()) {
            mNoData.setVisibility(View.VISIBLE);
            mPermissionsRecyclerView.setVisibility(View.GONE);
        } else {
            mNoData.setVisibility(View.GONE);
            mPermissionsRecyclerView.setVisibility(View.VISIBLE);
        }

        // update modules state info
        daoProvider.getModuleDao().update(activeModules);

        // update module capability state
        updateModuleSensorState(SensorApiType.getApiName(capType), false);

        // synchronize db active module capabilities with running caps
        SensorProvider.getInstance(getApplicationContext()).synchronizeRunningSensorsWithDb();

        List<DbModule> allActive = daoProvider.getModuleDao().getAllActive(user.getId());

        if (allActive.isEmpty()) {
            HarvesterServiceProvider.getInstance(getApplicationContext()).showHarvestIcon(false);
        }
    }

    /**
     * Set switcher checked or not ;)
     *
     * @param capType
     * @param isChecked
     */
    private void updateModuleSensorSwitcher(int capType, boolean isChecked) {

        ModuleRunningSensorsAdapter adapter = (ModuleRunningSensorsAdapter) mPermissionsRecyclerView.getAdapter();

        if (adapter == null) {
            Log.d(TAG, "Adapter is null");
            return;
        }

        List<ModuleRunningSensorTypeItem> allowedPermItems = adapter.getData();

        for (ModuleRunningSensorTypeItem item : allowedPermItems) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

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
            Toaster.showLong(getApplicationContext(), string.error_unknown);
        }

        @Override
        public void onNext(Void aVoid) {
            Log.d(TAG, "subModuleDeactivation successfully called");
            SensorProvider.getInstance(getApplicationContext()).synchronizeRunningSensorsWithDb();
        }
    }
}