package de.tudarmstadt.informatik.tk.android.assistance.models.api.resetpassword;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 01.07.2015.
 */
public class ResetPasswordRequest {

    @SerializedName("email")
    @Expose
    private String email;

    public ResetPasswordRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "ResetPasswordRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}
