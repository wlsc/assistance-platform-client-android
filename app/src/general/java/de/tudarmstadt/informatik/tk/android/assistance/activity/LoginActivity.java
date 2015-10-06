package de.tudarmstadt.informatik.tk.android.assistance.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.error.ErrorResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.login.LoginRequest;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.login.LoginResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.login.UserDevice;
import de.tudarmstadt.informatik.tk.android.assistance.service.UserService;
import de.tudarmstadt.informatik.tk.android.assistance.util.CommonUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.HardwareUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.InputValidation;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferencesUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.assistance.view.SplashView;
import de.tudarmstadt.informatik.tk.android.kraken.KrakenConfig;
import de.tudarmstadt.informatik.tk.android.kraken.communication.ServiceGenerator;
import de.tudarmstadt.informatik.tk.android.kraken.db.DatabaseManager;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbDevice;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbDeviceDao;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbUser;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbUserDao;
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
public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = LoginActivity.class.getSimpleName();

    @Bind(R.id.email)
    protected AutoCompleteTextView mEmailTextView;

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

    private DbUserDao userDao;

    private DbDeviceDao deviceDao;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userToken = UserUtils.getUserToken(getApplicationContext());

        if (!userToken.isEmpty()) {
            Log.d(TAG, "User token found. Launching main activity!");
            loadMainActivity();
            return;
        } else {
            Log.d(TAG, "User token NOT found");
        }

        // first -> load splash screen
        hideSystemUI();

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
                        launchMainView(savedInstanceState);
                    }
                });
            }
        });

        // show splash screen
        setContentView(mSplashView);
    }

    /**
     * Setup main activity
     *
     * @param savedInstanceState
     */
    public void launchMainView(Bundle savedInstanceState) {

        showSystemUI();

        setContentView(R.layout.activity_login);
        setTitle(R.string.login_activity_title);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            Long userId = intent.getLongExtra("user_id", -1);
            if (userId != -1) {
                Toaster.showLong(this, R.string.register_successful);
            }
        }

        populateAutoComplete();
    }

    /**
     * Get user's emails
     */
    private void populateAutoComplete() {
        getSupportLoaderManager().initLoader(0, null, this);
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
        if (!TextUtils.isEmpty(password) && !InputValidation.isPasswordLengthValid(password)) {
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
            if (!InputValidation.isValidEmail(email)) {
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
            doLogin();
        }
    }

    /**
     * Login procedure
     */
    private void doLogin() {

        showProgress(true);

        String userEmail = mEmailTextView.getText().toString();
        UserUtils.saveUserEmail(getApplicationContext(), userEmail);

        if (userDao == null) {
            userDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDbUserDao();
        }

        DbUser user = userDao
                .queryBuilder()
                .where(DbUserDao.Properties.PrimaryEmail.eq(userEmail))
                .limit(1)
                .build()
                .unique();

        Long serverDeviceId = null;

        if (user != null) {

            UserUtils.saveCurrentUserId(getApplicationContext(), user.getId());
            UserUtils.saveUserEmail(getApplicationContext(), user.getPrimaryEmail());
            UserUtils.saveUserFirstname(getApplicationContext(), user.getFirstname());
            UserUtils.saveUserLastname(getApplicationContext(), user.getLastname());

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
        LoginRequest request = new LoginRequest();
        request.setUserEmail(email);
        request.setPassword(password);

        UserDevice userDevice = new UserDevice();

        if (serverDeviceId != null) {
            userDevice.setServerId(serverDeviceId);
        }

        userDevice.setOs(KrakenConfig.PLATFORM_NAME);
        userDevice.setOsVersion(HardwareUtils.getAndroidVersion());
        userDevice.setBrand(HardwareUtils.getDeviceBrandName());
        userDevice.setModel(HardwareUtils.getDeviceModelName());
        userDevice.setDeviceId(HardwareUtils.getAndroidId(this));

        request.setDevice(userDevice);

        /**
         * Logging in the user
         */
        UserService userService = ServiceGenerator.createService(UserService.class);
        userService.loginUser(request, new Callback<LoginResponse>() {

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

        if (userDao == null) {
            userDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDbUserDao();
        }

        if (deviceDao == null) {
            deviceDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDbDeviceDao();
        }

        DbUser user = userDao
                .queryBuilder()
                .where(DbUserDao.Properties.Token.eq(loginResponse.getUserToken()))
                .limit(1)
                .build()
                .unique();


        // check if that user was already saved in the system
        if (user == null) {
            // no such user found -> insert new user into db

            DbUser newUser = new DbUser();

            newUser.setToken(loginResponse.getUserToken());
            newUser.setPrimaryEmail(email);
            newUser.setToken(loginResponse.getUserToken());
            newUser.setCreated(createdDate);

            long newUserId = userDao.insert(newUser);

            UserUtils.saveCurrentUserId(getApplicationContext(), newUserId);

            // saving device info into db

            DbDevice device = new DbDevice();
            device.setServerDeviceId(loginResponse.getDeviceId());
            device.setOs(KrakenConfig.PLATFORM_NAME);
            device.setOsVersion(HardwareUtils.getAndroidVersion());
            device.setBrand(HardwareUtils.getDeviceBrandName());
            device.setModel(HardwareUtils.getDeviceModelName());
            device.setDeviceIdentifier(HardwareUtils.getAndroidId(this));
            device.setCreated(createdDate);
            device.setUserId(newUserId);

            long currentDeviceId = deviceDao.insert(device);

            UserUtils.saveCurrentDeviceId(getApplicationContext(), currentDeviceId);
            UserUtils.saveServerDeviceId(getApplicationContext(), loginResponse.getDeviceId());

        } else {

            List<DbDevice> userDevices = user.getDbDeviceList();

            String currentAndroidId = HardwareUtils.getAndroidId(this);
            boolean isDeviceAlreadyCreated = false;

            for (DbDevice device : userDevices) {
                if (device.getDeviceIdentifier().equals(currentAndroidId)) {
                    isDeviceAlreadyCreated = true;

                    UserUtils.saveCurrentDeviceId(getApplicationContext(), device.getId());
                    UserUtils.saveServerDeviceId(getApplicationContext(), device.getServerDeviceId());

                    break;
                }
            }

            if (!isDeviceAlreadyCreated) {
                // no such device found in db -> insert new

                DbDevice device = new DbDevice();
                device.setServerDeviceId(loginResponse.getDeviceId());
                device.setOs(KrakenConfig.PLATFORM_NAME);
                device.setOsVersion(HardwareUtils.getAndroidVersion());
                device.setBrand(HardwareUtils.getDeviceBrandName());
                device.setModel(HardwareUtils.getDeviceModelName());
                device.setDeviceIdentifier(HardwareUtils.getAndroidId(this));
                device.setCreated(createdDate);
                device.setUserId(user.getId());

                long currentDeviceId = deviceDao.insert(device);

                UserUtils.saveCurrentDeviceId(getApplicationContext(), currentDeviceId);
                UserUtils.saveServerDeviceId(getApplicationContext(), loginResponse.getDeviceId());
            }

            UserUtils.saveCurrentUserId(getApplicationContext(), user.getId());
        }
    }

    /**
     * Loads next user screen
     *
     * @param loginApiResponse
     */
    private void saveLoginGoNext(LoginResponse loginApiResponse) {

        String token = loginApiResponse.getUserToken();
        UserUtils.saveUserEmail(getApplicationContext(), mEmailTextView.getText().toString());

        if (InputValidation.isUserTokenValid(token)) {
            Log.d(TAG, "Token is valid. Proceeding with login...");

//            showProgress(false);

            saveLoginIntoDb(loginApiResponse);

            UserUtils.saveUserToken(getApplicationContext(), token);

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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

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

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.
     * <p>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context,
     * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param cursor The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {

        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailTextView.setAdapter(adapter);
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
        userDao = null;
        deviceDao = null;
        Log.d(TAG, "onDestroy -> unbound resources");
        super.onDestroy();
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
     * Processes error response from server
     *
     * @param TAG
     * @param retrofitError
     */
    protected void showErrorMessages(String TAG, RetrofitError retrofitError) {

        Response response = retrofitError.getResponse();

        if (response != null) {

            int httpCode = response.getStatus();

            switch (httpCode) {
                case 400:
                    ErrorResponse errorResponse = (ErrorResponse) retrofitError.getBodyAs(ErrorResponse.class);
                    errorResponse.setStatusCode(httpCode);

                    Integer apiResponseCode = errorResponse.getCode();
                    String apiMessage = errorResponse.getMessage();
                    int httpResponseCode = errorResponse.getStatusCode();

                    Log.d(TAG, "Response status: " + httpResponseCode);
                    Log.d(TAG, "Response code: " + apiResponseCode);
                    Log.d(TAG, "Response message: " + apiMessage);

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
}

