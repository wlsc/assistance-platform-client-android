package de.tudarmstadt.informatik.tk.android.assistance.handler;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface OnEmptyResponseHandler {

    void onSuccess(Response response);

    void onError(RetrofitError error);
}
