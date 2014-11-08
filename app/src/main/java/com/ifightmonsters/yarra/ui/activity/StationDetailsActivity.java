package com.ifightmonsters.yarra.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.ui.fragment.StationDetailsFragment;

public class StationDetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details);
        Bundle args = getIntent().getExtras();
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
