package de.tudarmstadt.informatik.tk.android.assistance.presenter.main;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.informatik.tk.android.assistance.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.controller.main.MainController;
import de.tudarmstadt.informatik.tk.android.assistance.controller.main.MainControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnGooglePlayServicesAvailable;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnModuleFeedbackResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.user.profile.ProfileResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.CommonPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbNews;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ActivatedModulesResponse;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ModuleCapabilityResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.AppUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.PermissionUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.ServiceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.MainView;
import de.tudarmstadt.informatik.tk.assistance.model.client.feedback.content.ClientFeedbackDto;
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

        view.initView();

        if (AppUtils.isDebug(getContext())) {
            PreferenceUtils.setDeveloperStatus(getContext(), true);
        }

        String userToken = PreferenceUtils.getUserToken(getContext());

        DbUser user = controller.getUserByToken(userToken);

        if (user == null) {
            view.startLoginActivity();
            return;
        }

        List<DbNews> assistanceNews = controller.getCachedNews(user.getId());

        if (assistanceNews.isEmpty()) {
            view.showNoNews();
        } else {
            view.setNewsItems(controller.convertDbEntries(assistanceNews));
        }

        List<DbModule> installedActiveModules = controller.getAllActiveModules(user.getId());

        if (installedActiveModules == null || installedActiveModules.isEmpty()) {

            stopHarvester();

            controller.initUUID(user);

            view.subscribeActiveAvailableModules(controller.requestActivatedModules(userToken));

        } else {

            view.prepareGCMRegistration();

            Log.d(TAG, "Active modules: " + installedActiveModules.size());

            // start sensing
            startHarvester();
        }
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

                if (ServiceUtils.isHarvesterAbleToRun(getContext())) {

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
            view.setNewsItems(clientFeedbackDto);
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

        view.showGooglePlayServicesImportantView();
    }

    @Override
    public void onActivatedModulesReceived(ActivatedModulesResponse activatedModulesResponse) {

        Log.d(TAG, "Received on activated modules");

        if (activatedModulesResponse == null) {
            Log.d(TAG, "ActivatedModulesResponse was null");
            return;
        }

        Set<String> activeModules = activatedModulesResponse.getActiveModules();

        if (activeModules == null || activeModules.isEmpty()) {
            Log.d(TAG, "No active modules found");
            view.showModulesList();
            return;
        }

        List<ModuleResponseDto> availableModules = activatedModulesResponse.getAvailableModules();

        if (availableModules == null || availableModules.isEmpty()) {
            Log.d(TAG, "availableModules is null or empty");
            return;
        }

        PermissionUtils permissionUtils = PermissionUtils.getInstance(getContext());
        Map<String, String[]> dangerousGroupPerms = permissionUtils.getDangerousPermissionsToDtoMapping();
        Set<String> permissionsToAsk = new HashSet<>();

        for (ModuleResponseDto moduleResponseDto : availableModules) {

            if (activeModules.contains(moduleResponseDto.getPackageName())) {

                List<ModuleCapabilityResponseDto> reqCaps = moduleResponseDto.getSensorsRequired();

                if (reqCaps == null || reqCaps.isEmpty()) {
                    continue;
                }

                for (ModuleCapabilityResponseDto capDto : reqCaps) {

                    if (capDto == null || capDto.getType() == null) {
                        continue;
                    }

                    String[] groupPerm = dangerousGroupPerms.get(capDto.getType());

                    if (groupPerm == null || groupPerm.length == 0) {
                        continue;
                    }

                    for (String perm : groupPerm) {

                        if (!permissionUtils.isGranted(perm)) {
                            permissionsToAsk.add(perm);
                        }
                    }
                }
            }
        }

        view.askPermissions(permissionsToAsk);
    }

    @Override
    public void onActivatedModulesFailed(RetrofitError error) {
        doDefaultErrorProcessing(error);
    }

    @Override
    public void presentRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case Constants.PERM_MODULE_ACTIVATED_REQUEST:

                Log.d(TAG, "Back from permissions request");

                Set<String> declinedPermissions = new HashSet<>();

                for (int i = 0, grantResultsLength = grantResults.length; i < grantResultsLength; i++) {

                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        // permission denied, it should be asked again

                        declinedPermissions.add(permissions[i]);
                    }
                }

                // ask user about permissions again
                if (declinedPermissions.size() > 0) {

                    view.showPermissionsAreCrucialDialog(declinedPermissions);

                } else {

                    HarvesterServiceProvider.getInstance(getContext()).startSensingService();
                }

                break;
            default:
                break;
        }
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