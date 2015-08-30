package de.tu_darmstadt.tk.android.assistance.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Wladimir Schmidt (wlsc.dev@gmail.com)
 * @date 30.08.2015
 */
public class DateUtils {

    /**
     * http://www.w3.org/TR/NOTE-datetime
     */
    private static final String DATE_ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private DateUtils() {
    }

    /**
     * Converts given date to ISO 8601 format
     *
     * @param date
     * @param locale
     * @return
     */
    public static String dateToISO8601String(Date date, Locale locale) {

        DateFormat dateFormat = null;

        if (locale == null) {
            dateFormat = new SimpleDateFormat(DATE_ISO8601_FORMAT, Locale.US);
        } else {
            dateFormat = new SimpleDateFormat(DATE_ISO8601_FORMAT, locale);
        }

        TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");

        dateFormat.setTimeZone(timeZoneUTC);
        return dateFormat.format(date);
    }
}
