package de.tu_darmstadt.tk.android.assistance.models.http.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Wladimir Schmidt on 30.06.2015.
 */
public class AvailableModuleResponse {

    @SerializedName("id")
    @Expose
    private String modulePackage;

    @SerializedName("name")
    @Expose
    private String title;

    @SerializedName("logoUrl")
    @Expose
    private String logo;

    @SerializedName("description_short")
    @Expose
    private String descriptionShort;

    @SerializedName("description_long")
    @Expose
    private String descriptionFull;

    @SerializedName("copyright")
    @Expose
    private String copyright;

    @SerializedName("requiredCapabilities")
    @Expose
    private List<String> sensorsRequired;

    @SerializedName("optionalCapabilites")
    @Expose
    private List<String> sensorsOptional;

    public AvailableModuleResponse() {
    }

    public String getModulePackage() {
        return modulePackage;
    }

    public void setModulePackage(String modulePackage) {
        this.modulePackage = modulePackage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getDescriptionShort() {
        return descriptionShort;
    }

    public void setDescriptionShort(String descriptionShort) {
        this.descriptionShort = descriptionShort;
    }

    public String getDescriptionFull() {
        return descriptionFull;
    }

    public void setDescriptionFull(String descriptionFull) {
        this.descriptionFull = descriptionFull;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public List<String> getSensorsRequired() {
        return sensorsRequired;
    }

    public void setSensorsRequired(List<String> sensorsRequired) {
        this.sensorsRequired = sensorsRequired;
    }

    public List<String> getSensorsOptional() {
        return sensorsOptional;
    }

    public void setSensorsOptional(List<String> sensorsOptional) {
        this.sensorsOptional = sensorsOptional;
    }
}
