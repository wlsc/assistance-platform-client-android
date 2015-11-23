package de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * User login api request
 * <p/>
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class LoginRequestDto {

    @SerializedName("email")
    @Expose
    private String userEmail;

    @SerializedName("password")
    @Expose
    private String password;

    @SerializedName("device")
    @Expose
    private UserDeviceDto device;

    public LoginRequestDto() {
    }

    public LoginRequestDto(String userEmail, String password, UserDeviceDto device) {
        this.userEmail = userEmail;
        this.password = password;
        this.device = device;
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

    public UserDeviceDto getDevice() {
        return this.device;
    }

    public void setDevice(UserDeviceDto device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "LoginRequestDto{" +
                "userEmail='" + userEmail + '\'' +
                ", password='" + password + '\'' +
                ", device=" + device +
                '}';
    }
}
