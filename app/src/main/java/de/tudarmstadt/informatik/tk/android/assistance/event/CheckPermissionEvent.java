package de.tudarmstadt.informatik.tk.android.assistance.event;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 22.11.2015
 */
public class CheckPermissionEvent {

    private String[] permissions;

    public CheckPermissionEvent(String[] permissions) {
        this.permissions = permissions;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }
}
