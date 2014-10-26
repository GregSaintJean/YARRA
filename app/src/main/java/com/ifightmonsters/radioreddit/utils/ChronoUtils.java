package com.ifightmonsters.radioreddit.utils;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Gregory on 10/24/2014.
 */
public final class ChronoUtils {

    public static final long MILLISECONDS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_MINUTE = 60L;

    private static final String STORE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String getHumanFormattedDate(Date date){
        return DateFormat.getDateTimeInstance().format(date);
    }

    public static final Date getCurrentDate(){
        return new Date();
    }

    private ChronoUtils(){}
}
