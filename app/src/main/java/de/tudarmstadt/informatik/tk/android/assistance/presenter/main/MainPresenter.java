package de.tudarmstadt.informatik.tk.android.assistance.presenter.main;

import android.app.Activity;

import de.tudarmstadt.informatik.tk.android.assistance.controller.main.MainController;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.CommonPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.view.MainView;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface MainPresenter extends CommonPresenter {

    void setView(MainView view);

    void setController(MainController controller);

    void checkPreconditions();

    void registerGCMPush(Activity activity);

    void handleResultCode(int requestCode);

    void handleRequestCode(int requestCode);
}
