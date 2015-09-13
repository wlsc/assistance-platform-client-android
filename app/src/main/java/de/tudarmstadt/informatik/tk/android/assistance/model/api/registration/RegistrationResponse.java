package de.tudarmstadt.informatik.tk.android.assistance.model.api.registration;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class RegistrationResponse {

    @SerializedName("user_id")
    @Expose
    private Long userId;

    public RegistrationResponse() {
    }

    public RegistrationResponse(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "RegistrationResponse{" +
                "userId=" + userId +
                '}';
    }
}
