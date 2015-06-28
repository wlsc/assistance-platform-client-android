package de.tu_darmstadt.tk.android.assistance.utils;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class Constants {

    /**
     * Drawer dimming threshold when drawer slides out
     */
    public static final double DRAWER_SLIDER_THRESHOLD = 0.4;

    /**
     * Token received from server to access client's and server's content
     */
    public static final String PREF_USER_TOKEN = "user_token";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    public static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
}
