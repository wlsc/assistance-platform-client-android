package de.tudarmstadt.informatik.tk.assistance.controller.register;

import de.tudarmstadt.informatik.tk.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.registration.RegistrationRequestDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.registration.RegistrationResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.UserApi;
import de.tudarmstadt.informatik.tk.assistance.presenter.register.RegisterPresenter;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.ApiGenerator;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public class RegisterControllerImpl extends
        CommonControllerImpl implements
        RegisterController {

    private final RegisterPresenter presenter;

    public RegisterControllerImpl(RegisterPresenter presenter) {
        super(presenter.getContext());
        this.presenter = presenter;
    }

    @Override
    public void doRegisterUser(RegistrationRequestDto request, final OnResponseHandler handler) {

        // calling api service
        UserApi service = ApiGenerator
                .getInstance(presenter.getContext())
                .create(UserApi.class);

        service.registerUser(request, new Callback<RegistrationResponseDto>() {

            @Override
            public void success(RegistrationResponseDto apiResponse, Response response) {
                handler.onSuccess(apiResponse, response);
            }

            @Override
            public void failure(RetrofitError error) {
                handler.onError(error);
            }
        });
    }
}
