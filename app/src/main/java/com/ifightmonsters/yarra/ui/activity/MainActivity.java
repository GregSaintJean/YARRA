package com.ifightmonsters.yarra.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ifightmonsters.yarra.MainApp;
import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.constant.Station;
import com.ifightmonsters.yarra.service.RadioService;
import com.ifightmonsters.yarra.sync.YarraSyncAdapter;
import com.ifightmonsters.yarra.ui.fragment.MainFragment;
import com.ifightmonsters.yarra.ui.fragment.PlaceHolderFragment;
import com.ifightmonsters.yarra.ui.fragment.StationDetailsFragment;
import com.ifightmonsters.yarra.utils.NetworkUtils;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String FRAGMENT_MAIN = "fragment_main";
    private static final String FRAGMENT_ELECTRONIC = "fragment_electronic";
    private static final String FRAGMENT_HIPHOP = "fragment_hiphop";
    private static final String FRAGMENT_INDIE = "fragment_indie";
    private static final String FRAGMENT_ROCK = "fragment_rock";
    private static final String FRAGMENT_METAL = "fragment_metal";
    private static final String FRAGMENT_RANDOM = "fragment_random";
    private static final String FRAGMENT_TALK = "fragment_talk";
    private static final String FRAGMENT_PLACEHOLDER = "fragment_placeholder";

    private long mCurrentStation = -1;

    private boolean mHasConnectivity = false;
    private boolean mSyncing = false;

    private LocalBroadcastManager mLocalBroadcastMgr;
    private ImageView mBackgroundIV, mErrorIV;
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
        if (app.isFirstRun()) {
            PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
            YarraSyncAdapter.initializeSyncAdapter(this);
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
        mBackgroundIV = (ImageView) findViewById(R.id.background);
        mLocalBroadcastMgr = LocalBroadcastManager.getInstance(this);

        if (savedInstanceState == null) {

            Fragment mainFragment = MainFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.station_container, mainFragment).commit();

            if (findViewById(R.id.station_details_container) != null) {

                mStationDetailsContainer = findViewById(R.id.station_details_container);

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

    public void launchWifiSettingsActivity() {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    public void launchSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceivers();
        checkNetworkConnectivity();
        setBackgroundImage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceivers();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.nav_tv) {
            launchWifiSettingsActivity();
        }
    }

    private void registerReceivers() {
        IntentFilter internalFilter = new IntentFilter(RadioService.BROADCAST_ERROR);
        internalFilter.addAction(RadioService.BROADCAST_STATUS);
        internalFilter.addAction(RadioService.BROADCAST_KILLED);
        internalFilter.addAction(YarraSyncAdapter.BROADCAST_SYNC_BEGIN);
        internalFilter.addAction(YarraSyncAdapter.BROADCAST_SYNC_COMPLETED);
        IntentFilter externalFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mLocalBroadcastMgr.registerReceiver(mMainActivityReceiver, internalFilter);
        registerReceiver(mMainActivityReceiver, externalFilter);
    }

    private void unregisterReceivers() {
        mLocalBroadcastMgr.unregisterReceiver(mMainActivityReceiver);
        unregisterReceiver(mMainActivityReceiver);
    }

    /*
        I wrote the parameters for this method and thought "this just got stupid".
        There is probably a way to simplify this but I can't think of it right now.
        I don't like this at all.
     */
    public void handleFragments(String station_name, int databaseId, int stationid) {

        if (findViewById(R.id.station_details_container) != null) {

            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            Fragment nextFragment;

            switch (stationid) {
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

            if (nextFragment == null) {
                nextFragment = StationDetailsFragment.newInstance(station_name, databaseId);
            }

            switch (stationid) {
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
        } else {
            startStationDetailsActivity(station_name, databaseId);
        }

    }

    public void handleStationPlayback(int id) {

        if (mCurrentStation == id && RadioService.isPlaying()) {
            RadioService.stop(this);
            return;
        }

        RadioService.play(this, id);
        mCurrentStation = id;

    }

    private void placePlaceHolderFragment() {
        if (findViewById(R.id.station_details_container) != null) {
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            Fragment nextFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_PLACEHOLDER);
            if (nextFragment == null) {
                nextFragment = PlaceHolderFragment.newInstance();
            }
            trans.replace(R.id.station_details_container, nextFragment, FRAGMENT_PLACEHOLDER);
            trans.commit();
        }
    }

    private void checkNetworkConnectivity() {
        if (NetworkUtils.hasNetworkConnectivity(this)) {
            mStationContainer.setVisibility(View.VISIBLE);

            if (findViewById(R.id.station_details_container) != null) {
                mStationDetailsContainer.setVisibility(View.VISIBLE);
            }

            mMessageContainer.setVisibility(View.GONE);
            mErrorIV.setVisibility(View.GONE);
            mErrorTV.setVisibility(View.GONE);
            mNavTV.setVisibility(View.GONE);
            mHasConnectivity = true;
        } else {

            mStationContainer.setVisibility(View.GONE);

            if (findViewById(R.id.station_details_container) != null) {
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

    private void showSyncing(boolean show) {

        if (show) {
            mProgress.setVisibility(View.VISIBLE);
            mStationContainer.setVisibility(View.GONE);
            mMessageContainer.setVisibility(View.VISIBLE);

            if (findViewById(R.id.station_details_container) != null) {
                mStationDetailsContainer.setVisibility(View.GONE);
            }

        } else {
            mProgress.setVisibility(View.GONE);
            mStationContainer.setVisibility(View.VISIBLE);
            mMessageContainer.setVisibility(View.GONE);

            if (findViewById(R.id.station_details_container) != null) {
                mStationDetailsContainer.setVisibility(View.VISIBLE);
            }
        }

        mErrorIV.setVisibility(View.GONE);
        mErrorTV.setVisibility(View.GONE);
        mNavTV.setVisibility(View.GONE);
        invalidateOptionsMenu();
    }

    private void setBackgroundImage() {
        Pair<Integer, Integer> size = getScreenSize();
        new ResizeBackgroundImageTask(this, R.drawable.background,
                mBackgroundIV).execute(size.first, size.second);
    }

    private static class ResizeBackgroundImageTask extends AsyncTask<Integer, Void, Bitmap> {

        private Context mCtx;
        private int mResId;
        private ImageView mBackgroundCanvas;

        public ResizeBackgroundImageTask(Context ctx,
                                         int resId,
                                         ImageView view) {
            mCtx = ctx;
            mResId = resId;
            mBackgroundCanvas = view;
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            return decodeSampledBitmapFromResource(
                    mCtx.getResources(), mResId, params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mBackgroundCanvas.setImageBitmap(bitmap);
        }

        //The following bitmap code was just copied and pasted from Google's Development site.

        public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        public Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                      int reqWidth, int reqHeight) {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(res, resId, options);
        }
    }

    public Pair<Integer, Integer> getScreenSize() {
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();

        int height = display.getHeight();
        int width = display.getWidth();
        return new Pair<Integer, Integer>(height, width);
    }

    private void startStationDetailsActivity(String station, int id) {
        Intent intent = new Intent(this, StationDetailsActivity.class);
        intent.putExtra(StationDetailsFragment.EXTRA_STATION_NAME, station);
        intent.putExtra(StationDetailsFragment.EXTRA_STATION_ID, id);
        startActivity(intent);
    }

}
