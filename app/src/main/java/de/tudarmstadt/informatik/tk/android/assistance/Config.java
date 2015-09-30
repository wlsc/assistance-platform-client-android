package de.tudarmstadt.informatik.tk.android.assistance;

/**
 * This is a main configuration file for an assistance platform
 *
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 28.06.2015
 */
public class Config {

    private Config() {
    }

    /**
     * ENDPOINTS
     */

    public static final String ASSISTANCE_MODULE_LIST_ENDPOINT = "/assistance/list";

    public static final String ASSISTANCE_MODULE_ACTIVE_ENDPOINT = "/assistance/activations";

    public static final String ASSISTANCE_MODULE_ACTIVATE_ENDPOINT = "/assistance/activate";

    public static final String ASSISTANCE_MODULE_DEACTIVATE_ENDPOINT = "/assistance/deactivate";

    public static final String ASSISTANCE_USER_REGISTER_ENDPOINT = "/users/register";

    public static final String ASSISTANCE_USER_LOGIN_ENDPOINT = "/users/login";

    public static final String ASSISTANCE_USER_PASSWORD_ENDPOINT = "/users/password";

    public static final String ASSISTANCE_USER_PROFILE_SHORT_ENDPOINT = "/users/profile/short";

    public static final String ASSISTANCE_USER_PROFILE_FULL_ENDPOINT = "/users/profile/long";

    public static final String ASSISTANCE_USER_PROFILE_UPDATE_ENDPOINT = "/users/profile";

    /**
     * GOOGLE ANALYTICS
     */

    // Replace the tracker-id with your app one from https://www.google.com/analytics/web/
    public static final String GOOGLE_ANALYTICS_TRACKING_ID = "UA-58106124-4";

    // Provide unhandled exceptions reports. Do that first after creating the tracker
    public static final boolean GOOGLE_ANALYTICS_EXCEPTION_REPORTING = true;

    // Enable Remarketing, Demographics & Interests reports
    // https://developers.google.com/analytics/devguides/collection/android/display-features
    public static final boolean GOOGLE_ANALYTICS_ADVERTISING_COLLECTION = true;

    // Enable automatic activity tracking for your app
    public static final boolean GOOGLE_ANALYTICS_AUTO_ACTIVITY_TRACKING = true;

    /**
     * Min password length
     */
    public static final int PASSWORD_MIN_LENGTH = 4;

    /**
     * Path to save user picture
     */
    public static final String USER_PIC_PATH = "assistance/user/img";

    /**
     * An update interval for every request to server
     * In milliseconds
     * Current: 5 MIN
     */
    public static final int UPDATE_REQUEST_INTERVAL = 5 * 60 * 1000;
}
