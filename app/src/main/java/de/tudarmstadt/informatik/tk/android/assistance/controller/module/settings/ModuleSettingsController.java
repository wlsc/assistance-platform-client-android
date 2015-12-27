package de.tudarmstadt.informatik.tk.android.assistance.controller.module.settings;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonController;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModuleCapability;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 25.12.2015
 */
public interface ModuleSettingsController extends CommonController {

    void updateModuleCapability(DbModuleCapability moduleCapability);
}
