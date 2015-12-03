package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.AvailableModulesAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.adapter.PermissionAdapter;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleInstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleInstallationSuccessfulEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleShowMoreInfoEvent;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleUninstallEvent;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.AvailableModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.ModuleCapabilityResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.ToggleModuleRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.ModuleEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.model.item.PermissionListItem;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.modules.ModulesPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.modules.ModulesPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.service.HarvesterService;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.DeviceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.LoginUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.ModulesView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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

    private DaoProvider daoProvider;

    private List<String> mActiveModules;

    private Map<String, AvailableModuleResponseDto> availableModuleResponseMapping;

    private SwipeRefreshLayout.OnRefreshListener onRefreshHandler;

    private String selectedModuleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPresenter(new ModulesPresenterImpl(this));
        presenter.doInitView();

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

                    // request new modules infomation
                    requestAvailableModules();
                }
            };
        }

        mSwipeRefreshLayout.setOnRefreshListener(onRefreshHandler);
        mSwipeRefreshLayout.setNestedScrollingEnabled(true);

        // register this activity to events
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        loadModules();
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
     * Loads module list from db or in case its empty
     * loads it from server
     */
    private void loadModules() {

        String userEmail = PreferenceUtils.getUserEmail(getApplicationContext());

        DbUser user = daoProvider.getUserDao().getByEmail(userEmail);

        if (user == null) {

            LoginUtils.doLogout(getApplicationContext());
            finish();
            return;
        }

        List<DbModule> installedModules = user.getDbModuleList();

        // no modules was found -> request from server
        if (installedModules.isEmpty()) {
            Log.d(TAG, "Module list not found in db. Requesting from server...");

            requestAvailableModules();

        } else {

            Log.d(TAG, "Installed modules found in the db. Showing them...");

            availableModuleResponseMapping = new HashMap<>();

            for (DbModule module : installedModules) {

                availableModuleResponseMapping.put(
                        module.getPackageName(),
                        ConverterUtils.convertModule(module));
            }

            mAvailableModulesRecyclerView
                    .setAdapter(new AvailableModulesAdapter(installedModules));

            Set<String> permsToAsk = checkPermissionsGranted();

            // ask if there is something to ask
            if (!permsToAsk.isEmpty()) {

                ActivityCompat.requestPermissions(this,
                        permsToAsk.toArray(new String[permsToAsk.size()]),
                        Constants.PERM_MODULE_INSTALL);
            }
        }
    }

    /**
     * Check permissions is still granted to modules
     */
    private Set<String> checkPermissionsGranted() {

        Map<String, String[]> mappings = PermissionUtils
                .getInstance(getApplicationContext())
                .getDangerousPermissionsToDtoMapping();

        Set<String> permissionsToAsk = new HashSet<>();

        long userId = PreferenceUtils.getCurrentUserId(getApplicationContext());

        List<DbModule> allActiveModules = daoProvider
                .getModuleDao()
                .getAllActive(userId);

        if (allActiveModules == null || allActiveModules.isEmpty()) {
            return Collections.emptySet();
        }

        for (DbModule module : allActiveModules) {

            if (module == null) {
                continue;
            }

            List<DbModuleCapability> capabilities = module.getDbModuleCapabilityList();

            for (DbModuleCapability cap : capabilities) {

                if (cap == null) {
                    continue;
                }

                final String[] perms = mappings == null ? null : mappings.get(cap.getType());

                if (perms == null) {
                    continue;
                }

                for (String perm : perms) {
                    if (PermissionUtils
                            .getInstance(getApplicationContext())
                            .isPermissionGranted(perm)) {

                        permissionsToAsk.add(perm);
                    }
                }
            }
        }

        return permissionsToAsk;
    }

    /**
     * Request available modules from service
     */
    private void requestAvailableModules() {

        final String userToken = PreferenceUtils.getUserToken(getApplicationContext());

        // calling api service
        final ModuleEndpoint moduleEndpoint = EndpointGenerator
                .getInstance(getApplicationContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.getAvailableModules(userToken,
                new Callback<List<AvailableModuleResponseDto>>() {

                    /**
                     * Successful HTTP response.
                     *
                     * @param availableModulesList
                     * @param response
                     */
                    @Override
                    public void success(final List<AvailableModuleResponseDto> availableModulesList,
                                        Response response) {

                        if (availableModulesList != null &&
                                !availableModulesList.isEmpty()) {

                            Log.d(TAG, availableModulesList.toString());

                            if (availableModuleResponseMapping == null) {
                                availableModuleResponseMapping = new HashMap<>();
                            } else {
                                availableModuleResponseMapping.clear();
                            }

                            for (AvailableModuleResponseDto resp : availableModulesList) {
                                availableModuleResponseMapping.put(resp.getModulePackage(), resp);
                            }

                            boolean hasUserRequestedActiveModules = PreferenceUtils
                                    .hasUserRequestedActiveModules(getApplicationContext());

                            if (hasUserRequestedActiveModules) {

                                mActiveModules = new ArrayList<>(0);
                                mSwipeRefreshLayout.setRefreshing(false);
                                processAvailableModules(availableModulesList);
                                return;
                            }

                            // get list of already activated modules
                            moduleEndpoint.getActiveModules(userToken,
                                    new Callback<List<String>>() {

                                        @Override
                                        public void success(List<String> activeModules,
                                                            Response response) {

                                            mSwipeRefreshLayout.setRefreshing(false);

                                            PreferenceUtils.setUserRequestedActiveModules(getApplicationContext(), true);

                                            if (activeModules != null && !activeModules.isEmpty()) {

                                                Log.d(TAG, activeModules.toString());
                                                mActiveModules = activeModules;

                                            } else {
                                                mActiveModules = new ArrayList<>(0);
                                            }

                                            processAvailableModules(availableModulesList);
                                        }

                                        @Override
                                        public void failure(RetrofitError error) {
                                            showErrorMessages(error);
                                            mActiveModules = new ArrayList<>(0);
                                            mSwipeRefreshLayout.setRefreshing(false);
                                            processAvailableModules(availableModulesList);
                                        }
                                    });

                        } else {
                            mActiveModules = new ArrayList<>(0);
                            mAvailableModulesRecyclerView.setAdapter(new AvailableModulesAdapter(Collections.EMPTY_LIST));
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    /**
                     * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
                     * exception.
                     *
                     * @param error
                     */
                    @Override
                    public void failure(RetrofitError error) {
                        showErrorMessages(error);
                        mAvailableModulesRecyclerView.setAdapter(new AvailableModulesAdapter(Collections.EMPTY_LIST));
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    /**
     * Populates and saves available modules
     *
     * @param availableModulesResponse
     */
    private void processAvailableModules(List<AvailableModuleResponseDto> availableModulesResponse) {

        List<DbModule> convertedModules = new ArrayList<>();

        for (AvailableModuleResponseDto response : availableModulesResponse) {

            convertedModules.add(ConverterUtils.convertModule(response));
        }

        // show list of modules to user
        populateAvailableModuleList(convertedModules);

        // insert only active modules into db
        insertActiveModulesIntoDb(convertedModules);

        // request permissions to inserted modules
        requestActiveModulesPermissions();
    }

    /**
     * Insert already activated modules earlier
     *
     * @param convertedModules
     */
    private void insertActiveModulesIntoDb(List<DbModule> convertedModules) {

        for (DbModule module : convertedModules) {

            if (module.getActive()) {

                // insert active module into db
                long installId = daoProvider.getModuleDao().insert(module);

                // inserting module capabilities
                AvailableModuleResponseDto moduleResponse = availableModuleResponseMapping
                        .get(module.getPackageName());

                List<ModuleCapabilityResponseDto> requiredCaps = moduleResponse.getSensorsRequired();
                List<ModuleCapabilityResponseDto> optionalCaps = moduleResponse.getSensorsOptional();

                List<DbModuleCapability> dbRequiredCaps = new ArrayList<>(requiredCaps.size());
                List<DbModuleCapability> dbOptionalCaps = new ArrayList<>(optionalCaps.size());

                for (ModuleCapabilityResponseDto response : requiredCaps) {

                    final DbModuleCapability dbCap = ConverterUtils.convertModuleCapability(response);
                    dbCap.setModuleId(installId);
                    dbRequiredCaps.add(dbCap);
                }

                for (ModuleCapabilityResponseDto response : optionalCaps) {

                    final DbModuleCapability dbCap = ConverterUtils.convertModuleCapability(response);
                    dbCap.setModuleId(installId);
                    dbOptionalCaps.add(dbCap);
                }

                daoProvider
                        .getModuleCapabilityDao()
                        .insert(dbRequiredCaps);

                daoProvider
                        .getModuleCapabilityDao()
                        .insert(dbOptionalCaps);
            }
        }
    }

    /**
     * Request permissions for installed active modules
     */
    private void requestActiveModulesPermissions() {

        long userId = PreferenceUtils.getCurrentUserId(getApplicationContext());

        List<DbModule> activeModulesList = daoProvider
                .getModuleDao()
                .getAllActive(userId);

        // there are active modules
        if (!activeModulesList.isEmpty()) {

            Set<String> permsToAsk = new HashSet<>();

            for (DbModule module : activeModulesList) {

                List<DbModuleCapability> capabilities = daoProvider
                        .getModuleCapabilityDao()
                        .getAllActive(module.getId());

                for (DbModuleCapability cap : capabilities) {

                    String[] perms = PermissionUtils
                            .getInstance(getApplicationContext())
                            .getDangerousPermissionsToDtoMapping()
                            .get(cap.getType());

                    permsToAsk.addAll(Arrays.asList(perms));
                }
            }


        }
    }

    /**
     * Show available modules to user
     *
     * @param modules
     */
    private void populateAvailableModuleList(List<DbModule> modules) {

        applyAlreadyActiveModulesFromRequest(modules);
        applyAlreadyActiveModulesFromDb(modules);

        AvailableModulesAdapter adapter = (AvailableModulesAdapter) mAvailableModulesRecyclerView
                .getAdapter();

        // we have entries already -> just swap them with new ones
        if (adapter != null && adapter.getItemCount() > 0) {

            adapter.swapData(modules);

        } else {

            // create new recycler view adapter
            mAvailableModulesRecyclerView.setAdapter(new AvailableModulesAdapter(modules));
        }
    }

    /**
     * check local db for active modules
     *
     * @param modules
     */
    private void applyAlreadyActiveModulesFromDb(List<DbModule> modules) {

        long userId = PreferenceUtils.getCurrentUserId(getApplicationContext());

        List<DbModule> activeModules = daoProvider
                .getModuleDao()
                .getAllActive(userId);

        if (activeModules != null && !activeModules.isEmpty()) {

            List<String> allActiveModulePackageIds = new ArrayList<>();

            for (DbModule activeModule : activeModules) {
                allActiveModulePackageIds.add(activeModule.getPackageName());
            }

            for (DbModule dbModule : modules) {
                if (allActiveModulePackageIds.contains(dbModule.getPackageName())) {
                    dbModule.setActive(true);
                }
            }
        }
    }

    /**
     * Just sets activated state for new modules
     *
     * @param modules
     */
    private void applyAlreadyActiveModulesFromRequest(List<DbModule> modules) {

        if (mActiveModules == null || mActiveModules.isEmpty()) {
            return;
        }

        long userId = PreferenceUtils.getCurrentUserId(getApplicationContext());

        for (DbModule module : modules) {

            if (mActiveModules.contains(module.getPackageName())) {

                module.setActive(true);
                module.setUserId(userId);
            }
        }
    }

    /**
     * On module install event
     *
     * @param event
     */
    public void onEvent(ModuleInstallEvent event) {
        Log.d(TAG, "Received installation event. Module id: " + event.getModuleId());

        this.selectedModuleId = event.getModuleId();

        showPermissionDialog();
    }

    /**
     * On module install event
     *
     * @param event
     */
    public void onEvent(ModuleUninstallEvent event) {
        Log.d(TAG, "Received uninstall event. Module id: " + event.getModuleId());

        this.selectedModuleId = event.getModuleId();

        showUninstallDialog();
    }

    /**
     * On module show more info event
     *
     * @param event
     */
    public void onEvent(ModuleShowMoreInfoEvent event) {
        Log.d(TAG, "Received show more info event. Module id: " + event.getModuleId());

        this.selectedModuleId = event.getModuleId();

        showMoreModuleInformationDialog();
    }

    /**
     * On module successful installation
     *
     * @param event
     */
    public void onEvent(ModuleInstallationSuccessfulEvent event) {
        Log.d(TAG, "After module successful installation. Module id: " + event.getModuleId());

        changeModuleLayout(event.getModuleId());

        startHarvester();
    }

    /**
     * Changes layout of installed module to installed state
     *
     * @param moduleId
     */
    private void changeModuleLayout(String moduleId) {

        Log.d(TAG, "Changing layout of a module to installed...");

        final AvailableModulesAdapter adapter = (AvailableModulesAdapter) mAvailableModulesRecyclerView
                .getAdapter();

        DbModule module = adapter.getItem(moduleId);

        // defensive programming
        if (module != null) {

            module.setActive(true);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Shows more information about an assistance module
     */
    private void showMoreModuleInformationDialog() {

        final AvailableModuleResponseDto selectedModule = availableModuleResponseMapping
                .get(selectedModuleId);

        if (selectedModule == null) {
            return;
        }

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

    /**
     * Shows uninstall dialog to user
     */
    private void showUninstallDialog() {

        final AvailableModuleResponseDto selectedModule = availableModuleResponseMapping
                .get(selectedModuleId);

        if (selectedModule == null) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setPositiveButton(R.string.button_ok_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "User tapped UNINSTALL " + selectedModule.getTitle() + " module");

                moduleUninstall();
            }
        });

        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                selectedModuleId = "";
            }
        });

        dialogBuilder.setTitle(getString(R.string.module_uninstall_title, selectedModule.getTitle()));
        dialogBuilder.setMessage(R.string.module_uninstall_message);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Shows a permission dialog to user
     * Each permission is used actually by a module
     */
    private void showPermissionDialog() {

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_permissions, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setPositiveButton(R.string.button_accept_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "User tapped accept button");

                askUserEnablePermissions();
            }
        });

        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                selectedModuleId = "";
            }
        });

        final AvailableModuleResponseDto selectedModule = availableModuleResponseMapping
                .get(selectedModuleId);

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

    /**
     * Asking user for grant permissions
     */

    private void askUserEnablePermissions() {

        if (availableModuleResponseMapping == null || availableModuleResponseMapping.isEmpty()) {

            Log.d(TAG, "availableModulesResponse is NULL or EMPTY");
            return;
        }

        // accumulate all permissions
        List<String> permsRequiredAccumulator = new ArrayList<>();

        AvailableModuleResponseDto module = availableModuleResponseMapping.get(selectedModuleId);

        if (module == null) {
            return;
        }

        List<ModuleCapabilityResponseDto> requiredSensors = module.getSensorsRequired();

        // handle required perms
        if (requiredSensors != null) {

            // these permissions are crucial for an operation of module
            for (ModuleCapabilityResponseDto capResponse : requiredSensors) {

                String apiType = capResponse.getType();
                String[] perms = PermissionUtils.getInstance(getApplicationContext())
                        .getDangerousPermissionsToDtoMapping()
                        .get(apiType);

                permsRequiredAccumulator.addAll(Arrays.asList(perms));
            }
        }

        // handle optional perms
        // get all checked optional sensors/events permissions
        List<DbModuleCapability> optionalSensors = getAllEnabledOptionalPermissions();

        if (optionalSensors != null) {

            for (DbModuleCapability response : optionalSensors) {

                String apiType = response.getType();
                String[] perms = PermissionUtils.getInstance(getApplicationContext())
                        .getDangerousPermissionsToDtoMapping()
                        .get(apiType);

                permsRequiredAccumulator.addAll(Arrays.asList(perms));
            }
        }

        ActivityCompat.requestPermissions(this,
                permsRequiredAccumulator.toArray(new String[permsRequiredAccumulator.size()]),
                Constants.PERM_MODULE_INSTALL);
    }

    /**
     * Returns list of permissions which were optional enabled by user
     *
     * @return
     */
    private List<DbModuleCapability> getAllEnabledOptionalPermissions() {

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

    /**
     * Saves information into db / install a module for user
     */
    private void installModule(final DbModule module) {

        if (module == null) {
            Log.d(TAG, "installModule: Module is NULL");
            return;
        }

        String userToken = PreferenceUtils.getUserToken(getApplicationContext());

        DbUser user = daoProvider
                .getUserDao()
                .getByToken(userToken);

        if (user == null) {

            LoginUtils.doLogout(getApplicationContext());
            return;
        }

        DbModule existingModule = daoProvider
                .getModuleDao()
                .getByPackageIdUserId(module.getPackageName(), user.getId());

        // module already existing for that user, abort installation
        if (existingModule != null) {
            return;
        }

        Log.d(TAG, "Installation of a module " + module.getPackageName() + " has started...");
        Log.d(TAG, "Requesting service...");

        ToggleModuleRequestDto toggleModuleRequest = new ToggleModuleRequestDto();
        toggleModuleRequest.setModuleId(module.getPackageName());

        ModuleEndpoint moduleEndpoint = EndpointGenerator.getInstance(
                getApplicationContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.activateModule(userToken, toggleModuleRequest,
                new Callback<Void>() {

                    @Override
                    public void success(Void aVoid, Response response) {

                        if (response.getStatus() == 200 || response.getStatus() == 204) {

                            Log.d(TAG, "Module is activated!");

                            saveModuleInstallationInDb(module);

                            Log.d(TAG, "Installation has finished!");

                            Set<String> permsToAsk = checkPermissionsGranted();

                            if (!permsToAsk.isEmpty()) {

                                ActivityCompat.requestPermissions(ModulesActivity.this,
                                        permsToAsk.toArray(new String[permsToAsk.size()]),
                                        Constants.PERM_MODULE_INSTALL);
                            }

                            EventBus.getDefault()
                                    .post(new ModuleInstallationSuccessfulEvent(
                                            module.getPackageName()));

                        } else {
                            Log.d(TAG, "FAIL: service responded with code: " + response.getStatus());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        showErrorMessages(error);

                        Log.d(TAG, "Installation has failed!");
                    }
                });
    }

    /**
     * Saves module installations status on device
     *
     * @param dbModule
     */
    private void saveModuleInstallationInDb(DbModule dbModule) {

        String userEmail = PreferenceUtils.getUserEmail(getApplicationContext());

        DbUser user = daoProvider.getUserDao().getByEmail(userEmail);

        if (user == null) {

            Log.d(TAG, "Installation cancelled: user is null");
            LoginUtils.doLogout(getApplicationContext());
            finish();
            return;
        }

        dbModule.setActive(true);
        dbModule.setUserId(user.getId());

        Long installId = daoProvider.getModuleDao().insert(dbModule);

        if (installId == null) {

            Toaster.showLong(getApplicationContext(), R.string.module_installation_unsuccessful);

        } else {

            // saving module capabilities
            AvailableModuleResponseDto moduleResponse = availableModuleResponseMapping.get(dbModule.getPackageName());

            List<ModuleCapabilityResponseDto> requiredCaps = moduleResponse.getSensorsRequired();
            List<ModuleCapabilityResponseDto> optionalCaps = moduleResponse.getSensorsOptional();

            List<DbModuleCapability> dbRequiredCaps = new ArrayList<>(
                    requiredCaps == null ? 0 : requiredCaps.size());
            List<DbModuleCapability> dbOptionalCaps = new ArrayList<>(
                    optionalCaps == null ? 0 : optionalCaps.size());

            for (ModuleCapabilityResponseDto response : requiredCaps) {

                final DbModuleCapability dbCap = ConverterUtils.convertModuleCapability(response);
                dbCap.setModuleId(installId);
                dbRequiredCaps.add(dbCap);
            }

            for (ModuleCapabilityResponseDto response : optionalCaps) {

                final DbModuleCapability dbCap = ConverterUtils.convertModuleCapability(response);
                dbCap.setModuleId(installId);
                dbOptionalCaps.add(dbCap);
            }

            daoProvider
                    .getModuleCapabilityDao()
                    .insert(dbRequiredCaps);

            daoProvider
                    .getModuleCapabilityDao()
                    .insert(dbOptionalCaps);

            PreferenceUtils.setUserHasModules(getApplicationContext(), true);
            Toaster.showLong(getApplicationContext(), R.string.module_installation_successful);
        }

        Log.d(TAG, "Installation id: " + installId);
    }

    /**
     * Uninstalls currently selected module
     */
    private void moduleUninstall() {

        String userToken = PreferenceUtils.getUserToken(getApplicationContext());

        if (userToken.isEmpty()) {

            Log.d(TAG, "userToken is empty");
            LoginUtils.doLogout(getApplicationContext());
            return;
        }

        final DbUser user = daoProvider
                .getUserDao()
                .getByToken(userToken);

        if (user == null) {

            Log.d(TAG, "user is NULL");
            LoginUtils.doLogout(getApplicationContext());
            return;
        }

        final DbModule module = daoProvider
                .getModuleDao()
                .getByPackageIdUserId(selectedModuleId, user.getId());

        Log.d(TAG, "Uninstall module. ModuleId: " + module.getId() +
                " package: " + module.getPackageName());

        // forming request to server
        ToggleModuleRequestDto toggleModuleRequest = new ToggleModuleRequestDto();
        toggleModuleRequest.setModuleId(module.getPackageName());

        ModuleEndpoint moduleEndpoint = EndpointGenerator.getInstance(getApplicationContext()).create(ModuleEndpoint.class);
        moduleEndpoint.deactivateModule(userToken, toggleModuleRequest,
                new Callback<Void>() {

                    @Override
                    public void success(Void aVoid, Response response) {

                        // deactivation successful
                        if (response.getStatus() == 200 || response.getStatus() == 204) {

                            uninstallModuleFromDb(module);

                            int numberOfModules = daoProvider
                                    .getModuleDao()
                                    .getAll(user.getId())
                                    .size();

                            // we have no entries in db, stop the sensing
                            if (numberOfModules == 0) {

                                stopHarvester();
                            }

                            Snackbar
                                    .make(findViewById(android.R.id.content),
                                            R.string.main_activity_undo_uninstall,
                                            Snackbar.LENGTH_LONG)
                                    .setAction(R.string.main_activity_undo_uninstall_button_title,
                                            new View.OnClickListener() {

                                                @Override
                                                public void onClick(View v) {
                                                    Log.d(TAG, "User tapped UNDO uninstall of a module!");

                                                    installModule(module);
                                                }
                                            })
                                    .setActionTextColor(Color.RED)
                                    .show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        showErrorMessages(error);

                        // no such installed module -> remove it immediately
                        if (error.getResponse() == null || error.getResponse().getStatus() == 400) {
                            uninstallModuleFromDb(module);
                        }
                    }
                });
    }

    /**
     * Starts harvester service
     */
    private void startHarvester() {

        boolean isRunning = DeviceUtils.isServiceRunning(
                getApplicationContext(),
                HarvesterService.class);

        if (!isRunning) {

            HarvesterServiceProvider
                    .getInstance(getApplicationContext())
                    .startSensingService();
        }
    }

    /**
     * Stops harvester service
     */
    private void stopHarvester() {

        boolean isRunning = DeviceUtils.isServiceRunning(
                getApplicationContext(),
                HarvesterService.class);

        if (isRunning) {

            HarvesterServiceProvider
                    .getInstance(getApplicationContext())
                    .stopSensingService();
        }
    }

    /**
     * Removes module from db
     *
     * @param module
     */
    private void uninstallModuleFromDb(DbModule module) {

        Log.d(TAG, "Removing module from db...");

        List<DbModule> modulesToDelete = new ArrayList<>(1);
        modulesToDelete.add(module);

        daoProvider
                .getModuleDao()
                .delete(modulesToDelete);

        Log.d(TAG, "Finished removing module from db!");
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

        switch (requestCode) {
            case Constants.PERM_MODULE_INSTALL:

                Log.d(TAG, "Back from module permissions request");

                List<String> declinedPermissions = new ArrayList<>();

                for (int i = 0, grantResultsLength = grantResults.length; i < grantResultsLength; i++) {

                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        // permission denied, it should be asked again

                        declinedPermissions.add(permissions[i]);
                    }
                }

                // ask user about permissions again
                if (declinedPermissions.size() > 0) {
                    showPermissionsAreCrucialDialog(declinedPermissions);
                } else {
                    // if all permissions were granted, we can install that module
                    installModule(ConverterUtils
                            .convertModule(availableModuleResponseMapping
                                    .get(selectedModuleId)));
                }

                break;
            default:
                break;
        }

    }

    /**
     * Shows some dialog, because that permissions are crucial for that module
     *
     * @param declinedPermissions
     */
    private void showPermissionsAreCrucialDialog(List<String> declinedPermissions) {


    }

    /**
     * Processes error response from server
     *
     * @param retrofitError
     */
    protected void showErrorMessages(RetrofitError retrofitError) {

        Response response = retrofitError.getResponse();

        if (response != null) {

            int httpCode = response.getStatus();

            switch (httpCode) {
                case 400:
                    break;
                case 401:
                    Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
                    PreferenceUtils.clearUserCredentials(getApplicationContext());
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
                case 404:
//                    Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
                    break;
                case 503:
//                    Toaster.showLong(getApplicationContext(), R.string.error_server_temporary_unavailable);
                    break;
                default:
//                    Toaster.showLong(getApplicationContext(), R.string.error_unknown);
                    break;
            }
        } else {
//            Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
        }
    }

    @Override
    public void initView() {

    }

    @Override
    public void startLoginActivity() {

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
    public void showUserActionForbidden() {

    }

    @Override
    public void setPresenter(ModulesPresenter presenter) {
        this.presenter = presenter;
    }
}
