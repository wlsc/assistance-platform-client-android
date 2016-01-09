package de.tudarmstadt.informatik.tk.android.assistance.controller.login;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonController;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnUserValidHandler;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.login.LoginResponseDto;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface LoginController extends CommonController {

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

    void initAllowedModuleCaps(DbUser user);
}
