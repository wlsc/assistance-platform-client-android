package de.tudarmstadt.informatik.tk.assistance.controller.password;

import de.tudarmstadt.informatik.tk.assistance.controller.CommonController;
import de.tudarmstadt.informatik.tk.assistance.handler.OnEmptyResponseHandler;
import de.tudarmstadt.informatik.tk.assistance.model.api.user.resetpassword.ResetPasswordRequestDto;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface ResetPasswordController extends CommonController {

    void resetUserPassword(ResetPasswordRequestDto resetRequest, OnEmptyResponseHandler handler);
}
