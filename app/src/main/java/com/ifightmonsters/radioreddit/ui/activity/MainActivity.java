package com.ifightmonsters.radioreddit.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ifightmonsters.radioreddit.MainApp;
import com.ifightmonsters.radioreddit.R;
import com.ifightmonsters.radioreddit.constant.Station;
import com.ifightmonsters.radioreddit.service.RadioService;
import com.ifightmonsters.radioreddit.sync.RadioRedditSyncAdapter;
import com.ifightmonsters.radioreddit.ui.fragment.MainFragment;
import com.ifightmonsters.radioreddit.ui.fragment.PlaceHolderFragment;
import com.ifightmonsters.radioreddit.ui.fragment.StationDetailsFragment;
import com.ifightmonsters.radioreddit.utils.NetworkUtils;


public class MainActivity extends ActionBarActivity
implements View.OnClickListener{

    private static final String FRAGMENT_MAIN = "fragment_main";
    private static final String FRAGMENT_ELECTRONIC = "fragment_electronic";
    private static final String FRAGMENT_HIPHOP = "fragment_hiphop";
    private static final String FRAGMENT_INDIE = "fragment_indie";
    private static final String FRAGMENT_ROCK = "fragment_rock";
    private static final String FRAGMENT_METAL = "fragment_metal";
    private static final String FRAGMENT_RANDOM = "fragment_random";
    private static final String FRAGMENT_TALK = "fragment_talk";
    private static final String FRAGMENT_PLACEHOLDER = "fragment_placeholder";

    private static final String ACTIVITY_AUTHORITY = "com.ifightmonsters.radioreddit.ui.activity.MainActivity";
    public static final Uri BASE_ACTIVITY_URI = Uri.parse("app://" + ACTIVITY_AUTHORITY);

    public static final String PATH_STATION = "station";

    public static final int STATION = 1;
    private long mCurrentStation = -1;

    private boolean mHasConnectivity = false;
    private boolean mSyncing = false;

    private static UriMatcher sUriMatcher = buildUriMatcher();

    private LocalBroadcastManager mLocalBroadcastMgr;
    private ImageView mErrorIV;
    private TextView mNavTV, mErrorTV;
    private View mMessageContainer;
    private View mStationContainer;
    private View mStationDetailsContainer;
    private ProgressBar mProgress;

    private final BroadcastReceiver mMainActivityReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if(context == null || intent == null){
                return;
            }

            String action = intent.getAction();

            if(action == null){
                return;
            }

            if(action.equals(RadioService.BROADCAST_ERROR)){
                Bundle extras = intent.getExtras();

                if(extras == null || !extras.containsKey(RadioService.EXTRA_ERROR)){
                    displayUnknownErrorToast();
                }

                int error = extras.getInt(RadioService.EXTRA_ERROR);

                Toast.makeText(MainActivity.this,
                        error, Toast.LENGTH_SHORT).show();
            }

            if(action.equals(RadioService.BROADCAST_STATUS)){
                Bundle extras = intent.getExtras();

                if(extras == null || !extras.containsKey(RadioService.EXTRA_STATUS)){
                    //TODO Change to set the default title
                    displayUnknownErrorToast();
                }

                int message = extras.getInt(RadioService.EXTRA_STATUS);
                setActionBarTitle(message);
            }

            if(action.equals(RadioService.BROADCAST_KILLED)){
                placePlaceHolderFragment();
            }

            if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                checkNetworkConnectivity();
            }

            if(action.equals(RadioRedditSyncAdapter.BROADCAST_SYNC_BEGIN)){
                showSyncing(true);
                mSyncing = true;
            }

            if(action.equals(RadioRedditSyncAdapter.BROADCAST_SYNC_COMPLETED)){
                showSyncing(false);
                mSyncing = false;
            }
        }
    };

    private void displayUnknownErrorToast(){
        Toast.makeText(
                this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
    }

    private static final UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(ACTIVITY_AUTHORITY, PATH_STATION + "/#", STATION);
        return matcher;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApp app = (MainApp)getApplicationContext();
        if(app.isFirstRun()){
            PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
            RadioRedditSyncAdapter.initializeSyncAdapter(this);
            app.setFirstRun();
        }
        setContentView(R.layout.activity_main);

        mMessageContainer = findViewById(R.id.message_container);
        mStationContainer = findViewById(R.id.station_container);
        mErrorIV = (ImageView)findViewById(R.id.error_iv);
        mErrorTV = (TextView)findViewById(R.id.error_tv);
        mNavTV = (TextView)findViewById(R.id.nav_tv);
        mNavTV.setOnClickListener(this);
        mProgress = (ProgressBar)findViewById(R.id.progress);
        mLocalBroadcastMgr = LocalBroadcastManager.getInstance(this);

        if(savedInstanceState == null){

            Fragment mainFragment = MainFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.station_container, mainFragment).commit();

            if(findViewById(R.id.station_details_container) != null){

                mStationDetailsContainer = findViewById(R.id.station_details_container);

                Fragment placeHolderFragment
                        = getSupportFragmentManager().findFragmentByTag(FRAGMENT_PLACEHOLDER);

                if(placeHolderFragment == null){
                    placeHolderFragment = PlaceHolderFragment.newInstance();
                }

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.station_details_container,
                                placeHolderFragment, FRAGMENT_PLACEHOLDER).commit();
            }
        }
    }

    private void setActionBarTitle(int stringRes){
        ActionBar bar = getSupportActionBar();
        bar.setTitle(stringRes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_refresh).setVisible(mHasConnectivity && !mSyncing);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                launchSettingsActivity();
                return true;
            case R.id.action_refresh:
                RadioRedditSyncAdapter.syncImmediately(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void launchWifiSettingsActivity(){
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    public void launchSettingsActivity(){
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceivers();
        checkNetworkConnectivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceivers();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.nav_tv){
            launchWifiSettingsActivity();
        }
    }

    private void registerReceivers(){
        IntentFilter internalFilter = new IntentFilter(RadioService.BROADCAST_ERROR);
        internalFilter.addAction(RadioService.BROADCAST_STATUS);
        internalFilter.addAction(RadioService.BROADCAST_KILLED);
        internalFilter.addAction(RadioRedditSyncAdapter.BROADCAST_SYNC_BEGIN);
        internalFilter.addAction(RadioRedditSyncAdapter.BROADCAST_SYNC_COMPLETED);
        IntentFilter externalFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mLocalBroadcastMgr.registerReceiver(mMainActivityReceiver, internalFilter);
        registerReceiver(mMainActivityReceiver, externalFilter);
    }

    private void unregisterReceivers(){
        mLocalBroadcastMgr.unregisterReceiver(mMainActivityReceiver);
        unregisterReceiver(mMainActivityReceiver);
    }

    public void onFragmentInteraction(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch(match){
            case STATION:
                handleStation(ContentUris.parseId(uri));
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

    }

    private void handleStation(long id){

        if(mCurrentStation == id && RadioService.isPlaying()){
            RadioService.stop(this);
            return;
        }

        RadioService.play(this, (int)id);
        mCurrentStation = id;

        placeCorrectFragment();
    }

    private void placeCorrectFragment(){

        if(findViewById(R.id.station_details_container) != null){

            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            Fragment nextFragment;

            switch((int)mCurrentStation){
                case Station.MAIN:
                default:
                    nextFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_MAIN);
                    break;
                case Station.ELECTRONIC:
                    nextFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_ELECTRONIC);
                    break;
                case Station.HIPHOP:
                    nextFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_HIPHOP);
                    break;
                case Station.INDIE:
                    nextFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_INDIE);
                    break;
                case Station.METAL:
                    nextFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_METAL);
                    break;
                case Station.RANDOM:
                    nextFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_RANDOM);
                    break;
                case Station.ROCK:
                    nextFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_ROCK);
                    break;
                case Station.TALK:
                    nextFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TALK);
                    break;
            }

            if(nextFragment == null){
                nextFragment = StationDetailsFragment.newInstance();
            }

            switch((int)mCurrentStation){
                case Station.MAIN:
                default:
                    trans.replace(R.id.station_details_container, nextFragment,
                            FRAGMENT_MAIN);
                    break;
                case Station.ELECTRONIC:
                    trans.replace(R.id.station_details_container, nextFragment,
                            FRAGMENT_ELECTRONIC);
                    break;
                case Station.HIPHOP:
                    trans.replace(R.id.station_details_container, nextFragment,
                            FRAGMENT_HIPHOP);
                    break;
                case Station.INDIE:
                    trans.replace(R.id.station_details_container, nextFragment,
                            FRAGMENT_INDIE);
                    break;
                case Station.METAL:
                    trans.replace(R.id.station_details_container, nextFragment,
                            FRAGMENT_METAL);
                    break;
                case Station.RANDOM:
                    trans.replace(R.id.station_details_container, nextFragment,
                            FRAGMENT_RANDOM);
                    break;
                case Station.ROCK:
                    trans.replace(R.id.station_details_container, nextFragment,
                            FRAGMENT_ROCK);
                    break;
                case Station.TALK:
                    trans.replace(R.id.station_details_container, nextFragment,
                            FRAGMENT_TALK);
                    break;
            }

            trans.commit();

        }

    }

    private void placePlaceHolderFragment(){
        if(findViewById(R.id.station_details_container) != null){
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            Fragment nextFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_PLACEHOLDER);
            if(nextFragment == null){
                nextFragment = PlaceHolderFragment.newInstance();
            }
            trans.replace(R.id.station_details_container, nextFragment, FRAGMENT_PLACEHOLDER);
            trans.commit();
        }
    }

    private void checkNetworkConnectivity(){
        if(NetworkUtils.hasNetworkConnectivity(this)){
            mStationContainer.setVisibility(View.VISIBLE);

            if(findViewById(R.id.station_details_container) != null){
                mStationDetailsContainer.setVisibility(View.VISIBLE);
            }

            mMessageContainer.setVisibility(View.GONE);
            mErrorIV.setVisibility(View.GONE);
            mErrorTV.setVisibility(View.GONE);
            mNavTV.setVisibility(View.GONE);
            mHasConnectivity = true;
        } else {

            mStationContainer.setVisibility(View.GONE);

            if(findViewById(R.id.station_details_container) != null){
                mStationDetailsContainer.setVisibility(View.GONE);
            }

            mMessageContainer.setVisibility(View.VISIBLE);
            mErrorIV.setVisibility(View.VISIBLE);
            mErrorTV.setVisibility(View.VISIBLE);
            mNavTV.setVisibility(View.VISIBLE);
            mErrorTV.setText(R.string.error_message_connectivity);
            mHasConnectivity = false;
        }

        invalidateOptionsMenu();
    }

    private void showSyncing(boolean show){

        if(show){
            mProgress.setVisibility(View.VISIBLE);
            mStationContainer.setVisibility(View.GONE);
            mMessageContainer.setVisibility(View.VISIBLE);

            if(findViewById(R.id.station_details_container) != null){
                mStationDetailsContainer.setVisibility(View.GONE);
            }

        } else {
            mProgress.setVisibility(View.GONE);
            mStationContainer.setVisibility(View.VISIBLE);
            mMessageContainer.setVisibility(View.GONE);

            if(findViewById(R.id.station_details_container) != null){
                mStationDetailsContainer.setVisibility(View.VISIBLE);
            }
        }

        mErrorIV.setVisibility(View.GONE);
        mErrorTV.setVisibility(View.GONE);
        mNavTV.setVisibility(View.GONE);
        invalidateOptionsMenu();

    }
}
