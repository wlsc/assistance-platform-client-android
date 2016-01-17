package de.tudarmstadt.informatik.tk.assistance.event.module.settings;

import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbModuleCapability;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 27.12.2015
 */
public class ModuleCapabilityHasChangedEvent {

    private DbModuleCapability moduleCapability;

    public ModuleCapabilityHasChangedEvent(DbModuleCapability moduleCapability) {
        this.moduleCapability = moduleCapability;
    }

    public DbModuleCapability getModuleCapability() {
        return this.moduleCapability;
    }
}
