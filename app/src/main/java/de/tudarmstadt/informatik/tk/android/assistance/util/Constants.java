package de.tudarmstadt.informatik.tk.android.assistance.util;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class Constants {

    private Constants() {
    }

    /**
     * COMMON
     */

    public static final int BACK_BUTTON_DELAY_MILLIS = 2000;

    /**
     * SETTINGS CONSTANTS
     */

    /**
     * User login related data
     * Token received from server to access client's and server's content
     */
    public static final String PREF_USER_TOKEN = "user_token";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_PASSWORD = "user_password";
    public static final String PREF_USER_FIRSTNAME = "user_firstname";
    public static final String PREF_USER_LASTNAME = "user_lastname";
    public static final String PREF_USER_PIC = "user_picture";
    public static final String PREF_USER_HAS_MODULES = "user_has_modules";
    public static final String PREF_USER_ACTIVE_MODULES = "user_active_modules";
    public static final String PREF_USER_REQUESTED_ACTIVE_MODULES = "user_requested_active_modules";
    public static final String PREF_USER_ID = "current_user_id";
    public static final String PREF_DEVICE_ID = "current_device_id";
    public static final String PREF_SERVER_DEVICE_ID = "server_device_id";
    public static final String PREF_GCM_TOKEN_SENT = "gcm_token_sent";
    public static final String PREF_DEVELOPER_STATUS = "developer_status";
    public static final String PREF_CUSTOM_ENDPOINT = "custom_endpoint";

    /**
     * INTENT CONSTANTS
     */

    public static final int INTENT_CURRENT_DEVICE_ID_RESULT = 723;
    public static final int INTENT_AVAILABLE_MODULES_RESULT = 444;
    public static final int INTENT_ACCESSIBILITY_SERVICE_ENABLED_RESULT = 888;
    public static final int INTENT_ACCESSIBILITY_SERVICE_IGNORED_RESULT = 889;
    public static final int INTENT_SETTINGS_LOGOUT_RESULT = 333;

    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_CURRENT_DEVICE_ID = "current_device_id";

    /**
     * REQUEST PERMISSIONS
     */
    public static final int PERM_MODULE_INSTALL = 250;
}
