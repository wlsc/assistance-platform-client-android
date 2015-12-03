package de.tudarmstadt.informatik.tk.android.assistance.view;

import java.util.List;

import de.tudarmstadt.informatik.tk.android.assistance.presenter.modules.ModulesPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface ModulesView extends CommonView {

    void setPresenter(ModulesPresenter presenter);

    void setErrorView();

    void setNoModulesView();

    void stopSwipeRefresh();

    void finishActivity();

    void setModuleList(List<DbModule> installedModules);
}