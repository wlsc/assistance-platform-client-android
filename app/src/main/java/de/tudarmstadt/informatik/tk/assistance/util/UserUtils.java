package de.tudarmstadt.informatik.tk.assistance.util;

import android.content.Context;

import java.util.Locale;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 04.07.2015
 */
public class UserUtils {

    private UserUtils() {
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

}
