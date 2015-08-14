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

    @SerializedName("device")
    @Expose
    private LoginRequest.UserDevice device;

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

    public LoginRequest.UserDevice getDevice() {
        return this.device;
    }

    public void setDevice(LoginRequest.UserDevice device) {
        this.device = device;
    }

    /**
     * User's device information
     */
    private class UserDevice {

        @SerializedName("id")
        @Expose
        private long id;

        @SerializedName("device_identifier")
        @Expose
        private String deviceId;

        @SerializedName("os")
        @Expose
        private String os;

        @SerializedName("os_version")
        @Expose
        private String osVersion;

        @SerializedName("brand")
        @Expose
        private String brand;

        @SerializedName("model")
        @Expose
        private String model;

        public UserDevice() {
        }

        public long getId() {
            return this.id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getDeviceId() {
            return this.deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getOs() {
            return this.os;
        }

        public void setOs(String os) {
            this.os = os;
        }

        public String getOsVersion() {
            return this.osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public String getBrand() {
            return this.brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getModel() {
            return this.model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}
