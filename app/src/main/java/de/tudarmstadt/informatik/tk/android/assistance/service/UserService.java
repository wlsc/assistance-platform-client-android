package de.tudarmstadt.informatik.tk.android.assistance.service;

import de.tudarmstadt.informatik.tk.android.assistance.Config;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.login.LoginRequest;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.login.LoginResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.profile.ProfileResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.profile.UpdateProfileRequest;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.registration.RegistrationRequest;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.registration.RegistrationResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.resetpassword.ResetPasswordRequest;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;


/**
 * User service API endpoint
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public interface UserService {

    @POST(Config.ASSISTANCE_USER_REGISTER_ENDPOINT)
    void registerUser(@Body RegistrationRequest body,
                      Callback<RegistrationResponse> callback);

    @POST(Config.ASSISTANCE_USER_LOGIN_ENDPOINT)
    void loginUser(@Body LoginRequest body,
                   Callback<LoginResponse> callback);

    @POST(Config.ASSISTANCE_USER_PASSWORD_ENDPOINT)
    void resetUserPassword(@Body ResetPasswordRequest body,
                           Callback<Void> callback);

    @GET(Config.ASSISTANCE_USER_PROFILE_SHORT_ENDPOINT)
    void getUserProfileShort(@Header("X-AUTH-TOKEN") String userToken,
                             Callback<ProfileResponse> callback);

    @GET(Config.ASSISTANCE_USER_PROFILE_FULL_ENDPOINT)
    void getUserProfileFull(@Header("X-AUTH-TOKEN") String userToken,
                            Callback<ProfileResponse> callback);

    @PUT(Config.ASSISTANCE_USER_PROFILE_UPDATE_ENDPOINT)
    void updateUserProfile(@Header("X-AUTH-TOKEN") String userToken,
                           @Body UpdateProfileRequest body,
                           Callback<Void> callback);
}
