package de.tudarmstadt.informatik.tk.android.assistance.presenter;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.error.ApiError;
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
            JsonParser parser = new JsonParser();
            JsonObject jObj = parser.parse(jsonError).getAsJsonObject();
            ApiError apiError = gson.fromJson(jObj, ApiError.class);

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
