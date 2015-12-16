package de.tudarmstadt.informatik.tk.android.assistance.event.module;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 16.11.2015
 */
public class ModuleInstallSuccessfulEvent {

    private String modulePackageName;

    public ModuleInstallSuccessfulEvent(String modulePackageName) {
        this.modulePackageName = modulePackageName;
    }

    public String getModulePackageName() {
        return modulePackageName;
    }
}