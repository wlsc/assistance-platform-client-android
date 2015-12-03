package de.tudarmstadt.informatik.tk.android.assistance.handler;

import java.util.List;

import de.tudarmstadt.informatik.tk.android.assistance.model.api.dto.module.AvailableModuleResponseDto;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 03.12.2015
 */
public interface OnAvailableModulesResponseHandler {

    void onAvailableModulesSuccess(List<AvailableModuleResponseDto> apiResponse, Response response);

    void onAvailableModulesError(RetrofitError error);

}
