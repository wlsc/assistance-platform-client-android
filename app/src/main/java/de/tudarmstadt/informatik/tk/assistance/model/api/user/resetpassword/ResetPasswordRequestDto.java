package de.tudarmstadt.informatik.tk.assistance.model.api.user.resetpassword;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 01.07.2015.
 */
public class ResetPasswordRequestDto {

    @SerializedName("email")
    @Expose
    private String email;

    public ResetPasswordRequestDto() {
    }

    public ResetPasswordRequestDto(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "ResetPasswordRequestDto{" +
                "email='" + email + '\'' +
                '}';
    }
}
