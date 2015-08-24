package de.tu_darmstadt.tk.android.assistance.models.api.module;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 14.07.2015.
 */
public class ToggleModuleRequest {

    @SerializedName("module_id")
    @Expose
    private String moduleId;

    public ToggleModuleRequest() {
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
