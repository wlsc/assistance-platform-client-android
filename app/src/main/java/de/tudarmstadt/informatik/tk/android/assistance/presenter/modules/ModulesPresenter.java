package de.tudarmstadt.informatik.tk.android.assistance.presenter.modules;

import de.tudarmstadt.informatik.tk.android.assistance.controller.modules.ModulesController;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.CommonPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.view.ModulesView;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface ModulesPresenter extends CommonPresenter {

    void setView(ModulesView view);

    void setController(ModulesController controller);

    void requestAvailableModules();
}
