package de.tudarmstadt.informatik.tk.android.assistance.model.item;

import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ModuleCapabilityResponse;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 09.11.2015
 */
public class PermissionListItem {

    private ModuleCapabilityResponse capability;

    private boolean checked;

    public PermissionListItem() {
        this.capability = null;
        this.checked = true;
    }

    public PermissionListItem(ModuleCapabilityResponse capability) {
        this.capability = capability;
        this.checked = true;
    }

    public PermissionListItem(ModuleCapabilityResponse capability, boolean checked) {
        this.capability = capability;
        this.checked = checked;
    }

    public ModuleCapabilityResponse getCapability() {
        return this.capability;
    }

    public void setCapability(ModuleCapabilityResponse capability) {
        this.capability = capability;
    }

    public boolean isChecked() {
        return this.checked;
    }

    public void setChecked(boolean enabled) {
        this.checked = enabled;
    }
}
