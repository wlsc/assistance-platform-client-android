package de.tu_darmstadt.tk.android.assistance.services;

import java.util.HashMap;

import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by Wladimir Schmidt on 27.06.2015.
 */
public interface RegistrationService {

    public String EMAIL_FIELD = "email";

    public String PASSWORD_FIELD = "password";

    @POST("/register")
    Long registerUser(@Body HashMap<String, String> body);
}