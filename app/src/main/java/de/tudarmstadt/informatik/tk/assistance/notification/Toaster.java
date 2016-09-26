package de.tudarmstadt.informatik.tk.assistance.notification;

import android.content.Context;
import android.widget.Toast;

/**
 * Shows toasts to user in better form
 * <p>
 * Created by Wladimir Schmidt on 28.06.2015.
 */
public final class Toaster {

    private Toaster() {
    }

    public static void showLong(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showLong(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
    }

    public static void showShort(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showShort(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

}
