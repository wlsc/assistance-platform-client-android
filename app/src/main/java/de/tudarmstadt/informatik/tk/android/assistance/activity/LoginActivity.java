package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import de.tudarmstadt.informatik.tk.android.assistance.BuildConfig;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint.UserEndpoint;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.login.LoginRequest;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.login.LoginResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.login.UserDevice;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.util.CommonUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.HardwareUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferencesUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.ValidationUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.SplashView;
import de.tudarmstadt.informatik.tk.android.kraken.Config;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbDevice;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbUser;
import de.tudarmstadt.informatik.tk.android.kraken.model.api.endpoint.EndpointGenerator;
import de.tudarmstadt.informatik.tk.android.kraken.provider.DaoProvider;
import de.tudarmstadt.informatik.tk.android.kraken.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.kraken.util.DateUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A login screen that offers login via email/password
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class LoginActivity extends AppCompatActivity {

    private final String TAG = LoginActivity.class.getSimpleName();

    @Bind(R.id.email)
    protected EditText mEmailTextView;

    @Bind(R.id.password)
    protected EditText mPasswordView;

    @Bind(R.id.login_progress)
    protected View mProgressView;

    @Bind(R.id.login_form)
    protected ScrollView mLoginFormView;

    @Bind(R.id.tvRegister)
    protected TextView mRegisterLink;

    @Bind(R.id.tvPasswordReset)
    protected TextView mResetPassLink;

    @Bind(R.id.sign_in_button)
    protected Button mLoginButton;

    // SOCIAL BUTTONS
    @Bind(R.id.ibFacebookLogo)
    protected ImageButton mFacebookLogo;

    @Bind(R.id.ibGooglePlusLogo)
    protected ImageButton mGooglePlusLogo;

    @Bind(R.id.ibLiveLogo)
    protected ImageButton mLiveLogo;

    @Bind(R.id.ibTwitterLogo)
    protected ImageButton mTwitterLogo;

    @Bind(R.id.ibGithubLogo)
    protected ImageButton mGithubLogo;

    private boolean mBackButtonPressedOnce;

    private Handler uiThreadHandler = new Handler();
    private SplashView mSplashView;

    private String email;
    private String password;

    private DaoProvider daoProvider;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (daoProvider == null) {
            daoProvider = DaoProvider.getInstance(getApplicationContext());
        }

        // just init EventBus there
        HarvesterServiceProvider.getInstance(getApplicationContext());

//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this);
//        }

//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
//            checkLocationPermissionGranted();
//        } else {
        // proceed with login screen
        initLogin();
//        }
    }

    /**
     * Checks read contacts permission
     */
//    private void checkReadContactsPermissionGranted() {
//
//        boolean isReadContactsGranted = PermissionUtils
//                .getInstance(getApplicationContext())
//                .isPermissionGranted(Manifest.permission.READ_CONTACTS);
//
//        if (isReadContactsGranted) {
//
//            Log.d(TAG, "READ_CONTACTS permission was granted.");
//
//            EventBus.getDefault().post(new PermissionGrantedEvent(Manifest.permission.READ_CONTACTS));
//
//        } else {
//
//            Log.d(TAG, "READ_CONTACTS permission NOT granted!");
//
//            // check if explanation is needed for this permission
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.READ_CONTACTS)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//                Toaster.showLong(getApplicationContext(), R.string.permission_is_mandatory);
//            }
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_CONTACTS},
//                    Config.PERMISSIONS_REQUEST_READ_CONTACTS);
//
//        }
//    }

    /**
     * Checks location permission
     */
//    private void checkLocationPermissionGranted() {
//
//        boolean isLocationGranted = PermissionUtils
//                .getInstance(getApplicationContext())
//                .isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION);
//
//        if (isLocationGranted) {
//
//            Log.d(TAG, "COARSE_LOCATION permission was granted.");
//
//            EventBus.getDefault().post(new PermissionGrantedEvent(Manifest.permission.ACCESS_COARSE_LOCATION));
//
//        } else {
//
//            Log.d(TAG, "COARSE_LOCATION permission NOT granted!");
//
//            // check if explanation is needed for this permission
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//                Toaster.showLong(getApplicationContext(), R.string.permission_is_mandatory);
//            }
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                    Config.PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
//
//        }
//    }

    /**
     * Inits login view
     */
    private void initLogin() {

        String userToken = PreferencesUtils.getUserToken(getApplicationContext());

        if (userToken.isEmpty()) {
            Log.d(TAG, "User token NOT found");
            Log.d(TAG, "Searching for autologin...");

            String savedEmail = PreferencesUtils.getUserEmail(getApplicationContext());
            String savedPassword = PreferencesUtils.getUserPassword(getApplicationContext());

            if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
                Log.d(TAG, "Found email/password entries saved. Doing autologin...");

                email = savedEmail;
                password = savedPassword;

                doLogin();
                return;
            }
        } else {
            Log.d(TAG, "User token found. Launching main activity!");
            loadMainActivity();
            return;
        }

        // first -> load splash screen
        CommonUtils.hideSystemUI(getWindow());

        // init splash screen view
        if (mSplashView == null) {
            mSplashView = new SplashView(this);
        }

        // Set an event handler on the SplashView object, so that as soon
        // as it completes drawing we are
        // informed.  In response to that cue, we will *then* put up the main view,
        // replacing the content view of the main activity with that main view.
        mSplashView.setSplashScreenEvent(new SplashView.SplashScreenEvent() {
            @Override
            public void onSplashDrawComplete() {
                uiThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showView();
                    }
                });
            }
        });

        // show splash screen
        setContentView(mSplashView);
    }

    /**
     * Setup view
     */
    public void showView() {

        CommonUtils.showSystemUI(getWindow());

        setContentView(R.layout.activity_login);
        setTitle(R.string.login_activity_title);

        ButterKnife.bind(this);

        if (BuildConfig.DEBUG) {
            mEmailTextView.setText("test123@test.de");
            mPasswordView.setText("test123");
        }

        Intent intent = getIntent();

        if (intent != null) {
            Long userId = intent.getLongExtra("user_id", -1);
            if (userId != -1) {
                Toaster.showLong(this, R.string.register_successful);
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        CommonUtils.hideKeyboard(this, getCurrentFocus());

        // disable button to reduce flood of requests
        if (mLoginButton != null) {
            mLoginButton.setEnabled(false);
        }

        // Reset errors.
        mEmailTextView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        email = mEmailTextView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean isAnyErrors = false;
        View focusView = null;

        // check for password
        if (!TextUtils.isEmpty(password) && !ValidationUtils.isPasswordLengthValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            isAnyErrors = true;
        }

        // check for email address
        if (TextUtils.isEmpty(email)) {
            mEmailTextView.setError(getString(R.string.error_field_required));
            focusView = mEmailTextView;
            isAnyErrors = true;
        } else {
            if (!ValidationUtils.isValidEmail(email)) {
                mEmailTextView.setError(getString(R.string.error_invalid_email));
                focusView = mEmailTextView;
                isAnyErrors = true;
            }
        }

        if (isAnyErrors) {

            // enables login button
            if (mLoginButton != null) {
                mLoginButton.setEnabled(true);
            }

            focusView.requestFocus();

            // show again the keyboard
//            CommonUtils.showKeyboard(this, focusView);

        } else {
            showProgress(true);
            doLogin();
        }
    }

    /**
     * Login procedure
     */
    private void doLogin() {

        DbUser user = daoProvider.getUserDao().getUserByEmail(email);

        Long serverDeviceId = null;

        if (user != null) {

            PreferencesUtils.setCurrentUserId(getApplicationContext(), user.getId());
            PreferencesUtils.setUserEmail(getApplicationContext(), user.getPrimaryEmail());
            PreferencesUtils.setUserFirstname(getApplicationContext(), user.getFirstname());
            PreferencesUtils.setUserLastname(getApplicationContext(), user.getLastname());

            String currentAndroidId = HardwareUtils.getAndroidId(this);

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
        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setUserEmail(email);
        loginRequest.setPassword(password);

        UserDevice userDevice = new UserDevice();

        if (serverDeviceId != null) {
            userDevice.setServerId(serverDeviceId);
        }

        userDevice.setOs(Config.PLATFORM_NAME);
        userDevice.setOsVersion(HardwareUtils.getAndroidVersion());
        userDevice.setBrand(HardwareUtils.getDeviceBrandName());
        userDevice.setModel(HardwareUtils.getDeviceModelName());
        userDevice.setDeviceId(HardwareUtils.getAndroidId(this));

        loginRequest.setDevice(userDevice);

        /**
         * Logging in the user
         */
        UserEndpoint userEndpoint = EndpointGenerator.getInstance(getApplicationContext()).create(UserEndpoint.class);
        userEndpoint.loginUser(loginRequest, new Callback<LoginResponse>() {

            @Override
            public void success(LoginResponse apiResponse, Response response) {
                saveLoginGoNext(apiResponse);
                Log.d(TAG, "User token received: " + apiResponse.getUserToken());
            }

            @Override
            public void failure(RetrofitError error) {

                // enables login button
                if (mLoginButton != null) {
                    mLoginButton.setEnabled(true);
                }

                showErrorMessages(TAG, error);
                showProgress(false);
            }
        });
    }

    /**
     * Saves user device into database
     *
     * @param loginResponse
     */
    private void saveLoginIntoDb(LoginResponse loginResponse) {

        String createdDate = DateUtils.dateToISO8601String(new Date(), Locale.getDefault());

        DbUser user = daoProvider.getUserDao().getUserByToken(loginResponse.getUserToken());

        // check if that user was already saved in the system
        if (user == null) {
            // no such user found -> insert new user into db

            DbUser newUser = new DbUser();

            newUser.setToken(loginResponse.getUserToken());
            newUser.setPrimaryEmail(email);
            newUser.setCreated(createdDate);

            long newUserId = daoProvider.getUserDao().insertUser(newUser);

            PreferencesUtils.setCurrentUserId(getApplicationContext(), newUserId);

            // saving device info into db

            DbDevice device = new DbDevice();
            device.setServerDeviceId(loginResponse.getDeviceId());
            device.setOs(Config.PLATFORM_NAME);
            device.setOsVersion(HardwareUtils.getAndroidVersion());
            device.setBrand(HardwareUtils.getDeviceBrandName());
            device.setModel(HardwareUtils.getDeviceModelName());
            device.setDeviceIdentifier(HardwareUtils.getAndroidId(this));
            device.setCreated(createdDate);
            device.setUserId(newUserId);

            long currentDeviceId = daoProvider.getDeviceDao().insertDevice(device);

            PreferencesUtils.setCurrentDeviceId(getApplicationContext(), currentDeviceId);
            PreferencesUtils.setServerDeviceId(getApplicationContext(), loginResponse.getDeviceId());

        } else {

            List<DbDevice> userDevices = user.getDbDeviceList();

            String currentAndroidId = HardwareUtils.getAndroidId(this);
            boolean isDeviceAlreadyCreated = false;

            for (DbDevice device : userDevices) {
                if (device.getDeviceIdentifier().equals(currentAndroidId)) {
                    isDeviceAlreadyCreated = true;

                    PreferencesUtils.setCurrentDeviceId(getApplicationContext(), device.getId());
                    PreferencesUtils.setServerDeviceId(getApplicationContext(), device.getServerDeviceId());

                    break;
                }
            }

            if (!isDeviceAlreadyCreated) {
                // no such device found in db -> insert new

                DbDevice device = new DbDevice();
                device.setServerDeviceId(loginResponse.getDeviceId());
                device.setOs(Config.PLATFORM_NAME);
                device.setOsVersion(HardwareUtils.getAndroidVersion());
                device.setBrand(HardwareUtils.getDeviceBrandName());
                device.setModel(HardwareUtils.getDeviceModelName());
                device.setDeviceIdentifier(HardwareUtils.getAndroidId(this));
                device.setCreated(createdDate);
                device.setUserId(user.getId());

                long currentDeviceId = daoProvider.getDeviceDao().insertDevice(device);

                PreferencesUtils.setCurrentDeviceId(getApplicationContext(), currentDeviceId);
                PreferencesUtils.setServerDeviceId(getApplicationContext(), loginResponse.getDeviceId());
            }

            PreferencesUtils.setCurrentUserId(getApplicationContext(), user.getId());
        }
    }

    /**
     * Loads next user screen
     *
     * @param loginApiResponse
     */
    private void saveLoginGoNext(LoginResponse loginApiResponse) {

        String token = loginApiResponse.getUserToken();

        if (ValidationUtils.isUserTokenValid(token)) {
            Log.d(TAG, "Token is valid. Proceeding with login...");

            saveLoginIntoDb(loginApiResponse);

            PreferencesUtils.setUserToken(getApplicationContext(), token);
            PreferencesUtils.setUserEmail(getApplicationContext(), email);
            PreferencesUtils.setUserPassword(getApplicationContext(), password);

            loadMainActivity();

        } else {
            Toaster.showLong(this, R.string.error_user_token_not_valid);
            Log.d(TAG, "User token is INVALID!");
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);

        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

    }

    @OnClick(R.id.sign_in_button)
    protected void onUserLogin() {
        attemptLogin();
    }

    @OnClick(R.id.tvRegister)
    protected void onRegisterPressed() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.tvPasswordReset)
    protected void onPasswordResetPressed() {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.ibFacebookLogo)
    protected void onFacebookLogoPressed() {
        Toast.makeText(this, "oauth tbd", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.ibGooglePlusLogo)
    protected void onGooglePlusLogoPressed() {
        Toast.makeText(this, "oauth tbd", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.ibLiveLogo)
    protected void onLiveLogoPressed() {
        Toast.makeText(this, "oauth tbd", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.ibTwitterLogo)
    protected void onTwitterLogoPressed() {
        Toast.makeText(this, "oauth tbd", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.ibGithubLogo)
    protected void onGithubLogoPressed() {
        Toast.makeText(this, "oauth tbd", Toast.LENGTH_SHORT).show();
    }

    @OnEditorAction(R.id.email)
    protected boolean onEditorAction(KeyEvent key) {
        attemptLogin();
        return true;
    }

    /**
     * Disables back button for user
     * and starts main activity
     */
    private void loadMainActivity() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {

        if (mBackButtonPressedOnce) {
            super.onBackPressed();
            return;
        }

        mBackButtonPressedOnce = true;

        Toaster.showLong(this, R.string.action_back_button_pressed_once);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mBackButtonPressedOnce = false;
            }
        }, Constants.BACK_BUTTON_DELAY_MILLIS);
    }

    @Override
    protected void onDestroy() {

        ButterKnife.unbind(this);
        mSplashView = null;
        uiThreadHandler = null;

        Log.d(TAG, "onDestroy -> unbound resources");

        super.onDestroy();
    }

    /**
     * Processes error response from server
     *
     * @param TAG
     * @param retrofitError
     */
    protected void showErrorMessages(String TAG, RetrofitError retrofitError) {

        if (retrofitError.getKind() == RetrofitError.Kind.NETWORK) {
            Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
            return;
        }

        Response response = retrofitError.getResponse();

        if (response != null) {

            int httpCode = response.getStatus();

            switch (httpCode) {
                case 400:
                    Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
                    break;
                case 401:
                    Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
                    PreferencesUtils.clearUserCredentials(getApplicationContext());
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
                case 404:
                    Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
                    break;
                case 503:
                    Toaster.showLong(getApplicationContext(), R.string.error_server_temporary_unavailable);
                    break;
                default:
                    Toaster.showLong(getApplicationContext(), R.string.error_unknown);
                    break;
            }
        } else {
            Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this);
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        if (EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().unregister(this);
//        }
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//
//        switch (requestCode) {
//
//            case Config.PERMISSIONS_REQUEST_READ_CONTACTS:
//
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    Log.d(TAG, "User granted permission");
//
//                    EventBus.getDefault().post(new PermissionGrantedEvent(Manifest.permission.READ_CONTACTS));
//
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//
//                    Log.d(TAG, "User DENIED permission!");
//
//                    Toaster.showLong(getApplicationContext(), R.string.permission_is_mandatory);
//
//                    // TODO: show crucial permission view
//                    finish();   // for now
//                }
//
//                break;
//            case Config.PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:
//
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    Log.d(TAG, "User granted permission");
//
//                    EventBus.getDefault().post(new PermissionGrantedEvent(Manifest.permission.ACCESS_COARSE_LOCATION));
//
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//
//                    Log.d(TAG, "User DENIED permission!");
//
//                    Toaster.showLong(getApplicationContext(), R.string.permission_is_mandatory);
//
//                    // TODO: show crucial permission view
//                    finish();   // for now
//                }
//
//                break;
//
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }

    /**
     * On permission granted event
     *
     * @param event
     */
//    public void onEvent(PermissionGrantedEvent event) {
//
//        String permission = event.getPermission();
//
//        Log.d(TAG, "Permission granted: " + permission);
//
//        if (permission == null) {
//            return;
//        }
//
//        if (permission.equals(Manifest.permission.READ_CONTACTS)) {
//            checkLocationPermissionGranted();
//        }
//
//        if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
//
//            // proceed with login screen
//            initLogin();
//        }
//    }
}

