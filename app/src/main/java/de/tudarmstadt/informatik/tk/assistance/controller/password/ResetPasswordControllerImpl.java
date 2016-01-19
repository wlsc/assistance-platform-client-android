package de.tudarmstadt.informatik.tk.assistance.controller.password;

import de.tudarmstadt.informatik.tk.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.assistance.handler.OnEmptyResponseHandler;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.resetpassword.ResetPasswordRequestDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.UserApi;
import de.tudarmstadt.informatik.tk.assistance.presenter.password.ResetPasswordPresenter;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.ApiGenerator;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public class ResetPasswordControllerImpl extends
        CommonControllerImpl implements
        ResetPasswordController {

    private final ResetPasswordPresenter presenter;

    public ResetPasswordControllerImpl(ResetPasswordPresenter presenter) {
        super(presenter.getContext());
        this.presenter = presenter;
    }

    @Override
    public void resetUserPassword(ResetPasswordRequestDto resetRequest, final OnEmptyResponseHandler handler) {

        UserApi service = ApiGenerator
                .getInstance(presenter.getContext())
                .create(UserApi.class);

        service.resetUserPassword(resetRequest, new Callback<Void>() {

            @Override
            public void success(Void aVoid, Response response) {
                handler.onSuccess(response);
            }

            @Override
            public void failure(RetrofitError error) {
                handler.onError(error);
            }
        });
    }
}
