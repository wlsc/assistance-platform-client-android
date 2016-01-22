package de.tudarmstadt.informatik.tk.assistance.presenter.register;

import de.tudarmstadt.informatik.tk.assistance.controller.register.RegisterController;
import de.tudarmstadt.informatik.tk.assistance.presenter.CommonPresenter;
import de.tudarmstadt.informatik.tk.assistance.view.RegisterView;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public interface RegisterPresenter extends CommonPresenter {

    void setController(RegisterController controller);

    void setView(RegisterView view);

    void registerUser(String email, String password1, String password2, boolean checked);
}
