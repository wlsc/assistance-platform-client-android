package de.tudarmstadt.informatik.tk.android.assistance.event.module;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 22.11.2015
 */
public class CheckModuleAllowedPermissionEvent {

    private int dtoType;

    public CheckModuleAllowedPermissionEvent(int dtoType) {
        this.dtoType = dtoType;
    }

    public int getDtoType() {
        return this.dtoType;
    }

    @Override
    public String toString() {
        return "CheckModuleAllowedPermissionEvent{" +
                "dtoType=" + dtoType +
                '}';
    }
}