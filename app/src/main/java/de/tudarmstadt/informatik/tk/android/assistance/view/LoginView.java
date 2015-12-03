package de.tudarmstadt.informatik.tk.android.assistance.view;

import android.view.View;

import de.tudarmstadt.informatik.tk.android.assistance.presenter.login.LoginPresenter;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface LoginView extends CommonView {

    void setPresenter(LoginPresenter presenter);

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

    void showUserTokenInvalid();

    void showSystemUI();

    void setContent();

    void setDebugViewInformation();

    void requestFocus(View view);

    void showErrorPasswordInvalid();

    void showErrorEmailRequired();

    void showErrorEmailInvalid();
}
