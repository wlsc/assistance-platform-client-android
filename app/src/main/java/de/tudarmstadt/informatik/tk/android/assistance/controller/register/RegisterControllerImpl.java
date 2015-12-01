package de.tudarmstadt.informatik.tk.android.assistance.controller.register;

import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.registration.RegistrationRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.registration.RegistrationResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.UserEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.register.RegisterPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.endpoint.EndpointGenerator;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public class RegisterControllerImpl implements RegisterController {

    private final RegisterPresenter presenter;

    public RegisterControllerImpl(RegisterPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void doRegisterUser(RegistrationRequestDto request, final OnResponseHandler handler) {

        // calling api service
        UserEndpoint service = EndpointGenerator
                .getInstance(presenter.getContext())
                .create(UserEndpoint.class);

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
