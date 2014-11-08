package com.ifightmonsters.yarra.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ifightmonsters.yarra.MainApp;
import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.data.YarraProvider;
import com.ifightmonsters.yarra.service.RadioService;
import com.ifightmonsters.yarra.sync.YarraSyncAdapter;
import com.ifightmonsters.yarra.ui.fragment.MainFragment;
import com.ifightmonsters.yarra.ui.fragment.PlaceHolderFragment;
import com.ifightmonsters.yarra.ui.fragment.StationDetailsFragment;
import com.ifightmonsters.yarra.utils.NetworkUtils;

/**
 * First screen the user comes to when they enter the app. Displays a list of radio stations
 * to play or an appropriate error message when there is something wrong.
 */
public class MainActivity extends ActionBarActivity
        implements View.OnClickListener,
        OnFragmentInteractionListener {

    private static final String FRAGMENT_PLACEHOLDER = "fragment_placeholder";

    private long mCurrentStation = -1;

    /* Determines whethers or not the device has network connectivity.
     * Helps with showing the menu button for refreshing data.
     */
    private boolean mHasConnectivity = false;

    /* Determines whethers or not the app is grabbing data from the servers.
     * Helps with showing the menu button for refreshing data.
     */
    private boolean mSyncing = false;

    //For registering broadcast receivers.
    private LocalBroadcastManager mLocalBroadcastMgr;

    private ImageView mErrorIV;
    private TextView mNavTV, mErrorTV;
    private View mMessageContainer;
    private View mStationContainer;
    private View mStationDetailsContainer;
    private ProgressBar mProgress;

    private final BroadcastReceiver mMainActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                return;
            }

            String action = intent.getAction();

            if (action == null) {
                return;
            }

            if (action.equals(RadioService.BROADCAST_ERROR)) {
                Bundle extras = intent.getExtras();

                if (extras == null || !extras.containsKey(RadioService.EXTRA_ERROR)) {
                    displayUnknownErrorToast();
                }

                int error = extras.getInt(RadioService.EXTRA_ERROR);

                Toast.makeText(MainActivity.this,
                        error, Toast.LENGTH_SHORT).show();
            }

            if (action.equals(RadioService.BROADCAST_STATUS)) {
                Bundle extras = intent.getExtras();

                if (extras == null || !extras.containsKey(RadioService.EXTRA_STATUS)) {
                    //TODO Change to set the default title
                    displayUnknownErrorToast();
                }

                int message = extras.getInt(RadioService.EXTRA_STATUS);
                setActionBarTitle(message);
            }

            if (action.equals(RadioService.BROADCAST_KILLED)) {
                placePlaceHolderFragment();
            }

            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                checkNetworkConnectivity();
            }

            if (action.equals(YarraSyncAdapter.BROADCAST_SYNC_BEGIN)) {
                showSyncing(true);
                mSyncing = true;
            }

            if (action.equals(YarraSyncAdapter.BROADCAST_SYNC_COMPLETED)) {
                showSyncing(false);
                mSyncing = false;
            }
        }
    };

    private void displayUnknownErrorToast() {
        Toast.makeText(
                this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApp app = (MainApp) getApplicationContext();

        //We want to prepare the app for syncing data on the first run of the app.
        if (app.isFirstRun()) {
            PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
            YarraSyncAdapter.initializeSyncAdapter(this);
            //After this, every other launch of the app won't rerun this section of code again.
            app.setFirstRun();
        }
        setContentView(R.layout.activity_main);

        mMessageContainer = findViewById(R.id.message_container);
        mStationContainer = findViewById(R.id.station_container);
        mErrorIV = (ImageView) findViewById(R.id.error_iv);
        mErrorTV = (TextView) findViewById(R.id.error_tv);
        mNavTV = (TextView) findViewById(R.id.nav_tv);
        mNavTV.setOnClickListener(this);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mLocalBroadcastMgr = LocalBroadcastManager.getInstance(this);

        if (findViewById(R.id.station_details_container) != null) {
            mStationDetailsContainer = findViewById(R.id.station_details_container);
        }

        if (savedInstanceState == null) {

            Fragment mainFragment = MainFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.station_container, mainFragment).commit();

            if (mStationDetailsContainer != null) {
                Fragment placeHolderFragment
                        = getSupportFragmentManager().findFragmentByTag(FRAGMENT_PLACEHOLDER);

                if (placeHolderFragment == null) {
                    placeHolderFragment = PlaceHolderFragment.newInstance();
                }

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.station_details_container,
                                placeHolderFragment, FRAGMENT_PLACEHOLDER).commit();
            }
        }
    }

    private void setActionBarTitle(int stringRes) {
        getSupportActionBar().setTitle(stringRes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /* We only want the menu option to come up if the device has network connectivity
         * or if the app isn't grabbing data from the servers. There is no way the app can grab data
         * if there is no network connectivity and we don't want to submit more than one request
         * to sync data while the data is already being grabbed.
        */
        menu.findItem(R.id.action_refresh).setVisible(mHasConnectivity && !mSyncing);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                launchSettingsActivity();
                return true;
            case R.id.action_refresh:
                YarraSyncAdapter.syncImmediately(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Responsible for launching the wifi settings screen for the device.
     */
    public void launchWifiSettingsActivity() {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    /**
     * Responsible for launching the settings screen within the app.
     */
    public void launchSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        /* The activity here is being brought back up so we need to listen for anything that might
         * require the UI to change based on the situation
         */
        registerReceivers();
        checkNetworkConnectivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //If the activity is suspended, we don't need to listen to anything that would affect the UI
        unregisterReceivers();
    }

    @Override
    public void onClick(View v) {

        //This only happens when there is no network connectivity.
        if (v.getId() == R.id.nav_tv) {
            launchWifiSettingsActivity();
        }
    }

    /**
     * Registers broadcast receivers responsible for listening in on different actions that
     * happen on the device and within the app.
     */
    private void registerReceivers() {
        //"RadioService.BROADCAST_ERRORResponsible for delivering messages from RadioService within the app
        //"internalFilter" is for listening to broadcasts that happen in the app using a LocalBroadcastManager.
        IntentFilter internalFilter = new IntentFilter(RadioService.BROADCAST_ERROR);
        //Responsible for updates from RadioService as far as what's playing and what state the RadioService is in
        internalFilter.addAction(RadioService.BROADCAST_STATUS);
        //Determines if the RadioService is killed
        internalFilter.addAction(RadioService.BROADCAST_KILLED);
        //Determines when syncing of data against radio reddit is occuring
        internalFilter.addAction(YarraSyncAdapter.BROADCAST_SYNC_BEGIN);
        //Determines when syncing is completed
        internalFilter.addAction(YarraSyncAdapter.BROADCAST_SYNC_COMPLETED);
        //"ConnectivityManager.CONNECTIVITY_ACTION" determines when the network connectivity of the device has changed.
        //The "externalFilter" is for broadcasts that happen on the device.
        IntentFilter externalFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        //Registers filters for broadcasts within the app.
        mLocalBroadcastMgr.registerReceiver(mMainActivityReceiver, internalFilter);
        //Registers filter for broadcasts that happen on the device.
        registerReceiver(mMainActivityReceiver, externalFilter);
    }

    /**
     * Unregisters broadcast receivers listening in on different actions that happens on the device and
     * within the app.
     */
    private void unregisterReceivers() {
        mLocalBroadcastMgr.unregisterReceiver(mMainActivityReceiver);
        unregisterReceiver(mMainActivityReceiver);
    }

    /**
     * Responsible for setting back a default transparent fragment into view when no music
     * is playing
     */

    private void placePlaceHolderFragment() {
        if (mStationDetailsContainer != null) {
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            Fragment nextFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_PLACEHOLDER);
            if (nextFragment == null) {
                nextFragment = PlaceHolderFragment.newInstance();
            }
            trans.replace(R.id.station_details_container, nextFragment, FRAGMENT_PLACEHOLDER);
            trans.commit();
        }
    }

    /**
     * Responsible for handling communication between fragments and activities.
     * Receives uris that will determine the action the activity must take.
     *
     * @param uri uri determines what action the activity must take
     */
    @Override
    public void onFragmentInteraction(Uri uri) {

        final int match = YarraProvider.sUriMatcher.match(uri);

        switch (match) {
            case YarraProvider.STATUS_ID:
                handleStationPlayback(ContentUris.parseId(uri));
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

    }

    public void handleStationPlayback(long id) {
        if (mCurrentStation == id && RadioService.isPlaying()) {
            RadioService.stop(this);
            return;
        }

        RadioService.play(this, id);
        handleFragments(mCurrentStation);
        mCurrentStation = id;
    }

    public void handleFragments(long id) {

        if (id != mCurrentStation) {
            if (mStationDetailsContainer != null) {
                FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
                trans.replace(R.id.station_details_container, StationDetailsFragment.newInstance(id));
                trans.commit();
            } else {
                startStationDetailsActivity(id);
            }
        }
    }

    /**
     * Starts the StationDetailsActivity if the device is not a tablet
     *
     * @param id the id of the station to display information
     */
    private void startStationDetailsActivity(long id) {
        Intent intent = new Intent(this, StationDetailsActivity.class);
        intent.putExtra(StationDetailsFragment.EXTRA_STATION_ID, id);
        startActivity(intent);
    }

    /**
     * Checks the network connectivity of the device. Will change the layout and display
     * an option to enable wifi if the device does not have network connectivity.
     */
    private void checkNetworkConnectivity() {

        if (NetworkUtils.hasNetworkConnectivity(this)) {
            mStationContainer.setVisibility(View.VISIBLE);

            if (mStationDetailsContainer != null) {
                mStationDetailsContainer.setVisibility(View.VISIBLE);
            }

            mMessageContainer.setVisibility(View.GONE);
            mErrorIV.setVisibility(View.GONE);
            mErrorTV.setVisibility(View.GONE);
            mNavTV.setVisibility(View.GONE);
            mHasConnectivity = true;
        } else {

            mStationContainer.setVisibility(View.GONE);

            if (mStationDetailsContainer != null) {
                mStationDetailsContainer.setVisibility(View.GONE);
            }

            mMessageContainer.setVisibility(View.VISIBLE);
            mErrorIV.setVisibility(View.VISIBLE);
            mErrorTV.setVisibility(View.VISIBLE);
            mNavTV.setVisibility(View.VISIBLE);
            mErrorTV.setText(R.string.error_message_connectivity);
            mHasConnectivity = false;
        }

        //If there is no network connectivity, we don't want the option to refresh data.
        invalidateOptionsMenu();
    }

    /**
     * Responsible for displaying a radial progress view or displaying
     * a list of stations for the user to play
     *
     * @param show whether or not the display the progress view or the list of radio stations to play
     */
    private void showSyncing(boolean show) {

        if (show) {
            mProgress.setVisibility(View.VISIBLE);
            mStationContainer.setVisibility(View.GONE);
            mMessageContainer.setVisibility(View.VISIBLE);

            if (mStationDetailsContainer != null) {
                mStationDetailsContainer.setVisibility(View.GONE);
            }

        } else {
            mProgress.setVisibility(View.GONE);
            mStationContainer.setVisibility(View.VISIBLE);
            mMessageContainer.setVisibility(View.GONE);

            if (mStationDetailsContainer != null) {
                mStationDetailsContainer.setVisibility(View.VISIBLE);
            }
        }

        mErrorIV.setVisibility(View.GONE);
        mErrorTV.setVisibility(View.GONE);
        mNavTV.setVisibility(View.GONE);
        //If the app is currently grabbing data from the server, we don't want the option to refresh data.
        invalidateOptionsMenu();
    }

}
