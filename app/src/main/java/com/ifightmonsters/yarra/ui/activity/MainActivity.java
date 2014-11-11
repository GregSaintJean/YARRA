package com.ifightmonsters.yarra.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
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
import com.ifightmonsters.yarra.utils.AndroidUtils;
import com.ifightmonsters.yarra.utils.NetworkUtils;

/**
 * First screen the user comes to when they enter the app. Displays a list of radio stations
 * to play or an appropriate error message when there is something wrong.
 */
public class MainActivity extends ActionBarActivity
        implements View.OnClickListener,
        OnFragmentInteractionListener {

    private static final String FRAGMENT_PLACEHOLDER = "fragment_placeholder";
    private static final String CURRENT_STATION_KEY = "current_station_key";
    private static final String BRING_ACTIVITY_BACK_KEY = "bring_activity_back_key";

    private static final int REQUEST_CODE_STATION_DETAILS = 1;

    private long mCurrentStation = -1;

    /* Determines whethers or not the device has network connectivity.
     * Helps with showing the menu button for refreshing data.
     */
    private boolean mHasConnectivity = false;

    /* Determines whethers or not the app is grabbing data from the servers.
     * Helps with showing the menu button for refreshing data.
     */
    private boolean mSyncing = false;

    private boolean mBringUpFragment = false;
    private boolean mBringUpActivity = false;

    //For listening to broadcasts within the app
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

            /**
             * Whatever error messages come back from the radio service,
             * we wish to display on the screen.
             */
            if (action.equals(RadioService.BROADCAST_ERROR)) {
                Bundle extras = intent.getExtras();

                if (extras == null || !extras.containsKey(RadioService.EXTRA_ERROR)) {
                    displayUnknownErrorToast();
                }

                int error = extras.getInt(RadioService.EXTRA_ERROR);

                if (error != 0) {
                    Toast.makeText(MainActivity.this,
                            error, Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * We want to update the title bar with the appropriate status from the radio service
             */
            if (action.equals(RadioService.BROADCAST_STATUS)) {
                Bundle extras = intent.getExtras();

                if (extras == null || !extras.containsKey(RadioService.EXTRA_STATUS)) {
                    setActionBarTitle(R.string.app_name);
                    displayUnknownErrorToast();
                }

                int message = extras.getInt(RadioService.EXTRA_STATUS);
                if (message != 0) {
                    setActionBarTitle(message);
                }
            }

            /**
             * When the radio service is killed, we want to reset the position
             * on the listview/gridview
             */
            if (action.equals(RadioService.BROADCAST_KILLED)) {
                placePlaceHolderFragment();
                mCurrentStation = -1;
                setActionBarTitle(R.string.app_name);
            }

            /**
             * If the network connectivity status has changed, we want to disable the ability to
             * sync with the servers since attempting to sync might cause a crash. We also want to display
             * the correct error message and a possible option on how to regain network connectivity.
             */
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                checkNetworkConnectivity();
            }

            /**
             * When the network is syncing, we want to disable the ability to send further request to sync
             * with the servers.
             */
            if (action.equals(YarraSyncAdapter.BROADCAST_SYNC_BEGIN)) {
                showSyncing(true);
                mSyncing = true;
            }

            /**
             * When the syncing is complete, we want to reenabled the ability to sync with the servers.
             */
            if (action.equals(YarraSyncAdapter.BROADCAST_SYNC_COMPLETED)) {
                showSyncing(false);
                mSyncing = false;
            }
        }
    };

    /**
     * In case the radio service broadcasts an error state but with no error message.
     */
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

        if (savedInstanceState == null || savedInstanceState.isEmpty()) {

            /**
             * We want to set a default state for the app if the app just came on or restored and
             * there is nothing streaming.
             */
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
        } else {

            if (savedInstanceState.containsKey(CURRENT_STATION_KEY)) {
                mCurrentStation = savedInstanceState.getLong(CURRENT_STATION_KEY);
            }

            if (savedInstanceState.containsKey(BRING_ACTIVITY_BACK_KEY)) {
                mBringUpActivity = savedInstanceState.getBoolean(BRING_ACTIVITY_BACK_KEY);
            }

        }

        /*
         * onCreate is called when a configuration change is made. We want to handle the configuration
         * change here because of this instead of overriding onConfigurationChanged. onConfigurationChanged
         * is called only when the activity has the attribute android:configChanges in the AndroidManifest.
         * However, when the attribute is used, you have to programmatically handle the changes yourself.
         * In order to avoid a custom unoptimized implementation of orientation changes, I wrote this code
         * here in onCreate instead.
         */

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                && AndroidUtils.isTwoPane(this)
                && mCurrentStation != -1
                && mBringUpActivity) {
            placePlaceHolderFragment();
            startStationDetailsActivity(mCurrentStation);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(CURRENT_STATION_KEY, mCurrentStation);
        outState.putBoolean(BRING_ACTIVITY_BACK_KEY, mBringUpActivity);
        super.onSaveInstanceState(outState);
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
    private void launchWifiSettingsActivity() {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    /**
     * Responsible for launching the settings screen within the app.
     */
    private void launchSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        /* The activity here is being brought back up so we need to listen for anything that might
         * require the UI to change based on the situation
         */
        registerReceivers();
        /*
         * When the activity is bought back into view, a check must be done to see if there
         * is network connectivity to determine whether or not the user should be able to play
         * a station.
         */
        checkNetworkConnectivity();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mBringUpFragment) {
            displayAppropriateFragment(mCurrentStation);
            mBringUpFragment = false;
        }
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
            mBringUpActivity = false;
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

    /**
     * Attempts to start or stop music playback from RadioService. This should only be done if
     * data is available in the database from servers containing relay urls and there is network
     * connectivity. It will also attempt to either launch an activity or show a fragment
     * displaying detailed information about the radio station.
     *
     * @param id primary key of radio station in database
     */
    private void handleStationPlayback(long id) {

        /**
         * If another request is made to the same station after the station is already playing,
         * this is an indication that the user wishes to stop playback. So here, we stop playback.
         */
        if (mCurrentStation == id && RadioService.isPlaying()) {
            RadioService.stop(this);
            mBringUpActivity = false;
            mCurrentStation = -1;
            return;
        }

        /**
         * When the user requests playback we want to display the appropriate fragment or activity
         * that gives detailed information about the station playing.
         */
        RadioService.play(this, id);
        handlingDetailedRadioStationInformation(id);
        mCurrentStation = id;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_STATION_DETAILS) {

            /**
             * We use this to bring up the correct fragment if the user rotates the screen and the device
             * supports a two-pane layout. We call startActivityForResult to bring up the activity but
             * if the user rotates the screen, it comes out of the activity and displays the second pane
             * with the same information as what is displayed in StationDetailsActivity. We setResult on rotation
             * and pass a long which represents the radio station in the database. We use the long to restore
             * the data in the second pane.
             */

            if (data != null
                    && data.getExtras() != null
                    && !data.getExtras().isEmpty()) {

                Bundle extras = data.getExtras();
                if (extras.containsKey(StationDetailsActivity.EXTRA_STATION_ID)) {
                    mCurrentStation = extras.getLong(StationDetailsActivity.EXTRA_STATION_ID);
                    mBringUpFragment = true;
                } else {
                    mBringUpActivity = false;
                }
            }

        }
    }

    /**
     * Attempts to either launch an activity or show a fragment displaying detailed information
     * about the station that is being played.
     *
     * @param id primary key of radio station in database
     */
    private void handlingDetailedRadioStationInformation(long id) {

        if (id != mCurrentStation) {

            if (mStationDetailsContainer != null) {
                displayAppropriateFragment(id);
            } else {
                startStationDetailsActivity(id);
            }
        }
    }

    /**
     * Displays a fragment on a device that supports a two pane view in landscape orientation
     * containing detailed information about the station being played
     *
     * @param id primary key of radio station in database
     */
    private void displayAppropriateFragment(long id) {
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.station_details_container, StationDetailsFragment.newInstance(id));
        trans.commit();
        mBringUpActivity = true;
    }

    /**
     * Starts the StationDetailsActivity if the device is not a tablet
     *
     * @param id the id of the station to display information
     */
    private void startStationDetailsActivity(long id) {
        Intent intent = new Intent(this, StationDetailsActivity.class);
        intent.putExtra(StationDetailsFragment.EXTRA_STATION_ID, id);
        startActivityForResult(intent, REQUEST_CODE_STATION_DETAILS);
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
