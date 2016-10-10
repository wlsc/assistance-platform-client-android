package de.tudarmstadt.informatik.tk.assistance.presenter;

import android.content.Context;

import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.error.ApiError;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.error.ApiHttpErrorCodes;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.ConverterUtils;
import de.tudarmstadt.informatik.tk.assistance.view.CommonView;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public abstract class CommonPresenterImpl implements CommonPresenter {

    private static final String TAG = CommonPresenterImpl.class.getSimpleName();

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
    public void initView() {
        // common view injector
    }

    @Override
    public void startHarvester() {
        HarvesterServiceProvider.getInstance(this.context).startSensingService();
    }

    @Override
    public void stopHarvester() {
        HarvesterServiceProvider.getInstance(this.context).stopSensingService();
    }

    @Override
    public void setView(CommonView view) {
        this.view = view;
    }

    @Override
    public void doDefaultErrorProcessing(HttpException error) {

        final Response response = error.response();

        if (response == null || !response.isSuccessful()) {
            view.showServiceUnavailable();
            return;
        }

        final ApiError apiError = ConverterUtils.parseError(context, response);

        switch (error.code()) {
            case 400:

                switch (apiError.getCode()) {
                    case ApiHttpErrorCodes.EMAIL_ALREADY_EXISTS:
                        presentEMailAlreadyExists();
                        break;
                    default:
                        view.showRetryLaterNotification();
                        break;
                }
                break;
            case 401:
                view.showUserForbidden();
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
    }

    @Override
    public void presentEMailAlreadyExists() {
        // empty
    }
}
