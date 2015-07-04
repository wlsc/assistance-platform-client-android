package de.tu_darmstadt.tk.android.assistance.models.http.request.profile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import de.tu_darmstadt.tk.android.assistance.models.http.UserSocialService;
import de.tu_darmstadt.tk.android.assistance.models.http.response.UserProfileResponse;

/**
 * Created by Wladimir Schmidt on 04.07.2015.
 */
public class UserProfile {

    @SerializedName("firstname")
    @Expose
    private String firstname;

    @SerializedName("lastname")
    @Expose
    private String lastname;

    @SerializedName("services")
    @Expose
    private List<UserSocialService> services;

    public UserProfile() {
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

    public List<UserSocialService> getServices() {
        return services;
    }

    public void setServices(List<UserSocialService> services) {
        this.services = services;
    }
}
