package de.tudarmstadt.informatik.tk.android.assistance.event;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.12.2015
 */
public class CheckModuleCapabilityPermissionEvent {

    private DbModuleCapability capability;

    public CheckModuleCapabilityPermissionEvent(DbModuleCapability capability) {
        this.capability = capability;
    }

    public DbModuleCapability getCapability() {
        return this.capability;
    }

    @Override
    public String toString() {
        return "CheckModuleCapabilityPermissionEvent{" +
                "capability=" + capability +
                '}';
    }
}