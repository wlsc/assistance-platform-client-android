package de.tudarmstadt.informatik.tk.assistance.handler;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public interface OnUserValidHandler {

    void onSaveUserCredentialsToPreference(String token);

    void onUserTokenInvalid();

    void showMainActivity();
}
