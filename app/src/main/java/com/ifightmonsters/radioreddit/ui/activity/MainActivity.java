package com.ifightmonsters.radioreddit.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.ifightmonsters.radioreddit.R;
import com.ifightmonsters.radioreddit.service.RadioService;
import com.ifightmonsters.radioreddit.sync.RadioRedditSyncAdapter;
import com.ifightmonsters.radioreddit.ui.fragment.MainFragment;


public class MainActivity extends ActionBarActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    //TODO Implement a network check
    private static final String LOG = "MainActivity";

    private static final String ACTIVITY_AUTHORITY = "com.ifightmonsters.radioreddit.ui.activity.MainActivity";
    public static final Uri BASE_ACTIVITY_URI = Uri.parse("app://" + ACTIVITY_AUTHORITY);

    public static final String PATH_STATION = "station";

    public static final int STATION = 1;
    private long mCurrentStation = -1;

    private static UriMatcher sUriMatcher = buildUriMatcher();

    private LocalBroadcastManager mLocalBroadcastMgr;
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
        setContentView(R.layout.activity_main);

        mLocalBroadcastMgr = LocalBroadcastManager.getInstance(this);

        if(savedInstanceState == null){
            MainFragment fragment = MainFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment).commit();
        }
    }

    private void setActionBarTitle(int stringRes){
        ActionBar bar = getSupportActionBar();
        bar.setTitle(stringRes);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceivers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceivers();
    }

    private void registerReceivers(){
        IntentFilter localFilter = new IntentFilter(RadioService.BROADCAST_ERROR);
        localFilter.addAction(RadioService.BROADCAST_STATUS);
        mLocalBroadcastMgr.registerReceiver(mMainActivityReceiver, localFilter);
    }

    private void unregisterReceivers(){
        mLocalBroadcastMgr.unregisterReceiver(mMainActivityReceiver);
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

        Log.d(LOG, "Uri id: " + id);

        if(mCurrentStation == id && RadioService.isPlaying()){
            RadioService.stop(this);
            return;
        }

        RadioService.play(this, (int)id);
        mCurrentStation = id;

    }

    @Override
    public void onRefresh() {
        RadioRedditSyncAdapter.syncImmediately(this);
    }
}
