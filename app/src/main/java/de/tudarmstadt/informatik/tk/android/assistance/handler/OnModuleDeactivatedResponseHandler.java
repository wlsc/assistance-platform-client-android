package de.tudarmstadt.informatik.tk.android.assistance.handler;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 04.12.2015
 */
public interface OnModuleDeactivatedResponseHandler {

    void onModuleDeactivateSuccess(Response response);

    void onModuleDeactivateFailed(RetrofitError error);
}
