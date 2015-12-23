package de.tudarmstadt.informatik.tk.android.assistance.handler;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 04.12.2015
 */
public interface OnModuleActivatedResponseHandler {

    void onModuleActivateSuccess(Response module);

    void onModuleActivateFailed(RetrofitError error);
}