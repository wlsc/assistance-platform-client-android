package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.base.BaseActivity;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.ModuleTypesAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.ModuleAllowedTypeItem;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleAllowedCapabilities;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.sensing.SensorApiType;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.DaoProvider;

public class ModuleTypesPermissionActivity extends BaseActivity {

    private static final String TAG = ModuleTypesPermissionActivity.class.getSimpleName();

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

        Resources resources = getResources();

        List<DbModuleAllowedCapabilities> allAllowedModuleCapsDb = daoProvider
                .getModuleAllowedCapsDao()
                .getAll();

        List<ModuleAllowedTypeItem> allAllowedModuleCaps = new ArrayList<>(allAllowedModuleCapsDb.size());

        for (DbModuleAllowedCapabilities dbAllowedCap : allAllowedModuleCapsDb) {
            int type = SensorApiType.getDtoType(dbAllowedCap.getType());
            allAllowedModuleCaps.add(
                    new ModuleAllowedTypeItem(
                            SensorApiType.getName(type, resources),
                            dbAllowedCap.getIsAllowed(),
                            0));
        }

        mPermissionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPermissionsRecyclerView.setAdapter(new ModuleTypesAdapter(allAllowedModuleCaps));
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }
}