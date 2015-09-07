package de.tudarmstadt.informatik.tk.android.assistance.fragments.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.tudarmstadt.informatik.tk.android.assistance.Config;
import de.tudarmstadt.informatik.tk.android.assistance.R;
import de.tudarmstadt.informatik.tk.android.assistance.activities.SettingsActivity;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.profile.ProfileResponse;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.profile.UpdateProfileRequest;
import de.tudarmstadt.informatik.tk.android.assistance.models.api.profile.UserSocialService;
import de.tudarmstadt.informatik.tk.android.assistance.services.ServiceGenerator;
import de.tudarmstadt.informatik.tk.android.assistance.services.UserService;
import de.tudarmstadt.informatik.tk.android.assistance.utils.CommonUtils;
import de.tudarmstadt.informatik.tk.android.assistance.utils.InputValidation;
import de.tudarmstadt.informatik.tk.android.assistance.utils.Toaster;
import de.tudarmstadt.informatik.tk.android.assistance.utils.UserUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Wladimir Schmidt on 29.06.2015.
 */
public class UserProfileSettingsFragment extends Fragment {

    private static final String TAG = UserProfileSettingsFragment.class.getSimpleName();

    private static final String IMAGE_TYPE_FILTER = "image/*";
    private static final String USER_PIC_NAME = "user_pic";

    private Toolbar mParentToolbar;

    private String userToken;

    @Bind(R.id.userPicVIew)
    protected CircularImageView userPicView;

    @Bind(R.id.firstname)
    protected EditText firstnameText;

    private String firstname;

    @Bind(R.id.lastname)
    protected EditText lastnameText;

    private String lastname;

    @Bind(R.id.social_account_google)
    protected EditText socialAccountGoogleText;

    private String socialAccountGoogle;

    @Bind(R.id.social_account_facebook)
    protected EditText socialAccountFacebookText;

    private String socialAccountFacebook;

    @Bind(R.id.social_account_live)
    protected EditText socialAccountLiveText;

    private String socialAccountLive;

    @Bind(R.id.social_account_twitter)
    protected EditText socialAccountTwitterText;

    private String socialAccountTwitter;

    @Bind(R.id.social_account_github)
    protected EditText socialAccountGithubText;

    private String socialAccountGithub;

    public UserProfileSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(R.string.settings_header_user_profile_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = null;

        // request user profile from server
        userToken = UserUtils.getUserToken(getActivity().getApplicationContext());

        if (!userToken.isEmpty()) {

            view = inflater.inflate(R.layout.fragment_preference_user_profile, container, false);

            ButterKnife.bind(this, view);

            String filename = UserUtils.getUserPicFilename(getActivity().getApplicationContext());

            if (!filename.isEmpty()) {
                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + Config.USER_PIC_PATH + "/" + filename + ".jpg");

                if (file.exists()) {
                    Log.d(TAG, "File exists");

                    Picasso.with(getActivity().getApplicationContext())
                            .load(file)
                            .placeholder(R.drawable.no_image)
                            .into(userPicView);
                } else {
                    Log.d(TAG, "File NOT exists");
                }
            } else {
                Log.d(TAG, "user pic filename NOT exists");
                userPicView.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.no_image));
            }

            UserService userService = ServiceGenerator.createService(UserService.class);
            userService.getUserProfileFull(userToken, new Callback<ProfileResponse>() {

                @Override
                public void success(ProfileResponse profileResponse, Response response) {
                    Log.d(TAG, "Successfully received the user profile!");

                    fillupFullUserProfile(profileResponse);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d(TAG, "Failed while getting full user profile");
                }
            });
        }

        return view;
    }

    /**
     * Populate user profile fields
     *
     * @param profileResponse
     */
    private void fillupFullUserProfile(ProfileResponse profileResponse) {

        firstnameText.setText(profileResponse.getFirstname());
        lastnameText.setText(profileResponse.getLastname());

        List<UserSocialService> socialServices = profileResponse.getSocialServices();

        if (!socialServices.isEmpty()) {
            String serviceName = "";

            for (UserSocialService service : socialServices) {
                if (service == null) {
                    continue;
                }

                serviceName = service.getName();

                if (serviceName.equals(UserSocialService.TYPE_GOOGLE)) {
                    socialAccountGoogleText.setText(serviceName);
                }

                if (serviceName.equals(UserSocialService.TYPE_FACEBOOK)) {
                    socialAccountFacebookText.setText(serviceName);
                }

                if (serviceName.equals(UserSocialService.TYPE_LIVE)) {
                    socialAccountLiveText.setText(serviceName);
                }

                if (serviceName.equals(UserSocialService.TYPE_TWITTER)) {
                    socialAccountTwitterText.setText(serviceName);
                }

                if (serviceName.equals(UserSocialService.TYPE_GITHUB)) {
                    socialAccountGithubText.setText(serviceName);
                }
            }
        }
    }

    @OnClick(R.id.userPicVIew)
    protected void onUserPhotoClicked() {
        Log.d(TAG, "User clicked selection of an image");
        pickImage();
    }

    /*
    *   Starts intent to pick some image
     */
    public void pickImage() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(IMAGE_TYPE_FILTER);
        startActivityForResult(intent, R.id.userPicVIew);
    }

    @OnTextChanged({R.id.firstname,
            R.id.lastname,
            R.id.social_account_google,
            R.id.social_account_facebook,
            R.id.social_account_live,
            R.id.social_account_twitter,
            R.id.social_account_github})
    void onFocusChanged(CharSequence text) {
        Log.d(TAG, text.toString());
        isUserInputOK();
    }

    /**
     * Validates user input
     */
    private boolean isUserInputOK() {

        firstname = firstnameText.getText().toString().trim();
        lastname = lastnameText.getText().toString().trim();

        socialAccountGoogle = socialAccountGoogleText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountGoogle)) {

            if (!InputValidation.isValidEmail(socialAccountGoogle)) {
                socialAccountGoogleText.setError(getString(R.string.error_invalid_email));
                socialAccountGoogleText.requestFocus();
                return false;
            }
        }

        socialAccountFacebook = socialAccountFacebookText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountFacebook)) {

            if (!InputValidation.isValidEmail(socialAccountFacebook)) {
                socialAccountFacebookText.setError(getString(R.string.error_invalid_email));
                socialAccountFacebookText.requestFocus();
                return false;
            }
        }

        socialAccountLive = socialAccountLiveText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountLive)) {

            if (!InputValidation.isValidEmail(socialAccountLive)) {
                socialAccountLiveText.setError(getString(R.string.error_invalid_email));
                socialAccountLiveText.requestFocus();
                return false;
            }
        }

        socialAccountTwitter = socialAccountTwitterText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountTwitter)) {

            if (!InputValidation.isValidEmail(socialAccountTwitter)) {
                socialAccountTwitterText.setError(getString(R.string.error_invalid_email));
                socialAccountTwitterText.requestFocus();
                return false;
            }
        }

        socialAccountGithub = socialAccountGithubText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountGithub)) {

            if (!InputValidation.isValidEmail(socialAccountGithub)) {
                socialAccountGithubText.setError(getString(R.string.error_invalid_email));
                socialAccountGithubText.requestFocus();
                return false;
            }
        }

        return true;
    }

    /**
     * Saves user profile -> send request to server
     */
    private void updateUserProfile() {

        Log.d(TAG, "updateUserProfile() invoked");

        UpdateProfileRequest request = new UpdateProfileRequest();

        request.setFirstname(firstname);
        request.setLastname(lastname);

        UserUtils.saveUserFirstname(getActivity().getApplicationContext(), firstname);
        UserUtils.saveUserLastname(getActivity().getApplicationContext(), lastname);

        List<UserSocialService> socialServices = new ArrayList<>();

        // GOOGLE
        UserSocialService googleService = new UserSocialService();
        googleService.setName(UserSocialService.TYPE_GOOGLE);
        googleService.setEmail(socialAccountGoogle);

        socialServices.add(googleService);

        // FACEBOOK
        UserSocialService facebookService = new UserSocialService();
        facebookService.setName(UserSocialService.TYPE_FACEBOOK);
        facebookService.setEmail(socialAccountFacebook);

        socialServices.add(facebookService);

        // LIVE
        UserSocialService liveService = new UserSocialService();
        liveService.setName(UserSocialService.TYPE_LIVE);
        liveService.setEmail(socialAccountLive);

        socialServices.add(liveService);

        // TWITTER
        UserSocialService twitterService = new UserSocialService();
        twitterService.setName(UserSocialService.TYPE_TWITTER);
        twitterService.setEmail(socialAccountTwitter);

        socialServices.add(twitterService);

        // GITHUB
        UserSocialService githubService = new UserSocialService();
        githubService.setName(UserSocialService.TYPE_GITHUB);
        githubService.setEmail(socialAccountGithub);

        socialServices.add(githubService);

        // set services
        request.setServices(socialServices);

        /**
         * SEND UPDATED USER PROFILE TO SERVER
         */
        UserService userService = ServiceGenerator.createService(UserService.class);
        userService.updateUserProfile(userToken, request, new Callback<Void>() {

            @Override
            public void success(Void aVoid, Response response) {
                Log.d(TAG, "Successfully updated user profile!");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "Failed while updating user profile");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == R.id.userPicVIew && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toaster.showLong(getActivity(), R.string.error_select_new_user_photo);
                return;
            }

            String oldFilename = UserUtils.getUserPicFilename(getActivity().getApplicationContext());
            Log.d(TAG, "old user pic filename: " + (oldFilename.isEmpty() ? "empty" : oldFilename));

            // process selected image and show it to user
            try {
                Uri uri = data.getData();

                InputStream inputStream = getActivity().getApplicationContext().getContentResolver().openInputStream(uri);

                CommonUtils.saveFile(getActivity().getApplicationContext(), uri, oldFilename);

                CircularImageView image = ButterKnife.findById(getActivity(), R.id.userPicVIew);
                image.setImageDrawable(Drawable.createFromStream(inputStream, USER_PIC_NAME));

            } catch (FileNotFoundException e) {
                Log.e(TAG, "User pic file not found!");
            }
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "User pressed back and profile is updating...");
        updateUserProfile();

        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
