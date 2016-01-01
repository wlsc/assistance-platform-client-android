package de.tudarmstadt.informatik.tk.android.assistance.controller.register;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonController;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.user.registration.RegistrationRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.user.registration.RegistrationResponseDto;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public interface RegisterController extends CommonController {

    void doRegisterUser(RegistrationRequestDto request,
                        OnResponseHandler<RegistrationResponseDto> handler);
}