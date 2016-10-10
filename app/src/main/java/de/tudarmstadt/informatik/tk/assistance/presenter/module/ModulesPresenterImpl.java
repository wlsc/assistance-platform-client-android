package de.tudarmstadt.informatik.tk.assistance.presenter.module;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.assistance.controller.module.ModulesController;
import de.tudarmstadt.informatik.tk.assistance.controller.module.ModulesControllerImpl;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleInstallSuccessfulEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleInstallationErrorEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModuleUninstallSuccessfulEvent;
import de.tudarmstadt.informatik.tk.assistance.event.module.ModulesListRefreshEvent;
import de.tudarmstadt.informatik.tk.assistance.presenter.CommonPresenterImpl;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ModuleCapabilityResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ToggleModuleRequestDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.PreferenceProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.SensorProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.view.ModulesView;
import retrofit2.adapter.rxjava.HttpException;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public class ModulesPresenterImpl extends
        CommonPresenterImpl implements
        ModulesPresenter {

    private static final String TAG = ModulesPresenterImpl.class.getSimpleName();

    private ModulesView view;
    private ModulesController controller;

    private final PermissionUtils permissionUtils;

    private Map<String, ModuleResponseDto> availableModuleResponseMapping;

    private String selectedModuleId;

    public ModulesPresenterImpl(Context context) {
        super(context);
        controller = new ModulesControllerImpl(this);
        permissionUtils = PermissionUtils.getInstance(context);
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
    public void initView() {

        view.initView();

        final String userToken = PreferenceUtils.getUserToken(getContext());
        final DbUser user = controller.getUserByToken(userToken);

        if (user == null) {
            Log.d(TAG, "User is null");
            view.startLoginActivity();
            return;
        }

//        final List<DbModule> installedModules = user.getDbModuleList();

        // no modules was found -> request from server
//        if (installedModules.isEmpty()) {
//            Log.d(TAG, "Module list not found in db. Requesting from server...");

        requestAvailableModules();

//        } else {
//            Log.d(TAG, "Installed modules found in the db. Showing them...");
//
//            availableModuleResponseMapping = new HashMap<>();
//
//            for (DbModule module : installedModules) {
//
//                availableModuleResponseMapping.put(
//                        module.getPackageName(),
//                        ConverterUtils.convertModule(module));
//            }
//
//            view.setModuleList(installedModules);
//
//            Set<String> permsToAsk = controller.getGrantedPermissions();
//
//            // ask if there is something to ask
//            if (!permsToAsk.isEmpty()) {
//                view.askPermissions(permsToAsk);
//            }
//        }
    }

    @Override
    public void requestAvailableModules() {

        final String userToken = PreferenceUtils.getUserToken(getContext());

        // subscribe to call api service
        view.subscribeActivatedModules(controller.requestActivatedModules(userToken));
    }

    @Override
    public void processAvailableModules(List<ModuleResponseDto> availableModulesResponse,
                                        Set<String> activeModules) {

        List<DbModule> convertedModules = new ArrayList<>(availableModulesResponse.size());

        for (ModuleResponseDto response : availableModulesResponse) {
            convertedModules.add(ConverterUtils.convertModule(response));
        }

        if (activeModules != null && !activeModules.isEmpty()) {

            Log.d(TAG, "Active modules: " + activeModules.toString());

            String userEmail = PreferenceUtils.getUserEmail(getContext());
            DbUser user = controller.getUserByEmail(userEmail);

            if (user == null) {
                Log.d(TAG, "User is null");
                return;
            }

            List<List<DbModuleCapability>> modulesCaps = new ArrayList<>();

            for (int i = 0, convertedModulesSize = convertedModules.size(); i < convertedModulesSize; i++) {

                DbModule module = convertedModules.get(i);

                if (activeModules.contains(module.getPackageName())) {

                    ModuleResponseDto moduleDto = availableModulesResponse.get(i);

                    module.setActive(true);
                    module.setUserId(user.getId());

                    List<DbModuleCapability> caps = ConverterUtils
                            .convertModuleCapability(moduleDto.getSensorsRequired(),
                                    moduleDto.getSensorsOptional());

                    for (DbModuleCapability cap : caps) {
                        if (cap == null) {
                            continue;
                        }

                        if (cap.getRequired()) {
                            cap.setActive(true);
                        }
                    }

                    modulesCaps.add(caps);
                }
            }

            // insert only active modules into db
            insertActiveModulesIntoDb(convertedModules, modulesCaps);

            if (view.getDisplayedModulesCount() > 0) {
                // list has items -> just swap them with new ones
                view.swapModuleData(convertedModules);
            } else {
                // create new recycler view adapter
                view.setModuleList(convertedModules);
            }

            EventBus.getDefault().post(new ModulesListRefreshEvent());

        } else {
            Log.d(TAG, "No active modules");

            if (view.getDisplayedModulesCount() > 0) {
                // list has items -> just swap them with new ones
                view.swapModuleData(convertedModules);
            } else {
                // create new recycler view adapter
                view.setModuleList(convertedModules);
            }
        }

        // request permissions to inserted modules
        requestModulesPermissions();
    }

    @Override
    public void insertActiveModulesIntoDb(List<DbModule> modules, List<List<DbModuleCapability>> modulesCaps) {

        try {
            for (int i = 0; i < modules.size(); i++) {

                DbModule module = modules.get(i);

                // insert active module into db
                if (module.getActive()) {

                    List<DbModuleCapability> caps = modulesCaps.get(i);

                    long moduleId = controller.insertModuleToDb(module);

                    if (caps == null) {
                        continue;
                    }

                    for (DbModuleCapability modCap : caps) {
                        modCap.setModuleId(moduleId);
                    }

                    controller.insertModuleCapabilitiesToDb(caps);
                }
            }
        } catch (Exception ignore) {

        }
    }

    @Override
    public void requestModulesPermissions() {

        String userToken = PreferenceUtils.getUserToken(getContext());

        List<DbModule> activeModulesList = controller.getAllActiveModules(userToken);

        // there are NO active modules
        if (activeModulesList.isEmpty()) {
            Log.d(TAG, "No active modules found in db.");
            return;
        }

        Set<String> permsToAsk = new HashSet<>();

        for (DbModule module : activeModulesList) {

            List<DbModuleCapability> capabilities = controller
                    .getAllActiveRequiredModuleCapabilities(module.getId());

            for (DbModuleCapability cap : capabilities) {

                String[] perms = PermissionUtils
                        .getInstance(getContext())
                        .getDangerousPermissionsToDtoMapping()
                        .get(cap.getType());

                if (perms != null) {

                    for (String perm : perms) {

                        // not granted -> ask user
                        if (!permissionUtils.isGranted(perm)) {
                            permsToAsk.add(perm);
                        }
                    }
                }
            }
        }

        view.askPermissions(permsToAsk, null);
    }

    @Override
    public void handleModuleActivationRequest(ModuleResponseDto moduleResponse) {

        if (moduleResponse == null) {
            Log.d(TAG, "installModule: Module response was NULL");
            return;
        }

        String userEmail = PreferenceUtils.getUserEmail(getContext());

        if (userEmail.isEmpty()) {
            view.startLoginActivity();
            return;
        }

        final DbUser user = controller.getUserByEmail(userEmail);

        if (user == null) {
            view.startLoginActivity();
            return;
        }

        Log.d(TAG, "Installation of a module " + moduleResponse.getPackageName() + " has started...");
        Log.d(TAG, "Requesting service...");

        String userToken = PreferenceUtils.getUserToken(getContext());

        ToggleModuleRequestDto toggleModuleRequest = new ToggleModuleRequestDto();
        toggleModuleRequest.setModulePackageName(moduleResponse.getPackageName());

        view.subscribeModuleActivation(controller.requestModuleActivation(userToken, toggleModuleRequest));
    }

    @Override
    public void handleModuleCapabilityStateChanged(DbModuleCapability moduleCapability) {

        if (moduleCapability == null) {
            return;
        }

        controller.updateModuleCapability(moduleCapability);
    }

    @Override
    public void presentModuleUninstall(final DbModule module) {

        final String userToken = PreferenceUtils.getUserToken(getContext());

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
        final ToggleModuleRequestDto toggleModuleRequest = new ToggleModuleRequestDto();
        toggleModuleRequest.setModulePackageName(module.getPackageName());

        view.subscribeModuleDeactivation(controller.requestModuleDeactivation(
                userToken,
                toggleModuleRequest));
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

        SensorProvider.getInstance(getContext()).synchronizeRunningSensorsWithDb();
        view.changeModuleLayout(selectedModuleId, true);
        startHarvester();
        view.showModuleInstallationSuccessful();
    }

    @Override
    public void handleModulePermissions() {

        // accumulate all required permissions
        Set<String> permsRequiredAccumulator = new HashSet<>();
        Set<String> permsOptionalAccumulator = new HashSet<>();

        ModuleResponseDto moduleResponse = getSelectedModuleResponse();

        if (moduleResponse == null) {
            Log.d(TAG, "Module response is null");
            return;
        }

        List<ModuleCapabilityResponseDto> requiredSensors = moduleResponse.getSensorsRequired();

        // handle required perms
        if (requiredSensors != null) {

            if (requiredSensors.isEmpty()) {
                Log.d(TAG, "requiredSensors is EMPTY -> ABORT!");
                view.showActionProhibited();
                return;
            }

            Map<String, String[]> dangerousPerms = permissionUtils
                    .getDangerousPermissionsToDtoMapping();

            // these permissions are crucial for an operation of module
            for (ModuleCapabilityResponseDto capResponse : requiredSensors) {

                String apiType = capResponse.getType();
                String[] perms = dangerousPerms.get(apiType);

                if (perms == null) {
                    Log.d(TAG, "Perms is null for type: " + apiType);
                    continue;
                }

                for (String perm : perms) {

                    // check permission was already granted
                    if (!permissionUtils.isGranted(perm)) {
                        permsRequiredAccumulator.add(perm);
                    }
                }
            }
        }

        // handle optional perms
        // get all checked optional sensors/events permissions
        List<DbModuleCapability> optionalSensors = view.getAllEnabledOptionalPermissions();

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
                if (!permissionUtils.isGranted(perm)) {
                    permsOptionalAccumulator.add(perm);
                }
            }
        }

        if (permsRequiredAccumulator.isEmpty() && permsOptionalAccumulator.isEmpty()) {
            Log.d(TAG, "permsRequiredAccumulator is empty. its ok. all perms granted");

            handleModuleActivationRequest(moduleResponse);

        } else {
            Log.d(TAG, "Asking permissions...");

            view.askPermissions(permsRequiredAccumulator, permsOptionalAccumulator);
        }
    }

    @Override
    public void presentModuleInstallationHasError(Set<String> declinedPermissions) {
        EventBus.getDefault().post(new ModuleInstallationErrorEvent(selectedModuleId));
    }

    @Override
    public void presentSuccessfulUninstall() {

        SensorProvider.getInstance(getContext()).synchronizeRunningSensorsWithDb();
        view.showModuleUninstallSuccessful();
    }

    @Override
    public ModuleResponseDto getSelectedModuleResponse() {

        if (availableModuleResponseMapping == null) {
            return null;
        }

        return availableModuleResponseMapping.get(selectedModuleId);
    }

    @Override
    public void onActivatedModulesReceived(ActivatedModulesResponse activatedModulesResponse) {

        Log.d(TAG, "onActivatedModulesReceived");
        Log.d(TAG, activatedModulesResponse.toString());

        view.setSwipeRefreshing(false);

        if (activatedModulesResponse == null ||
                activatedModulesResponse.getAvailableModules() == null ||
                activatedModulesResponse.getAvailableModules().isEmpty()) {
            view.setNoModulesView();
            return;
        } else {

            List<ModuleResponseDto> modulesResponse = activatedModulesResponse.getAvailableModules();

            Log.d(TAG, modulesResponse.toString());
            Log.d(TAG, "Filtering list according to device sensor availability...");

            // filter modules that have not runnable sensors in their required capabilities
            modulesResponse = controller.filterAvailableModulesList(modulesResponse);

            // mapping to available modules response
            if (availableModuleResponseMapping == null) {
                availableModuleResponseMapping = new HashMap<>();
            } else {
                availableModuleResponseMapping.clear();
            }

            for (ModuleResponseDto resp : modulesResponse) {
                availableModuleResponseMapping.put(resp.getPackageName(), resp);
            }
            // -------- mapping finished

            processAvailableModules(modulesResponse, activatedModulesResponse.getActiveModules());
        }
    }

    @Override
    public void onActivatedModulesFailed(HttpException error) {

        doDefaultErrorProcessing(error);
        view.setErrorView();
    }

    @Override
    public void refreshModuleList() {

        String userToken = PreferenceProvider.getInstance(getContext()).getUserToken();
        List<DbModule> allActiveModules = controller.getAllActiveModules(userToken);
        List<DbModule> displayedModules = view.getDisplayedModules();
        List<DbModule> newDisplayedList = new ArrayList<>(displayedModules.size());

        for (DbModule displayedModule : displayedModules) {
            for (DbModule activeModule : allActiveModules) {
                if (activeModule.getPackageName().equals(displayedModule.getPackageName())) {
                    displayedModule.setActive(true);
                }
            }

            if (!newDisplayedList.contains(displayedModule)) {
                newDisplayedList.add(displayedModule);
            }
        }

        view.swapModuleData(newDisplayedList);
    }

    @Override
    public void onModuleActivateSuccess() {

        Log.d(TAG, "Module is successfully activated!");

        DbModule module = view.getModuleFromList(selectedModuleId);

        if (module == null) {
            return;
        }

        String userEmail = PreferenceUtils.getUserEmail(getContext());
        DbUser user = controller.getUserByEmail(userEmail);

        List<DbModuleCapability> enabledOptionalPerms = view.getAllEnabledOptionalPermissions();

        module.setActive(true);
        module.setUserId(user.getId());

        long moduleId = controller.insertModuleToDb(module);

        if (moduleId < 0) {
            view.showModuleInstallationFailed();
            return;
        }

        // insert module capabilities
        ModuleResponseDto modDto = availableModuleResponseMapping.get(module.getPackageName());
        List<DbModuleCapability> reqCap = ConverterUtils.convertModuleCapability(modDto.getSensorsRequired());
        List<DbModuleCapability> optCap = ConverterUtils.convertModuleCapability(modDto.getSensorsOptional());

        for (DbModuleCapability cap : reqCap) {
            cap.setRequired(true);
        }

        List<DbModuleCapability> moduleCaps = new ArrayList<>(reqCap.size() + optCap.size());
        moduleCaps.addAll(reqCap);
        moduleCaps.addAll(optCap);

        if (moduleCaps != null) {
            if (enabledOptionalPerms != null) {
                for (DbModuleCapability cap : moduleCaps) {
                    if (!cap.getRequired()) {
                        for (DbModuleCapability enabledCap : enabledOptionalPerms) {
                            if (cap.getType().equals(enabledCap.getType())) {
                                cap.setActive(true);
                            }
                        }
                    } else {
                        // set all required caps to active
                        cap.setActive(true);
                    }

                    cap.setModuleId(moduleId);
                }
            }

            controller.insertModuleCapabilitiesToDb(moduleCaps);
        }

        // check if service not running -> start it
//        if (!HarvesterServiceProvider.getInstance(getContext()).isServiceRunning()) {
//            HarvesterServiceProvider.getInstance(getContext()).startSensingService();
//        }

        // update timing for sensors/events
//        SensorProvider.getInstance(getContext()).synchronizeRunningSensorsWithDb();

        Set<String> permsToAsk = controller.getGrantedPermissions();

        if (!permsToAsk.isEmpty()) {
            view.askPermissions(permsToAsk, null);
        } else {
            EventBus.getDefault().post(new ModuleInstallSuccessfulEvent(
                    module.getPackageName()));
            EventBus.getDefault().post(new ModulesListRefreshEvent());
        }

        Log.d(TAG, "Installation has finished!");
    }

    @Override
    public void onModuleActivateFailed(HttpException error) {

        doDefaultErrorProcessing(error);
        Log.d(TAG, "Installation has failed!");
    }

    @Override
    public void onModuleDeactivateSuccess() {

        String userToken = PreferenceUtils.getUserToken(getContext());

        if (userToken.isEmpty()) {
            view.startLoginActivity();
            return;
        }

        if (controller.uninstallModuleFromDb(userToken, selectedModuleId)) {

            SensorProvider.getInstance(getContext()).synchronizeRunningSensorsWithDb();

            int numberOfModules = controller.getAllUserModules(userToken).size();

            // we have no entries in db, stop the sensing
            if (numberOfModules == 0) {
                stopHarvester();
            }

            view.changeModuleLayout(selectedModuleId, false);

            EventBus.getDefault().post(new ModuleUninstallSuccessfulEvent(selectedModuleId));
            EventBus.getDefault().post(new ModulesListRefreshEvent());

//            view.showUndoAction(module);
        }
    }

    @Override
    public void onModuleDeactivateFailed(HttpException error) {

        doDefaultErrorProcessing(error);

        // no such installed module -> remove it immediately
        if (error.response() == null || error.code() == 400) {

            String userToken = PreferenceUtils.getUserToken(getContext());

            if (userToken.isEmpty()) {
                view.startLoginActivity();
                return;
            }

            if (controller.uninstallModuleFromDb(userToken, selectedModuleId)) {

                int numberOfModules = controller.getAllUserModules(userToken).size();

                // we have no entries in db, stop the sensing
                if (numberOfModules == 0) {
                    stopHarvester();
                }

                SensorProvider.getInstance(getContext()).synchronizeRunningSensorsWithDb();

                view.changeModuleLayout(selectedModuleId, false);

//                view.showUndoAction(module);
            }
        }
    }
}