package de.tu_darmstadt.tk.android.assistance.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.LoginActivity;
import de.tu_darmstadt.tk.android.assistance.fragments.DrawerFragment;
import de.tu_darmstadt.tk.android.assistance.handlers.DrawerHandler;
import de.tu_darmstadt.tk.android.assistance.models.http.HttpErrorCode;
import de.tu_darmstadt.tk.android.assistance.models.http.response.ErrorResponse;
import de.tu_darmstadt.tk.android.assistance.models.http.response.UserProfileResponse;
import de.tu_darmstadt.tk.android.assistance.services.ServiceGenerator;
import de.tu_darmstadt.tk.android.assistance.services.UserService;
import de.tu_darmstadt.tk.android.assistance.utils.Constants;
import de.tu_darmstadt.tk.android.assistance.utils.PreferencesUtils;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;
import de.tu_darmstadt.tk.android.assistance.utils.UserUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Base activity for common stuff
 */
public class DrawerActivity extends AppCompatActivity implements DrawerHandler {

    private static final String TAG = DrawerActivity.class.getSimpleName();

    private boolean mBackButtonPressedOnce;

    protected Toolbar mToolbar;

    protected FrameLayout mFrameLayout;

    protected DrawerFragment mDrawerFragment;

    protected DrawerLayout mDrawerLayout;

    protected String mUserEmail;

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

        if (userFirstname.isEmpty() || userLastname.isEmpty()) {
            requestUserProfile();
        } else {
            updateUserDrawerInfo();
        }
    }

    /**
     * Requests user profile information
     */
    private void requestUserProfile() {

        String userToken = UserUtils.getUserToken(getApplicationContext());

        UserService userservice = ServiceGenerator.createService(UserService.class);
        userservice.getUserProfileShort(userToken, new Callback<UserProfileResponse>() {

            @Override
            public void success(UserProfileResponse userProfileResponse, Response response) {

                String firstname = userProfileResponse.getFirstname();
                String lastname = userProfileResponse.getLastname();

                UserUtils.saveUserFirstname(getApplicationContext(), firstname);
                UserUtils.saveUserLastname(getApplicationContext(), lastname);

                updateUserDrawerInfo();
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorMessages(TAG, error);
            }
        });
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
                    HttpErrorCode.ErrorCode apiErrorType = HttpErrorCode.fromCode(apiResponseCode);

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
    public void onNavigationDrawerItemSelected(int position) {
    }
}
