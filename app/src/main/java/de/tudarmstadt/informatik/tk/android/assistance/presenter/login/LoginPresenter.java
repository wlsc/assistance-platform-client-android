package de.tudarmstadt.informatik.tk.android.assistance.presenter.login;

import de.tudarmstadt.informatik.tk.android.assistance.controller.login.LoginController;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.CommonPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.view.LoginView;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface LoginPresenter extends CommonPresenter {

    void setView(LoginView view);

    void setController(LoginController controller);

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    void attemptLogin(String email, String password);

    void checkAutologin(String userToken);

    void getSplashView();
}