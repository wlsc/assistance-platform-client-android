package de.tudarmstadt.informatik.tk.android.assistance.event;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 11.09.2015
 */
public class ModuleUninstallEvent {

    private String moduleId;

    public ModuleUninstallEvent(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleId() {
        return this.moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
