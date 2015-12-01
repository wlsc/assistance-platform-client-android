package de.tudarmstadt.informatik.tk.android.assistance.view;

import de.tudarmstadt.informatik.tk.android.assistance.presenter.ResetPasswordPresenter;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface ResetPasswordView {

    void setPresenter(ResetPasswordPresenter presenter);

    void clearErrors();

    void setErrorEmailFieldRequired();

    void setErrorEmailInvalid();

    void showRequestSuccessful();

    void startLoginActivity();

    void showServiceUnavailable();

    void showServiceTemporaryUnavailable();

    void showUnknownError();
}
