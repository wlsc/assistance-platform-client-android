package de.tudarmstadt.informatik.tk.assistance.view;

import java.util.List;
import java.util.Set;

import de.tudarmstadt.informatik.tk.assistance.presenter.module.ModulesPresenter;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleCapability;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ModuleResponseDto;
import rx.Observable;

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

    List<DbModule> getDisplayedModules();

    int getDisplayedModulesCount();

    void swapModuleData(List<DbModule> newModules);

    DbModule getModuleFromList(String packageName);

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
    void showPermissionsAreCrucialDialog(Set<String> declinedPermissions);

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

    void showAccessibilityServiceTutorial();

    List<DbModuleCapability> getAllEnabledOptionalPermissions();

    void showModuleInstallationFailed();

    void showModuleInstallationSuccessful();

    void showUndoAction();

    void showModuleUninstallSuccessful();

    void subscribeActivatedModules(Observable<ActivatedModulesResponse> observable);

    void subscribeModuleDeactivation(Observable<Void> voidObservable);

    void subscribeModuleActivation(Observable<Void> voidObservable);
}