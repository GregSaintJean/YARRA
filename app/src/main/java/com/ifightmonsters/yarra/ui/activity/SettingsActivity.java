package com.ifightmonsters.yarra.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.sync.RadioRedditSyncAdapter;

/**
 * Created by Gregory on 10/31/2014.
 */
public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals(getString(R.string.pref_sync_interval))){

            RadioRedditSyncAdapter.removePeriodicSync(this);

            int sync_interval = Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_sync_interval),
                    Integer.toString(getResources().getInteger(R.integer.default_sync_interval))));

            int flex_time = getResources().getInteger(R.integer.default_flextime_interval);

            RadioRedditSyncAdapter.configurePeriodicSync(this, sync_interval, flex_time);

        }

    }

}
