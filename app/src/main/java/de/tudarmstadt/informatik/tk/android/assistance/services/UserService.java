package de.tudarmstadt.informatik.tk.android.assistance.services;

import de.tudarmstadt.informatik.tk.android.assistance.models.api.login.LoginRequest;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.registration.RegistrationRequest;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.resetpassword.ResetPasswordRequest;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.profile.UpdateProfileRequest;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.login.LoginResponse;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.registration.RegistrationResponse;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.profile.ProfileResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public interface UserService {

    @POST("/users/register")
    void registerUser(@Body RegistrationRequest body,
                      Callback<RegistrationResponse> callback);

    @POST("/users/login")
    void loginUser(@Body LoginRequest body,
                   Callback<LoginResponse> callback);

    @POST("/users/password")
    void resetUserPassword(@Body ResetPasswordRequest body,
                           Callback<Void> callback);

    @GET("/users/profile/short")
    void getUserProfileShort(@Header("X-AUTH-TOKEN") String userToken,
                             Callback<ProfileResponse> callback);

    @GET("/users/profile/long")
    void getUserProfileFull(@Header("X-AUTH-TOKEN") String userToken,
                            Callback<ProfileResponse> callback);

    @PUT("/users/profile")
    void updateUserProfile(@Header("X-AUTH-TOKEN") String userToken,
                           @Body UpdateProfileRequest body,
                           Callback<Void> callback);
}
