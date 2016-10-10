package de.tudarmstadt.informatik.tk.assistance.presenter.login;

import android.content.Context;
import android.text.TextUtils;

import de.tudarmstadt.informatik.tk.assistance.BuildConfig;
import de.tudarmstadt.informatik.tk.assistance.controller.login.LoginController;
import de.tudarmstadt.informatik.tk.assistance.controller.login.LoginControllerImpl;
import de.tudarmstadt.informatik.tk.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.assistance.handler.OnUserValidHandler;
import de.tudarmstadt.informatik.tk.assistance.presenter.CommonPresenterImpl;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.login.LoginResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.util.ValidationUtils;
import de.tudarmstadt.informatik.tk.assistance.view.LoginView;
import retrofit2.adapter.rxjava.HttpException;

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
        this.controller = new LoginControllerImpl(this);
    }

    @Override
    public void setView(LoginView view) {
        super.setView(view);
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

        // check for password
        if (!TextUtils.isEmpty(password) && !ValidationUtils.isPasswordLengthValid(password)) {
            view.showErrorPasswordInvalid();
            view.setLoginButtonEnabled(true);
            return;
        }

        // check for email address
        if (TextUtils.isEmpty(email)) {
            view.showErrorEmailRequired();
            view.setLoginButtonEnabled(true);
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            view.showErrorEmailInvalid();
            view.setLoginButtonEnabled(true);
            return;
        }

        view.showProgress(true);
        controller.doLogin(email, password, this);
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
    public void getSplashView() {

        view.showSystemUI();
        view.setContent();

        if (BuildConfig.DEBUG) {
            view.setDebugViewInformation();
        }
    }

    @Override
    public void onSuccess(LoginResponseDto apiResponse) {

        controller.saveLoginGoNext(apiResponse, this);

        Log.d(TAG, "User token received: " + apiResponse.getUserToken());
    }

    @Override
    public void onError(HttpException error) {

        view.setLoginButtonEnabled(true);
        view.showProgress(false);

        doDefaultErrorProcessing(error);
    }

    @Override
    public void showMainActivity() {
        view.loadMainActivity();
    }

    @Override
    public void initView() {
        view.initView();
    }
}