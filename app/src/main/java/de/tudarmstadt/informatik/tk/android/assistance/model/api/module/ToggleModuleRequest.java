package de.tudarmstadt.informatik.tk.android.assistance.model.api.module;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Error class for JSON response
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 14.07.2015
 */
public class ToggleModuleRequest {

    @SerializedName("module_id")
    @Expose
    private String moduleId;

    public ToggleModuleRequest() {
    }

    public ToggleModuleRequest(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public String toString() {
        return "ToggleModuleRequest{" +
                "moduleId='" + moduleId + '\'' +
                '}';
    }
}
