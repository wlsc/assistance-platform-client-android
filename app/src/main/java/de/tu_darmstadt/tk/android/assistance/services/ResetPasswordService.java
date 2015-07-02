package de.tu_darmstadt.tk.android.assistance.services;

import de.tu_darmstadt.tk.android.assistance.models.http.request.RegistrationRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.request.ResetPasswordRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.response.RegistrationResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by Wladimir Schmidt on 01.07.2015.
 */
public interface ResetPasswordService {

    @POST("/users/password")
    void resetUserPassword(@Body ResetPasswordRequest body, Callback<Void> callback);

}
