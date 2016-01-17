package de.tudarmstadt.informatik.tk.assistance.event.module;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 16.12.2015
 */
public class ModuleUninstallSuccessfulEvent {

    private String modulePackageName;

    public ModuleUninstallSuccessfulEvent(String modulePackageName) {
        this.modulePackageName = modulePackageName;
    }

    public String getModulePackageName() {
        return modulePackageName;
    }

}
