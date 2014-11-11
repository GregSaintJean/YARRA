package com.ifightmonsters.yarra;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.ifightmonsters.yarra.utils.ChronoUtils;

import java.util.Date;

/**
 * Created by Gregory on 10/31/2014.
 */
public class MainApp extends Application {

    public final boolean isFirstRun() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getBoolean(getString(R.string.first_launch), true);
    }

    public final boolean setFirstRun() {
        SharedPreferences.Editor editor
                = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(getString((R.string.first_launch)), false);
        return editor.commit();
    }

    public final Date getLastSyncTimestamp() {
        SharedPreferences sharedPref
                = PreferenceManager.getDefaultSharedPreferences(this);

        String timestampDateString = sharedPref.getString(getString(R.string.sync_timestamp), "");

        if (TextUtils.isEmpty(timestampDateString)) {
            return null;
        }

        Date timestampDate = ChronoUtils.generateDate(timestampDateString);
        return timestampDate;
    }

    public final void setSyncTimestamp() {
        SharedPreferences sharedPref
                = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = sharedPref.edit();
        Date currentDate = ChronoUtils.getCurrentDate();
        String storageDate = ChronoUtils.getStorageFormattedDate(currentDate);
        editor.putString(getString(R.string.sync_timestamp), storageDate);
        editor.commit();
    }

}
