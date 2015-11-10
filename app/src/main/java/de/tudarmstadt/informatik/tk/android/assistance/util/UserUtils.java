package de.tudarmstadt.informatik.tk.android.assistance.util;

import android.content.Context;

import java.util.Locale;

import de.tudarmstadt.informatik.tk.android.kraken.provider.HarvesterServiceProvider;
import de.tudarmstadt.informatik.tk.android.kraken.service.HarvesterService;
import de.tudarmstadt.informatik.tk.android.kraken.util.DeviceUtils;

/**
 * Created by Wladimir Schmidt on 04.07.2015.
 */
public class UserUtils {

    private UserUtils() {
    }

    /**
     * Reset user token/email and log him out
     */
    public static void doLogout(Context context) {

        PreferencesUtils.clearUserCredentials(context);

        // stop the kraken
        if (DeviceUtils.isServiceRunning(
                context,
                HarvesterService.class)) {

            HarvesterServiceProvider.getInstance(context).stopSensingService();
        }
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
     * Returns current user id saved in SharedPreferences
     *
     * @param context
     * @return
     */
    public static long getCurrentUserId(Context context) {
        return PreferencesUtils.getPreference(context, Constants.PREF_USER_ID, -1);
    }

    /**
     * Saves current user id into SharedPreferences
     *
     * @param context
     * @param value
     */
    public static void saveCurrentUserId(Context context, long value) {
        PreferencesUtils.savePreference(context, Constants.PREF_USER_ID, value);
    }

    /**
     * Returns current device id saved in SharedPreferences
     *
     * @param context
     * @return
     */
    public static long getCurrentDeviceId(Context context) {
        return PreferencesUtils.getPreference(context, Constants.PREF_DEVICE_ID, -1);
    }

    /**
     * Saves current device id into SharedPreferences
     *
     * @param context
     * @param value
     */
    public static void saveCurrentDeviceId(Context context, long value) {
        PreferencesUtils.savePreference(context, Constants.PREF_DEVICE_ID, value);
    }

    /**
     * Returns current server device id saved in SharedPreferences
     *
     * @param context
     * @return
     */
    public static long getServerDeviceId(Context context) {
        return PreferencesUtils.getPreference(context, Constants.PREF_SERVER_DEVICE_ID, -1);
    }

    /**
     * Saves current server device id into SharedPreferences
     *
     * @param context
     * @param value
     */
    public static void saveServerDeviceId(Context context, long value) {
        PreferencesUtils.savePreference(context, Constants.PREF_SERVER_DEVICE_ID, value);
    }

    /**
     * Returns user email saved in SharedPreferences
     *
     * @return
     */
    public static String getUserEmail(Context context) {
        return PreferencesUtils.getPreference(context, Constants.PREF_USER_EMAIL, "");
    }

    /**
     * Saves user email to preferences
     *
     * @param context
     * @param value
     */
    public static void saveUserEmail(Context context, String value) {
        PreferencesUtils.savePreference(context, Constants.PREF_USER_EMAIL, value);
    }

    /**
     * Returns user password saved in SharedPreferences
     *
     * @return
     */
    public static String getUserPassword(Context context) {
        return PreferencesUtils.getPreference(context, Constants.PREF_USER_PASSWORD, "");
    }

    /**
     * Saves user password to preferences
     *
     * @param context
     * @param value
     */
    public static void saveUserPassword(Context context, String value) {
        PreferencesUtils.savePreference(context, Constants.PREF_USER_PASSWORD, value);
    }

    /**
     * Returns user firstname
     *
     * @param context
     * @return
     */
    public static String getUserFirstname(Context context) {
        return PreferencesUtils.getPreference(context, Constants.PREF_USER_FIRSTNAME, "");
    }

    /**
     * Saves user firstname to preferences
     *
     * @param context
     * @param value
     */
    public static void saveUserFirstname(Context context, String value) {
        PreferencesUtils.savePreference(context, Constants.PREF_USER_FIRSTNAME, value);
    }

    /**
     * Returns user lastname
     *
     * @param context
     * @return
     */
    public static String getUserLastname(Context context) {
        return PreferencesUtils.getPreference(context, Constants.PREF_USER_LASTNAME, "");
    }

    /**
     * Saves user lastname to preferences
     *
     * @param context
     * @param value
     */
    public static void saveUserLastname(Context context, String value) {
        PreferencesUtils.savePreference(context, Constants.PREF_USER_LASTNAME, value);
    }

    /**
     * Returns user token saved in SharedPreferences
     *
     * @return
     */
    public static String getUserToken(Context context) {
        return PreferencesUtils.getPreference(context, Constants.PREF_USER_TOKEN, "");
    }

    /**
     * Saves user access token to preferences
     *
     * @param context
     * @param value
     */
    public static void saveUserToken(Context context, String value) {
        PreferencesUtils.savePreference(context, Constants.PREF_USER_TOKEN, value);
    }

    /**
     * Returns user picture filename
     *
     * @param context
     * @return
     */
    public static String getUserPicFilename(Context context) {
        return PreferencesUtils.getPreference(context, Constants.PREF_USER_PIC, "");
    }

    /**
     * Saves user picture to preferences
     *
     * @param context
     * @param value
     */
    public static void saveUserPicFilename(Context context, String value) {
        PreferencesUtils.savePreference(context, Constants.PREF_USER_PIC, value);
    }

    /**
     * Returns user has any modules installed
     *
     * @param context
     * @return
     */
    public static boolean isUserHasModules(Context context) {
        return PreferencesUtils.getPreference(context, Constants.PREF_USER_HAS_MODULES, false);
    }

    /**
     * Saves user has any modules installed
     *
     * @param context
     * @param value
     */
    public static void saveUserHasModules(Context context, boolean value) {
        PreferencesUtils.savePreference(context, Constants.PREF_USER_HAS_MODULES, value);
    }

    /**
     * Returns user has requested active modules for the first time app starts
     *
     * @param context
     * @return
     */
    public static boolean hasUserRequestedActiveModules(Context context) {
        return PreferencesUtils.getPreference(context,
                Constants.PREF_USER_REQUESTED_ACTIVE_MODULES,
                false);
    }

    /**
     * Saves user has any modules installed
     *
     * @param context
     * @param value
     */
    public static void saveUserRequestedActiveModules(Context context, boolean value) {
        PreferencesUtils.savePreference(context, Constants.PREF_USER_REQUESTED_ACTIVE_MODULES, value);
    }

    /**
     * Returns user is a developer status
     *
     * @param context
     * @return
     */
    public static boolean isUserDeveloper(Context context) {
        return PreferencesUtils.getPreference(context, Constants.PREF_DEVELOPER_STATUS, false);
    }

    /**
     * Saves user developer status into SharedPreferences
     *
     * @param context
     * @param value
     */
    public static void saveDeveloperStatus(Context context, boolean value) {
        PreferencesUtils.savePreference(context, Constants.PREF_DEVELOPER_STATUS, value);
    }

    /**
     * Returns GCM was sent flag
     *
     * @param context
     * @return
     */
    public static boolean isGcmTokenWasSent(Context context) {
        return PreferencesUtils.getPreference(context, Constants.PREF_GCM_TOKEN_SENT, false);
    }

    /**
     * Saves GCM was sent flag into SharedPreferences
     *
     * @param context
     * @param value
     */
    public static void saveGcmTokenWasSent(Context context, boolean value) {
        PreferencesUtils.savePreference(context, Constants.PREF_GCM_TOKEN_SENT, value);
    }
}
