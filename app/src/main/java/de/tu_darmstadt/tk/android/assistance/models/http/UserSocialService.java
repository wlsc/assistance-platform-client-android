package de.tu_darmstadt.tk.android.assistance.models.http;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 02.07.2015.
 */
public class UserSocialService {

    public static final String TYPE_GOOGLE = "Google";
    public static final String TYPE_FACEBOOK = "Facebook";
    public static final String TYPE_LIVE = "Live";
    public static final String TYPE_TWITTER = "Twitter";
    public static final String TYPE_GITHUB = "Github";

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("firstname")
    @Expose
    private String firstname;

    @SerializedName("lastname")
    @Expose
    private String lastname;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("updated")
    @Expose
    private Long updated;

    @SerializedName("created")
    @Expose
    private Long created;

    public UserSocialService() {
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }
}
