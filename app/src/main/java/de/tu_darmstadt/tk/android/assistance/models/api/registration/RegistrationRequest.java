package de.tu_darmstadt.tk.android.assistance.models.api.registration;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class RegistrationRequest {

    @SerializedName("email")
    @Expose
    private String userEmail;

    @SerializedName("password")
    @Expose
    private String password;

    public RegistrationRequest() {
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
