package de.tudarmstadt.informatik.tk.assistance.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.tudarmstadt.informatik.tk.assistance.Config;
import de.tudarmstadt.informatik.tk.assistance.sdk.util.logger.Log;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 07.06.2015
 */
public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    private CommonUtils() {
    }

    /**
     * Hides soft keyboard
     *
     * @param context
     * @param currentFocus
     */
    public static void hideKeyboard(Context context, View currentFocus) {

        if (context == null || currentFocus == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    /**
     * Shows soft keyboard
     *
     * @param context
     * @param currentFocus
     */
    public static void showKeyboard(Context context, View currentFocus) {

        if (context == null || currentFocus == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.toggleSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        }
    }

    /**
     * Saves given image stream to file with picasso library
     *
     * @param uri
     */
    public static void saveFile(final Context context, Uri uri, final String oldFilename) {

        Target target = new Target() {

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {

                new Thread(() -> {

                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                    String currentTimeStamp = format.format(new Date());
                    String filename = oldFilename;

                    if (oldFilename.isEmpty()) {
                        filename = "assi_" + currentTimeStamp;

                        Log.d(TAG, "new user pic filename: " + filename);
                    }

                    PreferenceUtils.setUserPicFilename(context, filename);

                    Log.e(TAG, Environment.getExternalStorageDirectory().getPath() + '/' + Config.USER_PIC_PATH + '/' + filename + ".jpg");

                    File file = new File(Environment.getExternalStorageDirectory().getPath() + '/' + Config.USER_PIC_PATH + '/' + filename + ".jpg");
                    FileOutputStream oStream = null;
                    try {
                        file.createNewFile();

                        oStream = new FileOutputStream(file);

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, oStream);

                    } catch (Exception e) {
                        Log.e(TAG, "Cannot save image to internal file storage! Error: " + e.getMessage());
                    } finally {
                        try {
                            if (oStream != null) {
                                oStream.close();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Cannot close output stream! Error: " + e.getMessage());
                        }
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        // save file to internal storage
        Picasso.with(context)
                .load(uri)
                .into(target);
    }

    /**
     * Hides system ui layout
     *
     * @param window
     */
    public static void hideSystemUI(Window window) {

        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        int visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }

        window.getDecorView().setSystemUiVisibility(visibility);
    }

    /**
     * Shows system ui layout
     *
     * @param window
     */
    public static void showSystemUI(Window window) {

        int visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        window.getDecorView().setSystemUiVisibility(visibility);
    }
}
