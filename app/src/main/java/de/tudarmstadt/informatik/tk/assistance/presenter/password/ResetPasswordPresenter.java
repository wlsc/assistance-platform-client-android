package de.tudarmstadt.informatik.tk.assistance.presenter.password;

import de.tudarmstadt.informatik.tk.assistance.controller.password.ResetPasswordController;
import de.tudarmstadt.informatik.tk.assistance.presenter.CommonPresenter;
import de.tudarmstadt.informatik.tk.assistance.view.ResetPasswordView;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface ResetPasswordPresenter extends CommonPresenter {

    void setController(ResetPasswordController controller);

    void setView(ResetPasswordView view);

    void doResetPassword(String email);
}
