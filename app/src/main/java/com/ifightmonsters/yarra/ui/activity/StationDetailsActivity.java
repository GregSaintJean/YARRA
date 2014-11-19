package com.ifightmonsters.yarra.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.ui.fragment.StationDetailsFragment;
import com.ifightmonsters.yarra.utils.AndroidUtils;

/**
 * Activity for displaying detailed information about the radio station the user is listening
 * to if the user isn't on a tablet in landscape
 */
public class StationDetailsActivity extends ActionBarActivity {

    private static final String EXTRA = "com.ifightmonsters.yarra.ui.activity.EXTRA";
    public static final String EXTRA_STATION_ID = EXTRA + ".STATION_ID";

    private StationDetailsFragment mFragment;
    private ShareActionProvider mShareActionProvider;
    private String mDownloadUrl;


    private long mStationId;

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
            return;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStationId = args.getLong(StationDetailsFragment.EXTRA_STATION_ID);

        /*
         * onCreate is called when a configuration change is made. We want to handle the configuration
         * change here because of this instead of overriding onConfigurationChanged. onConfigurationChanged
         * is called only when the activity has the attribute android:configChanges in the AndroidManifest.
         * However, when the attribute is used, you have to programmatically handle the changes yourself.
         * In order to avoid a custom unoptimized implementation of orientation changes, I wrote this code
         * here in onCreate instead.
         */
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && AndroidUtils.isTwoPane(this)) {
            launchTwoPane();
            return;
        }

        mFragment = StationDetailsFragment.newInstance(mStationId);

        getSupportFragmentManager()
                .beginTransaction().add(R.id.station_details_container, mFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.station_details_menu, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem shareItem = menu.findItem(R.id.action_share);

        if(mDownloadUrl != null){
            shareItem.setVisible(true);
            mShareActionProvider.setShareIntent(shareUrl(mDownloadUrl));
        } else {
            shareItem.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private Intent shareUrl(String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    public void setDownloadUrl(String url){
        mDownloadUrl = url;
        invalidateOptionsMenu();
    }

    /**
     * Sends information back to the MainActivity to inform it that is should display the fragment
     * this activity is displaying in it's two pane view.
     */
    private void launchTwoPane() {
        Intent intent = new Intent();
        Bundle extra = new Bundle();
        extra.putLong(EXTRA_STATION_ID, mStationId);
        intent.putExtras(extra);
        setResult(RESULT_OK, intent);
        finish();
    }
}
