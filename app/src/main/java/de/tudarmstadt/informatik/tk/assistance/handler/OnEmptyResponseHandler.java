package de.tudarmstadt.informatik.tk.assistance.handler;

import retrofit.RetrofitError;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface OnEmptyResponseHandler {

    void onSuccess();

    void onError(RetrofitError error);
}
