package de.tudarmstadt.informatik.tk.android.assistance.presenter;

import android.content.Context;
import android.text.TextUtils;

import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.controller.ResetPasswordController;
import de.tudarmstadt.informatik.tk.android.assistance.controller.ResetPasswordControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnRequestFinishedHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.resetpassword.ResetPasswordRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.ValidationUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.ResetPasswordView;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public class ResetPasswordPresenterImpl implements
        ResetPasswordPresenter, OnRequestFinishedHandler {

    private String TAG;

    private Context context;

    private ResetPasswordView view;
    private ResetPasswordController controller;

    public ResetPasswordPresenterImpl(Context context) {
        this.context = context;
        setController(new ResetPasswordControllerImpl(this));
    }

    @Override
    public void setController(ResetPasswordController controller) {
        this.controller = controller;
    }

    @Override
    public void setView(ResetPasswordView view) {
        this.view = view;
    }

    @Override
    public void doResetPassword(String email) {

        view.clearErrors();

        if (TextUtils.isEmpty(email)) {
            view.setErrorEmailFieldRequired();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            view.setErrorEmailInvalid();
            return;
        }

        Log.d(TAG, "Requesting reset password service...");

        ResetPasswordRequestDto request = new ResetPasswordRequestDto();
        request.setEmail(email);

        controller.resetUserPassword(request, this);
    }

    @Override
    public void setLogTag(String tag) {
        this.TAG = tag;
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    @Override
    public void onSuccess(Response response) {

        if (response.getStatus() == 200 || response.getStatus() == 204) {
            view.showRequestSuccessful();
        }
    }

    @Override
    public void onError(RetrofitError error) {

        Response response = error.getResponse();

        if (response != null) {

            int httpCode = response.getStatus();

            switch (httpCode) {
                case 400:
                    break;
                case 401:
                    view.startLoginActivity();
                    break;
                case 404:
                    view.showServiceUnavailable();
                    break;
                case 503:
                    view.showServiceTemporaryUnavailable();
                    break;
                default:
                    view.showUnknownError();
                    break;
            }
        } else {
            view.showServiceUnavailable();
        }
    }
}
