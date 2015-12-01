package de.tudarmstadt.informatik.tk.android.assistance.controller.login;

import java.util.List;

import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnUserValidHandler;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.login.LoginRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.login.LoginResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.login.UserDeviceDto;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.UserEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.login.LoginPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.Config;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbDevice;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.HardwareUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.ValidationUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public class LoginControllerImpl implements LoginController {

    private static final String TAG = LoginControllerImpl.class.getSimpleName();

    private final LoginPresenter presenter;

    private final DaoProvider daoProvider;

    public LoginControllerImpl(LoginPresenter presenter) {
        this.presenter = presenter;
        this.daoProvider = DaoProvider.getInstance(presenter.getContext());
    }

    @Override
    public void doLogin(String email, String password, final OnResponseHandler handler) {

        DbUser user = daoProvider.getUserDao().getByEmail(email);

        Long serverDeviceId = null;

        if (user != null) {

            PreferenceUtils.setCurrentUserId(presenter.getContext(), user.getId());
            PreferenceUtils.setUserEmail(presenter.getContext(), user.getPrimaryEmail());
            PreferenceUtils.setUserFirstname(presenter.getContext(), user.getFirstname());
            PreferenceUtils.setUserLastname(presenter.getContext(), user.getLastname());

            String currentAndroidId = HardwareUtils.getAndroidId(presenter.getContext());

            List<DbDevice> userDevices = user.getDbDeviceList();

            for (DbDevice device : userDevices) {
                if (device.getDeviceIdentifier().equals(currentAndroidId)) {
                    serverDeviceId = device.getServerDeviceId();
                    break;
                }
            }
        }

        /**
         * Forming a login request
         */
        LoginRequestDto loginRequest = new LoginRequestDto();

        loginRequest.setUserEmail(email);
        loginRequest.setPassword(password);

        UserDeviceDto userDevice = new UserDeviceDto();

        if (serverDeviceId != null) {
            userDevice.setServerId(serverDeviceId);
        }

        userDevice.setOs(Config.PLATFORM_NAME);
        userDevice.setOsVersion(HardwareUtils.getAndroidVersion());
        userDevice.setBrand(HardwareUtils.getDeviceBrandName());
        userDevice.setModel(HardwareUtils.getDeviceModelName());
        userDevice.setDeviceId(HardwareUtils.getAndroidId(presenter.getContext()));

        loginRequest.setDevice(userDevice);

        /**
         * Logging in the user
         */
        UserEndpoint userEndpoint = EndpointGenerator
                .getInstance(presenter.getContext())
                .create(UserEndpoint.class);

        userEndpoint.loginUser(loginRequest, new Callback<LoginResponseDto>() {

            @Override
            public void success(LoginResponseDto apiResponse, Response response) {
                handler.onSuccess(apiResponse, response);
            }

            @Override
            public void failure(RetrofitError error) {
                handler.onError(error);
            }
        });
    }

    @Override
    public void saveLoginGoNext(LoginResponseDto loginApiResponse, OnUserValidHandler handler) {

        String token = loginApiResponse.getUserToken();

        if (ValidationUtils.isUserTokenValid(token)) {
            Log.d(TAG, "Token is valid. Proceeding with login...");

            saveLoginIntoDb(loginApiResponse);

            handler.onSaveUserCredentialsToPreference(token);

        } else {
            Toaster.showLong(this, R.string.error_user_token_not_valid);
            Log.d(TAG, "User token is INVALID!");
        }
    }
}
