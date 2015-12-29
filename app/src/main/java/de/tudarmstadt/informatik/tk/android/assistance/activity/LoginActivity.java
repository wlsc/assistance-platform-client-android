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

import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.login.LoginPresenter;
import de.tudarmstadt.informatik.tk.android.assistance.presenter.login.LoginPresenterImpl;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.android.assistance.util.CommonUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
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

        setPresenter(new LoginPresenterImpl(this));
        presenter.doInitView();
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
                        presenter.getSplashView();
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
    }

    @Override
    public void showUserTokenInvalid() {
        Toaster.showLong(this, R.string.error_user_token_not_valid);
        Log.d(TAG, "User token is INVALID!");
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
    public void askPermissions(Set<String> permsToAsk) {
        // empty
    }
}