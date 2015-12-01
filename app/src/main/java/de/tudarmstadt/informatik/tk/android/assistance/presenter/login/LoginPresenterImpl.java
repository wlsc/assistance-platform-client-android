package de.tudarmstadt.informatik.tk.android.assistance.presenter.login;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.controller.login.LoginController;
import de.tudarmstadt.informatik.tk.android.assistance.controller.login.LoginControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnUserValidHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.login.LoginResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.CommonPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.ValidationUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.LoginView;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public class LoginPresenterImpl extends
        CommonPresenterImpl implements
        LoginPresenter, OnResponseHandler<LoginResponseDto>, OnUserValidHandler {

    private static final String TAG = LoginPresenterImpl.class.getSimpleName();

    private LoginView view;
    private LoginController controller;

    public LoginPresenterImpl(Context context) {
        super(context);
        setController(new LoginControllerImpl(this));

        view.initLogin();
    }

    @Override
    public void setView(LoginView view) {
        this.view = view;
    }

    @Override
    public void setController(LoginController controller) {
        this.controller = controller;
    }

    @Override
    public void attemptLogin(String email, String password) {

        view.hideKeyboard();

        // disable button to reduce flood of requests
        view.setLoginButtonEnabled(false);

        // Reset errors.
        view.clearErrors();

        boolean isAnyErrors = false;
        View focusView = null;

        // check for password
        if (!TextUtils.isEmpty(password) && !ValidationUtils.isPasswordLengthValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            isAnyErrors = true;
        }

        // check for email address
        if (TextUtils.isEmpty(email)) {
            mEmailTextView.setError(getString(R.string.error_field_required));
            focusView = mEmailTextView;
            isAnyErrors = true;
        } else {
            if (!ValidationUtils.isValidEmail(email)) {
                mEmailTextView.setError(getString(R.string.error_invalid_email));
                focusView = mEmailTextView;
                isAnyErrors = true;
            }
        }

        if (isAnyErrors) {

            view.setLoginButtonEnabled(true);
            focusView.requestFocus();

        } else {
            view.showProgress(true);
            controller.doLogin(email, password, this);
        }
    }

    @Override
    public boolean validateCredentials(String email, String password) {
        return false;
    }

    @Override
    public void checkAutologin(String userToken) {

        if (userToken.isEmpty()) {

            Log.d(TAG, "User token NOT found");
            Log.d(TAG, "Searching for autologin...");

            String savedEmail = PreferenceUtils.getUserEmail(getContext());
            String savedPassword = PreferenceUtils.getUserPassword(getContext());

            if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {

                Log.d(TAG, "Found email/password entries saved. Doing autologin...");

                controller.doLogin(savedEmail, savedPassword, this);

                return;
            }
        } else {
            Log.d(TAG, "User token found. Launching main activity!");
            view.loadMainActivity();
            return;
        }

        view.loadSplashView();
    }

    @Override
    public void onSuccess(LoginResponseDto apiResponse, Response response) {

        controller.saveLoginGoNext(apiResponse, this);

        Log.d(TAG, "User token received: " + apiResponse.getUserToken());
    }

    @Override
    public void onError(RetrofitError error) {

        view.setLoginButtonEnabled(true);
        view.showProgress(false);

        if (error.getKind() == RetrofitError.Kind.NETWORK) {
            view.showServiceUnavailable();
            return;
        }

        Response response = error.getResponse();

        if (response != null) {

            switch (response.getStatus()) {
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
                    view.showUnknownErrorOccurred();
                    break;
            }
        } else {
            view.showServiceUnavailable();
        }
    }

    @Override
    public void onSaveUserCredentialsToPreference(String token) {
        view.saveUserCredentialsToPreference(token);
        view.loadMainActivity();
    }
}
