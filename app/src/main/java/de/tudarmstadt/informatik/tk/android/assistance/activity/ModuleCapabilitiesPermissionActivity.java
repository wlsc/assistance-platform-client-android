package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.res.Resources;
import android.os.Bundle;
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
import de.tudarmstadt.informatik.tk.android.assistance.adapter.ModuleTypesAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.CheckModuleAllowedPermissionEvent;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.ModuleAllowedTypeItem;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.Config;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleAllowedCapabilities;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.sensing.SensorApiType;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.01.2016
 */
public class ModuleCapabilitiesPermissionActivity extends BaseActivity {

    private static final String TAG = ModuleCapabilitiesPermissionActivity.class.getSimpleName();

    private DaoProvider daoProvider;

    private PermissionUtil.PermissionRequestObject mRequestObject;

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

        daoProvider = DaoProvider.getInstance(getApplicationContext());

        ButterKnife.bind(this);

        try {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (Exception e) {
            // fix for Samsung Android 4.2.2 AppCompat ClassNotFoundException
        }

        setTitle(R.string.settings_module_types_permission_title);

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

        List<ModuleAllowedTypeItem> allAllowedModuleCaps = new ArrayList<>(allAllowedModuleCapsDb.size());

        for (DbModuleAllowedCapabilities dbAllowedCap : allAllowedModuleCapsDb) {

            int type = SensorApiType.getDtoType(dbAllowedCap.getType());

            allAllowedModuleCaps.add(
                    new ModuleAllowedTypeItem(
                            type,
                            SensorApiType.getName(type, resources),
                            dbAllowedCap.getIsAllowed(),
                            counters.get(type)));
        }

        mPermissionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPermissionsRecyclerView.setAdapter(new ModuleTypesAdapter(allAllowedModuleCaps));
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
        super.onDestroy();
    }

    /**
     * Checking allowed module permission
     *
     * @param event
     */
    public void onEvent(CheckModuleAllowedPermissionEvent event) {

        Log.d(TAG, "CheckModuleAllowedPermissionEvent invoked");

        String userToken = PreferenceUtils.getUserToken(getApplicationContext());
        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            Log.d(TAG, "User is NULL");
            return;
        }

        String type = SensorApiType.getApiName(event.getDtoType());

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
                        updateModuleAllowedCapabilitySwitcher(event.getDtoType(), false);
                    }
                })
                .onAllGranted(new Func() {

                    @Override
                    protected void call() {
                        Log.d(TAG, "Permission was granted");
                        updateModuleAllowedCapabilitySwitcher(event.getDtoType(), true);
                    }
                })
                .ask(Config.PERM_MODULE_ALLOWED_CAPABILITY);
    }

    /**
     * Set switcher checked or not ;)
     *
     * @param dtoType
     * @param isChecked
     */
    private void updateModuleAllowedCapabilitySwitcher(int dtoType, boolean isChecked) {

        ModuleTypesAdapter adapter = (ModuleTypesAdapter) mPermissionsRecyclerView.getAdapter();

        if (adapter == null) {
            Log.d(TAG, "Adapter is null");
            return;
        }

        List<ModuleAllowedTypeItem> allowedPermItems = adapter.getData();

        for (ModuleAllowedTypeItem item : allowedPermItems) {
            if (item.getType() == dtoType) {
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