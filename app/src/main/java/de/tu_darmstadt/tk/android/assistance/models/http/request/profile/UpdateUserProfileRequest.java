package de.tu_darmstadt.tk.android.assistance.models.http.request.profile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 04.07.2015.
 */
public class UpdateUserProfileRequest {

    @SerializedName("token")
    @Expose
    private String userToken;

    @SerializedName("profile")
    @Expose
    private UserProfile userProfile;

    public UpdateUserProfileRequest() {
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
}
