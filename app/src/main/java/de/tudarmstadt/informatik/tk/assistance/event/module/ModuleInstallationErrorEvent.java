package de.tudarmstadt.informatik.tk.assistance.event.module;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 20.12.2015
 */
public class ModuleInstallationErrorEvent {

    private String modulePackageName;

    public ModuleInstallationErrorEvent(String modulePackageName) {
        this.modulePackageName = modulePackageName;
    }

    public String getModulePackageName() {
        return modulePackageName;
    }

}
