package de.tudarmstadt.informatik.tk.android.assistance.presenter;

import android.content.Context;

import com.google.gson.Gson;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.dto.error.ErrorDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.error.ApiHttpErrorCodes;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.view.CommonView;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

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
        HarvesterServiceProvider.getInstance(getContext()).startSensingService();
    }

    @Override
    public void stopHarvester() {
        HarvesterServiceProvider.getInstance(getContext()).stopSensingService();
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

            String jsonError = new String(((TypedByteArray) error
                    .getResponse()
                    .getBody())
                    .getBytes());

            Gson gson = new Gson();
            ErrorDto apiError = gson.fromJson(jsonError, ErrorDto.class);

            switch (response.getStatus()) {
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
        } else {
            view.showServiceUnavailable();
        }
    }

    @Override
    public void presentEMailAlreadyExists() {
        // empty
    }
}
