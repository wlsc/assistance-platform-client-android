package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.squareup.picasso.Picasso;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.base.BaseActivity;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.ModulesAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.PermissionAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.CheckIfModuleCapabilityPermissionWasGrantedEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleInstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleInstallSuccessfulEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleOptionalPermissionEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleShowMoreInfoEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleUninstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleUninstallSuccessfulEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModulesListRefreshEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.settings.ModuleCapabilityHasChangedEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.settings.ModuleStateChangeEvent;
import de.tudarmstadt.informatik.tk.android.assistance.model.image.CircleTransformation;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.PermissionListItem;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.module.ModulesPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.module.ModulesPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.Config;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ModuleCapabilityResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.ModuleProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.RxUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.ModulesView;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * Shows a list of available assistance modules
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class ModulesActivity extends
        BaseActivity implements
        ModulesView {

    private static final String TAG = ModulesActivity.class.getSimpleName();

    private ModulesPresenter presenter;

    private Toolbar mToolbar;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private OnRefreshListener onRefreshHandler;

    private RecyclerView mAvailableModulesRecyclerView;
    private RecyclerView permissionRequiredRecyclerView;
    private RecyclerView permissionOptionalRecyclerView;

    private TextView permissionsEmptyRequired;
    private TextView permissionsEmptyOptional;
    private TextView noData;

    private Subscription subActivatedModules;

    private PermissionUtil.PermissionRequestObject mRequestObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPresenter(new ModulesPresenterImpl(this));
        presenter.initView();
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
     * When some1 needs to refresh the modules list
     *
     * @param event
     */
    public void onEvent(ModulesListRefreshEvent event) {
        Log.d(TAG, "ModulesListRefreshEvent is arrived");

        presenter.refreshModuleList();
    }

    /**
     * On module install event
     *
     * @param event
     */
    public void onEvent(ModuleInstallEvent event) {
        Log.d(TAG, "Received installation event. Module id: " + event.getModulePackageName());

        presenter.setSelectedModuleId(event.getModulePackageName());
        presenter.presentPermissionDialog();
    }

    /**
     * On module install event
     *
     * @param event
     */
    public void onEvent(ModuleUninstallEvent event) {
        Log.d(TAG, "Received uninstall event. Module id: " + event.getModulePackageName());

        presenter.setSelectedModuleId(event.getModulePackageName());
        presenter.presentUninstallDialog();
    }

    /**
     * On module show more info event
     *
     * @param event
     */
    public void onEvent(ModuleShowMoreInfoEvent event) {
        Log.d(TAG, "Received show more info event. Module id: " + event.getModulePackageName());

        presenter.setSelectedModuleId(event.getModulePackageName());
        presenter.presentMoreModuleInformationDialog();
    }

    /**
     * On module successful installation
     *
     * @param event
     */
    public void onEvent(ModuleInstallSuccessfulEvent event) {
        Log.d(TAG, "After module successful installation. Module id: " + event.getModulePackageName());

        presenter.setSelectedModuleId(event.getModulePackageName());
        presenter.presentSuccessfulInstallation();
    }

    /**
     * On module successful uninstalled
     *
     * @param event
     */
    public void onEvent(ModuleUninstallSuccessfulEvent event) {
        Log.d(TAG, "After module was successful uninstalled. Module id: " + event.getModulePackageName());

        presenter.setSelectedModuleId(event.getModulePackageName());
        presenter.presentSuccessfulUninstall();
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
     * On module install capability permission check
     *
     * @param event
     */
    public void onEvent(CheckIfModuleCapabilityPermissionWasGrantedEvent event) {
        Log.d(TAG, "CheckIfModuleCapabilityPermissionWasGrantedEvent was invoked");
        Log.d(TAG, "Capability: " + event.getCapability().toString());

        String[] notGrantedPerms = ModuleProvider.getInstance(getApplicationContext())
                .getNotGrantedModuleCapabilityPermission(this, event.getCapability());

        mRequestObject = PermissionUtil.with(this)
                .request(notGrantedPerms)
                .onAnyDenied(new Func() {

                    @Override
                    protected void call() {
                        EventBus.getDefault().post(
                                new ModuleOptionalPermissionEvent(false, event.getPosition()));
                    }
                })
                .onAllGranted(new Func() {

                    @Override
                    protected void call() {
                        EventBus.getDefault().post(new ModuleOptionalPermissionEvent(true, event.getPosition()));
                    }
                })
                .ask(Config.PERM_MODULE_OPTIONAL_CAPABILITY);
    }

    /**
     * Tell module about granted optional permission
     *
     * @param event
     */
    public void onEvent(ModuleOptionalPermissionEvent event) {

        // permission was not granted
        if (event.isGranted()) {
            // OK
        } else {
            // uncheck optional sensor
            PermissionAdapter adapter = (PermissionAdapter) permissionOptionalRecyclerView.getAdapter();

            if (adapter == null) {
                Log.d(TAG, "Adapter null");
                return;
            }

            List<PermissionListItem> items = adapter.getData();

            if (items.isEmpty()) {
                Log.d(TAG, "Capability items are empty");
                return;
            }

            int position = event.getPosition();

            if (position >= items.size() || position < 0) {
                Log.d(TAG, "Wrong item position!");
                return;
            }

            PermissionListItem permissionListItem = items.get(position);
            permissionListItem.setChecked(false);
            DbModuleCapability oldCap = permissionListItem.getCapability();
            oldCap.setActive(false);
            permissionListItem.setCapability(oldCap);
            items.set(position, permissionListItem);

            permissionOptionalRecyclerView.setAdapter(new PermissionAdapter(items, PermissionAdapter.OPTIONAL));
        }
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
    public void showUndoAction() {

        Snackbar
                .make(findViewById(android.R.id.content),
                        R.string.main_activity_undo_uninstall,
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.main_activity_undo_uninstall_button_title,
                        new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "User tapped UNDO uninstall of a module!");

                                final ModuleResponseDto moduleResponse = presenter.getSelectedModuleResponse();
                                presenter.handleModuleActivationRequest(moduleResponse);
                            }
                        })
                .setActionTextColor(Color.RED)
                .show();
    }

    @Override
    public void showModuleUninstallSuccessful() {
        Toaster.showShort(getApplicationContext(), R.string.module_uninstall_successful);
    }

    @Override
    public void subscribeActivatedModules(Observable<ActivatedModulesResponse> observable) {

        subActivatedModules = observable.subscribe(new Subscriber<ActivatedModulesResponse>() {

            @Override
            public void onCompleted() {
                // empty
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof RetrofitError) {
                    presenter.onActivatedModulesFailed((RetrofitError) e);
                }
            }

            @Override
            public void onNext(ActivatedModulesResponse activatedModulesResponse) {
                presenter.onActivatedModulesReceived(activatedModulesResponse);
            }
        });
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        RxUtils.unsubscribe(subActivatedModules);

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

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        mRequestObject.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        presenter.presentRequestPermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    public void initView() {

        Log.d(TAG, "Init view...");

        setContentView(R.layout.activity_module_list);

        mToolbar = ButterKnife.findById(this, R.id.toolbar);

        try {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (Exception e) {
            // fix for Samsung Android 4.2.2 AppCompat ClassNotFoundException
        }

        setTitle(R.string.module_list_activity_title);

        noData = ButterKnife.findById(this, R.id.noData);

        mAvailableModulesRecyclerView = ButterKnife.findById(this, R.id.moduleListRecyclerView);
        mAvailableModulesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout = ButterKnife.findById(this, R.id.module_list_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if (onRefreshHandler == null) {
            Log.d(TAG, "Setting swipe handler...");

            onRefreshHandler = new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {

                    setSwipeRefreshing(true);

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

        // set updating spinner to list
        setSwipeRefreshing(false);
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
    public void showUserForbidden() {
        Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
    }

    @Override
    public void showActionProhibited() {
        Toaster.showLong(getApplicationContext(), R.string.error_that_action_is_prohibited);
    }

    @Override
    public void showRetryLaterNotification() {
        Toaster.showLong(getApplicationContext(), R.string.error_service_retry_later);
    }

    @Override
    public void askPermissions(Set<String> permsToAsk) {

        if (permsToAsk != null && !permsToAsk.isEmpty()) {

            mRequestObject = PermissionUtil.with(this)
                    .request(permsToAsk.toArray(new String[permsToAsk.size()]))
                    .onAllGranted(
                            new Func() {
                                @Override
                                protected void call() {
                                    // TODO: take a look at handleModuleActivationRequest function -> its sometimes buggy
                                    presenter.handleModuleActivationRequest(presenter.getSelectedModuleResponse());
                                    HarvesterServiceProvider.getInstance(getApplicationContext()).startSensingService();
                                    EventBus.getDefault().post(new ModulesListRefreshEvent());
                                }
                            }).onAnyDenied(
                            new Func() {
                                @Override
                                protected void call() {
                                    showPermissionsAreCrucialDialog(permsToAsk);
                                    EventBus.getDefault().post(new ModulesListRefreshEvent());
                                }
                            }).ask(Constants.PERM_MODULE_INSTALL);

        } else {
            EventBus.getDefault().post(new ModulesListRefreshEvent());
            HarvesterServiceProvider.getInstance(getApplicationContext()).startSensingService();
        }
    }

    @Override
    public void setPresenter(ModulesPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setView(this);
    }

    @Override
    public void setErrorView() {
        setNoModulesView();
    }

    @Override
    public void setNoModulesView() {

        mAvailableModulesRecyclerView.setAdapter(new ModulesAdapter(Collections.EMPTY_LIST));
        mAvailableModulesRecyclerView.setVisibility(View.GONE);
        noData.setVisibility(View.VISIBLE);
        setSwipeRefreshing(false);
    }

    @Override
    public void setSwipeRefreshing(boolean isEnabled) {
        mSwipeRefreshLayout.setRefreshing(isEnabled);
    }

    @Override
    public void setModuleList(List<DbModule> installedModules) {

        Log.d(TAG, "Set new module data list...");

        mAvailableModulesRecyclerView.setAdapter(new ModulesAdapter(installedModules));
        mAvailableModulesRecyclerView.setVisibility(View.VISIBLE);
        noData.setVisibility(View.GONE);
    }

    @Override
    public List<DbModule> getDisplayedModules() {

        ModulesAdapter adapter = (ModulesAdapter) mAvailableModulesRecyclerView.getAdapter();

        if (adapter == null) {
            return Collections.emptyList();
        }

        return adapter.getItems();
    }

    @Override
    public int getDisplayedModulesCount() {

        ModulesAdapter adapter = (ModulesAdapter) mAvailableModulesRecyclerView.getAdapter();

        return adapter == null ? 0 : adapter.getItemCount();
    }

    @Override
    public void swapModuleData(List<DbModule> newModules) {

        Log.d(TAG, "Swapping module data list...");

        ModulesAdapter adapter = (ModulesAdapter) mAvailableModulesRecyclerView.getAdapter();

        if (adapter != null) {
            adapter.swapData(newModules);
        }

        if (newModules.isEmpty()) {
            mAvailableModulesRecyclerView.setVisibility(View.GONE);
            noData.setVisibility(View.VISIBLE);
        } else {
            mAvailableModulesRecyclerView.setVisibility(View.VISIBLE);
            noData.setVisibility(View.GONE);
        }
    }

    @Override
    public void showPermissionDialog(ModuleResponseDto selectedModule) {

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_permissions, null);

        permissionRequiredRecyclerView = ButterKnife.findById(
                dialogView,
                R.id.module_permission_required_list);
        permissionRequiredRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        permissionOptionalRecyclerView = ButterKnife.findById(
                dialogView,
                R.id.module_permission_optional_list);
        permissionOptionalRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        builder.setPositiveButton(R.string.button_accept_text, (dialog, which) -> {

            Log.d(TAG, "User tapped accept button");
            presenter.handleModulePermissions();
        });

        builder.setNegativeButton(R.string.button_cancel_text, (dialog, which) -> {

            Log.d(TAG, "User canceled module install");
            dialog.cancel();
        });

        builder.setOnCancelListener(dialog -> presenter.setSelectedModuleId(""));

        TextView title = ButterKnife.findById(dialogView, R.id.module_permission_title);
        title.setText(selectedModule.getTitle());

        ImageView imageView = ButterKnife.findById(dialogView, R.id.module_permission_icon);

        Picasso.with(this)
                .load(selectedModule.getLogo())
                .placeholder(R.drawable.no_image)
                .transform(new CircleTransformation())
                .into(imageView);

        permissionsEmptyRequired = ButterKnife.findById(
                dialogView,
                R.id.module_permissions_required_list_empty);

        permissionsEmptyOptional = ButterKnife.findById(
                dialogView,
                R.id.module_permissions_optional_list_empty);

        List<ModuleCapabilityResponseDto> requiredSensors = selectedModule.getSensorsRequired();
        List<ModuleCapabilityResponseDto> optionalSensors = selectedModule.getSensorsOptional();

        List<PermissionListItem> requiredModuleSensors = new ArrayList<>();
        List<PermissionListItem> optionalModuleSensors = new ArrayList<>();

        if (requiredSensors != null && !requiredSensors.isEmpty()) {

            for (ModuleCapabilityResponseDto capability : requiredSensors) {
                requiredModuleSensors.add(new PermissionListItem(
                        ConverterUtils.convertModuleCapability(capability)));
            }
        } else {
            toggleShowRequiredPermissions(true);
        }

        if (optionalSensors != null && !optionalSensors.isEmpty()) {

            for (ModuleCapabilityResponseDto capability : optionalSensors) {
                optionalModuleSensors.add(new PermissionListItem(
                        ConverterUtils.convertModuleCapability(capability),
                        false
                ));
            }
        } else {
            toggleShowOptionalPermissions(true);
        }


        permissionRequiredRecyclerView.setAdapter(new PermissionAdapter(
                requiredModuleSensors,
                PermissionAdapter.REQUIRED));

        permissionOptionalRecyclerView.setAdapter(new PermissionAdapter(
                optionalModuleSensors,
                PermissionAdapter.OPTIONAL));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void toggleShowRequiredPermissions(boolean isVisible) {

        permissionRequiredRecyclerView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        permissionsEmptyRequired.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void toggleShowOptionalPermissions(boolean isVisible) {

        permissionOptionalRecyclerView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        permissionsEmptyOptional.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showUninstallDialog(final ModuleResponseDto selectedModule) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton(R.string.button_ok_text, (dialog, which) -> {

            Log.d(TAG, "User tapped UNINSTALL " + selectedModule.getTitle() + " module");
            presenter.presentModuleUninstall(ConverterUtils.convertModule(selectedModule));
        });

        builder.setNegativeButton(R.string.button_cancel_text, (dialog, which) -> {

            Log.d(TAG, "User tapped cancel uninstall procedure");
            presenter.setSelectedModuleId("");
        });

        builder.setOnCancelListener(dialog -> presenter.setSelectedModuleId(""));

        builder.setTitle(getString(R.string.module_uninstall_title, selectedModule.getTitle()));
        builder.setMessage(R.string.module_uninstall_message);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void showPermissionsAreCrucialDialog(Set<String> declinedPermissions) {

        Toaster.showLong(getApplicationContext(), R.string.permission_is_crucial);

        presenter.presentModuleInstallationHasError(declinedPermissions);
    }

    @Override
    public void showMoreModuleInformationDialog(final ModuleResponseDto selectedModule) {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_module_more_info, null);

        permissionRequiredRecyclerView = ButterKnife.findById(
                dialogView,
                R.id.module_permission_required_list
        );
        permissionRequiredRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        permissionOptionalRecyclerView = ButterKnife.findById(
                dialogView,
                R.id.module_permission_optional_list
        );
        permissionOptionalRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        permissionsEmptyRequired = ButterKnife.findById(
                dialogView,
                R.id.module_permissions_required_list_empty);

        permissionsEmptyOptional = ButterKnife.findById(
                dialogView,
                R.id.module_permissions_optional_list_empty);

        List<ModuleCapabilityResponseDto> requiredSensors = selectedModule.getSensorsRequired();
        List<ModuleCapabilityResponseDto> optionalSensors = selectedModule.getSensorsOptional();

        List<PermissionListItem> requiredModuleSensors = new ArrayList<>();
        List<PermissionListItem> optionalModuleSensors = new ArrayList<>();

        if (requiredSensors != null && !requiredSensors.isEmpty()) {

            for (ModuleCapabilityResponseDto capability : requiredSensors) {
                requiredModuleSensors.add(new PermissionListItem(
                        ConverterUtils.convertModuleCapability(capability)
                ));
            }
        } else {
            toggleShowRequiredPermissions(true);
        }

        if (optionalSensors != null && !optionalSensors.isEmpty()) {

            for (ModuleCapabilityResponseDto capability : optionalSensors) {
                optionalModuleSensors.add(new PermissionListItem(
                        ConverterUtils.convertModuleCapability(capability)
                ));
            }
        } else {
            toggleShowOptionalPermissions(true);
        }

        permissionRequiredRecyclerView.setAdapter(new PermissionAdapter(
                requiredModuleSensors,
                PermissionAdapter.REQUIRED));

        permissionOptionalRecyclerView.setAdapter(new PermissionAdapter(
                optionalModuleSensors,
                PermissionAdapter.HIDDEN));

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setPositiveButton(R.string.button_ok_text, (dialog, which) -> {
            Log.d(TAG, "User tapped more information about the " + selectedModule.getTitle() + " module");
        });

        dialogBuilder.setTitle(selectedModule.getTitle());

        TextView moreInfoFull = ButterKnife.findById(dialogView, R.id.module_more_info);
        moreInfoFull.setText(selectedModule.getDescriptionFull());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void changeModuleLayout(String moduleId, boolean isModuleInstalled) {

        if (isModuleInstalled) {
            Log.d(TAG, "Changing layout of a module to installed...");
        } else {
            Log.d(TAG, "Changing layout of a module to uninstalled...");
        }

        ModulesAdapter adapter = (ModulesAdapter) mAvailableModulesRecyclerView.getAdapter();

        if (adapter == null) {
            Log.d(TAG, "ModulesAdapter is null!");
            return;
        }

        DbModule module = adapter.getItem(moduleId);

        // defensive programming
        if (module != null) {

            module.setActive(isModuleInstalled);
            adapter.notifyDataSetChanged();
        }
    }
}