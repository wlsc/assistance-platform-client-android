package de.tudarmstadt.informatik.tk.android.assistance.controller.register;

import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.registration.RegistrationRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.registration.RegistrationResponseDto;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public interface RegisterController {

    void doRegisterUser(RegistrationRequestDto request,
                        OnResponseHandler<RegistrationResponseDto> handler);
}