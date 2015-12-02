package de.tudarmstadt.informatik.tk.android.assistance.presenter;

import android.content.Context;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.service.HarvesterService;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.DeviceUtils;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 01.12.2015
 */
public class CommonPresenterImpl implements CommonPresenter {

    private final Context context;

    public CommonPresenterImpl(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    @Override
    public void startHarvester() {

        if (!DeviceUtils.isServiceRunning(getContext(), HarvesterService.class)) {

            HarvesterServiceProvider.getInstance(
                    getContext())
                    .startSensingService();
        }
    }

    @Override
    public void stopHarvester() {

        if (DeviceUtils.isServiceRunning(getContext(), HarvesterService.class)) {

            HarvesterServiceProvider.getInstance(
                    getContext())
                    .stopSensingService();
        }
    }
}
