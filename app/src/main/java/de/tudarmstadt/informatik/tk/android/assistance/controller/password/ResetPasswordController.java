package de.tudarmstadt.informatik.tk.android.assistance.controller.password;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonController;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnEmptyResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.resetpassword.ResetPasswordRequestDto;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface ResetPasswordController extends CommonController {

    void resetUserPassword(ResetPasswordRequestDto resetRequest, OnEmptyResponseHandler handler);
}
