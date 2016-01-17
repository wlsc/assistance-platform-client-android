package de.tudarmstadt.informatik.tk.assistance.event.module;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 22.11.2015
 */
public class ModuleAllowedPermissionStateChangedEvent {

    private int capType;

    private boolean isChecked;

    private int numReqModules;

    public ModuleAllowedPermissionStateChangedEvent(int capType, boolean isChecked, int numReqModules) {
        this.capType = capType;
        this.isChecked = isChecked;
        this.numReqModules = numReqModules;
    }

    public int getCapType() {
        return this.capType;
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public int getNumReqModules() {
        return this.numReqModules;
    }

    @Override
    public String toString() {
        return "ModuleAllowedPermissionStateChangedEvent{" +
                "capType=" + capType +
                ", isChecked=" + isChecked +
                ", numReqModules=" + numReqModules +
                '}';
    }
}