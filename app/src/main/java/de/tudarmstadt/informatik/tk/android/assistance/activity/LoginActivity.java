package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
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
import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.login.LoginResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.login.LoginPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.login.LoginPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.Config;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbDevice;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbUser;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.DateUtils;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.CommonUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.HardwareUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.LoginView;
import de.tudarmstadt.informatik.tk.android.assistance.view.SplashView;

/**
 * A login screen that offers login via email/password
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class LoginActivity extends
        AppCompatActivity implements
        LoginView {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Bind(R.id.email)
    protected EditText mEmailTextView;

    @Bind(R.id.password)
    protected EditText mPasswordView;

    @Bind(R.id.login_progress)
    protected View mProgressView;

    @Bind(R.id.login_form)
    protected ScrollView mLoginFormView;

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

    private LoginPresenter presenter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new LoginPresenterImpl(this);
    }

    /**
     * Inits login view
     */
    @Override
    public void initLogin() {

        // just init EventBus there
        HarvesterServiceProvider.getInstance(getApplicationContext());

        String userToken = PreferenceUtils.getUserToken(getApplicationContext());

        presenter.checkAutologin(userToken);
    }

    @Override
    public void loadMainActivity() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void setPresenter(LoginPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setView(this);
    }

    /**
     * Setup view
     */
    @Override
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
     * Saves user device into database
     *
     * @param loginResponse
     */
    private void saveLoginIntoDb(LoginResponseDto loginResponse) {

        String createdDate = DateUtils.dateToISO8601String(new Date(), Locale.getDefault());

        DbUser user = daoProvider.getUserDao().getByToken(loginResponse.getUserToken());

        // check if that user was already saved in the system
        if (user == null) {
            // no such user found -> insert new user into db

            DbUser newUser = new DbUser();

            newUser.setToken(loginResponse.getUserToken());
            newUser.setPrimaryEmail(email);
            newUser.setCreated(createdDate);

            long newUserId = daoProvider.getUserDao().insert(newUser);

            PreferenceUtils.setCurrentUserId(getApplicationContext(), newUserId);

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

            long currentDeviceId = daoProvider.getDeviceDao().insert(device);

            PreferenceUtils.setCurrentDeviceId(getApplicationContext(), currentDeviceId);
            PreferenceUtils.setServerDeviceId(getApplicationContext(), loginResponse.getDeviceId());

        } else {

            List<DbDevice> userDevices = user.getDbDeviceList();

            String currentAndroidId = HardwareUtils.getAndroidId(this);
            boolean isDeviceAlreadyCreated = false;

            for (DbDevice device : userDevices) {
                if (device.getDeviceIdentifier().equals(currentAndroidId)) {
                    isDeviceAlreadyCreated = true;

                    PreferenceUtils.setCurrentDeviceId(getApplicationContext(), device.getId());
                    PreferenceUtils.setServerDeviceId(getApplicationContext(), device.getServerDeviceId());

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

                long currentDeviceId = daoProvider.getDeviceDao().insert(device);

                PreferenceUtils.setCurrentDeviceId(getApplicationContext(), currentDeviceId);
                PreferenceUtils.setServerDeviceId(getApplicationContext(), loginResponse.getDeviceId());
            }

            PreferenceUtils.setCurrentUserId(getApplicationContext(), user.getId());
        }
    }

    @Override
    public void showProgress(final boolean isShowing) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(isShowing ? View.GONE : View.VISIBLE);

        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                isShowing ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(isShowing ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(isShowing ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                isShowing ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(isShowing ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void setLoginButtonEnabled(boolean isEnabled) {

        if (mLoginButton != null) {
            mLoginButton.setEnabled(isEnabled);
        }
    }

    @Override
    public void hideKeyboard() {
        CommonUtils.hideKeyboard(this, getCurrentFocus());
    }

    @Override
    public void loadSplashView() {

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

    @Override
    public void saveUserCredentialsToPreference(String token) {

        PreferenceUtils.setUserToken(getApplicationContext(), token);
        PreferenceUtils.setUserEmail(getApplicationContext(), mEmailTextView.getText().toString().trim());
        PreferenceUtils.setUserPassword(getApplicationContext(), mPasswordView.getText().toString().trim());
    }

    @OnClick(R.id.sign_in_button)
    protected void onUserLogin() {

        presenter.attemptLogin(
                mEmailTextView.getText().toString().trim(),
                mPasswordView.getText().toString().trim());
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

        presenter.attemptLogin(
                mEmailTextView.getText().toString().trim(),
                mPasswordView.getText().toString().trim());

        return true;
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

    @Override
    public void startLoginActivity() {
        // we are already here
    }

    @Override
    public void clearErrors() {
        mEmailTextView.setError(null);
        mPasswordView.setError(null);
    }

    @Override
    public void showServiceUnavailable() {
        Toaster.showLong(getApplicationContext(), R.string.error_service_not_available);
    }

    @Override
    public void showServiceTemporaryUnavailable() {
        Toaster.showLong(getApplicationContext(), R.string.error_server_temporary_unavailable);
    }

    @Override
    public void showUnknownErrorOccurred() {
        Toaster.showLong(getApplicationContext(), R.string.error_unknown);
    }
}