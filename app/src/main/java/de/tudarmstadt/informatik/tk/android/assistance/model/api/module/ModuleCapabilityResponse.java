package de.tudarmstadt.informatik.tk.android.assistance.model.api.module;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Capability of a particular assistance module
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 25.08.2015
 */
public class ModuleCapabilityResponse {

    @SerializedName("type")
    @Expose
    private String type;

    // The frequency of needed event readings of this type (per second).
    // Readings can be cached on the client. (per second)
    // double; 1 = 1 measurement per second,
    // 0.01666 = 1 measurement per minute
    @SerializedName("collection_frequency")
    @Expose
    private Double collectionFrequency;

    // The frequency in which at least {@link #min_required_readings_on_update}
    // have to be sent to the platform.
    @SerializedName("required_update_frequency")
    @Expose
    private Double requiredUpdateFrequency;

    // The minimum number that has to be sent in {@link #required_update_frequency}
    // so the module can keep up with processing.
    @SerializedName("min_required_readings_on_update")
    @Expose
    private Integer minRequiredReadingsOnUpdate;

    public ModuleCapabilityResponse() {
    }

    public ModuleCapabilityResponse(String type, Double collectionFrequency, Double requiredUpdateFrequency, Integer minRequiredReadingsOnUpdate) {
        this.type = type;
        this.collectionFrequency = collectionFrequency;
        this.requiredUpdateFrequency = requiredUpdateFrequency;
        this.minRequiredReadingsOnUpdate = minRequiredReadingsOnUpdate;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getCollectionFrequency() {
        return this.collectionFrequency;
    }

    public void setCollectionFrequency(Double collectionFrequency) {
        this.collectionFrequency = collectionFrequency;
    }

    public Double getRequiredUpdateFrequency() {
        return this.requiredUpdateFrequency;
    }

    public void setRequiredUpdateFrequency(Double requiredUpdateFrequency) {
        this.requiredUpdateFrequency = requiredUpdateFrequency;
    }

    public Integer getMinRequiredReadingsOnUpdate() {
        return this.minRequiredReadingsOnUpdate;
    }

    public void setMinRequiredReadingsOnUpdate(Integer minRequiredReadingsOnUpdate) {
        this.minRequiredReadingsOnUpdate = minRequiredReadingsOnUpdate;
    }

    @Override
    public String toString() {
        return "ModuleCapabilityResponse{" +
                "type='" + type + '\'' +
                ", collectionFrequency=" + collectionFrequency +
                ", requiredUpdateFrequency=" + requiredUpdateFrequency +
                ", minRequiredReadingsOnUpdate=" + minRequiredReadingsOnUpdate +
                '}';
    }
}
