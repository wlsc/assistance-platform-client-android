package de.tu_darmstadt.tk.android.assistance.models.http.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class LoginResponse {

    @SerializedName("token")
    @Expose
    private String userToken;

    public LoginResponse() {
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
}
