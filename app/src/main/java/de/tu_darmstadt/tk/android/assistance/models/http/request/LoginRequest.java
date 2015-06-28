package de.tu_darmstadt.tk.android.assistance.models.http.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class LoginRequest {

    @SerializedName("email")
    @Expose
    private String userEmail;

    @SerializedName("password")
    @Expose
    private String password;

    public LoginRequest() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
