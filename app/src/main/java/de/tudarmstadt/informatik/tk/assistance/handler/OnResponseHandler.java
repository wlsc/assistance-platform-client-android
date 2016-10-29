package de.tudarmstadt.informatik.tk.assistance.handler;

import java.net.NoRouteToHostException;

import retrofit2.adapter.rxjava.HttpException;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public interface OnResponseHandler<T> {

    void onSuccess(T apiResponse);

    void onError(HttpException error);

    void onError(NoRouteToHostException error);
}