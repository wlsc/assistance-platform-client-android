package de.tudarmstadt.informatik.tk.android.assistance.presenter.module;

import java.util.List;
import java.util.Set;

import de.tudarmstadt.informatik.tk.android.assistance.controller.module.ModulesController;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.CommonPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.view.ModulesView;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface ModulesPresenter extends CommonPresenter {

    void setView(ModulesView view);

    void setController(ModulesController controller);

    void requestAvailableModules();

    /**
     * Populates and saves available modules
     *
     * @param availableModulesResponse
     */
    void processAvailableModules(List<ModuleResponseDto> availableModulesResponse);

    void applyAlreadyActiveModulesFromDb(List<DbModule> modules);

    /**
     * Insert already activated modules earlier
     *
     * @param convertedModules
     */
    void insertActiveModulesIntoDb(List<DbModule> convertedModules);

    /**
     * Request permissions for installed active modules
     */
    void requestActiveModulesPermissions();

    /**
     * Saves information into db / install a module for user
     */
    void handleModuleActivationRequest(ModuleResponseDto moduleResponse);

    void handleModuleCapabilityStateChanged(DbModuleCapability moduleCapability);

    /**
     * Uninstalls currently selected module
     *
     * @param module
     */
    void presentModuleUninstall(DbModule module);

    void presentPermissionDialog();

    void presentUninstallDialog();

    void setSelectedModuleId(String packageName);

    void presentRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults);

    void presentMoreModuleInformationDialog();

    void presentSuccessfulInstallation();

    /**
     * Ask user for grant permissions
     */
    void handleModulePermissions();

    void presentModuleInstallationHasError(Set<String> declinedPermissions);

    void presentSuccessfulUninstall();

    ModuleResponseDto getSelectedModuleResponse();
}
