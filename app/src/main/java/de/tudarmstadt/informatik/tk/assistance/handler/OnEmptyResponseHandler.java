package de.tudarmstadt.informatik.tk.assistance.handler;

import retrofit2.adapter.rxjava.HttpException;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.11.2015
 */
public interface OnEmptyResponseHandler {

    void onSuccess();

    void onError(HttpException error);
}
