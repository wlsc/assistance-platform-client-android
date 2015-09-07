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
    private Long deviceId;

    public LoginResponse() {
    }

    public String getUserToken() {
        return userToken;
    }

    public Long getDeviceId() {
        return this.deviceId;
    }

}
