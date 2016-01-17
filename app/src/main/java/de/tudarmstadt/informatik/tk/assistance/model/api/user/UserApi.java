package de.tudarmstadt.informatik.tk.assistance.model.api.user;

import de.tudarmstadt.informatik.tk.assistance.Config;
import de.tudarmstadt.informatik.tk.assistance.model.api.user.profile.ProfileResponseDto;
import de.tudarmstadt.informatik.tk.assistance.model.api.user.profile.UpdateProfileRequestDto;
import de.tudarmstadt.informatik.tk.assistance.model.api.user.registration.RegistrationRequestDto;
import de.tudarmstadt.informatik.tk.assistance.model.api.user.registration.RegistrationResponseDto;
import de.tudarmstadt.informatik.tk.assistance.model.api.user.resetpassword.ResetPasswordRequestDto;
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
public interface UserApi {

    @POST(Config.ASSISTANCE_USER_REGISTER_ENDPOINT)
    void registerUser(@Body RegistrationRequestDto body,
                      Callback<RegistrationResponseDto> callback);

    @POST(Config.ASSISTANCE_USER_PASSWORD_ENDPOINT)
    void resetUserPassword(@Body ResetPasswordRequestDto body,
                           Callback<Void> callback);

    @GET(Config.ASSISTANCE_USER_PROFILE_SHORT_ENDPOINT)
    void getUserProfileShort(@Header("X-AUTH-TOKEN") String userToken,
                             Callback<ProfileResponseDto> callback);

    @GET(Config.ASSISTANCE_USER_PROFILE_FULL_ENDPOINT)
    void getUserProfileFull(@Header("X-AUTH-TOKEN") String userToken,
                            Callback<ProfileResponseDto> callback);

    @PUT(Config.ASSISTANCE_USER_PROFILE_UPDATE_ENDPOINT)
    void updateUserProfile(@Header("X-AUTH-TOKEN") String userToken,
                           @Body UpdateProfileRequestDto body,
                           Callback<Void> callback);
}
