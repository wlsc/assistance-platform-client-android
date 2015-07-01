package de.tu_darmstadt.tk.android.assistance.models.http.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Wladimir Schmidt on 01.07.2015.
 */
public class ResetPasswordRequest {

    @SerializedName("email")
    @Expose
    private String email;

}
