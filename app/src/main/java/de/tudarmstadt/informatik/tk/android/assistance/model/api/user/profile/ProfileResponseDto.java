package de.tudarmstadt.informatik.tk.android.assistance.model.api.user.profile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Wladimir Schmidt on 02.07.2015.
 */
public class ProfileResponseDto {

    @SerializedName("firstName")
    @Expose
    private String firstname;

    @SerializedName("lastName")
    @Expose
    private String lastname;

    @SerializedName("email")
    @Expose
    private String primaryEmail;

    @SerializedName("lastLogin")
    @Expose
    private Long lastLogin;

    @SerializedName("joinedSince")
    @Expose
    private Long joinedSince;

    @SerializedName("services")
    @Expose
    private List<UserSocialServiceDto> socialServices;

    public ProfileResponseDto() {
    }

    public ProfileResponseDto(String firstname, String lastname, String primaryEmail, Long lastLogin, Long joinedSince, List<UserSocialServiceDto> socialServices) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.primaryEmail = primaryEmail;
        this.lastLogin = lastLogin;
        this.joinedSince = joinedSince;
        this.socialServices = socialServices;
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

    public List<UserSocialServiceDto> getSocialServices() {
        return socialServices;
    }

    public void setSocialServices(List<UserSocialServiceDto> socialServices) {
        this.socialServices = socialServices;
    }

    @Override
    public String toString() {
        return "ProfileResponseDto{" +
                "firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", primaryEmail='" + primaryEmail + '\'' +
                ", lastLogin=" + lastLogin +
                ", joinedSince=" + joinedSince +
                ", socialServices=" + socialServices +
                '}';
    }
}
