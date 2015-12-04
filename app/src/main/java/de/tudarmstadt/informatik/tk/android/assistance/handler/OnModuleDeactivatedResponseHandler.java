package de.tudarmstadt.informatik.tk.android.assistance.handler;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.db.DbModule;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 04.12.2015
 */
public interface OnModuleDeactivatedResponseHandler {

    void onModuleDeactivateSuccess(DbModule module, Response response);

    void onModuleDeactivateFailed(DbModule module, RetrofitError error);
}
