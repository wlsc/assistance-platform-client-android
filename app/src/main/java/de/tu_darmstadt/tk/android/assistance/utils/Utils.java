package de.tu_darmstadt.tk.android.assistance.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Wladimir Schmidt (wlsc.dev@gmail.com) on 07.06.2015.
 */
public class Utils {

    private Utils() {
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

            result = convertBytesToString(bytes);

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

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
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
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }

    /**
     * Shows soft keyboard
     *
     * @param context
     * @param currentFocus
     */
    public static void showKeyboard(Context context, View currentFocus) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }

    /**
     * Saves given stream to file
     *
     * @param dirPath
     * @param inputStream
     */
    public static void saveFile(String dirPath, InputStream inputStream) {

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault());
        String currentTimeStamp = format.format(new Date());

        File imagesFolder = new File(Environment.getExternalStorageDirectory(), dirPath);
        imagesFolder.mkdirs();

        File imageFile = new File(imagesFolder, "assi_" + currentTimeStamp + ".png");

        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(imageFile));
        } catch (IOException e) {
            Log.e("saveFile", "Cannot save file to system!");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e("saveFile", "Cannot close file!");
                }
            }
        }
    }
}
