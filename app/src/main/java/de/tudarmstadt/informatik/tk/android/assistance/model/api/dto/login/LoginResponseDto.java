package de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class LoginResponseDto {

    @SerializedName("token")
    @Expose
    private String userToken;

    @SerializedName("device_id")
    @Expose
    private Long deviceId;

    public LoginResponseDto() {
    }

    public LoginResponseDto(String userToken, Long deviceId) {
        this.userToken = userToken;
        this.deviceId = deviceId;
    }

    public String getUserToken() {
        return userToken;
    }

    public Long getDeviceId() {
        return this.deviceId;
    }

    @Override
    public String toString() {
        return "LoginResponseDto{" +
                "userToken='" + userToken + '\'' +
                ", deviceId=" + deviceId +
                '}';
    }
}
