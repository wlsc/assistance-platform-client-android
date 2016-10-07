package de.tudarmstadt.informatik.tk.assistance.fragment.settings;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import de.tudarmstadt.informatik.tk.assistance.R.id;
import de.tudarmstadt.informatik.tk.assistance.R.layout;
import de.tudarmstadt.informatik.tk.assistance.R.string;
import de.tudarmstadt.informatik.tk.assistance.activity.SettingsActivity;
import de.tudarmstadt.informatik.tk.assistance.notification.Toaster;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.profile.ProfileResponseDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.profile.UpdateProfileRequestDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.model.api.user.profile.UserSocialServiceDto;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.ApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.provider.api.UserApiProvider;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;
import de.tudarmstadt.informatik.tk.assistance.util.PreferenceUtils;
import de.tudarmstadt.informatik.tk.assistance.util.ValidationUtils;
import rx.Subscriber;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 29.06.2015
 */
public class UserProfileSettingsFragment extends Fragment {

    static final String TAG = UserProfileSettingsFragment.class.getSimpleName();

    private static final String IMAGE_TYPE_FILTER = "image/*";
    private static final String USER_PIC_NAME = "user_pic";

    private Toolbar mParentToolbar;

    private String userToken;

    private Unbinder unbinder;

//    @Bind(R.id.userPicVIew)
//    protected CircularImageView userPicView;

    @BindView(id.firstname)
    protected AppCompatEditText firstnameText;

    private String firstname;

    @BindView(id.lastname)
    protected AppCompatEditText lastnameText;

    private String lastname;

    @BindView(id.social_account_google)
    protected AppCompatEditText socialAccountGoogleText;

    private String socialAccountGoogle;

    @BindView(id.social_account_facebook)
    protected AppCompatEditText socialAccountFacebookText;

    private String socialAccountFacebook;

    @BindView(id.social_account_live)
    protected AppCompatEditText socialAccountLiveText;

    private String socialAccountLive;

    @BindView(id.social_account_twitter)
    protected AppCompatEditText socialAccountTwitterText;

    private String socialAccountTwitter;

    @BindView(id.social_account_github)
    protected AppCompatEditText socialAccountGithubText;

    private String socialAccountGithub;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentToolbar = ((SettingsActivity) getActivity()).getToolBar();
        mParentToolbar.setTitle(string.settings_header_user_profile_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = null;

        // request user profile from server
        userToken = PreferenceUtils.getUserToken(getActivity());

        if (!userToken.isEmpty()) {

            view = inflater.inflate(layout.fragment_preference_user_profile, container, false);

            unbinder = ButterKnife.bind(this, view);

//            String filename = UserUtils.getUserPicFilename(getActivity());
//
//            if (!filename.isEmpty()) {
//                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + Config.USER_PIC_PATH + "/" + filename + ".jpg");
//
//                if (file.exists()) {
//                    Log.d(TAG, "File exists");
//
//                    Picasso.with(getActivity())
//                            .load(file)
//                            .placeholder(R.drawable.no_image)
//                            .into(userPicView);
//                } else {
//                    Log.d(TAG, "File NOT exists");
//                }
//            } else {
//                Log.d(TAG, "user pic filename NOT exists");
//                userPicView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.no_image));
//            }

            UserApiProvider userApi = ApiProvider.getInstance(getActivity()).getUserApiProvider();

            userApi.getUserProfileFull(userToken)
                    .subscribe(new Subscriber<ProfileResponseDto>() {

                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "Failed while getting full user profile");
                            Toaster.showLong(getActivity(),
                                    string.error_service_not_available);
                        }

                        @Override
                        public void onNext(ProfileResponseDto profileResponseDto) {

                            Log.d(TAG, "Successfully received the user profile!");

                            fillupFullUserProfile(profileResponseDto);
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
    void fillupFullUserProfile(ProfileResponseDto profileResponse) {

        firstnameText.setText(profileResponse.getFirstname());
        lastnameText.setText(profileResponse.getLastname());

        List<UserSocialServiceDto> socialServices = profileResponse.getSocialServices();

        if (!socialServices.isEmpty()) {
            String serviceName;

            for (UserSocialServiceDto service : socialServices) {
                if (service == null) {
                    continue;
                }

                serviceName = service.getName();

                if (serviceName.equals(UserSocialServiceDto.TYPE_GOOGLE)) {
                    socialAccountGoogleText.setText(serviceName);
                }

                if (serviceName.equals(UserSocialServiceDto.TYPE_FACEBOOK)) {
                    socialAccountFacebookText.setText(serviceName);
                }

                if (serviceName.equals(UserSocialServiceDto.TYPE_LIVE)) {
                    socialAccountLiveText.setText(serviceName);
                }

                if (serviceName.equals(UserSocialServiceDto.TYPE_TWITTER)) {
                    socialAccountTwitterText.setText(serviceName);
                }

                if (serviceName.equals(UserSocialServiceDto.TYPE_GITHUB)) {
                    socialAccountGithubText.setText(serviceName);
                }
            }
        }
    }

//    @OnClick(R.id.userPicVIew)
//    protected void onUserPhotoClicked() {
//        Log.d(TAG, "User clicked selection of an image");
//        pickImage();
//    }

    /*
    *   Starts intent to pick some image
     */
//    public void pickImage() {
//
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType(IMAGE_TYPE_FILTER);
//        startActivityForResult(intent, R.id.userPicVIew);
//    }

    @OnTextChanged({id.firstname,
            id.lastname,
            id.social_account_google,
            id.social_account_facebook,
            id.social_account_live,
            id.social_account_twitter,
            id.social_account_github})
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

            if (!ValidationUtils.isValidEmail(socialAccountGoogle)) {
                socialAccountGoogleText.setError(getString(string.error_invalid_email));
                socialAccountGoogleText.requestFocus();
                return false;
            }
        }

        socialAccountFacebook = socialAccountFacebookText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountFacebook)) {

            if (!ValidationUtils.isValidEmail(socialAccountFacebook)) {
                socialAccountFacebookText.setError(getString(string.error_invalid_email));
                socialAccountFacebookText.requestFocus();
                return false;
            }
        }

        socialAccountLive = socialAccountLiveText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountLive)) {

            if (!ValidationUtils.isValidEmail(socialAccountLive)) {
                socialAccountLiveText.setError(getString(string.error_invalid_email));
                socialAccountLiveText.requestFocus();
                return false;
            }
        }

        socialAccountTwitter = socialAccountTwitterText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountTwitter)) {

            if (!ValidationUtils.isValidEmail(socialAccountTwitter)) {
                socialAccountTwitterText.setError(getString(string.error_invalid_email));
                socialAccountTwitterText.requestFocus();
                return false;
            }
        }

        socialAccountGithub = socialAccountGithubText.getText().toString().trim();

        if (!TextUtils.isEmpty(socialAccountGithub)) {

            if (!ValidationUtils.isValidEmail(socialAccountGithub)) {
                socialAccountGithubText.setError(getString(string.error_invalid_email));
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

        UpdateProfileRequestDto request = new UpdateProfileRequestDto();

        request.setFirstname(firstname);
        request.setLastname(lastname);

        PreferenceUtils.setUserFirstname(getActivity(), firstname);
        PreferenceUtils.setUserLastname(getActivity(), lastname);

        List<UserSocialServiceDto> socialServices = new ArrayList<>();

        // GOOGLE
        UserSocialServiceDto googleService = new UserSocialServiceDto();
        googleService.setName(UserSocialServiceDto.TYPE_GOOGLE);
        googleService.setEmail(socialAccountGoogle);

        socialServices.add(googleService);

        // FACEBOOK
        UserSocialServiceDto facebookService = new UserSocialServiceDto();
        facebookService.setName(UserSocialServiceDto.TYPE_FACEBOOK);
        facebookService.setEmail(socialAccountFacebook);

        socialServices.add(facebookService);

        // LIVE
        UserSocialServiceDto liveService = new UserSocialServiceDto();
        liveService.setName(UserSocialServiceDto.TYPE_LIVE);
        liveService.setEmail(socialAccountLive);

        socialServices.add(liveService);

        // TWITTER
        UserSocialServiceDto twitterService = new UserSocialServiceDto();
        twitterService.setName(UserSocialServiceDto.TYPE_TWITTER);
        twitterService.setEmail(socialAccountTwitter);

        socialServices.add(twitterService);

        // GITHUB
        UserSocialServiceDto githubService = new UserSocialServiceDto();
        githubService.setName(UserSocialServiceDto.TYPE_GITHUB);
        githubService.setEmail(socialAccountGithub);

        socialServices.add(githubService);

        // set services
        request.setServices(socialServices);

        /**
         * SEND UPDATED USER PROFILE TO SERVER
         */
        UserApiProvider userApi = ApiProvider.getInstance(
                getActivity())
                .getUserApiProvider();

        userApi.updateUserProfile(userToken, request)
                .subscribe(new Subscriber<Void>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Failed while updating user profile");
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        Log.d(TAG, "Successfully updated user profile!");
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == R.id.userPicVIew && resultCode == Activity.RESULT_OK) {
//            if (data == null) {
//                Toaster.showLong(getActivity(), R.string.error_select_new_user_photo);
//                return;
//            }
//
//            String oldFilename = UserUtils.getUserPicFilename(getActivity());
//            Log.d(TAG, "old user pic filename: " + (oldFilename.isEmpty() ? "empty" : oldFilename));
//
//            // process selected image and show it to user
//            try {
//                Uri uri = data.getData();
//
//                InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
//
//                CommonUtils.saveFile(getActivity(), uri, oldFilename);
//
//                CircularImageView image = ButterKnife.findById(getActivity(), R.id.userPicVIew);
//                image.setImageDrawable(Drawable.createFromStream(inputStream, USER_PIC_NAME));
//
//            } catch (FileNotFoundException e) {
//                Log.e(TAG, "User pic file not found!");
//            }
//        }
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
        unbinder.unbind();
    }
}
