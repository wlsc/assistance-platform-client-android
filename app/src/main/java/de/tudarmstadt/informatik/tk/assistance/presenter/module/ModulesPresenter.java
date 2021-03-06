package de.tudarmstadt.informatik.tk.assistance.presenter.module;

import java.util.List;
import java.util.Set;

import de.tudarmstadt.informatik.tk.assistance.controller.module.ModulesController;
import de.tudarmstadt.informatik.tk.assistance.presenter.CommonPresenter;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.assistance.view.ModulesView;
import retrofit2.adapter.rxjava.HttpException;

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
     * @param activeModules
     */
    void processAvailableModules(List<ModuleResponseDto> availableModulesResponse, Set<String> activeModules);

    /**
     * Insert already activated modules earlier
     *
     * @param modules
     * @param modulesCaps
     */
    void insertActiveModulesIntoDb(List<DbModule> modules, List<List<DbModuleCapability>> modulesCaps);

    /**
     * Request permissions for installed active modules
     */
    void requestModulesPermissions();

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

    void presentMoreModuleInformationDialog();

    void presentSuccessfulInstallation();

    /**
     * Ask user for grant permissions
     */
    void handleModulePermissions();

    void presentModuleInstallationHasError(Set<String> declinedPermissions);

    void presentSuccessfulUninstall();

    ModuleResponseDto getSelectedModuleResponse();

    void onActivatedModulesReceived(ActivatedModulesResponse activatedModulesResponse);

    void onActivatedModulesFailed(HttpException error);

    void refreshModuleList();

    void onModuleActivateSuccess();

    void onModuleActivateFailed(HttpException error);

    void onModuleDeactivateSuccess();

    void onModuleDeactivateFailed(HttpException error);
}
