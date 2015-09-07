package de.tudarmstadt.informatik.tk.android.assistance.utils;

import android.content.Context;

import java.util.Locale;

/**
 * Created by Wladimir Schmidt on 04.07.2015.
 */
public class UserUtils {

    private UserUtils() {
    }

    /**
     * Supplies with current user locale
     *
     * @param context
     * @return
     */
    public static Locale getCurrentLocale(Context context) {
        return context.getResources().getConfiguration().locale;
    }

    /**
     * Returns user has learned navigation drawer saved in SharedPreferences
     *
     * @return
     */
    public static boolean getUserHasLearnedDrawer(Context context) {
        return PreferencesUtils.readFromPreferences(context, Constants.PREF_USER_LEARNED_DRAWER, false);
    }

    /**
     * Saves user has learned navigation drawer in SharedPreferences
     *
     * @param context
     * @param value
     */
    public static void saveUserHasLearnedDrawer(Context context, boolean value) {
        PreferencesUtils.saveToPreferences(context, Constants.PREF_USER_LEARNED_DRAWER, value);
    }

    /**
     * Returns user email saved in SharedPreferences
     *
     * @return
     */
    public static String getUserEmail(Context context) {
        return PreferencesUtils.readFromPreferences(context, Constants.PREF_USER_EMAIL, "");
    }

    /**
     * Saves user email to preferences
     *
     * @param context
     * @param value
     */
    public static void saveUserEmail(Context context, String value) {
        PreferencesUtils.saveToPreferences(context, Constants.PREF_USER_EMAIL, value);
    }

    /**
     * Returns user firstname
     *
     * @param context
     * @return
     */
    public static String getUserFirstname(Context context) {
        return PreferencesUtils.readFromPreferences(context, Constants.PREF_USER_FIRSTNAME, "");
    }

    /**
     * Saves user firstname to preferences
     *
     * @param context
     * @param value
     */
    public static void saveUserFirstname(Context context, String value) {
        PreferencesUtils.saveToPreferences(context, Constants.PREF_USER_FIRSTNAME, value);
    }

    /**
     * Returns user lastname
     *
     * @param context
     * @return
     */
    public static String getUserLastname(Context context) {
        return PreferencesUtils.readFromPreferences(context, Constants.PREF_USER_LASTNAME, "");
    }

    /**
     * Saves user lastname to preferences
     *
     * @param context
     * @param value
     */
    public static void saveUserLastname(Context context, String value) {
        PreferencesUtils.saveToPreferences(context, Constants.PREF_USER_LASTNAME, value);
    }

    /**
     * Returns user token saved in SharedPreferences
     *
     * @return
     */
    public static String getUserToken(Context context) {
        return PreferencesUtils.readFromPreferences(context, Constants.PREF_USER_TOKEN, "");
    }

    /**
     * Saves user access token to preferences
     *
     * @param context
     * @param value
     */
    public static void saveUserToken(Context context, String value) {
        PreferencesUtils.saveToPreferences(context, Constants.PREF_USER_TOKEN, value);
    }

    /**
     * Returns user picture filename
     *
     * @param context
     * @return
     */
    public static String getUserPicFilename(Context context) {
        return PreferencesUtils.readFromPreferences(context, Constants.PREF_USER_PIC, "");
    }

    /**
     * Saves user picture to preferences
     *
     * @param context
     * @param value
     */
    public static void saveUserPicFilename(Context context, String value) {
        PreferencesUtils.saveToPreferences(context, Constants.PREF_USER_PIC, value);
    }

    /**
     * Returns user has any modules installed
     *
     * @param context
     * @return
     */
    public static boolean isUserHasModules(Context context) {
        return PreferencesUtils.readFromPreferences(context, Constants.PREF_USER_HAS_MODULES, false);
    }

    /**
     * Saves user has any modules installed
     *
     * @param context
     * @param value
     */
    public static void saveUserHasModules(Context context, boolean value) {
        PreferencesUtils.saveToPreferences(context, Constants.PREF_USER_HAS_MODULES, value);
    }
}
