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
