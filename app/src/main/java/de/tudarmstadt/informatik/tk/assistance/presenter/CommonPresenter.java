package de.tudarmstadt.informatik.tk.assistance.presenter;

import android.content.Context;

import de.tudarmstadt.informatik.tk.assistance.view.CommonView;
import retrofit.RetrofitError;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public interface CommonPresenter {

    Context getContext();

    void setView(CommonView view);

    void initView();

    /**
     * Starting harvesting service if not running
     */
    void startHarvester();

    /**
     * Stopping harvesting service if not running
     */
    void stopHarvester();

    void doDefaultErrorProcessing(RetrofitError error);

    void presentEMailAlreadyExists();
}
