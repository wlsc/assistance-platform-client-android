package de.tudarmstadt.informatik.tk.android.assistance.controller;

import de.tudarmstadt.informatik.tk.android.assistance.handler.OnRequestFinishedHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.resetpassword.ResetPasswordRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.UserEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.ResetPasswordPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.endpoint.EndpointGenerator;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public class ResetPasswordControllerImpl implements ResetPasswordController {

    private final ResetPasswordPresenter presenter;

    public ResetPasswordControllerImpl(ResetPasswordPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void resetUserPassword(ResetPasswordRequestDto resetRequest, final OnRequestFinishedHandler handler) {

        UserEndpoint service = EndpointGenerator
                .getInstance(presenter.getContext())
                .create(UserEndpoint.class);

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
