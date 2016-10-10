package de.tudarmstadt.informatik.tk.assistance.handler;


import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 04.12.2015
 */
public interface OnModuleActivatedResponseHandler {

    void onModuleActivateSuccess(Response module);

    void onModuleActivateFailed(HttpException error);
}