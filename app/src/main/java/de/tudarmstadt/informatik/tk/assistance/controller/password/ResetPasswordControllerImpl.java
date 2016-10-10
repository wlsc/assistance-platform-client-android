package de.tudarmstadt.informatik.tk.assistance.controller.password;

import de.tudarmstadt.informatik.tk.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.assistance.handler.OnEmptyResponseHandler;
import de.tudarmstadt.informatik.tk.assistance.presenter.password.ResetPasswordPresenter;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.resetpassword.ResetPasswordRequestDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.ApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.api.UserApiProvider;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;

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

        UserApiProvider service = ApiProvider.getInstance(presenter.getContext()).getUserApiProvider();

        service.resetUserPassword(resetRequest)
                .subscribe(new Subscriber<Void>() {

                    @Override
                    public void onCompleted() {
                        // empty
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            handler.onError((HttpException) e);
                        }
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        handler.onSuccess();
                    }
                });
    }
}