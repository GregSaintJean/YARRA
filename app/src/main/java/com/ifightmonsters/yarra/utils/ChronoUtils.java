package com.ifightmonsters.yarra.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Gregory on 10/31/2014.
 */
public final class ChronoUtils {

    private static final String LOG = "ChronoUtils";

    public static final long MILLISECONDS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_MINUTE = 60L;

    private static final String STORE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String getHumanFormattedDate(Date date) {
        return DateFormat.getDateTimeInstance().format(date);
    }

    public static final String getStorageFormattedDate(Date date) {
        return new SimpleDateFormat(STORE_DATE_FORMAT).format(date);
    }

    public static final Date getCurrentDate() {
        return new Date();
    }

    public static final boolean isDateOldEnough(Date oldDate, long interval) {

        Calendar oldCal = Calendar.getInstance();
        oldCal.setTime(oldDate);

        return Calendar.getInstance().getTimeInMillis() - oldCal.getTimeInMillis() > interval;
    }

    public static final Date generateDate(String timestamp) {
        SimpleDateFormat df = new SimpleDateFormat(STORE_DATE_FORMAT);
        try {
            return df.parse(timestamp);
        } catch (ParseException e) {
            Log.e(LOG, e.toString());
            return null;
        }
    }

    private ChronoUtils() {
    }
}
