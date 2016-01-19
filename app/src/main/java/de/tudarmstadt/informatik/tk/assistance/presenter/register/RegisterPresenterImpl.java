package de.tudarmstadt.informatik.tk.assistance.presenter.register;

import android.content.Context;
import android.text.TextUtils;

import de.tudarmstadt.informatik.tk.assistance.controller.register.RegisterController;
import de.tudarmstadt.informatik.tk.assistance.controller.register.RegisterControllerImpl;
import de.tudarmstadt.informatik.tk.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.assistance.presenter.CommonPresenterImpl;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.registration.RegistrationRequestDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.registration.RegistrationResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.ValidationUtils;
import de.tudarmstadt.informatik.tk.assistance.view.RegisterView;
import retrofit.RetrofitError;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public class RegisterPresenterImpl extends
        CommonPresenterImpl implements
        RegisterPresenter, OnResponseHandler<RegistrationResponseDto> {

    private static final String TAG = RegisterPresenterImpl.class.getSimpleName();

    private RegisterView view;
    private RegisterController controller;

    public RegisterPresenterImpl(Context context) {
        super(context);
        this.controller = new RegisterControllerImpl(this);
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
        super.setView(view);
        this.view = view;
    }

    @Override
    public void onSuccess(RegistrationResponseDto apiResponse) {

        // save for autologin feature
        view.saveUserCredentials();
        view.startLoginActivity();

        Log.d(TAG, "success! userId: " + apiResponse.getUserId());
    }

    @Override
    public void onError(RetrofitError error) {
        doDefaultErrorProcessing(error);
    }

    @Override
    public void initView() {
        view.initView();
    }

    @Override
    public void presentEMailAlreadyExists() {
        view.showErrorEmailAreadyExists();
    }
}
