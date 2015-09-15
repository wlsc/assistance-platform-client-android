package de.tudarmstadt.informatik.tk.android.assistance.model.api.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * User's device information
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 22.08.2015
 */
public class UserDevice {

    @SerializedName("id")
    @Expose
    private Long serverId;

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

    public UserDevice(Long serverId, String deviceId, String os, String osVersion, String brand, String model) {
        this.serverId = serverId;
        this.deviceId = deviceId;
        this.os = os;
        this.osVersion = osVersion;
        this.brand = brand;
        this.model = model;
    }

    public Long getServerId() {
        return this.serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
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

    @Override
    public String toString() {
        return "UserDevice{" +
                "id=" + serverId +
                ", deviceId='" + deviceId + '\'' +
                ", os='" + os + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}
