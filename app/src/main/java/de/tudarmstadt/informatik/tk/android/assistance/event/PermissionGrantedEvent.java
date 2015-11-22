package de.tudarmstadt.informatik.tk.android.assistance.event;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 07.10.2015
 */
public class PermissionGrantedEvent {

    private String[] permissions;

    public PermissionGrantedEvent(String[] permissions) {
        this.permissions = permissions;
    }

    public String[] getPermissions() {
        return this.permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }
}
