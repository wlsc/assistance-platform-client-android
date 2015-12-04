package de.tudarmstadt.informatik.tk.android.assistance.util;

import android.content.Context;

import de.tudarmstadt.informatik.tk.android.assistance.sdk.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.service.HarvesterService;
import de.tudarmstadt.informatik.tk.android.assistance.sdk.util.DeviceUtils;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 11.11.2015
 */
public class LoginUtils {

    private LoginUtils() {
    }

    /**
     * Reset user token/email and log him out
     */
    public static void doLogout(Context context) {

        PreferenceUtils.clearUserCredentials(context);

        // stop the sensing service
        if (DeviceUtils.isServiceRunning(context, HarvesterService.class)) {

            HarvesterServiceProvider.getInstance(context).stopSensingService();
        }
    }
}
