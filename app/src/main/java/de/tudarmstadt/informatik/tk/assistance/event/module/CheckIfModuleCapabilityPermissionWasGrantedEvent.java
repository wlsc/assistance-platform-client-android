package de.tudarmstadt.informatik.tk.assistance.event.module;

import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleCapability;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.12.2015
 */
public class CheckIfModuleCapabilityPermissionWasGrantedEvent {

    private DbModuleCapability capability;

    private int position;

    public CheckIfModuleCapabilityPermissionWasGrantedEvent(DbModuleCapability capability, int position) {
        this.capability = capability;
        this.position = position;
    }

    public DbModuleCapability getCapability() {
        return this.capability;
    }

    public int getPosition() {
        return this.position;
    }

    @Override
    public String toString() {
        return "CheckIfModuleCapabilityPermissionWasGrantedEvent{" +
                "capability=" + capability +
                ", position=" + position +
                '}';
    }
}