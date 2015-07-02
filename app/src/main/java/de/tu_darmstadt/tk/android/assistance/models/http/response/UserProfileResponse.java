package de.tu_darmstadt.tk.android.assistance.models.http.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Wladimir Schmidt on 02.07.2015.
 */
public class UserProfileResponse {

    @SerializedName("firstname")
    @Expose
    private String firstname;

    @SerializedName("lastname")
    @Expose
    private String lastname;

    @SerializedName("primary_email")
    @Expose
    private String primaryEmail;

    @SerializedName("last_login")
    @Expose
    private Long lastLogin;

    @SerializedName("joined_since")
    @Expose
    private Long joinedSince;

    @SerializedName("services")
    @Expose
    private List<UserSocialService> socialServices;

    public UserProfileResponse() {
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public Long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Long getJoinedSince() {
        return joinedSince;
    }

    public void setJoinedSince(Long joinedSince) {
        this.joinedSince = joinedSince;
    }

    public List<UserSocialService> getSocialServices() {
        return socialServices;
    }

    public void setSocialServices(List<UserSocialService> socialServices) {
        this.socialServices = socialServices;
    }
}
