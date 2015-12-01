package de.tudarmstadt.informatik.tk.android.assistance.presenter;

import android.content.Context;

import de.tudarmstadt.informatik.tk.android.assistance.controller.ResetPasswordController;
import de.tudarmstadt.informatik.tk.android.assistance.view.ResetPasswordView;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface ResetPasswordPresenter {

    void setController(ResetPasswordController controller);

    void setView(ResetPasswordView view);

    void doResetPassword(String email);

    void setLogTag(String tag);

    Context getContext();
}
