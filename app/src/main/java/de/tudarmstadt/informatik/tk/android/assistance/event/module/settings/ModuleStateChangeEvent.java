package de.tudarmstadt.informatik.tk.android.assistance.event.module.settings;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 26.12.2015
 */
public class ModuleStateChangeEvent {

    private long moduleId;

    private boolean active;

    public ModuleStateChangeEvent(long moduleId, boolean active) {
        this.moduleId = moduleId;
        this.active = active;
    }

    public long getModuleId() {
        return this.moduleId;
    }

    public boolean isActive() {
        return this.active;
    }
}