package de.tudarmstadt.informatik.tk.android.assistance.view;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public interface CommonView {

    void startLoginActivity();

    void clearErrors();

    void showServiceUnavailable();

    void showServiceTemporaryUnavailable();

    void showUnknownErrorOccurred();
}
