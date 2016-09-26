package de.tudarmstadt.informatik.tk.assistance.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.github.kayvannj.permission_utils.PermissionUtil.PermissionRequestObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.assistance.Constants;
import de.tudarmstadt.informatik.tk.assistance.R.color;
import de.tudarmstadt.informatik.tk.assistance.R.drawable;
import de.tudarmstadt.informatik.tk.assistance.R.id;
import de.tudarmstadt.informatik.tk.assistance.R.layout;
import de.tudarmstadt.informatik.tk.assistance.R.string;
import de.tudarmstadt.informatik.tk.assistance.R.style;
import de.tudarmstadt.informatik.tk.assistance.activity.base.BaseActivity;
import de.tudarmstadt.informatik.tk.assistance.adapter.ModulesAdapter;
import de.tudarmstadt.informatik.tk.assistance.adapter.PermissionAdapter;
import de.tudarmstadt.informatik.tk.assistance.event.module.CheckIfModuleCapabilityPermissionWasGrantedEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleInstallEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleInstallSuccessfulEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleOptionalPermissionEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleShowMoreInfoEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleUninstallEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleUninstallSuccessfulEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModulesListRefreshEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.settings.ModuleCapabilityHasChangedEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.settings.ModuleStateChangeEvent;
import de.tudarmstadt.informatik.tk.assistance.model.image.CircleTransformation;
import de.tudarmstadt.informatik.tk.assistance.model.item.PermissionListItem;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.presenter.module.ModulesPresenter;
import de.tudarmstadt.informatik.tk.assistance.presenter.module.ModulesPresenterImpl;
import de.tudarmstadt.informatik.tk.assistance.sdk.Config;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.assistance.sdk.event.ShowAccessibilityServiceTutorialEvent;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ModuleCapabilityResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.ModuleProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.PreferenceProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.RxUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.view.ModulesView;
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

    private AppCompatTextView permissionsEmptyRequired;
    private AppCompatTextView permissionsEmptyOptional;
    private AppCompatTextView noData;

    private Subscription subActivatedModules;
    private Subscription subModuleActivation;
    private Subscription subModuleDeactivation;

    private PermissionRequestObject mRequestObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

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
        Log.d(TAG, "Capability: " + event.getCapability().getType());

        String[] notGrantedPerms = ModuleProvider.getInstance(getApplicationContext())
                .getNotGrantedModuleCapabilityPermission(this, event.getCapability());

        if (notGrantedPerms != null && notGrantedPerms.length > 0) {

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
        } else {
            // special capabilities that do not request Android permissions
        }
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

            permissionOptionalRecyclerView.setAdapter(
                    new PermissionAdapter(
                            items,
                            PermissionAdapter.OPTIONAL,
                            adapter.isModuleActive(),
                            true));
        }
    }

    public void onEvent(ShowAccessibilityServiceTutorialEvent event) {
        Log.d(TAG, "ShowAccessibilityServiceTutorialEvent has arrived.");

        showAccessibilityServiceTutorial();
    }

    @Override
    public void showAccessibilityServiceTutorial() {

        boolean isActivated = PreferenceProvider
                .getInstance(getApplicationContext())
                .getActivated();

        if (!isActivated) {
            Intent intent = new Intent(this, AccessibilityTutorialActivity.class);
            startActivityForResult(intent, Constants.INTENT_ACCESSIBILITY_SERVICE_IGNORED_RESULT);
        }
    }

    /**
     * Returns list of permissions which were optional enabled by user
     *
     * @return
     */
    @Override
    public List<DbModuleCapability> getAllEnabledOptionalPermissions() {

        List<PermissionListItem> allAdapterPerms = ((PermissionAdapter) permissionOptionalRecyclerView
                .getAdapter())
                .getData();

        if (allAdapterPerms == null || allAdapterPerms.isEmpty()) {
            return Collections.emptyList();
        }

        List<DbModuleCapability> result = new ArrayList<>();

        for (PermissionListItem permItem : allAdapterPerms) {

            if (permItem == null) {
                continue;
            }

            if (permItem.isChecked()) {

                DbModuleCapability cap = permItem.getCapability();
                if (cap != null) {
                    result.add(cap);
                }
            }
        }

        return result;
    }

    @Override
    public void showModuleInstallationFailed() {
        Toaster.showLong(getApplicationContext(), string.module_installation_unsuccessful);
    }

    @Override
    public void showModuleInstallationSuccessful() {
        Toaster.showLong(getApplicationContext(), string.module_installation_successful);
    }

    @Override
    public void showUndoAction() {

        Snackbar
                .make(findViewById(android.R.id.content),
                        string.main_activity_undo_uninstall,
                        Snackbar.LENGTH_LONG)
                .setAction(string.main_activity_undo_uninstall_button_title,
                        v -> {
                            Log.d(TAG, "User tapped UNDO uninstall of a module!");

                            final ModuleResponseDto moduleResponse = presenter.getSelectedModuleResponse();
                            presenter.handleModuleActivationRequest(moduleResponse);
                        })
                .setActionTextColor(Color.RED)
                .show();
    }

    @Override
    public void showModuleUninstallSuccessful() {
        Toaster.showShort(getApplicationContext(), string.module_uninstall_successful);
    }

    @Override
    public void subscribeActivatedModules(Observable<ActivatedModulesResponse> observable) {
        subActivatedModules = observable.subscribe(new ActivatedModulesSubscriber());
    }

    @Override
    public void subscribeModuleDeactivation(Observable<Void> observable) {
        subModuleDeactivation = observable.subscribe(new ModuleDeactivationSubscriber());
    }

    @Override
    public void subscribeModuleActivation(Observable<Void> observable) {

        subModuleActivation = observable.subscribe(new ModuleActivationSubscriber());
    }

    @Override
    protected void onDestroy() {

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        RxUtils.unsubscribe(subActivatedModules);
        RxUtils.unsubscribe(subModuleDeactivation);
        RxUtils.unsubscribe(subModuleActivation);

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        mRequestObject.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        presenter.presentRequestPermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    public void initView() {

        Log.d(TAG, "Init view...");

        setContentView(layout.activity_module_list);

        mToolbar = ButterKnife.findById(this, id.toolbar);

        try {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (Exception e) {
            // fix for Samsung Android 4.2.2 AppCompat ClassNotFoundException
        }

        setTitle(string.module_list_activity_title);

        noData = ButterKnife.findById(this, id.noData);

        mAvailableModulesRecyclerView = ButterKnife.findById(this, id.moduleListRecyclerView);
        mAvailableModulesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAvailableModulesRecyclerView.addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                final int topRowVerticalPosition = (recyclerView == null || recyclerView.getChildCount() == 0)
                        ?
                        0 :
                        recyclerView.getChildAt(0).getTop();

                mSwipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
            }
        });

        mSwipeRefreshLayout = ButterKnife.findById(this, id.module_list_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if (onRefreshHandler == null) {
            Log.d(TAG, "Setting swipe handler...");

            onRefreshHandler = () -> {

                setSwipeRefreshing(true);

                // request new modules information
                presenter.requestAvailableModules();
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
        Toaster.showLong(getApplicationContext(), string.error_service_not_available);
    }

    @Override
    public void showServiceTemporaryUnavailable() {
        Toaster.showLong(getApplicationContext(), string.error_server_temporary_unavailable);
    }

    @Override
    public void showUnknownErrorOccurred() {
        Toaster.showLong(getApplicationContext(), string.error_unknown);
    }

    @Override
    public void showUserForbidden() {
        Toaster.showLong(getApplicationContext(), string.error_user_login_not_valid);
    }

    @Override
    public void showActionProhibited() {
        Toaster.showLong(getApplicationContext(), string.error_that_action_is_prohibited);
    }

    @Override
    public void showRetryLaterNotification() {
        Toaster.showLong(getApplicationContext(), string.error_service_retry_later);
    }

    @Override
    public void askPermissions(Set<String> permsRequired, Set<String> permsOptional) {

        if (permsRequired != null && !permsRequired.isEmpty()) {

            mRequestObject = PermissionUtil.with(this)
                    .request(permsRequired.toArray(new String[permsRequired.size()]))
                    .onAllGranted(
                            new Func() {
                                @Override
                                protected void call() {

                                    if (permsOptional != null && !permsOptional.isEmpty()) {

                                        mRequestObject = PermissionUtil.with(ModulesActivity.this)
                                                .request(permsOptional.toArray(new String[permsOptional.size()]))
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
                                                                EventBus.getDefault().post(new ModulesListRefreshEvent());
                                                            }
                                                        }).ask(Constants.PERM_MODULE_INSTALL_OPT_PERMS);

                                    } else {

                                        // TODO: take a look at handleModuleActivationRequest function -> its sometimes buggy
                                        presenter.handleModuleActivationRequest(presenter.getSelectedModuleResponse());
                                        HarvesterServiceProvider.getInstance(getApplicationContext()).startSensingService();
                                        EventBus.getDefault().post(new ModulesListRefreshEvent());
                                    }
                                }
                            }).onAnyDenied(
                            new Func() {
                                @Override
                                protected void call() {
                                    showPermissionsAreCrucialDialog(permsRequired);
                                    EventBus.getDefault().post(new ModulesListRefreshEvent());
                                }
                            }).ask(Constants.PERM_MODULE_INSTALL_REQ_PERMS);

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

        mAvailableModulesRecyclerView.setAdapter(new ModulesAdapter(Collections.<DbModule>emptyList()));
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
    public DbModule getModuleFromList(String packageName) {

        if (packageName == null || packageName.isEmpty()) {
            return null;
        }

        ModulesAdapter adapter = (ModulesAdapter) mAvailableModulesRecyclerView.getAdapter();

        return adapter.getItem(packageName);
    }

    @Override
    public void showPermissionDialog(ModuleResponseDto selectedModule) {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(layout.dialog_permissions, null);

        permissionRequiredRecyclerView = ButterKnife.findById(
                dialogView,
                id.module_permission_required_list);
        permissionRequiredRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        permissionOptionalRecyclerView = ButterKnife.findById(
                dialogView,
                id.module_permission_optional_list);
        permissionOptionalRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Builder builder = new Builder(this, style.MyAppCompatAlertDialog);
        builder.setView(dialogView);

        builder.setPositiveButton(string.button_accept, (dialog, which) -> {

            Log.d(TAG, "User tapped accept button");
            presenter.handleModulePermissions();
        });

        builder.setNegativeButton(string.button_cancel, (dialog, which) -> {

            Log.d(TAG, "User canceled module install");
            dialog.cancel();
        });

        builder.setOnCancelListener(dialog -> presenter.setSelectedModuleId(""));

        AppCompatTextView title = ButterKnife.findById(dialogView, id.module_permission_title);
        title.setText(selectedModule.getTitle());

        AppCompatImageView imageView = ButterKnife.findById(dialogView, id.module_permission_icon);

        Picasso.with(this)
                .load(selectedModule.getLogo())
                .placeholder(drawable.no_image)
                .transform(new CircleTransformation())
                .into(imageView);

        permissionsEmptyRequired = ButterKnife.findById(
                dialogView,
                id.module_permissions_required_list_empty);

        permissionsEmptyOptional = ButterKnife.findById(
                dialogView,
                id.module_permissions_optional_list_empty);

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
                PermissionAdapter.REQUIRED,
                false,
                true));

        permissionOptionalRecyclerView.setAdapter(new PermissionAdapter(
                optionalModuleSensors,
                PermissionAdapter.OPTIONAL,
                false,
                true));

        AlertDialog alertDialog = builder.create();

        if (!isFinishing()) {

            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, color.myAccentColor));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, color.myAccentColor));
        }
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

        Builder builder = new Builder(this, style.MyAppCompatAlertDialog);

        builder.setPositiveButton(string.button_ok, (dialog, which) -> {

            Log.d(TAG, "User tapped UNINSTALL " + selectedModule.getTitle() + " module");
            presenter.presentModuleUninstall(ConverterUtils.convertModule(selectedModule));
        });

        builder.setNegativeButton(string.button_cancel, (dialog, which) -> {

            Log.d(TAG, "User tapped cancel uninstall procedure");
            presenter.setSelectedModuleId("");
        });

        builder.setOnCancelListener(dialog -> presenter.setSelectedModuleId(""));

        builder.setTitle(getString(string.module_uninstall_title, selectedModule.getTitle()));
        builder.setMessage(string.module_uninstall_message);

        AlertDialog alertDialog = builder.create();

        if (!isFinishing()) {

            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, color.myAccentColor));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, color.myAccentColor));
        }
    }

    @Override
    public void showPermissionsAreCrucialDialog(Set<String> declinedPermissions) {

        Toaster.showLong(getApplicationContext(), string.permission_is_crucial);

        presenter.presentModuleInstallationHasError(declinedPermissions);
    }

    @Override
    public void showMoreModuleInformationDialog(final ModuleResponseDto selectedModule) {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(layout.dialog_module_more_info, null);

        permissionRequiredRecyclerView = ButterKnife.findById(
                dialogView,
                id.module_permission_required_list
        );
        permissionRequiredRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        permissionOptionalRecyclerView = ButterKnife.findById(
                dialogView,
                id.module_permission_optional_list
        );
        permissionOptionalRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        permissionsEmptyRequired = ButterKnife.findById(
                dialogView,
                id.module_permissions_required_list_empty);

        permissionsEmptyOptional = ButterKnife.findById(
                dialogView,
                id.module_permissions_optional_list_empty);

        // check if module active
        String userToken = PreferenceProvider.getInstance(this).getUserToken();
        DbUser user = DaoProvider.getInstance(this).getUserDao().getByToken(userToken);
        DbModule activeModule = DaoProvider.getInstance(this)
                .getModuleDao()
                .getByPackageIdUserId(selectedModule.getPackageName(), user.getId());
        boolean isModulesActive = activeModule != null ? activeModule.getActive() : false;

        LinearLayoutCompat modulePermReq = ButterKnife.findById(dialogView, id.module_perm_req_view);
        LinearLayoutCompat modulePermOpt = ButterKnife.findById(dialogView, id.module_perm_opt_view);

        if (!isModulesActive) {
            modulePermReq.setVisibility(View.GONE);
            modulePermOpt.setVisibility(View.GONE);
        } else {
            modulePermReq.setVisibility(View.VISIBLE);
            modulePermOpt.setVisibility(View.VISIBLE);
        }

        List<ModuleCapabilityResponseDto> requiredSensors = selectedModule.getSensorsRequired();
        List<ModuleCapabilityResponseDto> optionalSensors = selectedModule.getSensorsOptional();

        List<PermissionListItem> requiredModuleSensors = new ArrayList<>();
        List<PermissionListItem> optionalModuleSensors = new ArrayList<>();

        if (requiredSensors != null && !requiredSensors.isEmpty()) {

            if (isModulesActive && activeModule != null) {

                List<DbModuleCapability> caps = activeModule.getDbModuleCapabilityList();

                for (DbModuleCapability cap : caps) {
                    if (cap.getRequired()) {
                        requiredModuleSensors.add(new PermissionListItem(cap));
                    }
                }

            } else {
                for (ModuleCapabilityResponseDto capability : requiredSensors) {
                    requiredModuleSensors.add(new PermissionListItem(
                            ConverterUtils.convertModuleCapability(capability)
                    ));
                }
            }
        } else {
            toggleShowRequiredPermissions(true);
        }

        if (optionalSensors != null && !optionalSensors.isEmpty()) {

            if (isModulesActive && activeModule != null) {

                List<DbModuleCapability> caps = activeModule.getDbModuleCapabilityList();

                for (DbModuleCapability cap : caps) {
                    if (!cap.getRequired()) {
                        optionalModuleSensors.add(new PermissionListItem(cap));
                    }
                }

            } else {
                for (ModuleCapabilityResponseDto capability : optionalSensors) {
                    optionalModuleSensors.add(new PermissionListItem(
                            ConverterUtils.convertModuleCapability(capability)
                    ));
                }
            }
        } else {
            toggleShowOptionalPermissions(true);
        }

        permissionRequiredRecyclerView.setAdapter(new PermissionAdapter(
                requiredModuleSensors,
                PermissionAdapter.REQUIRED,
                isModulesActive,
                false));

        permissionOptionalRecyclerView.setAdapter(new PermissionAdapter(
                optionalModuleSensors,
                PermissionAdapter.HIDDEN,
                isModulesActive,
                false));

        Builder dialogBuilder = new Builder(this, style.MyAppCompatAlertDialog);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setPositiveButton(string.button_ok,
                (dialog, which) -> Log.d(TAG, "User tapped more information about the " + selectedModule.getTitle() + " module"));

        dialogBuilder.setTitle(selectedModule.getTitle());

        AppCompatTextView moreInfoFull = ButterKnife.findById(dialogView, id.module_more_info);
        moreInfoFull.setText(selectedModule.getDescriptionFull());

        AlertDialog alertDialog = dialogBuilder.create();

        if (!isFinishing()) {

            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, color.myAccentColor));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, color.myAccentColor));
        }
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

    private class ActivatedModulesSubscriber extends Subscriber<ActivatedModulesResponse> {

        @Override
        public void onStart() {
            super.onStart();
            showLoading();
        }

        @Override
        public void onCompleted() {
            // empty
            hideLoading();
        }

        @Override
        public void onError(Throwable e) {

            hideLoading();

            if (e instanceof RetrofitError) {
                presenter.onActivatedModulesFailed((RetrofitError) e);
            }
        }

        @Override
        public void onNext(ActivatedModulesResponse activatedModulesResponse) {
            presenter.onActivatedModulesReceived(activatedModulesResponse);
        }
    }

    private class ModuleActivationSubscriber extends Subscriber<Void> {

        @Override
        public void onStart() {
            super.onStart();
            showLoading();
        }

        @Override
        public void onCompleted() {
            hideLoading();
        }

        @Override
        public void onError(Throwable e) {

            hideLoading();

            if (e instanceof RetrofitError) {
                presenter.onModuleActivateFailed((RetrofitError) e);
            }
        }

        @Override
        public void onNext(Void aVoid) {
            presenter.onModuleActivateSuccess();
        }
    }

    private class ModuleDeactivationSubscriber extends Subscriber<Void> {

        @Override
        public void onStart() {
            super.onStart();
            showLoading();
        }

        @Override
        public void onCompleted() {
            hideLoading();
        }

        @Override
        public void onError(Throwable e) {

            hideLoading();

            if (e instanceof RetrofitError) {

                RetrofitError error = (RetrofitError) e;
                presenter.onModuleDeactivateFailed(error);
            }
        }

        @Override
        public void onNext(Void aVoid) {
            presenter.onModuleDeactivateSuccess();
        }
    }
}