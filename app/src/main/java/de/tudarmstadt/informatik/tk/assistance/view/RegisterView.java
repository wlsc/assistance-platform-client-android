package de.tudarmstadt.informatik.tk.assistance.view;

import de.tudarmstadt.informatik.tk.assistance.presenter.register.RegisterPresenter;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public interface RegisterView extends CommonView {

    void setPresenter(RegisterPresenter presenter);

    void setErrorEmailEmpty();

    void setErrorPassword1Empty();

    void setErrorPassword2Empty();

    void setErrorEmailInvalid();

    void setErrorPasswordsNotSame();

    void setErrorPasswordLengthInvalid();

    void hideKeyboard();

    void saveUserCredentials();

    void showErrorEmailAreadyExists();
}