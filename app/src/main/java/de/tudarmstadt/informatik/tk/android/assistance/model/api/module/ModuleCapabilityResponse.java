package de.tudarmstadt.informatik.tk.android.assistance.model.api.module;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 25.08.2015
 */
public class ModuleCapabilityResponse {

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("frequency")
    @Expose
    private double frequency;

    public ModuleCapabilityResponse() {
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getFrequency() {
        return this.frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "ModuleCapabilityResponse{" +
                "type='" + type + '\'' +
                ", frequency=" + frequency +
                '}';
    }
}
