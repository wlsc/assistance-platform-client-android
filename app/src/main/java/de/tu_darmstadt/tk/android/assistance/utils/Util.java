package de.tu_darmstadt.tk.android.assistance.utils;

/**
 * Created by Wladimir Schmidt (wlsc.dev@gmail.com) on 07.06.2015.
 */
public class Util {

    public static final int PASSWORD_MIN_LENGTH = 4;

    public static boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    public static boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > PASSWORD_MIN_LENGTH;
    }
}
