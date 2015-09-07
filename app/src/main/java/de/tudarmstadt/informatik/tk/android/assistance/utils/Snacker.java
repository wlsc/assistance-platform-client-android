package de.tudarmstadt.informatik.tk.android.assistance.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 22.07.2015
 */
public class Snacker {

    private Snacker() {
    }

    public static void showLong(View view, String message) {

        Snackbar
                .make(view, message, Snackbar.LENGTH_LONG)
                .show();
    }

    public static void showLong(View view, int resId) {
        Snackbar
                .make(view, resId, Snackbar.LENGTH_LONG)
                .show();
    }

    public static void showLong(View view, String message, String actionText, View.OnClickListener handler) {

        Snackbar
                .make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionText, handler)
                .show();
    }

    public static void showLong(View view, int resId, String actionText, View.OnClickListener handler) {

        Snackbar
                .make(view, resId, Snackbar.LENGTH_LONG)
                .setAction(actionText, handler)
                .show();
    }

    public static void showLong(View view, String message, int actionRes, View.OnClickListener handler) {

        Snackbar
                .make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionRes, handler)
                .show();
    }

    public static void showLong(View view, int resId, int actionRes, View.OnClickListener handler) {

        Snackbar
                .make(view, resId, Snackbar.LENGTH_LONG)
                .setAction(actionRes, handler)
                .show();
    }

    public static void showShort(View view, String message) {
        Snackbar
                .make(view, message, Snackbar.LENGTH_SHORT)
                .show();
    }

    public static void showShort(View view, int resId) {
        Snackbar
                .make(view, resId, Snackbar.LENGTH_SHORT)
                .show();
    }

    public static void showShort(View view, String message, String actionText, View.OnClickListener handler) {
        Snackbar
                .make(view, message, Snackbar.LENGTH_SHORT)
                .setAction(actionText, handler)
                .show();
    }

    public static void showShort(View view, int resId, String actionText, View.OnClickListener handler) {
        Snackbar
                .make(view, resId, Snackbar.LENGTH_SHORT)
                .setAction(actionText, handler)
                .show();
    }

    public static void showShort(View view, String message, int actionRes, View.OnClickListener handler) {
        Snackbar
                .make(view, message, Snackbar.LENGTH_SHORT)
                .setAction(actionRes, handler)
                .show();
    }

    public static void showShort(View view, int resId, int actionRes, View.OnClickListener handler) {
        Snackbar
                .make(view, resId, Snackbar.LENGTH_SHORT)
                .setAction(actionRes, handler)
                .show();
    }
}
