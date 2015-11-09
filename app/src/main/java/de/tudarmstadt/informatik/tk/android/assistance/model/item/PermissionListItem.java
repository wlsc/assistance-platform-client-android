package de.tudarmstadt.informatik.tk.android.assistance.model.item;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.11.2015
 */
public class PermissionListItem {

    String title;

    boolean enabled;

    public PermissionListItem() {
        this.title = "";
        this.enabled = false;
    }

    public PermissionListItem(String title, boolean enabled) {
        this.title = title;
        this.enabled = enabled;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
