package de.tudarmstadt.informatik.tk.assistance.presenter.main;

import android.app.Activity;

import java.util.List;

import de.tudarmstadt.informatik.tk.assistance.controller.main.MainController;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ClientFeedbackDto;
import de.tudarmstadt.informatik.tk.assistance.presenter.CommonPresenter;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.assistance.view.MainView;
import retrofit2.adapter.rxjava.HttpException;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public interface MainPresenter extends CommonPresenter {

    void setView(MainView view);

    void setController(MainController controller);

    void registerGCMPush(Activity activity);

    void handleResultCode(int requestCode);

    void handleRequestCode(int requestCode);

    void presentModuleCardNews(List<ClientFeedbackDto> clientFeedbackDto);

    void onActivatedModulesReceived(ActivatedModulesResponse activatedModulesResponse);

    void onActivatedModulesFailed(HttpException error);

    void presentRequestPermissionResult(int requestCode, String[] permissions, int... grantResults);

    void requestNewNews();
}
