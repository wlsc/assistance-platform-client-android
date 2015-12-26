package de.tudarmstadt.informatik.tk.android.assistance.event.module.settings;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 27.12.2015
 */
public class ModuleDetailedSettingsEvent {

    private DbModule module;

    public ModuleDetailedSettingsEvent(DbModule module) {
        this.module = module;
    }

    public DbModule getModule() {
        return this.module;
    }
}