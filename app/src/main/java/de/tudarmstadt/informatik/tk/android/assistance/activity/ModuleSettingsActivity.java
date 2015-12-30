package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.ModuleSettingsListAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.PermissionAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.settings.ModuleCapabilityHasChangedEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.settings.ModuleDetailedSettingsEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.settings.ModuleStateChangeEvent;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.PermissionListItem;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.module.settings.ModuleSettingsPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.module.settings.ModuleSettingsPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.ModuleProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.ModuleSettingsView;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 25.12.2015
 */
public class ModuleSettingsActivity extends
        AppCompatActivity implements
        ModuleSettingsView {

    private static final String TAG = ModuleSettingsActivity.class.getSimpleName();

    private ModuleSettingsPresenter presenter;

    @Bind(R.id.toolbar)
    protected Toolbar mToolbar;

    @Bind(R.id.moduleSettingsRecyclerView)
    protected RecyclerView moduleSettingsRecyclerView;

    @Bind(R.id.noData)
    protected TextView noData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPresenter(new ModuleSettingsPresenterImpl(this));
        presenter.doInitView();
    }

    @Override
    public void initView() {

        Log.d(TAG, "Init view");

        setContentView(R.layout.activity_module_settings);

        ButterKnife.bind(this);

        try {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (Exception e) {
            // fix for Samsung Android 4.2.2 AppCompat ClassNotFoundException
        }

        setTitle(R.string.module_settings_activity_title);

        moduleSettingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "On normal back pressed");
        finish();
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
    protected void onPause() {

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        super.onPause();
    }

    @Override
    protected void onResume() {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        super.onResume();
    }

    @Override
    public void startLoginActivity() {

        PreferenceUtils.clearUserCredentials(getApplicationContext());
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void clearErrors() {

    }

    @Override
    public void showServiceUnavailable() {

    }

    @Override
    public void showServiceTemporaryUnavailable() {

    }

    @Override
    public void showUnknownErrorOccurred() {

    }

    @Override
    public void showUserForbidden() {

    }

    @Override
    public void showActionProhibited() {

    }

    @Override
    public void showRetryLaterNotification() {

    }

    @Override
    public void askPermissions(Set<String> permsToAsk) {

    }

    @Override
    public void setPresenter(ModuleSettingsPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setView(this);
    }

    @Override
    public void setAdapter(List<DbModule> allModules) {

        moduleSettingsRecyclerView.setAdapter(new ModuleSettingsListAdapter(allModules));

        if (allModules.isEmpty()) {
            noData.setVisibility(View.VISIBLE);
            moduleSettingsRecyclerView.setVisibility(View.GONE);
        } else {
            noData.setVisibility(View.GONE);
            moduleSettingsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy -> unbound resources");
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    /**
     * On module install event
     *
     * @param event
     */
    public void onEvent(ModuleStateChangeEvent event) {

        Log.d(TAG, "Received module state change event");
        Log.d(TAG, "Module id: " + event.getModuleId());
        Log.d(TAG, "IsEnabled: " + event.isActive());

        ModuleProvider.getInstance(getApplicationContext())
                .toggleModuleState(event.getModuleId(), event.isActive());
    }

    /**
     * Handler for module detailed settings event
     *
     * @param event
     */
    public void onEvent(ModuleDetailedSettingsEvent event) {
        Log.d(TAG, "Module detailed settings event called");
        launchModuleDetailedSettingsView(event.getModule());
    }

    /**
     * Handler for module capability state change event
     *
     * @param event
     */
    public void onEvent(ModuleCapabilityHasChangedEvent event) {
        Log.d(TAG, "Module capability state has been changed");

        if (event.getModuleCapability() == null) {
            Log.d(TAG, "Module capability is null!");
            return;
        }

        presenter.handleModuleCapabilityStateChanged(event.getModuleCapability());
    }

    /**
     * Launches detailed module settigns view
     *
     * @param module
     */
    private void launchModuleDetailedSettingsView(DbModule module) {

        if (module == null) {
            Log.d(TAG, "Module is null");
            return;
        }

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_module_settings, null);

        assignCapabilitiesToView(module.getDbModuleCapabilityList(), dialogView);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle(R.string.module_settings_dialog_header);

        builder.setPositiveButton(R.string.button_ok_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "Dialog OK pressed");
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Assigns information from module capabilities to views
     *
     * @param capabilities
     * @param dialogView
     */
    private void assignCapabilitiesToView(List<DbModuleCapability> capabilities, View dialogView) {

        if (capabilities == null || dialogView == null) {
            Log.d(TAG, "Illegal arguments");
            return;
        }

        List<PermissionListItem> reqList = new ArrayList<>();
        List<PermissionListItem> optList = new ArrayList<>();

        for (DbModuleCapability cap : capabilities) {

            if (cap == null) {
                continue;
            }

            PermissionListItem item = new PermissionListItem(cap, cap.getActive());

            if (cap.getRequired()) {
                reqList.add(item);
            } else {
                optList.add(item);
            }
        }

        RecyclerView requiredPermListView = ButterKnife.findById(
                dialogView,
                R.id.module_permission_required_list);

        RecyclerView optionalPermListView = ButterKnife.findById(
                dialogView,
                R.id.module_permission_optional_list);

        requiredPermListView.setLayoutManager(new LinearLayoutManager(this));
        requiredPermListView.setAdapter(new PermissionAdapter(reqList, PermissionAdapter.REQUIRED));

        optionalPermListView.setLayoutManager(new LinearLayoutManager(this));
        optionalPermListView.setAdapter(new PermissionAdapter(optList, PermissionAdapter.OPTIONAL));

        if (reqList.isEmpty()) {

            TextView noData = ButterKnife.findById(dialogView, R.id.module_permissions_required_list_empty);
            noData.setVisibility(View.VISIBLE);
            requiredPermListView.setVisibility(View.GONE);
        }

        if (optList.isEmpty()) {

            TextView noData = ButterKnife.findById(dialogView, R.id.module_permissions_optional_list_empty);
            noData.setVisibility(View.VISIBLE);
            optionalPermListView.setVisibility(View.GONE);
        }
    }
}