package de.tudarmstadt.informatik.tk.assistance.view;

import java.util.List;
import java.util.Set;

import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ClientFeedbackDto;
import de.tudarmstadt.informatik.tk.assistance.presenter.main.MainPresenter;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ActivatedModulesResponse;
import rx.Observable;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface MainView extends CommonView {

    void setPresenter(MainPresenter presenter);

    void showAccessibilityServiceTutorial();

    void showNoNews();

    void setNewsItems(List<ClientFeedbackDto> assistanceNews);

    void prepareGCMRegistration();

    void startGcmRegistrationService();

    void showGooglePlayServicesImportantView();

    void showModulesList();

    void subscribeActiveAvailableModules(Observable<ActivatedModulesResponse> observable);

    void showPermissionsAreCrucialDialog(Set<String> declinedPermissions);

    void showAvailableModulesTutorial();

    void subscribeModuleFeedback(Observable<List<ClientFeedbackDto>> listObservable);
}
