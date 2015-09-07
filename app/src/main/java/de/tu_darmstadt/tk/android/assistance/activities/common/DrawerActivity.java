package de.tu_darmstadt.tk.android.assistance.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.FrameLayout;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import de.greenrobot.dao.query.Query;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.LoginActivity;
import de.tu_darmstadt.tk.android.assistance.fragments.DrawerFragment;
import de.tu_darmstadt.tk.android.assistance.handlers.DrawerHandler;
import de.tu_darmstadt.tk.android.assistance.models.api.error.ErrorResponse;
import de.tu_darmstadt.tk.android.assistance.models.api.profile.ProfileResponse;
import de.tu_darmstadt.tk.android.assistance.services.ServiceGenerator;
import de.tu_darmstadt.tk.android.assistance.services.UserService;
import de.tu_darmstadt.tk.android.assistance.utils.Constants;
import de.tu_darmstadt.tk.android.assistance.utils.PreferencesUtils;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;
import de.tu_darmstadt.tk.android.assistance.utils.UserUtils;
import de.tudarmstadt.informatik.tk.kraken.android.sdk.db.User;
import de.tudarmstadt.informatik.tk.kraken.android.sdk.db.UserDao;
import de.tudarmstadt.informatik.tk.kraken.android.sdk.utils.DatabaseManager;
import de.tudarmstadt.informatik.tk.kraken.android.sdk.utils.DateUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Base activity for common stuff
 */
public class DrawerActivity extends AppCompatActivity implements DrawerHandler {

    private static final String TAG = DrawerActivity.class.getSimpleName();

    private boolean mBackButtonPressedOnce;

    private static boolean isUserProfileRequestSent = false;

    protected Toolbar mToolbar;

    protected FrameLayout mFrameLayout;

    protected DrawerFragment mDrawerFragment;

    protected DrawerLayout mDrawerLayout;

    protected String mUserEmail;

    protected long currentDeviceId = -1;

    public DrawerActivity() {
        this.mBackButtonPressedOnce = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_base);

        mToolbar = ButterKnife.findById(this, R.id.toolbar_actionbar);

        mUserEmail = UserUtils.getUserEmail(getApplicationContext());

        mFrameLayout = ButterKnife.findById(this, R.id.container_frame);
        mDrawerLayout = ButterKnife.findById(this, R.id.drawer_layout);

        setSupportActionBar(mToolbar);
        setupDrawer(mToolbar);

        // if user data was not cached, request new
        String userFirstname = UserUtils.getUserFirstname(getApplicationContext());
        String userLastname = UserUtils.getUserLastname(getApplicationContext());

        // TODO: optimize it to execute it only once!

        if (userFirstname.isEmpty() || userLastname.isEmpty()) {
            if (!isUserProfileRequestSent) {
                requestUserProfile();
            }
        } else {
            updateUserDrawerInfo();
        }
    }

    /**
     * Requests user profile information
     */
    private void requestUserProfile() {

        isUserProfileRequestSent = true;

        String userToken = UserUtils.getUserToken(getApplicationContext());

        UserService userservice = ServiceGenerator.createService(UserService.class);
        userservice.getUserProfileShort(userToken, new Callback<ProfileResponse>() {

            @Override
            public void success(ProfileResponse profileResponse, Response response) {

                isUserProfileRequestSent = false;

                String firstname = profileResponse.getFirstname();
                String lastname = profileResponse.getLastname();

                UserUtils.saveUserFirstname(getApplicationContext(), firstname);
                UserUtils.saveUserLastname(getApplicationContext(), lastname);

                updateUserLogin(profileResponse);
                updateUserDrawerInfo();
            }

            @Override
            public void failure(RetrofitError error) {
                isUserProfileRequestSent = false;
                showErrorMessages(TAG, error);
            }
        });
    }

    /**
     * Updates existent user login or creates one in db
     *
     * @param profileResponse
     */
    private void updateUserLogin(ProfileResponse profileResponse) {

        User user = new User();

        user.setFirstname(profileResponse.getFirstname());
        user.setLastname(profileResponse.getLastname());
        user.setPrimaryEmail(profileResponse.getPrimaryEmail());
        user.setJoinedSince(DateUtils.dateToISO8601String(new Date(profileResponse.getJoinedSince()), Locale.getDefault()));
        user.setLastLogin(DateUtils.dateToISO8601String(new Date(profileResponse.getLastLogin()), Locale.getDefault()));
        user.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

        UserDao userDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getUserDao();

        Query<User> userQuery = userDao
                .queryBuilder()
                .where(UserDao.Properties.PrimaryEmail.eq(profileResponse.getPrimaryEmail()))
                .limit(1)
                .build();

        List<User> users = userQuery.list();

        // check for user existance in the db
        if (users.size() == 0) {
            // no user found -> create one
            userDao.insert(user);
        } else {
            // found a user -> update for device and user information
            userDao.update(user);
        }
    }

    /**
     * Updates user information from preference
     */
    private void updateUserDrawerInfo() {

        String userFirstname = UserUtils.getUserFirstname(getApplicationContext());
        String userLastname = UserUtils.getUserLastname(getApplicationContext());
        String userEmail = UserUtils.getUserEmail(getApplicationContext());
        String userPicFilename = UserUtils.getUserPicFilename(getApplicationContext());

        mDrawerFragment.updateUserData(userFirstname + " " + userLastname, userEmail, userPicFilename);
    }

    /**
     * Setup navigation drawer
     *
     * @param mToolbar
     */
    protected void setupDrawer(Toolbar mToolbar) {

        mDrawerFragment = (DrawerFragment) getFragmentManager().findFragmentById(R.id.drawer_fragment);
        mDrawerFragment.setup(R.id.drawer_fragment, mDrawerLayout, mToolbar);
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

    @Override
    public void onBackPressed() {
        if (mDrawerFragment.isDrawerOpen()) {
            mDrawerFragment.closeDrawer();
        } else {

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == Constants.INTENT_CURRENT_DEVICE_ID_RESULT) {
            if (data.hasExtra(Constants.INTENT_CURRENT_DEVICE_ID)) {
                currentDeviceId = data.getLongExtra(Constants.INTENT_CURRENT_DEVICE_ID, -1);
                Log.d(TAG, "Current DB device id received: " + currentDeviceId);
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    }
}
