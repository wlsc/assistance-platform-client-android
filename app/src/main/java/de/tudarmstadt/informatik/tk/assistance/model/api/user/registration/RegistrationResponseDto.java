package de.tudarmstadt.informatik.tk.assistance.model.api.user.registration;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class RegistrationResponseDto {

    @SerializedName("user_id")
    @Expose
    private Long userId;

    public RegistrationResponseDto() {
    }

    public RegistrationResponseDto(Long userId) {
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
        return "RegistrationResponseDto{" +
                "userId=" + userId +
                '}';
    }
}
