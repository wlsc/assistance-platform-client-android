package de.tu_darmstadt.tk.android.assistance.events;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 19.07.2015
 */
public class ModuleShowMoreInfoEvent {

    private String moduleId;

    public ModuleShowMoreInfoEvent(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

}
