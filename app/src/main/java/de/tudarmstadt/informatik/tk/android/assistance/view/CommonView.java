package de.tudarmstadt.informatik.tk.android.assistance.view;

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

    void showUserActionForbidden();

    void askPermissions(Set<String> permsToAsk);
}
