package de.tudarmstadt.informatik.tk.assistance.view;

import java.util.Set;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public interface CommonView {

    void initView();

    void startLoginActivity();

    void clearErrors();

    void showServiceUnavailable();

    void showServiceTemporaryUnavailable();

    void showUnknownErrorOccurred();

    void showUserForbidden();

    void showActionProhibited();

    void showRetryLaterNotification();

    void askPermissions(Set<String> permsToAsk);
}
