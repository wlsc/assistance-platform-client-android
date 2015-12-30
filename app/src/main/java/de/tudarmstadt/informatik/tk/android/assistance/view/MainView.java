package de.tudarmstadt.informatik.tk.android.assistance.view;

import java.util.List;

import de.tudarmstadt.informatik.tk.android.assistance.presenter.main.MainPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbNews;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface MainView extends CommonView {

    void setPresenter(MainPresenter presenter);

    void showAccessibilityServiceTutorial();

    void showNoNews();

    void setNewsItems(List<DbNews> assistanceNews);

    void prepareGCMRegistration();

    void startGcmRegistrationService();

    void showGooglePlayServicesImportantView();

    void showModulesList();
}
