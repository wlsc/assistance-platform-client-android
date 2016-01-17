package de.tudarmstadt.informatik.tk.assistance.util;

import android.content.Context;

import de.tudarmstadt.informatik.tk.assistance.sdk.provider.HarvesterServiceProvider;

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
        HarvesterServiceProvider.getInstance(context).stopSensingService();
    }
}
