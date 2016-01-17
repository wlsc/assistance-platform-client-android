package de.tudarmstadt.informatik.tk.assistance.handler;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public interface OnResponseHandler<T> {

    void onSuccess(T apiResponse, Response response);

    void onError(RetrofitError error);
}