package de.tu_darmstadt.tk.android.assistance.fragments.settings;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tu_darmstadt.tk.android.assistance.R;
import de.tu_darmstadt.tk.android.assistance.activities.SettingsActivity;
import de.tu_darmstadt.tk.android.assistance.models.RoundImage;
import de.tu_darmstadt.tk.android.assistance.models.http.UserSocialService;
import de.tu_darmstadt.tk.android.assistance.models.http.request.profile.UpdateUserProfileRequest;
import de.tu_darmstadt.tk.android.assistance.models.http.request.profile.UserProfile;
import de.tu_darmstadt.tk.android.assistance.services.ServiceGenerator;
import de.tu_darmstadt.tk.android.assistance.services.UserService;
import de.tu_darmstadt.tk.android.assistance.utils.InputValidation;
import de.tu_darmstadt.tk.android.assistance.utils.UserUtils;
import hugo.weaving.DebugLog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Wladimir Schmidt on 29.06.2015.
 */
public class UserProfileSettingsFragment extends Fragment {

    private static final String TAG = UserProfileSettingsFragment.class.getSimpleName();

    private Toolbar mParentToolbar;

    @Bind(R.id.userPhoto)
    protected ImageView userPhotoView;

    @Bind(R.id.firstname)
    protected EditText firstnameText;

    @Bind(R.id.lastname)
    protected EditText lastnameText;

    @Bind(R.id.social_account_google)
    protected EditText socialAccountGoogleText;

    @Bind(R.id.social_account_facebook)
    protected EditText socialAccountFacebookText;

    @Bind(R.id.social_account_live)
    protected EditText socialAccountLiveText;

    @Bind(R.id.social_account_twitter)
    protected EditText socialAccountTwitterText;

    @Bind(R.id.social_account_github)
    protected EditText socialAccountGithubText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(R.string.settings_header_user_profile_title);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_preference_user_profile, container, false);

        ButterKnife.bind(this, view);

        Bitmap userPhotoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_user_pic);
        userPhotoView.setImageDrawable(new RoundImage(userPhotoBitmap));

        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    updateUserProfile();
                    getActivity().finish();
                    return true;
                }

                return false;
            }
        });


        return view;
    }

    /*
    *   Saves user profile -> send request to server
     */
    @DebugLog
    private void updateUserProfile() {

        Log.d(TAG, "updateUserProfile() invoked");

        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setUserToken(UserUtils.getUserToken(getActivity().getApplicationContext()));

        UserProfile userProfile = new UserProfile();

        String firstname = firstnameText.getText().toString().trim();
        String lastname = lastnameText.getText().toString().trim();

        userProfile.setFirstname(firstname);
        userProfile.setLastname(lastname);

        List<UserSocialService> socialServices = new ArrayList<>();

        // GOOGLE

        String googleEmail = socialAccountGoogleText.getText().toString();

        if (!TextUtils.isEmpty(googleEmail)) {

            if (!InputValidation.isValidEmail(googleEmail)) {
                socialAccountGoogleText.setError(getString(R.string.error_invalid_email));
                socialAccountGoogleText.requestFocus();
                return;
            }

            UserSocialService googleService = new UserSocialService();
            googleService.setName(UserSocialService.TYPE_GOOGLE);
            googleService.setEmail(googleEmail);

            socialServices.add(googleService);
        }

        // FACEBOOK

        String facebookEmail = socialAccountFacebookText.getText().toString();

        if (!TextUtils.isEmpty(facebookEmail)) {

            if (!InputValidation.isValidEmail(facebookEmail)) {
                socialAccountFacebookText.setError(getString(R.string.error_invalid_email));
                socialAccountFacebookText.requestFocus();
                return;
            }

            UserSocialService facebookService = new UserSocialService();
            facebookService.setName(UserSocialService.TYPE_FACEBOOK);
            facebookService.setEmail(facebookEmail);

            socialServices.add(facebookService);
        }

        // LIVE

        String liveEmail = socialAccountLiveText.getText().toString();

        if (!TextUtils.isEmpty(liveEmail)) {

            if (!InputValidation.isValidEmail(liveEmail)) {
                socialAccountLiveText.setError(getString(R.string.error_invalid_email));
                socialAccountLiveText.requestFocus();
                return;
            }

            UserSocialService liveService = new UserSocialService();
            liveService.setName(UserSocialService.TYPE_LIVE);
            liveService.setEmail(liveEmail);

            socialServices.add(liveService);
        }

        // TWITTER

        String twitterEmail = socialAccountTwitterText.getText().toString();

        if (!TextUtils.isEmpty(twitterEmail)) {

            if (!InputValidation.isValidEmail(twitterEmail)) {
                socialAccountTwitterText.setError(getString(R.string.error_invalid_email));
                socialAccountTwitterText.requestFocus();
                return;
            }

            UserSocialService twitterService = new UserSocialService();
            twitterService.setName(UserSocialService.TYPE_TWITTER);
            twitterService.setEmail(twitterEmail);

            socialServices.add(twitterService);
        }

        // GITHUB

        String githubEmail = socialAccountGithubText.getText().toString();

        if (!TextUtils.isEmpty(githubEmail)) {

            if (!InputValidation.isValidEmail(githubEmail)) {
                socialAccountGithubText.setError(getString(R.string.error_invalid_email));
                socialAccountGithubText.requestFocus();
                return;
            }

            UserSocialService githubService = new UserSocialService();
            githubService.setName(UserSocialService.TYPE_GITHUB);
            githubService.setEmail(githubEmail);

            socialServices.add(githubService);
        }

        userProfile.setServices(socialServices);
        request.setUserProfile(userProfile);

        // SEND UPDATE TO SERVER
        UserService userService = ServiceGenerator.createService(UserService.class);
        userService.updateUserProfile(request, new Callback<Void>() {

            @Override
            public void success(Void aVoid, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
