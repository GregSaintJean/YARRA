package com.ifightmonsters.yarra.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.ui.fragment.StationDetailsFragment;

/**
 * Activity for displaying detailed information about the radio station the user is listening
 * to if the user isn't on a tablet in landscape
 */
public class StationDetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details);
        Bundle args = getIntent().getExtras();
        /*
         * We can't display any data from the database if the appropriate id isn't passed.
         */
        if (args == null || !args.containsKey(StationDetailsFragment.EXTRA_STATION_ID)) {
            finish();
        }
        StationDetailsFragment fragment =
                StationDetailsFragment
                        .newInstance(args.getLong(StationDetailsFragment.EXTRA_STATION_ID));
        getSupportFragmentManager()
                .beginTransaction().add(R.id.station_details_container, fragment).commit();
    }
}
