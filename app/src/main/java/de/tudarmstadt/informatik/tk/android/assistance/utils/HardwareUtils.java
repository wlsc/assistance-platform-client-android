package de.tudarmstadt.informatik.tk.android.assistance.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 22.08.2015
 */
public class HardwareUtils {

    /**
     * Returns current version of Android operating system
     *
     * @return
     */
    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * Returns device brand name
     *
     * @return
     */
    public static String getDeviceBrandName() {
        return Build.BRAND;
    }

    /**
     * Returns device model name
     *
     * @return
     */
    public static String getDeviceModelName() {
        return Build.MODEL;
    }

    /**
     * Returns unique Android id for current device
     *
     * @param context
     * @return
     */
    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}
