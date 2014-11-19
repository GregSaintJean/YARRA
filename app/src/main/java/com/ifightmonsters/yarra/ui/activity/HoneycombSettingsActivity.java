package com.ifightmonsters.yarra.ui.activity;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.ui.fragment.SettingsFragment;

/**
 * Created by Gregory on 11/19/2014.
 */
@TargetApi(11)
public class HoneycombSettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_honeycomb);
        /*
         * I'll try to theme this correctly as soon as I can figure out how theming works for the
          * toolbar. Just putting the actionbar theme on the toolbar doesn't seem to
          * be working.
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}