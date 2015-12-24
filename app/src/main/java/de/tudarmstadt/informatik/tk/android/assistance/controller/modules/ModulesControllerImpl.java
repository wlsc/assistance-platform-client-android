package de.tudarmstadt.informatik.tk.android.assistance.controller.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;
import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnActiveModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnAvailableModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleActivatedResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleDeactivatedResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.ModuleEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.modules.ModulesPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.event.UpdateSensorIntervalEvent;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.dto.DtoType;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.dto.module.ModuleCapabilityResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.dto.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.dto.module.ToggleModuleRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.SensorProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public class ModulesControllerImpl extends
        CommonControllerImpl implements
        ModulesController {

    private static final String TAG = ModulesControllerImpl.class.getSimpleName();

    private final ModulesPresenter presenter;

    public ModulesControllerImpl(ModulesPresenter presenter) {
        super(presenter.getContext());
        this.presenter = presenter;
    }

    @Override
    public void requestAvailableModules(String userToken, final OnAvailableModulesResponseHandler availableModulesHandler) {

        final ModuleEndpoint moduleEndpoint = EndpointGenerator
                .getInstance(presenter.getContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.getAvailableModules(userToken,
                new Callback<List<ModuleResponseDto>>() {

                    /**
                     * Successful HTTP response.
                     *
                     * @param availableModulesList
                     * @param response
                     */
                    @Override
                    public void success(final List<ModuleResponseDto> availableModulesList,
                                        Response response) {
                        availableModulesHandler.onAvailableModulesSuccess(availableModulesList,
                                response);
                    }

                    /**
                     * Unsuccessful HTTP response due to network failure, non-2XX status code, or unexpected
                     * exception.
                     *
                     * @param error
                     */
                    @Override
                    public void failure(RetrofitError error) {
                        availableModulesHandler.onAvailableModulesError(error);
                    }
                });
    }

    @Override
    public void requestActiveModules(final String userToken,
                                     final OnActiveModulesResponseHandler handler) {

        final ModuleEndpoint moduleEndpoint = EndpointGenerator
                .getInstance(presenter.getContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.getActiveModules(userToken,
                new Callback<Set<String>>() {

                    @Override
                    public void success(Set<String> activeModules, Response response) {
                        handler.onActiveModulesReceived(activeModules, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        handler.onActiveModulesFailed(error);
                    }
                });
    }

    @Override
    public Set<String> getGrantedPermissions() {

        Map<String, String[]> mappings = PermissionUtils
                .getInstance(presenter.getContext())
                .getDangerousPermissionsToDtoMapping();

        Set<String> permissionsToAsk = new HashSet<>();

        long userId = PreferenceUtils.getCurrentUserId(presenter.getContext());

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
                    if (!PermissionUtils.getInstance(presenter.getContext())
                            .isGranted(perm)) {

                        permissionsToAsk.add(perm);
                    }
                }
            }
        }

        return permissionsToAsk;
    }

    @Override
    public long insertModuleToDb(DbModule module) {
        return daoProvider.getModuleDao().insert(module);
    }

    @Override
    public void insertModuleCapabilitiesToDb(List<DbModuleCapability> dbRequiredCaps) {
        daoProvider.getModuleCapabilityDao().insert(dbRequiredCaps);
    }

    @Override
    public DbModule getModuleByPackageIdUserId(String packageName, Long userId) {
        return daoProvider.getModuleDao().getByPackageIdUserId(packageName, userId);
    }

    @Override
    public boolean uninstallModuleFromDb(String userToken, String modulePackageName) {

        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            Log.d(TAG, "User is null");
            return false;
        }

        DbModule actualDbModule = daoProvider.getModuleDao().getByPackageIdUserId(
                modulePackageName,
                user.getId());

        if (actualDbModule == null) {
            Log.d(TAG, "No such module instralled!");
            return false;
        }

        Log.d(TAG, "Removing module " + actualDbModule.getTitle() + " from db...");

        List<DbModuleCapability> moduleCaps = actualDbModule.getDbModuleCapabilityList();

        // remove module capabilities
        if (moduleCaps != null) {
            daoProvider.getModuleCapabilityDao().delete(moduleCaps);
        }

        // remove module
        daoProvider.getModuleDao().delete(actualDbModule);

        Log.d(TAG, "Successfully removed module from db.");

        return true;
    }

    @Override
    public void requestModuleActivation(ToggleModuleRequestDto toggleModuleRequest,
                                        String userToken,
                                        final OnModuleActivatedResponseHandler handler) {

        ModuleEndpoint moduleEndpoint = EndpointGenerator.getInstance(
                presenter.getContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.activateModule(userToken, toggleModuleRequest,
                new Callback<Void>() {

                    @Override
                    public void success(Void aVoid, Response response) {
                        handler.onModuleActivateSuccess(response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        handler.onModuleActivateFailed(error);
                    }
                });
    }

    @Override
    public void requestModuleDeactivation(ToggleModuleRequestDto toggleModuleRequest,
                                          String userToken,
                                          final OnModuleDeactivatedResponseHandler handler) {

        ModuleEndpoint moduleEndpoint = EndpointGenerator.getInstance(
                presenter.getContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.deactivateModule(userToken, toggleModuleRequest,
                new Callback<Void>() {

                    @Override
                    public void success(Void aVoid, Response response) {
                        handler.onModuleDeactivateSuccess(response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        handler.onModuleDeactivateFailed(error);
                    }
                });
    }

    @Override
    public void updateSensorTimingsFromDb() {

        String userToken = PreferenceUtils.getUserToken(presenter.getContext());

        DbUser user = daoProvider.getUserDao().getByToken(userToken);

        if (user == null) {
            Log.d(TAG, "updateSensorTimingsFromDb: User is NULL");
            return;
        }

        List<DbModule> activeModules = daoProvider.getModuleDao().getAllActive(user.getId());

        if (activeModules == null || activeModules.isEmpty()) {
            Log.d(TAG, "updateSensorTimingsFromDb: active modules is NULL or EMPTY");
            return;
        }

        Map<String, DbModuleCapability> activeCapabilities = new HashMap<>();

        for (DbModule module : activeModules) {

            List<DbModuleCapability> moduleActiveCaps = daoProvider
                    .getModuleCapabilityDao()
                    .getAllActive(module.getId());

            if (moduleActiveCaps == null) {
                continue;
            }

            for (DbModuleCapability cap : moduleActiveCaps) {

                // insert when only new capability type is present
                if (activeCapabilities.get(cap.getType()) == null) {

                    // firing up an event to change sensors collection frequencies
                    EventBus.getDefault().post(
                            new UpdateSensorIntervalEvent(
                                    DtoType.getDtoType(cap.getType()),
                                    cap.getCollectionFrequency()));
                }
            }
        }
    }

    @Override
    public List<ModuleResponseDto> filterAvailableModulesList(List<ModuleResponseDto> apiResponse) {

        if (apiResponse == null) {
            return Collections.emptyList();
        }

        SensorProvider sensorProvider = SensorProvider.getInstance(presenter.getContext());

        List<ModuleResponseDto> result = new ArrayList<>();

        for (ModuleResponseDto moduleDto : apiResponse) {

            List<ModuleCapabilityResponseDto> moduleReqCaps = moduleDto.getSensorsRequired();

            if (moduleReqCaps == null) {
                continue;
            }

            boolean canUseThatModule = true;

            for (ModuleCapabilityResponseDto capDto : moduleReqCaps) {

                if (capDto == null || capDto.getType() == null) {
                    continue;
                }

                // if user CAN'T run that sensor
                if (!sensorProvider.hasUserAbilityToRunSensor(capDto.getType())) {
                    canUseThatModule = false;
                }
            }

            // user can use this module -> add to list
            if (canUseThatModule) {
                result.add(moduleDto);
            }
        }

        return result;
    }

    @Override
    public boolean insertModuleResponseWithCapabilities(ModuleResponseDto moduleResponse) {

        DbUser user = getUserByEmail(PreferenceUtils.getUserEmail(presenter.getContext()));

        if (user == null) {
            Log.d(TAG, "User is null");
            return false;
        }

        DbModule module = ConverterUtils.convertModule(moduleResponse);

        if (module == null) {
            Log.d(TAG, "Module is null");
            return false;
        }

        module.setActive(true);
        module.setUserId(user.getId());

        long installId = insertModuleToDb(module);

        if (installId == -1) {
            return false;
        }

        List<ModuleCapabilityResponseDto> requiredCaps = moduleResponse.getSensorsRequired();
//        List<ModuleCapabilityResponseDto> optionalCaps = moduleResponse.getSensorsOptional();

        List<DbModuleCapability> dbRequiredCaps = new ArrayList<>(requiredCaps.size());
//        List<DbModuleCapability> dbOptionalCaps = new ArrayList<>(optionalCaps.size());

        for (ModuleCapabilityResponseDto response : requiredCaps) {

            final DbModuleCapability cap = ConverterUtils.convertModuleCapability(response);

            if (cap == null) {
                continue;
            }

            cap.setModuleId(installId);
            cap.setRequired(true);
            cap.setActive(true);

            dbRequiredCaps.add(cap);
        }

//        for (ModuleCapabilityResponseDto response : optionalCaps) {
//
//            final DbModuleCapability cap = ConverterUtils.convertModuleCapability(response);
//
//            cap.setModuleId(installId);
//            cap.setActive(true);
//
//            dbOptionalCaps.add(cap);
//        }

        insertModuleCapabilitiesToDb(dbRequiredCaps);
//        insertModuleCapabilitiesToDb(dbOptionalCaps);

        return true;
    }
}