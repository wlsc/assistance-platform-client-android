package de.tudarmstadt.informatik.tk.android.assistance.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.tudarmstadt.informatik.tk.android.assistance.Config;

/**
 * Created by Wladimir Schmidt (wlsc.dev@gmail.com) on 07.06.2015.
 */
public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    private CommonUtils() {
    }

    /**
     * Generates SHA256 hash in HEX of a given string
     *
     * @param someString
     * @return
     */
    public static String generateSHA256(String someString) {

        MessageDigest md = null;
        String result = "";

        try {
            md = MessageDigest.getInstance("SHA-256");

            md.update(someString.getBytes());

            byte bytes[] = md.digest();

            result = CommonUtils.convertBytesToString(bytes);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Converts bytes to string
     *
     * @param bytes
     * @return
     */
    public static String convertBytesToString(byte[] bytes) {

        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
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

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                        String currentTimeStamp = format.format(new Date());
                        String filename = oldFilename;

                        if (oldFilename.isEmpty()) {
                            filename = "assi_" + currentTimeStamp;

                            Log.d(TAG, "new user pic filename: " + filename);
                        }

                        PreferenceUtils.setUserPicFilename(context, filename);

                        Log.e(TAG, Environment.getExternalStorageDirectory().getPath() + "/" + Config.USER_PIC_PATH + "/" + filename + ".jpg");

                        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + Config.USER_PIC_PATH + "/" + filename + ".jpg");
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
