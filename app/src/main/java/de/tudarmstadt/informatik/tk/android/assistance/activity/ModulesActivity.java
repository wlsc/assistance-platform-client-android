package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.ModulesAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.PermissionAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleInstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleInstallationSuccessfulEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleShowMoreInfoEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleUninstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.AvailableModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.ModuleCapabilityResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.PermissionListItem;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.modules.ModulesPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.modules.ModulesPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.ModulesView;

/**
 * Shows a list of available assistance modules
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class ModulesActivity extends
        AppCompatActivity implements
        ModulesView {

    private static final String TAG = ModulesActivity.class.getSimpleName();

    private ModulesPresenter presenter;

    private Toolbar mToolbar;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mAvailableModulesRecyclerView;

    private RecyclerView permissionRequiredRecyclerView;

    private RecyclerView permissionOptionalRecyclerView;

    private SwipeRefreshLayout.OnRefreshListener onRefreshHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPresenter(new ModulesPresenterImpl(this));
        presenter.doInitView();
    }

    @Override
    protected void onResume() {

        // register this activity to events
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {

        // register this activity to events
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        super.onPause();
    }

    /**
     * On module install event
     *
     * @param event
     */
    public void onEvent(ModuleInstallEvent event) {
        Log.d(TAG, "Received installation event. Module id: " + event.getModuleId());

        presenter.setSelectedModuleId(event.getModuleId());
        presenter.presentPermissionDialog();
    }

    /**
     * On module install event
     *
     * @param event
     */
    public void onEvent(ModuleUninstallEvent event) {
        Log.d(TAG, "Received uninstall event. Module id: " + event.getModuleId());

        presenter.setSelectedModuleId(event.getModuleId());
        presenter.presentUninstallDialog();
    }

    /**
     * On module show more info event
     *
     * @param event
     */
    public void onEvent(ModuleShowMoreInfoEvent event) {
        Log.d(TAG, "Received show more info event. Module id: " + event.getModuleId());

        presenter.setSelectedModuleId(event.getModuleId());
        presenter.presentMoreModuleInformationDialog();
    }

    /**
     * On module successful installation
     *
     * @param event
     */
    public void onEvent(ModuleInstallationSuccessfulEvent event) {
        Log.d(TAG, "After module successful installation. Module id: " + event.getModuleId());

        presenter.setSelectedModuleId(event.getModuleId());
        presenter.presentSuccessfulInstallation();
    }

    /**
     * Returns list of permissions which were optional enabled by user
     *
     * @return
     */
    @Override
    public List<DbModuleCapability> getAllEnabledOptionalPermissions() {

        List<DbModuleCapability> result = new ArrayList<>();

        List<PermissionListItem> allAdapterPerms = ((PermissionAdapter) permissionOptionalRecyclerView
                .getAdapter())
                .getData();

        if (allAdapterPerms != null && !allAdapterPerms.isEmpty()) {

            for (PermissionListItem permItem : allAdapterPerms) {
                if (permItem.isChecked()) {
                    DbModuleCapability cap = permItem.getCapability();
                    if (cap != null) {
                        result.add(cap);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void showModuleInstallationFailed() {
        Toaster.showLong(getApplicationContext(), R.string.module_installation_unsuccessful);
    }

    @Override
    public void showModuleInstallationSuccessful() {
        Toaster.showLong(getApplicationContext(), R.string.module_installation_successful);
    }

    @Override
    public void showUndoAction(final DbModule module) {

        Snackbar
                .make(findViewById(android.R.id.content),
                        R.string.main_activity_undo_uninstall,
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.main_activity_undo_uninstall_button_title,
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "User tapped UNDO uninstall of a module!");

                                presenter.presentModuleInstallation(module);
                            }
                        })
                .setActionTextColor(Color.RED)
                .show();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy -> unbound resources");

        ButterKnife.unbind(this);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        super.onDestroy();
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

        setResult(Constants.INTENT_AVAILABLE_MODULES_RESULT);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        presenter.presentRequestPermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    public void initView() {

        Log.d(TAG, "Init view...");

        setContentView(R.layout.activity_available_modules);

        mToolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle(R.string.module_list_activity_title);

        mAvailableModulesRecyclerView = ButterKnife.findById(this, R.id.moduleListRecyclerView);
        mAvailableModulesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout = ButterKnife.findById(this, R.id.module_list_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if (onRefreshHandler == null) {
            onRefreshHandler = new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {

                    mSwipeRefreshLayout.setRefreshing(true);

                    // request new modules information
                    presenter.requestAvailableModules();
                }
            };
        }

        mSwipeRefreshLayout.setOnRefreshListener(onRefreshHandler);
        mSwipeRefreshLayout.setNestedScrollingEnabled(true);

        // register this activity to events
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
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
        // empty
    }

    @Override
    public void showServiceUnavailable() {
        Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
    }

    @Override
    public void showServiceTemporaryUnavailable() {
        Toaster.showLong(getApplicationContext(), R.string.error_server_temporary_unavailable);
    }

    @Override
    public void showUnknownErrorOccurred() {
        Toaster.showLong(getApplicationContext(), R.string.error_unknown);
    }

    @Override
    public void showUserActionForbidden() {
        Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
    }

    @Override
    public void askPermissions(Set<String> permsToAsk) {

        ActivityCompat.requestPermissions(this,
                permsToAsk.toArray(new String[permsToAsk.size()]),
                Constants.PERM_MODULE_INSTALL);
    }

    @Override
    public void setPresenter(ModulesPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setView(this);
    }

    @Override
    public void setErrorView() {
        mAvailableModulesRecyclerView.setAdapter(new ModulesAdapter(Collections.EMPTY_LIST));
        setSwipeRefreshing(false);
    }

    @Override
    public void setNoModulesView() {
        mAvailableModulesRecyclerView.setAdapter(new ModulesAdapter(Collections.EMPTY_LIST));
        setSwipeRefreshing(false);
    }

    @Override
    public void setSwipeRefreshing(boolean isEnabled) {
        mSwipeRefreshLayout.setRefreshing(isEnabled);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void setModuleList(List<DbModule> installedModules) {

        mAvailableModulesRecyclerView
                .setAdapter(new ModulesAdapter(installedModules));
    }

    @Override
    public int getModulesAmount() {

        ModulesAdapter adapter = (ModulesAdapter) mAvailableModulesRecyclerView
                .getAdapter();

        if (adapter != null) {
            return adapter.getItemCount();
        } else {
            return 0;
        }
    }

    @Override
    public void swapModuleData(List<DbModule> newModules) {

        ModulesAdapter adapter = (ModulesAdapter) mAvailableModulesRecyclerView
                .getAdapter();

        if (adapter != null) {
            adapter.swapData(newModules);
        }
    }

    @Override
    public void showPermissionDialog(AvailableModuleResponseDto selectedModule) {

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_permissions, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setPositiveButton(R.string.button_accept_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "User tapped accept button");

                presenter.askUserEnablePermissions();
            }
        });

        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                presenter.setSelectedModuleId("");
            }
        });

        TextView title = ButterKnife.findById(dialogView, R.id.module_permission_title);
        title.setText(selectedModule.getTitle());

        CircularImageView imageView = ButterKnife.findById(dialogView, R.id.module_permission_icon);

        Picasso.with(this)
                .load(selectedModule.getLogo())
                .placeholder(R.drawable.no_image)
                .into(imageView);

        List<ModuleCapabilityResponseDto> requiredSensors = selectedModule.getSensorsRequired();
        List<ModuleCapabilityResponseDto> optionalSensors = selectedModule.getSensorsOptional();

        List<PermissionListItem> requiredModuleSensors = new ArrayList<>();
        List<PermissionListItem> optionalModuleSensors = new ArrayList<>();

        if (requiredSensors != null) {

            for (ModuleCapabilityResponseDto capability : requiredSensors) {
                requiredModuleSensors.add(new PermissionListItem(
                        ConverterUtils.convertModuleCapability(capability)));
            }
        }

        if (optionalSensors != null) {

            for (ModuleCapabilityResponseDto capability : optionalSensors) {
                optionalModuleSensors.add(new PermissionListItem(
                        ConverterUtils.convertModuleCapability(capability)
                ));
            }
        }

        permissionRequiredRecyclerView = ButterKnife.findById(dialogView,
                R.id.module_permission_required_list);
        permissionRequiredRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        permissionRequiredRecyclerView.setAdapter(new PermissionAdapter(
                requiredModuleSensors,
                PermissionAdapter.REQUIRED));

        permissionOptionalRecyclerView = ButterKnife.findById(dialogView,
                R.id.module_permission_optional_list);
        permissionOptionalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        permissionOptionalRecyclerView.setAdapter(new PermissionAdapter(
                optionalModuleSensors,
                PermissionAdapter.OPTIONAL));

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void showUninstallDialog(final AvailableModuleResponseDto selectedModule) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setPositiveButton(R.string.button_ok_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "User tapped UNINSTALL " + selectedModule.getTitle() + " module");

                presenter.presentModuleUninstall(ConverterUtils.convertModule(selectedModule));
            }
        });

        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                presenter.setSelectedModuleId("");
            }
        });

        dialogBuilder.setTitle(getString(R.string.module_uninstall_title, selectedModule.getTitle()));
        dialogBuilder.setMessage(R.string.module_uninstall_message);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void showPermissionsAreCrucialDialog(List<String> declinedPermissions) {

    }

    @Override
    public void showMoreModuleInformationDialog(final AvailableModuleResponseDto selectedModule) {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_module_more_info, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setPositiveButton(R.string.button_ok_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "User tapped more information about the " + selectedModule.getTitle() + " module");
            }
        });

        dialogBuilder.setTitle(selectedModule.getTitle());

        TextView moreInfoFull = ButterKnife.findById(dialogView, R.id.module_more_info);
        moreInfoFull.setText(selectedModule.getDescriptionFull());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void changeModuleLayout(String moduleId) {

        Log.d(TAG, "Changing layout of a module to installed...");

        final ModulesAdapter adapter = (ModulesAdapter) mAvailableModulesRecyclerView
                .getAdapter();

        DbModule module = adapter.getItem(moduleId);

        // defensive programming
        if (module != null) {

            module.setActive(true);
            adapter.notifyDataSetChanged();
        }
    }
}