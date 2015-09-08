package de.tudarmstadt.informatik.tk.android.assistance.models.api.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * User login api request
 * <p/>
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class LoginRequest {

    @SerializedName("email")
    @Expose
    private String userEmail;

    @SerializedName("password")
    @Expose
    private String password;

    @SerializedName("device")
    @Expose
    private UserDevice device;

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

    public UserDevice getDevice() {
        return this.device;
    }

    public void setDevice(UserDevice device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "userEmail='" + userEmail + '\'' +
                ", password='" + password + '\'' +
                ", device=" + device +
                '}';
    }
}
