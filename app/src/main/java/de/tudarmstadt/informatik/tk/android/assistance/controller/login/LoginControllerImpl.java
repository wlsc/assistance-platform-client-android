package de.tudarmstadt.informatik.tk.android.assistance.controller.login;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tudarmstadt.informatik.tk.android.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.android.assistance.handler.OnUserValidHandler;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.login.LoginPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.Config;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbDevice;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.dto.login.LoginRequestDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.dto.login.LoginResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.dto.login.UserDeviceDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.endpoint.LoginEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.DateUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.HardwareUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.ValidationUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public class LoginControllerImpl extends
        CommonControllerImpl implements
        LoginController {

    private static final String TAG = LoginControllerImpl.class.getSimpleName();

    private final LoginPresenter presenter;

    public LoginControllerImpl(LoginPresenter presenter) {
        super(presenter.getContext());
        this.presenter = presenter;
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
        LoginEndpoint userEndpoint = EndpointGenerator
                .getInstance(presenter.getContext())
                .create(LoginEndpoint.class);

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

            handler.onSaveUserCredentialsToPreference(token);
            saveLoginIntoDb(loginApiResponse);
            handler.showMainActivity();

        } else {
            handler.onUserTokenInvalid();
        }
    }

    @Override
    public void saveLoginIntoDb(LoginResponseDto response) {

        String createdDate = DateUtils.dateToISO8601String(new Date(), Locale.getDefault());

        DbUser user = daoProvider.getUserDao().getByToken(response.getUserToken());

        // check if that user was already saved in the system
        if (user == null) {
            // no such user found -> insert new user into db

            String email = PreferenceUtils.getUserEmail(presenter.getContext());

            DbUser newUser = new DbUser();

            newUser.setToken(response.getUserToken());
            newUser.setPrimaryEmail(email);
            newUser.setCreated(createdDate);

            long newUserId = daoProvider.getUserDao().insert(newUser);

            PreferenceUtils.setCurrentUserId(presenter.getContext(), newUserId);

            // saving device info into db

            DbDevice device = new DbDevice();

            device.setServerDeviceId(response.getDeviceId());
            device.setOs(Config.PLATFORM_NAME);
            device.setOsVersion(HardwareUtils.getAndroidVersion());
            device.setBrand(HardwareUtils.getDeviceBrandName());
            device.setModel(HardwareUtils.getDeviceModelName());
            device.setDeviceIdentifier(HardwareUtils.getAndroidId(presenter.getContext()));
            device.setCreated(createdDate);
            device.setUserId(newUserId);

            long currentDeviceId = daoProvider.getDeviceDao().insert(device);

            PreferenceUtils.setCurrentDeviceId(presenter.getContext(), currentDeviceId);
            PreferenceUtils.setServerDeviceId(presenter.getContext(), response.getDeviceId());

        } else {

            List<DbDevice> userDevices = user.getDbDeviceList();

            String currentAndroidId = HardwareUtils.getAndroidId(presenter.getContext());
            boolean isDeviceAlreadyCreated = false;

            for (DbDevice device : userDevices) {
                if (device.getDeviceIdentifier().equals(currentAndroidId)) {
                    isDeviceAlreadyCreated = true;

                    PreferenceUtils.setCurrentDeviceId(presenter.getContext(), device.getId());
                    PreferenceUtils.setServerDeviceId(presenter.getContext(), device.getServerDeviceId());

                    break;
                }
            }

            if (!isDeviceAlreadyCreated) {
                // no such device found in db -> insert new

                DbDevice device = new DbDevice();

                device.setServerDeviceId(response.getDeviceId());
                device.setOs(Config.PLATFORM_NAME);
                device.setOsVersion(HardwareUtils.getAndroidVersion());
                device.setBrand(HardwareUtils.getDeviceBrandName());
                device.setModel(HardwareUtils.getDeviceModelName());
                device.setDeviceIdentifier(HardwareUtils.getAndroidId(presenter.getContext()));
                device.setCreated(createdDate);
                device.setUserId(user.getId());

                long currentDeviceId = daoProvider.getDeviceDao().insert(device);

                PreferenceUtils.setCurrentDeviceId(presenter.getContext(), currentDeviceId);
                PreferenceUtils.setServerDeviceId(presenter.getContext(), response.getDeviceId());
            }

            PreferenceUtils.setCurrentUserId(presenter.getContext(), user.getId());
        }
    }
}
