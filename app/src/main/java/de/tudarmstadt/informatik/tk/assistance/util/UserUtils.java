package de.tudarmstadt.informatik.tk.assistance.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;
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

    /**
     * Checks if email client is present
     *
     * @param context
     * @return
     */
    public static boolean isEMailClientExists(Context context) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);

        return !list.isEmpty();
    }
}