package de.tudarmstadt.informatik.tk.assistance;

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
