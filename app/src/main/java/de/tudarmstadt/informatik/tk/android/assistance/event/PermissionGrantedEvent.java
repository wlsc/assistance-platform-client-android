package de.tudarmstadt.informatik.tk.android.assistance.event;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 07.10.2015
 */
public class PermissionGrantedEvent {

    private String permission;

    public PermissionGrantedEvent(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
