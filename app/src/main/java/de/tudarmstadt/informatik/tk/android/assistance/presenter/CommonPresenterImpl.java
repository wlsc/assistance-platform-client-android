package de.tudarmstadt.informatik.tk.android.assistance.presenter;

import android.content.Context;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.view.CommonView;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public abstract class CommonPresenterImpl implements CommonPresenter {

    private final Context context;

    private CommonView view;

    public CommonPresenterImpl(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    @Override
    public void doInitView() {
        // common view injector
    }

    @Override
    public void startHarvester() {

//        if (!DeviceUtils.isServiceRunning(getContext(), HarvesterService.class)) {

        HarvesterServiceProvider.getInstance(
                getContext())
                .startSensingService();
//        }
    }

    @Override
    public void stopHarvester() {

//        if (DeviceUtils.isServiceRunning(getContext(), HarvesterService.class)) {

        HarvesterServiceProvider.getInstance(
                getContext())
                .stopSensingService();
//        }
    }

    @Override
    public void setView(CommonView view) {
        this.view = view;
    }

    @Override
    public void doDefaultErrorProcessing(RetrofitError error) {

        if (error.getKind() == RetrofitError.Kind.NETWORK) {
            view.showServiceUnavailable();
            return;
        }

        Response response = error.getResponse();

        if (response != null) {

            switch (response.getStatus()) {
                case 400:
                    view.showRetryLaterNotification();
                    break;
                case 401:
                    view.showUserActionForbidden();
                    view.startLoginActivity();
                    break;
                case 404:
                    view.showServiceUnavailable();
                    break;
                case 503:
                    view.showServiceTemporaryUnavailable();
                    break;
                default:
                    view.showUnknownErrorOccurred();
                    break;
            }
        } else {
            view.showServiceUnavailable();
        }
    }
}
