package de.tudarmstadt.informatik.tk.android.assistance.presenter;

import android.content.Context;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public interface CommonPresenter {

    Context getContext();

    /**
     * Starting harvesting service if not running
     */
    void startHarvester();

    /**
     * Stopping harvesting service if not running
     */
    void stopHarvester();
}
