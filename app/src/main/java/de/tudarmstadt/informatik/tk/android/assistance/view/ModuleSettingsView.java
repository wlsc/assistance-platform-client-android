package de.tudarmstadt.informatik.tk.android.assistance.view;

import java.util.List;

import de.tudarmstadt.informatik.tk.android.assistance.presenter.module.settings.ModuleSettingsPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 25.12.2015
 */
public interface ModuleSettingsView extends CommonView {

    void setPresenter(ModuleSettingsPresenter presenter);

    void setAdapter(List<DbModule> allModules);
}