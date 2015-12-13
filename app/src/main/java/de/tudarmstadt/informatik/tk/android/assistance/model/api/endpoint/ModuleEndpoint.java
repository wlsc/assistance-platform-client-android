package de.tudarmstadt.informatik.tk.android.assistance.model.api.endpoint;

import java.util.List;
import java.util.Set;

import de.tudarmstadt.informatik.tk.android.assistance.Config;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.dto.module.ModuleResponseDto;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.model.api.dto.module.ToggleModuleRequestDto;
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
public interface ModuleEndpoint {

    @GET(Config.ASSISTANCE_MODULE_LIST_ENDPOINT)
    void getAvailableModules(@Header("X-AUTH-TOKEN") String userToken,
                             Callback<List<ModuleResponseDto>> callback);

    @GET(Config.ASSISTANCE_MODULE_ACTIVE_ENDPOINT)
    void getActiveModules(@Header("X-AUTH-TOKEN") String userToken,
                          Callback<Set<String>> callback);

    @POST(Config.ASSISTANCE_MODULE_ACTIVATE_ENDPOINT)
    void activateModule(@Header("X-AUTH-TOKEN") String userToken,
                        @Body ToggleModuleRequestDto body,
                        Callback<Void> callback);

    @POST(Config.ASSISTANCE_MODULE_DEACTIVATE_ENDPOINT)
    void deactivateModule(@Header("X-AUTH-TOKEN") String userToken,
                          @Body ToggleModuleRequestDto body,
                          Callback<Void> callback);
}
