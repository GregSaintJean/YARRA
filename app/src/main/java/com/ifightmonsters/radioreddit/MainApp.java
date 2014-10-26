package com.ifightmonsters.radioreddit;

import android.app.Application;
import android.content.SharedPreferences;

import com.ifightmonsters.radioreddit.utils.ChronoUtils;

import java.util.Date;

/**
 * Created by Gregory on 10/4/2014.
 */
public class MainApp extends Application {

    private static final long DEFAULT_SYNC_INTERVAL_IN_MINUTES = 20L;
    public static final long DEFAULT_SYNC_INTERVAL
            = DEFAULT_SYNC_INTERVAL_IN_MINUTES *
            ChronoUtils.SECONDS_PER_MINUTE *
            ChronoUtils.MILLISECONDS_PER_SECOND;

    public final Date getLastSyncTimestamp(){
        SharedPreferences pref
                = this.getSharedPreferences(getString(R.string.pref_app), MODE_PRIVATE);

        String timestampDateString = pref.getString(getString(R.string.sync_timestamp), null);

        if(timestampDateString == null){
            return null;
        }

        Date timestampDate = ChronoUtils.generateDate(timestampDateString);
        return timestampDate;
    }

    public final void setSyncTimestamp(){
        SharedPreferences pref
                = this.getSharedPreferences(getString(R.string.pref_app), MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
        Date currentDate = ChronoUtils.getCurrentDate();
        String storageDate = ChronoUtils.getStorageFormattedDate(currentDate);
        editor.putString(getString(R.string.sync_timestamp), storageDate);
        editor.commit();
    }
}
