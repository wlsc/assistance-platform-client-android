package de.tu_darmstadt.tk.android.assistance.utils;

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
     * Remember the position of the selected item.
     */
    public static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Drawer dimming threshold when drawer slides out
     */
    public static final double DRAWER_SLIDER_THRESHOLD = 0.4;

    /**
     * SETTINGS CONSTANTS
     */

    /**
     * User login related data
     * Token received from server to access client's and server's content
     */
    public static final String PREF_USER_TOKEN = "user_token";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_FIRSTNAME = "user_firstname";
    public static final String PREF_USER_LASTNAME = "user_lastname";
    public static final String PREF_USER_PIC = "user_picture";
    public static final String PREF_USER_HAS_MODULES = "user_has_modules";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    public static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * INTENT CONSTANTS
     */

    public static final String INTENT_USER_ID = "user_id";

}
