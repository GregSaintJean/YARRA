package com.ifightmonsters.radioreddit.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Gregory on 10/24/2014.
 */
public final class ChronoUtils {

    private static final String LOG = "ChronoUtils";

    public static final long MILLISECONDS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long MINUTES_PER_HOUR = 60L;
    public static final long HOURS_PER_DAY = 24L;
    public static final long DAYS_PER_WEEK = 7L;

    private static final String STORE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String getHumanFormattedDate(Date date){
        return DateFormat.getDateTimeInstance().format(date);
    }

    public static final String getStorageFormattedDate(Date date){
        return new SimpleDateFormat(STORE_DATE_FORMAT).format(date);
    }

    public static final Date getCurrentDate(){
        return new Date();
    }

    public static final Date generateDate(String timestamp){
        SimpleDateFormat df = new SimpleDateFormat(STORE_DATE_FORMAT);
        try{
            return df.parse(timestamp);
        } catch(ParseException e){
            Log.e(LOG, e.toString());
            return null;
        }
    }

    private ChronoUtils(){}
}
