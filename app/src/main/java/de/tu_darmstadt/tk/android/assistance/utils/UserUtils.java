package de.tu_darmstadt.tk.android.assistance.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Wladimir Schmidt on 04.07.2015.
 */
public class UserUtils {

    /**
     * Returns user email saved in SharedPreferences
     *
     * @return
     */
    public static String getUserEmail(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(Constants.PREF_USER_EMAIL, "");
    }

    /**
     * Returns user token saved in SharedPreferences
     *
     * @return
     */
    public static String getUserToken(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(Constants.PREF_USER_TOKEN, "");
    }

}
