package de.tu_darmstadt.tk.android.assistance;

/**
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public class Config {

    private Config() {
    }

    /**
     * Set it to false when compiling for release
     */
    public static final boolean DEVELOPER_MODE_ENABLED = true;

    /*
    *   Assistance login server. CAUTION: Please write this address without "/" at the end!
    */
    public static final String ASSISTANCE_URL = "https://130.83.163.115";

    /**
     * Min password length
     */
    public static final int PASSWORD_MIN_LENGTH = 4;
}
