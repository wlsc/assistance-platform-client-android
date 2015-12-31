package de.tudarmstadt.informatik.tk.android.assistance.model.api.social;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 31.12.2015
 */
public class FacebookOAuthResponse {

    @SerializedName("id")
    @Expose
    private Long id;

    @SerializedName("first_name")
    @Expose
    private String firstname;

    @SerializedName("last_name")
    @Expose
    private String lastname;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("gender")
    @Expose
    private String gender;

    public FacebookOAuthResponse(Long id, String firstname, String lastname, String email, String gender) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.gender = gender;
    }

    public Long getId() {
        return this.id;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public String getEmail() {
        return this.email;
    }

    public String getGender() {
        return this.gender;
    }

    @Override
    public String toString() {
        return "FacebookOAuthResponse{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}