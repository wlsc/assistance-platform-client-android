package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.base.BaseActivity;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.ModuleTypesAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.ModuleAllowedTypeItem;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleAllowedCapabilities;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.sensing.SensorApiType;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;

public class ModuleCapabilitiesPermissionActivity extends BaseActivity {

    private static final String TAG = ModuleCapabilitiesPermissionActivity.class.getSimpleName();

    private DaoProvider daoProvider;

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
        Map<Integer, Integer> counters = new HashMap<>();

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

                        if (counters.get(intType) == null) {
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
                            SensorApiType.getName(type, resources),
                            dbAllowedCap.getIsAllowed(),
                            counters.get(type) == null ? 0 : counters.get(type)));
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
}