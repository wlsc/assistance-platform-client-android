package de.tudarmstadt.informatik.tk.assistance.view;

import de.tudarmstadt.informatik.tk.assistance.presenter.password.ResetPasswordPresenter;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface ResetPasswordView extends CommonView {

    void setPresenter(ResetPasswordPresenter presenter);

    void showRequestSuccessful();

    void setErrorEmailFieldRequired();

    void setErrorEmailInvalid();
}
