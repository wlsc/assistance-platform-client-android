package de.tu_darmstadt.tk.android.assistance.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Wladimir Schmidt on 12.07.2015.
 */
public class PreferencesUtils {

    private PreferencesUtils() {
    }

    /**
     * Puts some string value to shared preferences
     *
     * @param context
     * @param preferenceName
     * @param preferenceValue
     */
    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putString(preferenceName, preferenceValue)
                .apply();
    }

    /**
     * Puts some boolean value to shared preferences
     *
     * @param context
     * @param preferenceName
     * @param preferenceValue
     */
    public static void saveToPreferences(Context context, String preferenceName, boolean preferenceValue) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences
                .edit()
                .putBoolean(preferenceName, preferenceValue)
                .apply();
    }

    /**
     * Gets some string value from shared preferences
     *
     * @param context
     * @param preferenceName
     * @param defaultValue
     * @return
     */
    public static String readFromPreferences(Context context, String preferenceName, String defaultValue) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }

    /**
     * Gets some boolean value from shared preferences
     *
     * @param context
     * @param preferenceName
     * @param defaultValue
     * @return
     */
    public static boolean readFromPreferences(Context context, String preferenceName, boolean defaultValue) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(preferenceName, defaultValue);
    }

    public static void clearUserCredentials(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit()
                .remove(Constants.PREF_USER_TOKEN)
                .remove(Constants.PREF_USER_EMAIL)
                .apply();
    }

}
