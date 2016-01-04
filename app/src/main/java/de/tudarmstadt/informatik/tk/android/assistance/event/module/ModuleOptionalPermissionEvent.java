package de.tudarmstadt.informatik.tk.android.assistance.event.module;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 03.01.2016
 */
public class ModuleOptionalPermissionEvent {

    private String permission;

    public ModuleOptionalPermissionEvent(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return this.permission;
    }
}