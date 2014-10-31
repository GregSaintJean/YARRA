package com.ifightmonsters.yarra.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ifightmonsters.yarra.MainApp;
import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.data.YarraContract;
import com.ifightmonsters.yarra.data.YarraDbHelper;
import com.ifightmonsters.yarra.service.RadioService;
import com.ifightmonsters.yarra.ui.activity.MainActivity;

/**
 * Created by Gregory on 10/31/2014.
 */
public class MainFragment extends Fragment
        implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    private String songSortOrder = YarraContract.Song.COLUMN_STATUS_ID + " DESC";

    private int mCurrentSelection = -1;

    private static final String[] SONG_PROJECTION = {
            YarraContract.Song.TABLE_NAME + "." + YarraContract.Song._ID,
            YarraContract.Song.COLUMN_REDDIT_ID,
            YarraContract.Song.COLUMN_STATUS_ID,
            YarraContract.Song.COLUMN_TITLE,
            YarraContract.Song.COLUMN_ARTIST,
            YarraContract.Song.COLUMN_ALBUM,
            YarraContract.Song.COLUMN_GENRE,
            YarraContract.Song.COLUMN_SCORE,
            YarraContract.Song.COLUMN_REDDIT_TITLE,
            YarraContract.Song.COLUMN_REDDIT_URL,
            YarraContract.Song.COLUMN_REDDITOR,
            YarraContract.Song.COLUMN_DOWNLOAD_URL,
            YarraContract.Song.COLUMN_PREVIEW_URL
    };

    private LocalBroadcastManager mLocalMgr;

    private final BroadcastReceiver mMainFragmentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (context == null || intent == null) {
                return;
            }

            String action = intent.getAction();

            if (action == null) {
                return;
            }

            if (action.equals(RadioService.BROADCAST_KILLED)) {
                if (mListView != null) {
                    mListView.setItemChecked(mCurrentSelection, false);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mGridView.setItemChecked(mCurrentSelection, false);
                }
            }

        }
    };

    private int SONG_LOADER = 0;

    private MainApp mApp;
    private MainActivity mActivity;
    private ListView mListView;
    private GridView mGridView;
    private CursorAdapter mAdapter;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public MainFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(SONG_LOADER, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocalMgr = LocalBroadcastManager.getInstance(mActivity);
        mApp = (MainApp) mActivity.getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mAdapter = new StationCursorAdapter(mActivity, null, false);
        View lister = v.findViewById(R.id.list);

        if (lister == null) {
            mGridView = (GridView) v.findViewById(R.id.grid);
            mGridView.setAdapter(mAdapter);
            mGridView.setOnItemClickListener(this);
        } else {
            mListView = (ListView) v.findViewById(R.id.list);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(this);
        }
        return v;
    }

    private void registerReceivers() {
        IntentFilter internalFilter = new IntentFilter(RadioService.BROADCAST_KILLED);
        mLocalMgr.registerReceiver(mMainFragmentReceiver, internalFilter);
    }

    private void unregisterReceivers() {
        mLocalMgr.unregisterReceiver(mMainFragmentReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceivers();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceivers();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                YarraContract.Song.CONTENT_URI,
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
    public void onDestroy() {
        super.onDestroy();
        mListView = null;
        mGridView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Uri stationUri = MainActivity.BASE_ACTIVITY_URI.buildUpon()
                .appendPath(MainActivity.PATH_STATION)
                .appendPath(Integer.toString(position))
                .build();

        mActivity.onFragmentInteraction(stationUri);
        mCurrentSelection = position;
    }

    public class StationCursorAdapter extends CursorAdapter {

        private TypedArray mStationArray;
        private String[] mStation;

        public StationCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mStation = context.getResources().getStringArray(R.array.stations);
            mStationArray = getResources().obtainTypedArray(R.array.station_banners);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(context).inflate(R.layout.list_item_station, viewGroup, false);
        }

        @Override
        public void bindView(View view, final Context context, Cursor cursor) {

            final int position = cursor.getPosition();

            ((ImageView) view.findViewById(R.id.card_banner))
                    .setImageDrawable(mStationArray.getDrawable(position));

            ((TextView) view.findViewById(R.id.station_name)).setText(mStation[cursor.getPosition()]);

            String artist_name = String.format(context.getString(R.string.artist_name),
                    cursor.getString(YarraDbHelper.SONG_COLUMN_ARTIST));
            String song_name = String.format(context.getString(R.string.song_name),
                    cursor.getString(YarraDbHelper.SONG_COLUMN_TITLE));
            String score = cursor.getString(YarraDbHelper.SONG_COLUMN_SCORE);
            score = TextUtils.isEmpty(score) ? "0" : score;

            score = String.format(context.getString(R.string.score),
                    score);

            ((TextView) view.findViewById(R.id.artist_name)).setText(artist_name);
            ((TextView) view.findViewById(R.id.song_name)).setText(song_name);
            ((TextView) view.findViewById(R.id.score)).setText(score);
        }
    }

}
