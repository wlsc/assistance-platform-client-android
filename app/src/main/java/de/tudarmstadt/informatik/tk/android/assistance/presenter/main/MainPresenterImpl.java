package de.tudarmstadt.informatik.tk.android.assistance.presenter.main;

import android.app.Activity;
import android.content.Context;

import java.util.List;
import java.util.Set;

import de.tudarmstadt.informatik.tk.android.assistance.controller.main.MainController;
import de.tudarmstadt.informatik.tk.android.assistance.controller.main.MainControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnActiveModulesResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnGooglePlayServicesAvailable;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleFeedbackResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.profile.ProfileResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.CommonPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbNews;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.AppUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.MainView;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.ClientFeedbackDto;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 02.12.2015
 */
public class MainPresenterImpl extends
        CommonPresenterImpl implements
        MainPresenter,
        OnGooglePlayServicesAvailable,
        OnActiveModulesResponseHandler,
        OnResponseHandler<ProfileResponseDto>,
        OnModuleFeedbackResponseHandler {

    private static final String TAG = MainPresenterImpl.class.getSimpleName();

    private MainView view;
    private MainController controller;

    public MainPresenterImpl(Context context) {
        super(context);
        setController(new MainControllerImpl(this));
    }

    @Override
    public void setView(MainView view) {
        super.setView(view);
        this.view = view;
    }

    @Override
    public void setController(MainController controller) {
        this.controller = controller;
    }

    @Override
    public void doInitView() {

//        boolean accessibilityServiceActivated = PreferenceProvider
//                .getInstance(getContext())
//                .getActivated();

//        if (accessibilityServiceActivated) {

        view.initView();

        if (AppUtils.isDebug(getContext())) {
            PreferenceUtils.setDeveloperStatus(getContext(), true);
        }

        long userId = PreferenceUtils.getCurrentUserId(getContext());

        List<DbNews> assistanceNews = controller.getCachedNews(userId);

        if (assistanceNews.isEmpty()) {
            view.showNoNews();
        } else {
            view.setNewsItems(assistanceNews);
        }

        view.prepareGCMRegistration();

        List<DbModule> installedModules = controller.getAllActiveModules(userId);

        if (installedModules == null || installedModules.isEmpty()) {

            stopHarvester();

            final String userToken = PreferenceUtils.getUserToken(getContext());

            controller.requestActiveModules(userToken, this);

        } else {

            Log.d(TAG, "Active modules: " + installedModules.size());

            // user got some active modules
            startHarvester();
        }

//        } else {
//
//            Log.d(TAG, "Accessibility Service is NOT active! Showing tutorial...");
//
//            view.showAccessibilityServiceTutorial();
//        }
    }

    @Override
    public void registerGCMPush(Activity activity) {
        controller.registerGCMPush(activity, this);
    }

    @Override
    public void handleResultCode(int resultCode) {

        switch (resultCode) {

            case Constants.INTENT_ACCESSIBILITY_SERVICE_IGNORED_RESULT:
            case Constants.INTENT_ACCESSIBILITY_SERVICE_ENABLED_RESULT:

                Log.d(TAG, "Back from accessibility service tutorial");

                view.initView();

                break;

            case Constants.INTENT_SETTINGS_LOGOUT_RESULT:

                Log.d(TAG, "Back from settings logout action");

                view.startLoginActivity();

                break;

            default:
                Log.d(TAG, "Back from UNKNOWN result: " + resultCode);
        }
    }

    @Override
    public void handleRequestCode(int requestCode) {

        switch (requestCode) {

            case Constants.INTENT_AVAILABLE_MODULES_RESULT:

                Log.d(TAG, "Back from available modules activity");

                if (PreferenceUtils.hasUserModules(getContext())) {

                    Log.d(TAG, "User have modules installed");

                    startHarvester();

                } else {
                    Log.d(TAG, "User have NO modules installed");
                }
                break;

            default:
                Log.d(TAG, "Back from UNKNOWN request: " + requestCode);
        }
    }

    @Override
    public void presentModuleCardNews(List<ClientFeedbackDto> clientFeedbackDto) {

        if (clientFeedbackDto.isEmpty()) {
            view.showNoNews();
        } else {
//            view.

        }
    }

    @Override
    public void onPlayServicesAvailable() {

        view.startGcmRegistrationService();
        PreferenceUtils.setGcmTokenWasSent(getContext(), true);
    }

    @Override
    public void onPlayServicesNotAvailable() {

        PreferenceUtils.setGcmTokenWasSent(getContext(), false);

        // TODO: tell user that it is impossible without play services
        // or just make here impl without play services.
    }

    @Override
    public void onActiveModulesReceived(Set<String> activeModules, Response response) {

        if (activeModules != null && !activeModules.isEmpty()) {

            Log.d(TAG, "Modules activated: " + activeModules.toString());

            PreferenceUtils.setPreference(getContext(),
                    Constants.PREF_USER_ACTIVE_MODULES,
                    activeModules);
        }
    }

    @Override
    public void onActiveModulesFailed(RetrofitError error) {
        doDefaultErrorProcessing(error);
    }

    @Override
    public void onSuccess(ProfileResponseDto apiResponse, Response response) {

        if (apiResponse == null) {
            return;
        }

        PreferenceUtils.setUserFirstname(getContext(), apiResponse.getFirstname());
        PreferenceUtils.setUserLastname(getContext(), apiResponse.getLastname());
        PreferenceUtils.setUserEmail(getContext(), apiResponse.getPrimaryEmail());

        controller.persistLogin(apiResponse);
    }

    @Override
    public void onError(RetrofitError error) {
        doDefaultErrorProcessing(error);
    }

    @Override
    public void onModuleFeedbackSuccess(List<ClientFeedbackDto> clientFeedbackDto, Response response) {

        if (clientFeedbackDto == null) {
            view.showUnknownErrorOccurred();
            return;
        }

        presentModuleCardNews(clientFeedbackDto);
    }

    @Override
    public void onModuleFeedbackFailed(RetrofitError error) {
        doDefaultErrorProcessing(error);
    }
}