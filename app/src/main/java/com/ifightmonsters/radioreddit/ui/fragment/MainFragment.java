package com.ifightmonsters.radioreddit.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ifightmonsters.radioreddit.MainApp;
import com.ifightmonsters.radioreddit.R;
import com.ifightmonsters.radioreddit.data.RadioRedditContract;
import com.ifightmonsters.radioreddit.data.RadioRedditDbHelper;
import com.ifightmonsters.radioreddit.sync.RadioRedditSyncAdapter;
import com.ifightmonsters.radioreddit.ui.activity.MainActivity;
import com.ifightmonsters.radioreddit.ui.activity.SettingsActivity;
import com.ifightmonsters.radioreddit.utils.ChronoUtils;
import com.ifightmonsters.radioreddit.utils.NetworkUtils;

import java.util.Date;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final String LOG = "MainFragment";

    private static final String OUTPUT_TV_KEY = "output_tv";
    private String songSortOrder = RadioRedditContract.Song.COLUMN_STATUS_ID + " DESC";

    private static final String[] SONG_PROJECTION = {
            RadioRedditContract.Song.TABLE_NAME + "." + RadioRedditContract.Song._ID,
            RadioRedditContract.Song.COLUMN_REDDIT_ID,
            RadioRedditContract.Song.COLUMN_STATUS_ID,
            RadioRedditContract.Song.COLUMN_TITLE,
            RadioRedditContract.Song.COLUMN_ARTIST,
            RadioRedditContract.Song.COLUMN_ALBUM,
            RadioRedditContract.Song.COLUMN_GENRE,
            RadioRedditContract.Song.COLUMN_SCORE,
            RadioRedditContract.Song.COLUMN_REDDIT_TITLE,
            RadioRedditContract.Song.COLUMN_REDDIT_URL,
            RadioRedditContract.Song.COLUMN_REDDITOR,
            RadioRedditContract.Song.COLUMN_DOWNLOAD_URL,
            RadioRedditContract.Song.COLUMN_PREVIEW_URL
    };

    private LocalBroadcastManager mLocalMgr;

    private final BroadcastReceiver mMainFragmentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(context == null || intent == null){
                return;
            }

            String action = intent.getAction();

            if(action == null){
                return;
            }

            if(action.equals(RadioRedditSyncAdapter.BROADCAST_SYNC_COMPLETED)){
                mSyncIndicator.setText(getLastSyncTimeStamp());
            }

            if(action.equals(RadioRedditSyncAdapter.BROADCAST_SYNC_BEGIN)){
                mSyncIndicator.setText(R.string.sync_status_syncing);
            }

            if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                checkNetworkConnectivity();
            }

        }
    };

    private int SONG_LOADER = 0;
    private boolean mHasConnectivity = false;

    private MainApp mApp;
    private TextView mSyncIndicator;
    private TextView mNavTextView;
    private MainActivity mActivity;
    private ListView mListView;
    private View mErrorContainer;
    private ProgressBar mProgress;
    private CursorAdapter mAdapter;

    public static MainFragment newInstance(){
        return new MainFragment();
    }

    public MainFragment() {}

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(SONG_LOADER, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocalMgr = LocalBroadcastManager.getInstance(mActivity);
        mApp =  (MainApp)mActivity.getApplication();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView)v.findViewById(R.id.list);
        mSyncIndicator = (TextView)inflater.inflate(R.layout.list_station_header, null, false);
        mSyncIndicator.setText(getLastSyncTimeStamp());
        mListView.addHeaderView(mSyncIndicator);
        mAdapter = new StationCursorAdapter(mActivity, null, false);
        mListView.setAdapter(mAdapter);
        mErrorContainer = v.findViewById(R.id.error_container);
        mProgress = (ProgressBar)v.findViewById(R.id.progress);
        mNavTextView = (TextView)v.findViewById(R.id.nav_text);
        mNavTextView.setOnClickListener(this);
        return v;
    }

    private void registerReceivers(){
        mActivity.registerReceiver(mMainFragmentReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        IntentFilter internalFilter =
                new IntentFilter(RadioRedditSyncAdapter.BROADCAST_SYNC_COMPLETED);
        internalFilter.addAction(RadioRedditSyncAdapter.BROADCAST_SYNC_BEGIN);
        mLocalMgr.registerReceiver(mMainFragmentReceiver, internalFilter);
    }

    private void unregisterReceivers(){
        mActivity.unregisterReceiver(mMainFragmentReceiver);
        mLocalMgr.unregisterReceiver(mMainFragmentReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceivers();
        checkNetworkConnectivity();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceivers();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_refresh).setVisible(mHasConnectivity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                launchSettingsActivity();
                return true;
            case R.id.action_refresh:
                RadioRedditSyncAdapter.syncImmediately(mActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                RadioRedditContract.Song.CONTENT_URI,
                SONG_PROJECTION,
                null,
                null,
                songSortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoaderLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoaderLoader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    public void launchWifiSettingsActivity(){
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    public void launchSettingsActivity(){
        startActivity(new Intent(getActivity(), SettingsActivity.class));
    }

    private void checkNetworkConnectivity(){
        if(NetworkUtils.hasNetworkConnectivity(mActivity)){
            mListView.setVisibility(View.VISIBLE);
            mErrorContainer.setVisibility(View.GONE);
            mHasConnectivity = true;
        } else {
            mListView.setVisibility(View.GONE);
            mErrorContainer.setVisibility(View.VISIBLE);
            mHasConnectivity = false;
        }

        mActivity.invalidateOptionsMenu();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.nav_text){
            launchWifiSettingsActivity();
        }
    }

    public class StationCursorAdapter extends CursorAdapter{

        private String[] mStation;

        public StationCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mStation = context.getResources().getStringArray(R.array.stations);
        }

        public StationCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(context).inflate(R.layout.list_item_station, viewGroup, false);
        }

        @Override
        public void bindView(View view, final Context context, Cursor cursor) {

            final int position = cursor.getPosition();

            ((TextView)view.findViewById(R.id.station_name)).setText(mStation[cursor.getPosition()]);

            String artist_name = String.format(context.getString(R.string.artist_name),
                    cursor.getString(RadioRedditDbHelper.SONG_COLUMN_ARTIST));
            String song_name = String.format(context.getString(R.string.song_name),
                    cursor.getString(RadioRedditDbHelper.SONG_COLUMN_TITLE));
            String score = cursor.getString(RadioRedditDbHelper.SONG_COLUMN_SCORE);
            score = TextUtils.isEmpty(score) ? "0" : score;

            score = String.format(context.getString(R.string.score),
                    score);

            ((TextView)view.findViewById(R.id.artist_name)).setText(artist_name);
            ((TextView)view.findViewById(R.id.song_name)).setText(song_name);
            ((TextView)view.findViewById(R.id.score)).setText(score);


            ImageButton toggleBtn = (ImageButton)view.findViewById(R.id.toggle_btn);

            if(mListView.getSelectedItemPosition() == cursor.getPosition()){
                toggleBtn.setImageResource(R.drawable.ic_stop);
            } else {
                toggleBtn.setImageResource(R.drawable.ic_play);
            }

            toggleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri stationUri = MainActivity.BASE_ACTIVITY_URI.buildUpon()
                            .appendPath(MainActivity.PATH_STATION)
                            .appendPath(Integer.toString(position))
                            .build();

                    ((MainActivity)context).onFragmentInteraction(stationUri);
                }
            });
        }
    }

    private String getLastSyncTimeStamp(){
        Date syncDate = mApp.getLastSyncTimestamp();

        String humanSyncDate;

        if(syncDate == null){
            humanSyncDate = getString(R.string.sync_status_never);
        } else {
            humanSyncDate = String.format(getString(R.string.sync_status_last_updated),
                    ChronoUtils.getHumanFormattedDate(syncDate));
        }

        return humanSyncDate;
    }
}
