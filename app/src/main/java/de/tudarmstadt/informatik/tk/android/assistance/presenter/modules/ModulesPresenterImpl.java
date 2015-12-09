package de.tudarmstadt.informatik.tk.android.assistance.presenter.modules;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.controller.modules.ModulesController;
import de.tudarmstadt.informatik.tk.android.assistance.controller.modules.ModulesControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.event.module.ModuleInstallationSuccessfulEvent;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnActiveModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnAvailableModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleActivatedResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleDeactivatedResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnUserNotFoundInDbHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.ModuleCapabilityResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.ToggleModuleRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.CommonPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.ModulesView;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public class ModulesPresenterImpl extends
        CommonPresenterImpl implements
        ModulesPresenter,
        OnUserNotFoundInDbHandler,
        OnAvailableModulesResponseHandler,
        OnActiveModulesResponseHandler,
        OnModuleActivatedResponseHandler,
        OnModuleDeactivatedResponseHandler {

    private static final String TAG = ModulesPresenterImpl.class.getSimpleName();

    private ModulesView view;
    private ModulesController controller;

    private List<ModuleResponseDto> availableModules;
    private List<String> mActiveModules;

    private Map<String, ModuleResponseDto> availableModuleResponseMapping;

    private String selectedModuleId;

    public ModulesPresenterImpl(Context context) {
        super(context);
        setController(new ModulesControllerImpl(this));
    }

    @Override
    public void setView(ModulesView view) {
        super.setView(view);
        this.view = view;
    }

    @Override
    public void setController(ModulesController controller) {
        this.controller = controller;
    }

    @Override
    public void doInitView() {
        view.initView();

        String userEmail = PreferenceUtils.getUserEmail(getContext());

        DbUser user = controller.getUserByEmail(userEmail);

        if (user == null) {
            view.startLoginActivity();
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

            view.setModuleList(installedModules);

            Set<String> permsToAsk = controller.getGrantedPermissions();

            // ask if there is something to ask
            if (!permsToAsk.isEmpty()) {
                view.askPermissions(permsToAsk);
            }
        }
    }

    @Override
    public void requestAvailableModules() {

        final String userToken = PreferenceUtils.getUserToken(getContext());

        // call api service
        controller.requestAvailableModules(userToken, this);
    }

    @Override
    public void onAvailableModulesSuccess(final List<ModuleResponseDto> apiResponse, Response response) {

        if (apiResponse != null && !apiResponse.isEmpty()) {

            Log.d(TAG, apiResponse.toString());

            availableModules = new ArrayList<>(apiResponse.size());
            availableModules.addAll(apiResponse);

            if (availableModuleResponseMapping == null) {
                availableModuleResponseMapping = new HashMap<>();
            } else {
                availableModuleResponseMapping.clear();
            }

            for (ModuleResponseDto resp : apiResponse) {
                availableModuleResponseMapping.put(resp.getModulePackage(), resp);
            }

            boolean hasUserRequestedActiveModules = PreferenceUtils
                    .hasUserRequestedActiveModules(getContext());

            if (hasUserRequestedActiveModules) {

                mActiveModules = new ArrayList<>(0);
                view.setSwipeRefreshing(false);

                processAvailableModules(apiResponse);

                return;
            }

            final String userToken = PreferenceUtils.getUserToken(getContext());

            // get list of already activated modules
            controller.requestActiveModules(userToken, this);

        } else {
            availableModules = new ArrayList<>(0);
            mActiveModules = new ArrayList<>(0);

            view.setNoModulesView();
        }
    }

    @Override
    public void onAvailableModulesError(RetrofitError error) {

        doDefaultErrorProcessing(error);
        view.setErrorView();
    }

    @Override
    public void onActiveModulesReceived(List<String> activeModules, Response response) {

        view.setSwipeRefreshing(false);

        PreferenceUtils.setUserRequestedActiveModules(getContext(), true);

        if (activeModules != null && !activeModules.isEmpty()) {

            Log.d(TAG, activeModules.toString());
            mActiveModules = activeModules;

        } else {
            mActiveModules = new ArrayList<>(0);
        }

        processAvailableModules(availableModules);
    }

    @Override
    public void onActiveModulesFailed(RetrofitError error) {

        doDefaultErrorProcessing(error);
        mActiveModules = new ArrayList<>(0);
        view.setSwipeRefreshing(false);
        processAvailableModules(availableModules);
    }

    @Override
    public void processAvailableModules(List<ModuleResponseDto> availableModulesResponse) {

        List<DbModule> convertedModules = new ArrayList<>();

        for (ModuleResponseDto response : availableModulesResponse) {
            convertedModules.add(ConverterUtils.convertModule(response));
        }

        // show list of modules to user
        applyAlreadyActiveModulesFromRequest(convertedModules);
        applyAlreadyActiveModulesFromDb(convertedModules);

        if (view.getModulesAmount() > 0) {
            // we have entries already -> just swap them with new ones
            view.swapModuleData(convertedModules);
        } else {
            // create new recycler view adapter
            view.setModuleList(convertedModules);
        }

        // insert only active modules into db
        insertActiveModulesIntoDb(convertedModules);

        // request permissions to inserted modules
        requestActiveModulesPermissions();
    }

    @Override
    public void insertActiveModulesIntoDb(List<DbModule> convertedModules) {

        for (DbModule module : convertedModules) {

            if (module.getActive()) {

                // insert active module into db
                controller.insertModuleWithCapabilities(availableModuleResponseMapping
                        .get(module.getPackageName()));

                // change layout after module insertion
                view.changeModuleLayout(module.getPackageName(), true);
            }
        }
    }

    @Override
    public void requestActiveModulesPermissions() {

        long userId = PreferenceUtils.getCurrentUserId(getContext());

        List<DbModule> activeModulesList = controller.getAllActiveModules(userId);

        // there are active modules
        if (!activeModulesList.isEmpty()) {

            Set<String> permsToAsk = new HashSet<>();

            for (DbModule module : activeModulesList) {

                List<DbModuleCapability> capabilities = controller
                        .getAllActiveModuleCapabilities(module.getId());

                for (DbModuleCapability cap : capabilities) {

                    String[] perms = PermissionUtils
                            .getInstance(getContext())
                            .getDangerousPermissionsToDtoMapping()
                            .get(cap.getType());

                    permsToAsk.addAll(Arrays.asList(perms));
                }
            }

            view.askPermissions(permsToAsk);
        }
    }

    @Override
    public void applyAlreadyActiveModulesFromRequest(List<DbModule> modules) {

        if (mActiveModules == null || mActiveModules.isEmpty()) {
            return;
        }

        long userId = PreferenceUtils.getCurrentUserId(getContext());

        for (DbModule module : modules) {

            if (mActiveModules.contains(module.getPackageName())) {

                module.setActive(true);
                module.setUserId(userId);
            }
        }
    }

    @Override
    public void applyAlreadyActiveModulesFromDb(List<DbModule> modules) {

        long userId = PreferenceUtils.getCurrentUserId(getContext());

        List<DbModule> activeModules = controller.getAllActiveModules(userId);

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

    @Override
    public void presentModuleInstallation(DbModule module) {

        if (module == null) {
            Log.d(TAG, "installModule: Module is NULL");
            return;
        }

        String userToken = PreferenceUtils.getUserToken(getContext());

        DbUser user = controller.getUserByToken(userToken);

        if (user == null) {
            view.startLoginActivity();
            return;
        }

        DbModule existingModule = controller.getModuleByPackageIdUserId(
                module.getPackageName(),
                user.getId());

        // module already existing for that user, abort installation
        if (existingModule != null) {
            Log.d(TAG, "existingModule is NULL");
            return;
        }

        Log.d(TAG, "Installation of a module " + module.getPackageName() + " has started...");
        Log.d(TAG, "Requesting service...");

        ToggleModuleRequestDto toggleModuleRequest = new ToggleModuleRequestDto();
        toggleModuleRequest.setModuleId(module.getPackageName());

        controller.requestModuleActivation(toggleModuleRequest, userToken, module, this);
    }

    @Override
    public void presentModuleUninstall(DbModule module) {

        String userToken = PreferenceUtils.getUserToken(getContext());

        if (userToken.isEmpty()) {
            Log.d(TAG, "userToken is empty");
            view.startLoginActivity();
            return;
        }

        final DbUser user = controller.getUserByToken(userToken);

        if (user == null) {
            Log.d(TAG, "user is NULL");
            view.startLoginActivity();
            return;
        }

        Log.d(TAG, "Uninstall module. ModuleId: " + module.getId() +
                " package: " + module.getPackageName());

        // forming request to server
        ToggleModuleRequestDto toggleModuleRequest = new ToggleModuleRequestDto();
        toggleModuleRequest.setModuleId(module.getPackageName());

        controller.requestModuleDeactivation(toggleModuleRequest, userToken, module, this);
    }

    @Override
    public void presentPermissionDialog() {

        final ModuleResponseDto selectedModule = availableModuleResponseMapping
                .get(selectedModuleId);

        if (selectedModule == null) {
            Log.d(TAG, "Module is not in mapping!");
            return;
        }

        view.showPermissionDialog(selectedModule);
    }

    @Override
    public void presentUninstallDialog() {

        final ModuleResponseDto selectedModule = availableModuleResponseMapping
                .get(selectedModuleId);

        if (selectedModule == null) {
            return;
        }

        view.showUninstallDialog(selectedModule);
    }

    @Override
    public void setSelectedModuleId(String modulePackageName) {
        selectedModuleId = modulePackageName;
    }

    @Override
    public void presentRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {

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
                    view.showPermissionsAreCrucialDialog(declinedPermissions);
                } else {
                    presentModuleInstallation(ConverterUtils.convertModule(
                            availableModuleResponseMapping.get(selectedModuleId)));
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void presentMoreModuleInformationDialog() {

        final ModuleResponseDto selectedModule = availableModuleResponseMapping
                .get(selectedModuleId);

        if (selectedModule == null) {
            return;
        }

        view.showMoreModuleInformationDialog(selectedModule);
    }

    @Override
    public void presentSuccessfulInstallation() {

        view.changeModuleLayout(selectedModuleId, true);
        startHarvester();
        view.showModuleInstallationSuccessful();
    }

    @Override
    public void askUserEnablePermissions() {

        if (availableModuleResponseMapping == null || availableModuleResponseMapping.isEmpty()) {

            Log.d(TAG, "availableModulesResponse is NULL or EMPTY");
            return;
        }

        // accumulate all permissions
        Set<String> permsRequiredAccumulator = new HashSet<>();

        ModuleResponseDto module = availableModuleResponseMapping.get(selectedModuleId);

        if (module == null) {
            return;
        }

        List<ModuleCapabilityResponseDto> requiredSensors = module.getSensorsRequired();

        // handle required perms
        if (requiredSensors != null) {

            // these permissions are crucial for an operation of module
            for (ModuleCapabilityResponseDto capResponse : requiredSensors) {

                String apiType = capResponse.getType();
                String[] perms = PermissionUtils.getInstance(getContext())
                        .getDangerousPermissionsToDtoMapping()
                        .get(apiType);

                if (perms == null) {
                    continue;
                }

                for (String perm : perms) {

                    // check permission was already granted
                    if (ContextCompat.checkSelfPermission(getContext(), perm) !=
                            PackageManager.PERMISSION_GRANTED) {

                        permsRequiredAccumulator.add(perm);
                    }
                }
            }
        }

        // handle optional perms
        // get all checked optional sensors/events permissions
        List<DbModuleCapability> optionalSensors = view.getAllEnabledOptionalPermissions();

        if (optionalSensors != null) {

            for (DbModuleCapability response : optionalSensors) {

                if (response == null) {
                    continue;
                }

                String apiType = response.getType();
                String[] perms = PermissionUtils.getInstance(getContext())
                        .getDangerousPermissionsToDtoMapping()
                        .get(apiType);

                if (perms == null) {
                    continue;
                }

                for (String perm : perms) {

                    // check permission was already granted
                    if (ContextCompat.checkSelfPermission(getContext(), perm) !=
                            PackageManager.PERMISSION_GRANTED) {

                        permsRequiredAccumulator.add(perm);
                    }
                }
            }
        }

        if (permsRequiredAccumulator.isEmpty()) {

            Log.d(TAG, "permsRequiredAccumulator is empty. its ok. all perms granted");

            presentModuleInstallation(ConverterUtils.convertModule(availableModuleResponseMapping
                    .get(selectedModuleId)));

        } else {

            Log.d(TAG, "Asking permissions...");

            view.askPermissions(permsRequiredAccumulator);
        }
    }

    @Override
    public void onModuleActivateSuccess(DbModule module, Response response) {

        if (response.getStatus() == 200 || response.getStatus() == 204) {

            Log.d(TAG, "Module is activated!");

            String userEmail = PreferenceUtils.getUserEmail(getContext());

            DbUser user = controller.getUserByEmail(userEmail);

            if (user == null) {
                view.startLoginActivity();
                return;
            }

            module.setActive(true);
            module.setUserId(user.getId());

            Long installId = controller.insertModuleToDb(module);

            if (installId == null) {
                view.showModuleInstallationFailed();
                return;
            } else {

                // saving module capabilities
                ModuleResponseDto moduleResponse = availableModuleResponseMapping
                        .get(module.getPackageName());

                List<ModuleCapabilityResponseDto> requiredCaps = moduleResponse.getSensorsRequired();
                List<ModuleCapabilityResponseDto> optionalCaps = moduleResponse.getSensorsOptional();

                List<DbModuleCapability> dbRequiredCaps = new ArrayList<>(
                        requiredCaps == null ? 0 : requiredCaps.size());
                List<DbModuleCapability> dbOptionalCaps = new ArrayList<>(
                        optionalCaps == null ? 0 : optionalCaps.size());

                for (ModuleCapabilityResponseDto capResponse : requiredCaps) {

                    final DbModuleCapability cap = ConverterUtils.convertModuleCapability(capResponse);

                    cap.setModuleId(installId);
                    cap.setActive(true);

                    dbRequiredCaps.add(cap);
                }

                for (ModuleCapabilityResponseDto capResponse : optionalCaps) {

                    final DbModuleCapability cap = ConverterUtils.convertModuleCapability(capResponse);

                    cap.setModuleId(installId);
                    cap.setActive(true);

                    dbOptionalCaps.add(cap);
                }

                controller.insertModuleCapabilitiesToDb(dbRequiredCaps);
                controller.insertModuleCapabilitiesToDb(dbOptionalCaps);

                PreferenceUtils.setUserHasModules(getContext(), true);

                view.showModuleInstallationSuccessful();
            }

            Log.d(TAG, "Installation id: " + installId);
            Log.d(TAG, "Installation has finished!");

            Set<String> permsToAsk = controller.getGrantedPermissions();

            if (!permsToAsk.isEmpty()) {
                view.askPermissions(permsToAsk);
            } else {
                EventBus.getDefault().post(new ModuleInstallationSuccessfulEvent(
                        module.getPackageName()));
            }

        } else {
            Log.d(TAG, "FAIL: service responded with code: " + response.getStatus());
        }
    }

    @Override
    public void onModuleActivateFailed(RetrofitError error) {

        doDefaultErrorProcessing(error);
        Log.d(TAG, "Installation has failed!");
    }

    @Override
    public void onModuleDeactivateSuccess(DbModule module, Response response) {

        // deactivation successful
        if (response.getStatus() == 200 || response.getStatus() == 204) {

            if (controller.uninstallModuleFromDb(module)) {

                int numberOfModules = controller.getAllModules(module.getUserId()).size();

                // we have no entries in db, stop the sensing
                if (numberOfModules == 0) {
                    stopHarvester();
                }

                view.changeModuleLayout(module.getPackageName(), false);

//            view.showUndoAction(module);
            }
        }
    }

    @Override
    public void onModuleDeactivateFailed(DbModule module, RetrofitError error) {

        doDefaultErrorProcessing(error);

        // no such installed module -> remove it immediately
        if (error.getResponse() == null || error.getResponse().getStatus() == 400) {

            if (controller.uninstallModuleFromDb(module)) {

                int numberOfModules = controller.getAllModules(module.getUserId()).size();

                // we have no entries in db, stop the sensing
                if (numberOfModules == 0) {
                    stopHarvester();
                }

                view.changeModuleLayout(module.getPackageName(), false);

//                view.showUndoAction(module);
            }
        }
    }

    @Override
    public void onUserFound(DbUser user) {
        // empty
    }

    @Override
    public void onUserNotFound() {

        Log.d(TAG, "Installation cancelled: user is null");
        view.startLoginActivity();
    }
}