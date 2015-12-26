package de.tudarmstadt.informatik.tk.android.assistance.presenter.module.settings;

import de.tudarmstadt.informatik.tk.android.assistance.controller.module.settings.ModuleSettingsController;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.CommonPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.view.ModuleSettingsView;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 25.12.2015
 */
public interface ModuleSettingsPresenter extends CommonPresenter {

    void setView(ModuleSettingsView view);

    void setController(ModuleSettingsController controller);
}
