package de.tudarmstadt.informatik.tk.assistance.controller.login;

import java.net.NoRouteToHostException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tudarmstadt.informatik.tk.assistance.controller.CommonControllerImpl;
import de.tudarmstadt.informatik.tk.assistance.handler.OnResponseHandler;
import de.tudarmstadt.informatik.tk.assistance.handler.OnUserValidHandler;
import de.tudarmstadt.informatik.tk.assistance.presenter.login.LoginPresenter;
import de.tudarmstadt.informatik.tk.assistance.sdk.Config;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbDevice;
import de.tudarmstadt.informatik.tk.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.login.LoginRequestDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.login.LoginResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.login.UserDeviceDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.api.LoginApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.DateUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.HardwareUtils;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;

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
        UserDeviceDto userDevice = new UserDeviceDto();

        if (serverDeviceId != null) {
            userDevice.setServerId(serverDeviceId);
        }

        userDevice.setOs(Config.PLATFORM_NAME);
        userDevice.setOsVersion(HardwareUtils.getAndroidVersion());
        userDevice.setBrand(HardwareUtils.getDeviceBrandName());
        userDevice.setModel(HardwareUtils.getDeviceModelName());
        userDevice.setDeviceId(HardwareUtils.getAndroidId(presenter.getContext()));

        LoginRequestDto loginRequest = new LoginRequestDto(email, password, userDevice);

        PreferenceUtils.setUserEmail(presenter.getContext(), email);

        /**
         * Logging in the user
         */
        LoginApiProvider loginApiProvider = apiProvider.getLoginApiProvider();

        loginApiProvider.loginUser(loginRequest)
                .subscribe(new Subscriber<LoginResponseDto>() {

                    @Override
                    public void onCompleted() {
                        // do nothing
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            handler.onError((HttpException) e);
                        }

                        if(e instanceof NoRouteToHostException){
                            handler.onError((NoRouteToHostException) e);
                        }
                    }

                    @Override
                    public void onNext(LoginResponseDto response) {
                        handler.onSuccess(response);
                    }
                });
    }

    @Override
    public void saveLoginGoNext(LoginResponseDto loginApiResponse, OnUserValidHandler handler) {

        saveLoginIntoDb(loginApiResponse);
        handler.showMainActivity();
    }

    @Override
    public void saveLoginIntoDb(LoginResponseDto response) {

        String userToken = response.getUserToken();
        String userEmail = PreferenceUtils.getUserEmail(presenter.getContext());
        String createdDate = DateUtils.dateToISO8601String(new Date(), Locale.getDefault());

        DbUser user = daoProvider.getUserDao().getByEmail(userEmail);

        // check if that user was already saved in the system
        if (user == null) {
            // no such user found -> insert new user into db

            DbUser newUser = new DbUser();

            newUser.setToken(userToken);
            newUser.setPrimaryEmail(userEmail);
            newUser.setCreated(createdDate);

            long newUserId = daoProvider.getUserDao().insert(newUser);

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

            // renewing token
            user.setToken(userToken);

            daoProvider.getUserDao().update(user);

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
        }

        PreferenceUtils.setUserToken(presenter.getContext(), userToken);
    }
}