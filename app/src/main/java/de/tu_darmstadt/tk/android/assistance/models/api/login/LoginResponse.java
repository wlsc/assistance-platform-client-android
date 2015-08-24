package de.tu_darmstadt.tk.android.assistance.models.api.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class LoginResponse {

    @SerializedName("token")
    @Expose
    private String userToken;

    @SerializedName("device_id")
    @Expose
    private String deviceId;

    public LoginResponse() {
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
