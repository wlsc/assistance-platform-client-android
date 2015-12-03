package de.tudarmstadt.informatik.tk.android.assistance.presenter;

import android.content.Context;

import de.tudarmstadt.informatik.tk.android.assistance.view.CommonView;
import retrofit.RetrofitError;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public interface CommonPresenter {

    Context getContext();

    void setView(CommonView view);

    void doInitView();

    /**
     * Starting harvesting service if not running
     */
    void startHarvester();

    /**
     * Stopping harvesting service if not running
     */
    void stopHarvester();

    void doDefaultErrorProcessing(RetrofitError error);
}
