package de.tu_darmstadt.tk.android.assistance.services;

import java.util.List;

import de.tu_darmstadt.tk.android.assistance.models.AbstractModule;
import de.tu_darmstadt.tk.android.assistance.models.http.response.AvailableModuleResponse;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;

/**
 * Created by Wladimir Schmidt on 30.06.2015.
 */
public interface AvailableModulesService {

    @GET("/assistance/list")
    List<AvailableModuleResponse> getAvailableModules(@Header("X-AUTH-TOKEN") String userToken);
}
