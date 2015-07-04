package de.tu_darmstadt.tk.android.assistance.models.http.request.profile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 02.07.2015.
 */
public class GetUserProfileRequest {

    @SerializedName("token")
    @Expose
    private String userToken;

    @SerializedName("type")
    @Expose
    private String typeOfRequest;

    public GetUserProfileRequest() {
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getTypeOfRequest() {
        return typeOfRequest;
    }

    public void setTypeOfRequest(String typeOfRequest) {
        this.typeOfRequest = typeOfRequest;
    }
}
