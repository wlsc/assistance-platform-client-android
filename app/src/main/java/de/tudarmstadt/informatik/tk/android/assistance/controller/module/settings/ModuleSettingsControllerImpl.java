package de.tudarmstadt.informatik.tk.android.assistance.controller.module.settings;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.module.settings.ModuleSettingsPresenter;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 25.12.2015
 */
public class ModuleSettingsControllerImpl extends
        CommonControllerImpl implements
        ModuleSettingsController {

    private final ModuleSettingsPresenter presenter;

    public ModuleSettingsControllerImpl(ModuleSettingsPresenter presenter) {
        super(presenter.getContext());
        this.presenter = presenter;
    }
}
