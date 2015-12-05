package de.tudarmstadt.informatik.tk.android.assistance.controller.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnActiveModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnAvailableModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleActivatedResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleDeactivatedResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.ModuleCapabilityResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.ToggleModuleRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.ModuleEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.modules.ModulesPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.ConverterUtils;
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
                new Callback<List<String>>() {

                    @Override
                    public void success(List<String> activeModules,
                                        Response response) {

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
                    if (PermissionUtils
                            .getInstance(presenter.getContext())
                            .isPermissionGranted(perm)) {

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
    public void uninstallModuleFromDb(DbModule module) {

        Log.d(TAG, "Removing module from db...");

        List<DbModule> modulesToDelete = new ArrayList<>(1);
        modulesToDelete.add(module);

        daoProvider.getModuleDao().delete(modulesToDelete);

        Log.d(TAG, "Finished removing module from db!");
    }

    @Override
    public void requestModuleActivation(ToggleModuleRequestDto toggleModuleRequest,
                                        String userToken,
                                        final DbModule module,
                                        final OnModuleActivatedResponseHandler handler) {

        ModuleEndpoint moduleEndpoint = EndpointGenerator.getInstance(
                presenter.getContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.activateModule(userToken, toggleModuleRequest,
                new Callback<Void>() {

                    @Override
                    public void success(Void aVoid, Response response) {
                        handler.onModuleActivateSuccess(module, response);
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
                                          final DbModule module,
                                          final OnModuleDeactivatedResponseHandler handler) {

        ModuleEndpoint moduleEndpoint = EndpointGenerator.getInstance(
                presenter.getContext())
                .create(ModuleEndpoint.class);

        moduleEndpoint.deactivateModule(userToken, toggleModuleRequest,
                new Callback<Void>() {

                    @Override
                    public void success(Void aVoid, Response response) {
                        handler.onModuleDeactivateSuccess(module, response);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        handler.onModuleDeactivateFailed(module, error);
                    }
                });
    }

    @Override
    public void insertModuleWithCapabilities(ModuleResponseDto moduleResponse) {

        DbUser user = getUserByEmail(PreferenceUtils.getUserEmail(presenter.getContext()));

        if (user == null) {
            return;
        }

        DbModule module = ConverterUtils.convertModule(moduleResponse);

        module.setActive(true);
        module.setUserId(user.getId());

        long installId = insertModuleToDb(module);

        if (installId == -1) {
            return;
        }

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

        insertModuleCapabilitiesToDb(dbRequiredCaps);
        insertModuleCapabilitiesToDb(dbOptionalCaps);
    }

    @Override
    public List<DbModuleCapability> getAllActiveModuleCapabilities(Long moduleId) {
        return daoProvider.getModuleCapabilityDao().getAllActive(moduleId);
    }
}