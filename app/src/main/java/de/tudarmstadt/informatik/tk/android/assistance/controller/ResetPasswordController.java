package de.tudarmstadt.informatik.tk.android.assistance.controller;

import de.tudarmstadt.informatik.tk.android.assistance.handler.OnRequestFinishedHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.resetpassword.ResetPasswordRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.ResetPasswordPresenter;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface ResetPasswordController {

    void resetUserPassword(ResetPasswordRequestDto resetRequest, OnRequestFinishedHandler handler);
}
