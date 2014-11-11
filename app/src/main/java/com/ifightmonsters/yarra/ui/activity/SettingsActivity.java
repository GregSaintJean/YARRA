package com.ifightmonsters.yarra.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.sync.YarraSyncAdapter;

/**
 * Screen responible for giving the different setting options available for the app
 */
public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

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

        /**
         * When the sync interval is changed, we want to update the syncadapter right away.
         */
        if (key.equals(getString(R.string.pref_sync_interval))) {

            YarraSyncAdapter.removePeriodicSync(this);

            int sync_interval = Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_sync_interval),
                    Integer.toString(getResources().getInteger(R.integer.default_sync_interval))));

            int flex_time = getResources().getInteger(R.integer.default_flextime_interval);

            YarraSyncAdapter.configurePeriodicSync(this, sync_interval, flex_time);

        }

    }

}
