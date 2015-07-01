package de.tu_darmstadt.tk.android.assistance.services;

import de.tu_darmstadt.tk.android.assistance.models.http.request.RegistrationRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.response.RegistrationResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by Wladimir Schmidt on 27.06.2015.
 */
public interface RegistrationService {

    @POST("/users/register")
    void registerUser(@Body RegistrationRequest body, Callback<RegistrationResponse> callback);
}