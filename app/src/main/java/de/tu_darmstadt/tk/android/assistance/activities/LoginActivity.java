package de.tu_darmstadt.tk.android.assistance.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.BaseActivity;
import de.tu_darmstadt.tk.android.assistance.models.http.request.LoginRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.response.ErrorResponse;
import de.tu_darmstadt.tk.android.assistance.models.http.response.LoginResponse;
import de.tu_darmstadt.tk.android.assistance.services.LoginService;
import de.tu_darmstadt.tk.android.assistance.services.ServiceGenerator;
import de.tu_darmstadt.tk.android.assistance.utils.Constants;
import de.tu_darmstadt.tk.android.assistance.utils.InputValidation;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;
import de.tu_darmstadt.tk.android.assistance.views.SplashView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements LoaderCallbacks<Cursor> {

    private String TAG = LoginActivity.class.getName();

    private final LoginActivity mainThis = this;
    private Handler uiThreadHandler = new Handler();
    private SplashView splashView;

    @Bind(R.id.email)
    protected AutoCompleteTextView mEmailView;
    @Bind(R.id.password)
    protected EditText mPasswordView;
    @Bind(R.id.login_progress)
    protected View mProgressView;
    @Bind(R.id.login_form)
    protected ScrollView mLoginFormView;
    @Bind(R.id.tvRegister)
    protected TextView registerLink;
    @Bind(R.id.tvPasswordReset)
    protected TextView resetPassLink;
    @Bind(R.id.sign_in_button)
    protected Button mLoginButton;

    // SOCIAL BUTTONS
    @Bind(R.id.ibFacebookLogo)
    protected ImageButton ibFacebookLogo;
    @Bind(R.id.ibGooglePlusLogo)
    protected ImageButton ibGooglePlusLogo;
    @Bind(R.id.ibLiveLogo)
    protected ImageButton ibLiveLogo;
    @Bind(R.id.ibTwitterLogo)
    protected ImageButton ibTwitterLogo;
    @Bind(R.id.ibGithubLogo)
    protected ImageButton ibGithubLogo;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userToken = getUserToken();
        if (userToken != null && !userToken.isEmpty()) {
            loadMainActivity();
            return;
        }

        // first -> load splash screen
        hideSystemUI();

        // init splash screen view
        if (splashView == null) {
            splashView = new SplashView(this);
        }

        // Set an event handler on the SplashView object, so that as soon
        // as it completes drawing we are
        // informed.  In response to that cue, we will *then* put up the main view,
        // replacing the content view of the main activity with that main view.
        splashView.setSplashScreenEvent(new SplashView.SplashScreenEvent() {
            @Override
            public void onSplashDrawComplete() {
                uiThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        launchMainView(mainThis, savedInstanceState);
                    }
                });
            }
        });

        // show splash screen
        setContentView(splashView);
    }

    /**
     * Setup main activity
     *
     * @param activity
     * @param savedInstanceState
     */
    public void launchMainView(LoginActivity activity, Bundle savedInstanceState) {

        showSystemUI();

        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            Long userId = savedInstanceState.getLong("user_id");
            if (userId != null) {
                Toaster.showLong(this, R.string.register_successful);
            }
        }

        populateAutoComplete();
    }

    /**
     * Get user's emails
     */
    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // disable button to reduce flood of requests
        mLoginButton.setEnabled(false);

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean isAnyErrors = false;
        View focusView = null;

        // check for password
        if (!TextUtils.isEmpty(password) && !InputValidation.isPasswordLengthValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            isAnyErrors = true;
        }

        // check for email address
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            isAnyErrors = true;
        } else if (!InputValidation.isValidEmail(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            isAnyErrors = true;
        }

        if (isAnyErrors) {
            // enables login button
            mLoginButton.setEnabled(true);
            focusView.requestFocus();
        } else {
            doRegisterUser(email, password);
        }
    }

    private void doRegisterUser(String email, String password) {

        showProgress(true);

        // forming a login request
        LoginRequest request = new LoginRequest();
        request.setUserEmail(email);
        request.setPassword(password);

        // calling api service
        LoginService service = ServiceGenerator.createService(LoginService.class);
        service.loginUser(request, new Callback<LoginResponse>() {

            @Override
            public void success(LoginResponse apiResponse, Response response) {
                saveLoginAndShowNextScreen(apiResponse);
                Log.d(TAG, "User token received: " + apiResponse.getUserToken());
            }

            @Override
            public void failure(RetrofitError error) {

                // enables login button
                mLoginButton.setEnabled(true);

                ErrorResponse errorResponse = (ErrorResponse) error.getBodyAs(ErrorResponse.class);
                errorResponse.setStatusCode(error.getResponse().getStatus());

                handleError(errorResponse, TAG);
            }
        });
    }

    /**
     * Loads next user screen
     *
     * @param apiResponse
     */
    private void saveLoginAndShowNextScreen(LoginResponse apiResponse) {
        String token = apiResponse.getUserToken();

        if (InputValidation.isUserTokenValid(token)) {
            Log.d(TAG, "Token is valid. Proceeding with login...");

            showProgress(false);
            saveUserToken(token);
            loadMainActivity();

        } else {
            Toaster.showLong(this, R.string.error_user_token_not_valid);
            Log.d(TAG, "Token is INVALID.");
        }
    }

    /**
     * Saves token into SharedPreferences
     *
     * @param token
     */
    private void saveUserToken(String token) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putString(Constants.PREF_USER_TOKEN, token).apply();
    }

    /**
     * Returns user token saved in SharedPreferences
     *
     * @return
     */
    private String getUserToken() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String userToken = sp.getString(Constants.PREF_USER_TOKEN, "");
        return userToken;
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
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

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Disables back button for user
     * and starts main activity
     */
    private void loadMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void hideSystemUI() {

        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ButterKnife.unbind(this);
    }
}
