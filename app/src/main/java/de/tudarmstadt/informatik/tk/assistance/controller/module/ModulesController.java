package de.tudarmstadt.informatik.tk.assistance.controller.module;

import java.util.List;
import java.util.Set;

import de.tudarmstadt.informatik.tk.assistance.controller.CommonController;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ToggleModuleRequestDto;
import rx.Observable;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface ModulesController extends CommonController {

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

    DbModule getModuleByPackageIdUserId(String packageName, Long userId);

    /**
     * Removes module from db
     *
     * @param userToken
     * @param modulePackageName
     * @return
     */
    boolean uninstallModuleFromDb(String userToken, String modulePackageName);

    Observable<Void> requestModuleActivation(String userToken, ToggleModuleRequestDto toggleModuleRequest);

    Observable<Void> requestModuleDeactivation(String userToken,
                                               ToggleModuleRequestDto toggleModuleRequest);

    List<ModuleResponseDto> filterAvailableModulesList(List<ModuleResponseDto> apiResponse);

    void updateModuleCapability(DbModuleCapability moduleCapability);
}