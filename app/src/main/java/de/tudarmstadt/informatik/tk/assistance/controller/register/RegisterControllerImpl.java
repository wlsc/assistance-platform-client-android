package de.tudarmstadt.informatik.tk.assistance.controller.register;

import de.tudarmstadt.informatik.tk.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.assistance.presenter.register.RegisterPresenter;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.registration.RegistrationRequestDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.registration.RegistrationResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.api.UserApiProvider;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;

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
        UserApiProvider service = apiProvider.getUserApiProvider();

        service.registerUser(request)
                .subscribe(new Subscriber<RegistrationResponseDto>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            handler.onError((HttpException) e);
                        }
                    }

                    @Override
                    public void onNext(RegistrationResponseDto registrationResponseDto) {
                        handler.onSuccess(registrationResponseDto);
                    }
                });
    }
}