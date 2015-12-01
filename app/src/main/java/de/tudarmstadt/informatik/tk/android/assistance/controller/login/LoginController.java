package de.tudarmstadt.informatik.tk.android.assistance.controller.login;

import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnUserValidHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.login.LoginResponseDto;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface LoginController {

    /**
     * Login procedure
     *
     * @param email
     * @param password
     * @param handler
     */
    void doLogin(String email, String password, OnResponseHandler handler);

    /**
     * Saves login and proceeds with login
     *
     * @param loginApiResponse
     * @param handler
     */
    void saveLoginGoNext(LoginResponseDto loginApiResponse, OnUserValidHandler handler);

    /**
     * Saves login into db
     *
     * @param response
     */
    void saveLoginIntoDb(LoginResponseDto response);
}