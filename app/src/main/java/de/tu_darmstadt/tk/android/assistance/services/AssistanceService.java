package de.tu_darmstadt.tk.android.assistance.services;

import java.util.List;

import de.tu_darmstadt.tk.android.assistance.models.http.response.AvailableModuleResponse;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;

/**
 * Created by Wladimir Schmidt on 30.06.2015.
 */
public interface AssistanceService {

    @GET("/assistance/list")
    void getAvailableModules(@Header("X-AUTH-TOKEN") String userToken,
                             Callback<List<AvailableModuleResponse>> callback);
}
