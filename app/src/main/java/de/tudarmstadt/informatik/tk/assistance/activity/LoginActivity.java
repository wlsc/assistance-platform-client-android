package de.tudarmstadt.informatik.tk.assistance.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import de.tudarmstadt.informatik.tk.assistance.Constants;
import de.tudarmstadt.informatik.tk.assistance.R;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.presenter.login.LoginPresenter;
import de.tudarmstadt.informatik.tk.assistance.presenter.login.LoginPresenterImpl;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.AppUtils;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.LogWrapper;
import de.tudarmstadt.informatik.tk.assistance.util.CommonUtils;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.view.LoginView;
import de.tudarmstadt.informatik.tk.assistance.view.SplashView;

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

    private static boolean wasInitialized = false;

    /**
     * The Analytics singleton. The field is set in onCreate method override when the application
     * class is initially created.
     */
    private static GoogleAnalytics analytics;

    /**
     * The default app tracker. The field is from onCreate callback when the application is
     * initially created.
     */
    private static Tracker tracker;

    /**
     * Access to the global Analytics singleton. If this method returns null you forgot to either
     * set android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.analytics field in onCreate method override.
     */
    public static GoogleAnalytics getAnalytics() {
        return analytics;
    }

    /**
     * The default app tracker. If this method returns null you forgot to either set
     * android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.tracker field in onCreate method override.
     */
    public static Tracker getTracker() {
        return tracker;
    }

    @Bind(R.id.email)
    protected EditText mEmailTextView;

    @Bind(R.id.password)
    protected EditText mPasswordView;

    @Bind(R.id.login_progress)
    protected ContentLoadingProgressBar mProgressView;

    @Bind(R.id.login_form)
    protected NestedScrollView mLoginFormView;

    @Bind(R.id.sign_in_button)
    protected AppCompatButton mLoginButton;

    // SOCIAL BUTTONS
    @Bind(R.id.ibFacebookLogo)
    protected AppCompatImageButton mFacebookLogo;

    @Bind(R.id.ibGooglePlusLogo)
    protected AppCompatImageButton mGooglePlusLogo;

    @Bind(R.id.ibLiveLogo)
    protected AppCompatImageButton mLiveLogo;

    @Bind(R.id.ibTwitterLogo)
    protected AppCompatImageButton mTwitterLogo;

    @Bind(R.id.ibGithubLogo)
    protected AppCompatImageButton mGithubLogo;

    private boolean mBackButtonPressedOnce;

    private Handler uiThreadHandler = new Handler();
    private SplashView mSplashView;

    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!wasInitialized) {
            android.util.Log.d(TAG, "Initializing...");
            initGoogleAnalytics();
            initLogging();
            wasInitialized = true;
        }

        setPresenter(new LoginPresenterImpl(this));
        presenter.initView();
    }

    @Override
    public void loadMainActivity() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ActivityCompat.startActivity(this, intent, null);
        finish();
    }

    @Override
    public void setPresenter(LoginPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setView(this);
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

        mSplashView.setSplashScreenEvent(() -> uiThreadHandler.post(presenter::getSplashView));

        // show splash screen
        setContentView(mSplashView);
    }

    @Override
    public void showSystemUI() {
        CommonUtils.showSystemUI(getWindow());
    }

    @Override
    public void setContent() {

        setContentView(R.layout.activity_login);
        setTitle(R.string.login_activity_title);

        ButterKnife.bind(this);
    }

    @Override
    public void setDebugViewInformation() {
        mEmailTextView.setText("test123@test.de");
        mPasswordView.setText("test123");
    }

    @Override
    public void requestFocus(View view) {
        view.requestFocus();
    }

    @Override
    public void showErrorPasswordInvalid() {
        mPasswordView.setError(getString(R.string.error_invalid_password));
        mPasswordView.requestFocus();
    }

    @Override
    public void showErrorEmailRequired() {
        mEmailTextView.setError(getString(R.string.error_field_required));
        mEmailTextView.requestFocus();
    }

    @Override
    public void showErrorEmailInvalid() {
        mEmailTextView.setError(getString(R.string.error_invalid_email));
        mEmailTextView.requestFocus();
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
        ActivityCompat.startActivity(this, intent, null);
    }

    @OnClick(R.id.tvPasswordReset)
    protected void onPasswordResetPressed() {

        Toaster.showLong(this, R.string.feature_is_under_construction);
//        Intent intent = new Intent(this, ResetPasswordActivity.class);
//        ActivityCompat.startActivity(this, intent, null);
    }

    @OnClick(R.id.ibFacebookLogo)
    protected void onFacebookLogoPressed() {
        Toast.makeText(this, R.string.feature_is_under_construction, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.ibGooglePlusLogo)
    protected void onGooglePlusLogoPressed() {
        Toast.makeText(this, R.string.feature_is_under_construction, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.ibLiveLogo)
    protected void onLiveLogoPressed() {
        Toast.makeText(this, R.string.feature_is_under_construction, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.ibTwitterLogo)
    protected void onTwitterLogoPressed() {
        Toast.makeText(this, R.string.feature_is_under_construction, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.ibGithubLogo)
    protected void onGithubLogoPressed() {
        Toast.makeText(this, R.string.feature_is_under_construction, Toast.LENGTH_SHORT).show();
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

        new Handler().postDelayed(() -> mBackButtonPressedOnce = false, Constants.BACK_BUTTON_DELAY_MILLIS);
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        mSplashView = null;
        uiThreadHandler = null;
        super.onDestroy();
    }

    @Override
    public void initView() {

        // just init EventBus there
        HarvesterServiceProvider.getInstance(getApplicationContext());

        presenter.checkAutologin(PreferenceUtils.getUserToken(getApplicationContext()));
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

    @Override
    public void showUserForbidden() {
        Toaster.showLong(getApplicationContext(), R.string.error_user_login_not_valid);
    }

    @Override
    public void showActionProhibited() {
        // empty
    }

    @Override
    public void showRetryLaterNotification() {
        Toaster.showLong(getApplicationContext(), R.string.error_service_retry_later);
    }

    @Override
    public void askPermissions(Set<String> permsRequired, Set<String> permsOptional) {
        // empty
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Initializes logging
     */
    public void initLogging() {

        boolean isDebugEnabled = AppUtils.isDebug(getApplicationContext());

        LogWrapper logWrapper = new LogWrapper();
        Log.setDebug(isDebugEnabled);
        Log.setLogNode(logWrapper);

        Log.i(TAG, "Ready");
    }

    /**
     * Initialize Google Analytics
     */
    private void initGoogleAnalytics() {

        // initialize Google Analytics
        analytics = GoogleAnalytics.getInstance(this);

        // load config from xml file
        tracker = analytics.newTracker(R.xml.analytics_global_config);

    }
}