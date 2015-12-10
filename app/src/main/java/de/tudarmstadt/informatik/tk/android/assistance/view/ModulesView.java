package de.tudarmstadt.informatik.tk.android.assistance.view;

import java.util.List;

import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.modules.ModulesPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface ModulesView extends CommonView {

    void setPresenter(ModulesPresenter presenter);

    void setErrorView();

    void setNoModulesView();

    void setSwipeRefreshing(boolean isEnabled);

    void setModuleList(List<DbModule> installedModules);

    int getModulesAmount();

    void swapModuleData(List<DbModule> newModules);

    /**
     * Shows a permission dialog to user
     * Each permission is used actually by a module
     *
     * @param selectedModule
     */
    void showPermissionDialog(ModuleResponseDto selectedModule);

    void toggleShowRequiredPermissions(boolean isVisible);

    void toggleShowOptionalPermissions(boolean isVisible);

    /**
     * Shows uninstall dialog to user
     *
     * @param selectedModule
     */
    void showUninstallDialog(ModuleResponseDto selectedModule);

    /**
     * Shows some dialog, because that permissions are crucial for that module
     *
     * @param declinedPermissions
     */
    void showPermissionsAreCrucialDialog(List<String> declinedPermissions);

    /**
     * Shows more information about an assistance module
     */
    void showMoreModuleInformationDialog(ModuleResponseDto selectedModule);

    /**
     * Changes layout of installed module to installed state
     *
     * @param moduleId
     */
    void changeModuleLayout(String moduleId, boolean isModuleInstalled);

    List<DbModuleCapability> getAllEnabledOptionalPermissions();

    void showModuleInstallationFailed();

    void showModuleInstallationSuccessful();

    void showUndoAction(DbModule module);
}