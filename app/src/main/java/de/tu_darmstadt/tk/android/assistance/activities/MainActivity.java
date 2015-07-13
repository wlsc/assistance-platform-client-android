package de.tu_darmstadt.tk.android.assistance.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.common.DrawerActivity;
import de.tu_darmstadt.tk.android.assistance.models.http.response.UserProfileResponse;
import de.tu_darmstadt.tk.android.assistance.services.ServiceGenerator;
import de.tu_darmstadt.tk.android.assistance.services.UserService;
import de.tu_darmstadt.tk.android.assistance.utils.Constants;
import de.tu_darmstadt.tk.android.assistance.utils.PreferencesUtils;
import de.tu_darmstadt.tk.android.assistance.utils.Toaster;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * User home
 */
public class MainActivity extends DrawerActivity {

    private String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_main, frameLayout);

        setTitle(R.string.main_activity_title);

        requestUserProfile();
    }

    /**
     * Requests user profile information
     */
    private void requestUserProfile() {

        String userToken = PreferencesUtils.readFromPreferences(getApplicationContext(), Constants.PREF_USER_TOKEN, "");

        UserService userservice = ServiceGenerator.createService(UserService.class);
        userservice.getUserProfileShort(userToken, new Callback<UserProfileResponse>() {

            @Override
            public void success(UserProfileResponse userProfileResponse, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // drawer item was select

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
        super.onDestroy();
        ButterKnife.unbind(this);
        Log.d(TAG, "onDestroy -> unbound resources");
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}
