package de.tudarmstadt.informatik.tk.android.assistance.event.module;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 11.09.2015
 */
public class ModuleUninstallEvent {

    private String modulePackageName;

    public ModuleUninstallEvent(String modulePackageName) {
        this.modulePackageName = modulePackageName;
    }

    public String getModulePackageName() {
        return this.modulePackageName;
    }
}