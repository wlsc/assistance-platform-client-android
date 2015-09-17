package de.tudarmstadt.informatik.tk.android.assistance.activity.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.FrameLayout;

import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activity.LoginActivity;
import de.tudarmstadt.informatik.tk.android.assistance.fragment.DrawerFragment;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.profile.ProfileResponse;
import de.tudarmstadt.informatik.tk.android.assistance.service.ServiceGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.service.UserService;
import de.tudarmstadt.informatik.tk.android.assistance.util.Constants;
import de.tudarmstadt.informatik.tk.android.assistance.util.PreferencesUtils;
import de.tudarmstadt.informatik.tk.android.assistance.util.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.util.UserUtils;
import de.tudarmstadt.informatik.tk.android.kraken.db.DatabaseManager;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbUser;
import de.tudarmstadt.informatik.tk.android.kraken.db.DbUserDao;
import de.tudarmstadt.informatik.tk.android.kraken.utils.DateUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Base activity for common stuff
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class DrawerActivity extends AppCompatActivity {

    private static final String TAG = DrawerActivity.class.getSimpleName();

    private boolean mBackButtonPressedOnce;

    protected Toolbar mToolbar;

    protected FrameLayout mFrameLayout;

    protected DrawerFragment mDrawerFragment;

    protected DrawerLayout mDrawerLayout;

    protected long mCurrentDeviceId = -1;

    public DrawerActivity() {
        this.mBackButtonPressedOnce = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_base);

        mToolbar = ButterKnife.findById(this, R.id.toolbar_actionbar);

        String userEmail = UserUtils.getUserEmail(getApplicationContext());

        mFrameLayout = ButterKnife.findById(this, R.id.container_frame);
        mDrawerLayout = ButterKnife.findById(this, R.id.drawer_layout);

        setSupportActionBar(mToolbar);

        mDrawerFragment = (DrawerFragment) getFragmentManager().findFragmentById(R.id.drawer_fragment);
        mDrawerFragment.setup(mDrawerLayout, mToolbar);

        // if user data was not cached, request new

        if (!userEmail.isEmpty()) {

            Log.d(TAG, "User email is not empty");

            String userFirstname = UserUtils.getUserFirstname(getApplicationContext());
            String userLastname = UserUtils.getUserLastname(getApplicationContext());

            Log.d(TAG, "User firstname: " + userFirstname + " lastname: " + userLastname);

            if (userFirstname.isEmpty() || userLastname.isEmpty()) {

                Log.d(TAG, "No user info found cached. Checking db...");

                DbUserDao userDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDbUserDao();

                DbUser user = userDao
                        .queryBuilder()
                        .where(DbUserDao.Properties.PrimaryEmail.eq(userEmail))
                        .limit(1)
                        .build()
                        .unique();

                // found cached user in the db
                if (user != null) {

                    Log.d(TAG, "Found user in the db");

                    UserUtils.saveUserFirstname(getApplicationContext(), user.getFirstname());
                    UserUtils.saveUserLastname(getApplicationContext(), user.getLastname());

                    mDrawerFragment.updateDrawer();
                } else {
                    // no user profile found -> request from server
                    Log.d(TAG, "No user found in db");

//                    if (!isUserProfileRequestSent) {
                    Log.d(TAG, "Requesting from server...");
                    requestUserProfile();
//                    }
                }
            } else {
                mDrawerFragment.updateDrawer();
            }
        } else {
            Log.d(TAG, "User email is EMPTY!");
        }
    }

    /**
     * Requests user profile information
     */
    private void requestUserProfile() {

        String userToken = UserUtils.getUserToken(getApplicationContext());

        UserService userservice = ServiceGenerator.createService(UserService.class);
        userservice.getUserProfileShort(userToken, new Callback<ProfileResponse>() {

            @Override
            public void success(ProfileResponse profileResponse, Response response) {

                if (profileResponse == null) {
                    return;
                }

                UserUtils.saveUserFirstname(getApplicationContext(), profileResponse.getFirstname());
                UserUtils.saveUserLastname(getApplicationContext(), profileResponse.getLastname());
                UserUtils.saveUserEmail(getApplicationContext(), profileResponse.getPrimaryEmail());

                persistLogin(profileResponse);
                mDrawerFragment.updateDrawer();
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorMessages(TAG, error);
            }
        });
    }

    /**
     * Updates existent user login or creates one in db
     *
     * @param profileResponse
     */
    private void persistLogin(ProfileResponse profileResponse) {

        DbUserDao userDao = DatabaseManager.getInstance(getApplicationContext()).getDaoSession().getDbUserDao();

        // check already available user in db
        DbUser user = userDao
                .queryBuilder()
                .where(DbUserDao.Properties.PrimaryEmail.eq(profileResponse.getPrimaryEmail()))
                .limit(1)
                .build()
                .unique();

        // check for user existence in the db
        if (user == null) {
            // no user found -> create one

            user = new DbUser();

            user.setFirstname(profileResponse.getFirstname());
            user.setLastname(profileResponse.getLastname());
            user.setPrimaryEmail(profileResponse.getPrimaryEmail());
            user.setJoinedSince(DateUtils.dateToISO8601String(new Date(profileResponse.getJoinedSince()), Locale.getDefault()));
            user.setLastLogin(DateUtils.dateToISO8601String(new Date(profileResponse.getLastLogin()), Locale.getDefault()));
            user.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

            userDao.insertOrReplace(user);
        } else {
            // found a user -> update for device and user information

            user.setFirstname(profileResponse.getFirstname());
            user.setLastname(profileResponse.getLastname());
            user.setPrimaryEmail(profileResponse.getPrimaryEmail());
            user.setJoinedSince(DateUtils.dateToISO8601String(new Date(profileResponse.getJoinedSince()), Locale.getDefault()));
            user.setLastLogin(DateUtils.dateToISO8601String(new Date(profileResponse.getLastLogin()), Locale.getDefault()));
//            user.setCreated(DateUtils.dateToISO8601String(new Date(), Locale.getDefault()));

            userDao.update(user);
        }
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
                    Toaster.showLong(getApplicationContext(), R.string.error_service_bad_request);
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
                mCurrentDeviceId = data.getLongExtra(Constants.INTENT_CURRENT_DEVICE_ID, -1);
                Log.d(TAG, "Current DB device id received: " + mCurrentDeviceId);
            }
        }
    }

}
