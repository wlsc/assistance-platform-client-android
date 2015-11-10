package de.tudarmstadt.informatik.tk.android.assistance.model.item;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.11.2015
 */
public class PermissionListItem {

    String title;

    boolean checked;

    public PermissionListItem() {
        this.title = "";
        this.checked = true;
    }

    public PermissionListItem(String title) {
        this.title = title;
        this.checked = true;
    }

    public PermissionListItem(String title, boolean checked) {
        this.title = title;
        this.checked = checked;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChecked() {
        return this.checked;
    }

    public void setChecked(boolean enabled) {
        this.checked = enabled;
    }
}
