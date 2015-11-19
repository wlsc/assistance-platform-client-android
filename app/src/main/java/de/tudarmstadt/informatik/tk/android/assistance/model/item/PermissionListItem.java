package de.tudarmstadt.informatik.tk.android.assistance.model.item;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.11.2015
 */
public class PermissionListItem {

    private DbModuleCapability capability;

    private boolean checked;

    public PermissionListItem() {
        this.capability = null;
        this.checked = true;
    }

    public PermissionListItem(DbModuleCapability capability) {
        this.capability = capability;
        this.checked = true;
    }

    public PermissionListItem(DbModuleCapability capability, boolean checked) {
        this.capability = capability;
        this.checked = checked;
    }

    public DbModuleCapability getCapability() {
        return this.capability;
    }

    public void setCapability(DbModuleCapability capability) {
        this.capability = capability;
    }

    public boolean isChecked() {
        return this.checked;
    }

    public void setChecked(boolean enabled) {
        this.checked = enabled;
    }
}
