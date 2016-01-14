package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseIntArray;
import android.view.MenuItem;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.base.BaseActivity;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.ModuleGlobalCapsAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleAllowedPermissionStateChangedEvent;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.ModuleAllowedTypeItem;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.Config;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleAllowedCapabilities;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ToggleModuleRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.sensing.SensorApiType;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.ApiProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.SensorProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.RxUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
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

    private PermissionUtil.PermissionRequestObject mRequestObject;

    private Subscription moduleDisableSubscription;

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

    /**
     * Paints the view
     */
    private void initView() {

        apiProvider = ApiProvider.getInstance(getApplicationContext());
        daoProvider = DaoProvider.getInstance(getApplicationContext());

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
        SparseIntArray counters = new SparseIntArray();

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

                        if (counters.get(intType) == 0) {
                            counters.put(intType, 1);
                        } else {
                            int oldNumber = counters.get(intType);
                            oldNumber++;
                            counters.put(intType, oldNumber);
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
                            counters.get(type)));
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
        RxUtils.unsubscribe(moduleDisableSubscription);
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

            Map<String, String[]> dangerousGroup = PermissionUtils
                    .getInstance(getApplicationContext())
                    .getDangerousPermissionsToDtoMapping();

            String[] perms = dangerousGroup.get(type);

            if (perms == null) {
                Log.d(TAG, "Do not need perm for the type: " + type);
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
                            updateModuleAllowedCapabilitySwitcher(event.getCapType(), true);
                        }
                    })
                    .ask(Config.PERM_MODULE_ALLOWED_CAPABILITY);

        }
    }

    private void updateModuleAllowedCapabilityDbEntry(int capType, boolean isChecked) {

        Log.d(TAG, "UpdateModuleAllowedCapabilityStateEvent invoked");

        String type = SensorApiType.getApiName(capType);
        DbModuleAllowedCapabilities allowedCapability = daoProvider.getModuleAllowedCapsDao().get(type);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton(R.string.button_disable_text, (dialog, which) -> {
            Log.d(TAG, "User tapped positive button");
            disableActiveModulesForType(capType);
        });

        builder.setNegativeButton(R.string.button_cancel_text, (dialog, which) -> {

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

        String userToken = PreferenceUtils.getUserToken(getApplicationContext());
        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            Log.d(TAG, "User is NULL");
            return;
        }

        ToggleModuleRequestDto request = new ToggleModuleRequestDto();

        String typeStr = SensorApiType.getApiName(capType);

        List<DbModule> activeModules = daoProvider.getModuleDao().getAllActive(user.getId());

        for (DbModule dbModule : activeModules) {

            // deactivate module
            moduleDisableSubscription = apiProvider
                    .getModuleApiProvider()
                    .deactivateModule(userToken, request)
                    .subscribe(new Subscriber<Void>() {

                        @Override
                        public void onCompleted() {

                            Log.d(TAG, "moduleDisableSubscription has been completed");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toaster.showLong(getApplicationContext(), R.string.error_unknown);
                        }

                        @Override
                        public void onNext(Void aVoid) {
                            Log.d(TAG, "moduleDisableSubscription successfully called");
                        }
                    });

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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        mRequestObject.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}