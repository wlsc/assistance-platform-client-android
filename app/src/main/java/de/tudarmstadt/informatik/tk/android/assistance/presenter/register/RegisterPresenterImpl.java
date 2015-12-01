package de.tudarmstadt.informatik.tk.android.assistance.presenter.register;

import android.content.Context;
import android.text.TextUtils;

import de.tudarmstadt.informatik.tk.android.assistance.controller.register.RegisterController;
import de.tudarmstadt.informatik.tk.android.assistance.controller.register.RegisterControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.registration.RegistrationRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.registration.RegistrationResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.CommonPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.ValidationUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.RegisterView;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public class RegisterPresenterImpl extends
        CommonPresenterImpl implements
        RegisterPresenter, OnResponseHandler<RegistrationResponseDto> {

    private RegisterView view;
    private RegisterController controller;

    public RegisterPresenterImpl(Context context) {
        super(context);
        setController(new RegisterControllerImpl(this));
    }

    @Override
    public void registerUser(String email, String password1, String password2) {

        if (isInputOK(email, password1, password2)) {

            //        String passwordHashed = CommonUtils.generateSHA256(password);

            // forming a login request
            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setUserEmail(email);
            request.setPassword(password1);

            controller.doRegisterUser(request, this);
        }
    }

    /**
     * Validates user's input
     *
     * @return
     */
    private boolean isInputOK(String email, String password1, String password2) {

        // reset all errors
        view.clearErrors();

        // EMPTY FIELDS CHECK
        if (TextUtils.isEmpty(email)) {
            view.setErrorEmailEmpty();
            return false;
        }

        if (TextUtils.isEmpty(password1)) {
            view.setErrorPassword1Empty();
            return false;
        }

        if (TextUtils.isEmpty(password2)) {
            view.setErrorPassword2Empty();
            return false;
        }

        // NOT VALID EMAIL
        if (!ValidationUtils.isValidEmail(email)) {
            view.setErrorEmailInvalid();
            return false;
        }

        // NOT EQUAL PASSWORDS
        if (!password1.equals(password2)) {
            view.setErrorPasswordsNotSame();
            return false;
        }

        // NOT VALID LENGTH
        if (!ValidationUtils.isPasswordLengthValid(password1)) {
            view.setErrorPasswordLengthInvalid();
            return false;
        }

        view.hideKeyboard();

        return true;
    }

    @Override
    public void setController(RegisterController controller) {
        this.controller = controller;
    }

    @Override
    public void setView(RegisterView view) {
        this.view = view;
    }

    @Override
    public void onSuccess(RegistrationResponseDto apiResponse, Response response) {

        if (response.getStatus() == 200 || response.getStatus() == 204) {

            // save for autologin feature
            view.saveUserCredentials();
            view.startLoginActivity();

            Log.d(TAG, "success! userId: " + apiResponse.getUserId());
        }
    }

    @Override
    public void onError(RetrofitError error) {

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
}
