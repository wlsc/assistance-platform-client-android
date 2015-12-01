package de.tudarmstadt.informatik.tk.android.assistance.view;

import de.tudarmstadt.informatik.tk.android.assistance.presenter.login.LoginPresenter;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface LoginView extends CommonView {

    void setPresenter(LoginPresenter presenter);

    void showView();

    void initLogin();

    /**
     * Disables back button for user
     * and starts main activity
     */
    void loadMainActivity();

    /**
     * Shows the progress UI and hides the login form.
     */
    void showProgress(boolean isShowing);

    void setLoginButtonEnabled(boolean isEnabled);

    void hideKeyboard();

    void loadSplashView();

    void saveUserCredentialsToPreference(String token);
}
