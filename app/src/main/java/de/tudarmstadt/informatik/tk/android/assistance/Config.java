package de.tudarmstadt.informatik.tk.android.assistance;

/**
 * This is a main configuration file for an assistance platform
 * <p/>
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class Config {

    private Config() {
    }

    /*
    *   Assistance login server. CAUTION: Please write this address without "/" at the end!
    */
    public static final String ASSISTANCE_ENDPOINT = "https://130.83.163.115";

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
