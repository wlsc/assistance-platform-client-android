package de.tudarmstadt.informatik.tk.android.assistance.controller.module;

import java.util.List;
import java.util.Set;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonController;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleActivatedResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleDeactivatedResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ToggleModuleRequestDto;
import rx.Observable;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface ModulesController extends CommonController {

    /**
     * Request available modules from service
     */
    Observable<List<ModuleResponseDto>> requestAvailableModules(String userToken);

    /**
     * Request for active modules
     *
     * @param userToken
     */
    Observable<Set<String>> requestActiveModules(String userToken);

    /**
     * Request for combined active modules and available modules
     *
     * @param userToken
     * @return
     */
    Observable<ActivatedModulesResponse> requestActivatedModules(String userToken);

    /**
     * Check permissions is still granted to modules
     */
    Set<String> getGrantedPermissions();

    long insertModuleToDb(DbModule module);

    void insertModuleCapabilitiesToDb(List<DbModuleCapability> dbRequiredCaps);

    boolean insertModuleResponseWithCapabilities(ModuleResponseDto moduleResponseDto);

    DbModule getModuleByPackageIdUserId(String packageName, Long userId);

    /**
     * Removes module from db
     *
     * @param userToken
     * @param modulePackageName
     * @return
     */
    boolean uninstallModuleFromDb(String userToken, String modulePackageName);

    void requestModuleActivation(ToggleModuleRequestDto toggleModuleRequest,
                                 String userToken,
                                 OnModuleActivatedResponseHandler handler);

    void requestModuleDeactivation(ToggleModuleRequestDto toggleModuleRequest,
                                   String userToken,
                                   OnModuleDeactivatedResponseHandler handler);

    void updateSensorTimingsFromDb();

    List<ModuleResponseDto> filterAvailableModulesList(List<ModuleResponseDto> apiResponse);

    void updateModuleCapability(DbModuleCapability moduleCapability);
}
