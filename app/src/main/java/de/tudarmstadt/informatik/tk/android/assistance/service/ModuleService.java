package de.tudarmstadt.informatik.tk.android.assistance.service;

import java.util.List;

import de.tudarmstadt.informatik.tk.android.assistance.Config;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.AvailableModuleResponse;
import de.tudarmstadt.informatik.tk.android.assistance.model.api.module.ToggleModuleRequest;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;

/**
 * Module service API endpoint
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.06.2015
 */
public interface ModuleService {

    @GET(Config.ASSISTANCE_MODULE_LIST_ENDPOINT)
    void getAvailableModules(@Header("X-AUTH-TOKEN") String userToken,
                             Callback<List<AvailableModuleResponse>> callback);

    @GET(Config.ASSISTANCE_MODULE_ACTIVE_ENDPOINT)
    void getActiveModules(@Header("X-AUTH-TOKEN") String userToken,
                          Callback<List<String>> callback);

    @POST(Config.ASSISTANCE_MODULE_ACTIVATE_ENDPOINT)
    void activateModule(@Header("X-AUTH-TOKEN") String userToken,
                        @Body ToggleModuleRequest body,
                        Callback<Void> callback);

    @POST(Config.ASSISTANCE_MODULE_DEACTIVATE_ENDPOINT)
    void deactivateModule(@Header("X-AUTH-TOKEN") String userToken,
                          @Body ToggleModuleRequest body,
                          Callback<Void> callback);
}
