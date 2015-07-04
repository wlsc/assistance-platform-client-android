package de.tu_darmstadt.tk.android.assistance.services;

import de.tu_darmstadt.tk.android.assistance.models.http.request.LoginRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.request.RegistrationRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.request.ResetPasswordRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.request.profile.GetUserProfileRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.request.profile.UpdateUserProfileRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.response.LoginResponse;
import de.tu_darmstadt.tk.android.assistance.models.http.response.RegistrationResponse;
import de.tu_darmstadt.tk.android.assistance.models.http.response.UserProfileResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.PUT;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public interface UserService {

    @POST("/users/register")
    void registerUser(@Body RegistrationRequest body, Callback<RegistrationResponse> callback);

    @POST("/users/login")
    void loginUser(@Body LoginRequest body, Callback<LoginResponse> callback);

    @POST("/users/password")
    void resetUserPassword(@Body ResetPasswordRequest body, Callback<Void> callback);

    @POST("/users/profile")
    void getUserProfile(@Body GetUserProfileRequest body, Callback<UserProfileResponse> callback);

    @PUT("/users/profile")
    void updateUserProfile(@Body UpdateUserProfileRequest body, Callback<Void> callback);
}
