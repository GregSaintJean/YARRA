package com.ifightmonsters.yarra.ui.fragment;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.sync.YarraSyncAdapter;

/**
 * Created by Gregory on 11/19/2014.
 */
@TargetApi(11)
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        /**
         * When the sync interval is changed, we want to update the syncadapter right away.
         */
        if (key.equals(getString(R.string.pref_sync_interval))) {

            YarraSyncAdapter.removePeriodicSync(getActivity());

            int sync_interval = Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_sync_interval),
                    Integer.toString(getResources().getInteger(R.integer.default_sync_interval))));

            int flex_time = getResources().getInteger(R.integer.default_flextime_interval);

            YarraSyncAdapter.configurePeriodicSync(getActivity(), sync_interval, flex_time);

        }

    }

}
