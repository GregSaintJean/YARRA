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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.data.YarraContract;
import com.ifightmonsters.yarra.service.RadioService;
import com.ifightmonsters.yarra.ui.activity.MainActivity;
import com.ifightmonsters.yarra.ui.activity.StationDetailsActivity;

/**
 * Created by Gregory on 10/31/2014.
 */
public class MainFragment extends Fragment
        implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    private String statusSortOrder = YarraContract.Status._ID + " ASC";

    private int mCurrentSelection = -1;

    private static final String[] STATUS_PROJECTION = {
            YarraContract.Status.TABLE_NAME + "." + YarraContract.Status._ID,
            YarraContract.Status.COLUMN_PLAYLIST,
            YarraContract.Status.COLUMN_LISTENERS
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

    private int STATUS_LOADER = 0;

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
        getLoaderManager().initLoader(STATUS_LOADER, null, this);
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
                YarraContract.Status.CONTENT_URI,
                STATUS_PROJECTION,
                null,
                null,
                statusSortOrder);
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

        public StationCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
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

            String station_name = cursor.getString(cursor.getColumnIndex(YarraContract.Status.COLUMN_PLAYLIST));

            String listeners = String.format(context.getString(R.string.listeners),
                    cursor.getString(cursor.getColumnIndex(YarraContract.Status.COLUMN_LISTENERS)));

            station_name = (station_name.charAt(0) + "").toUpperCase() + station_name.substring(1);

            ((TextView) view.findViewById(R.id.station_name)).setText(station_name);
            ((TextView) view.findViewById(R.id.listeners_tv)).setText(listeners);

            ImageButton infoButton = (ImageButton) view.findViewById(R.id.info_btn);
            ImageButton playButton = (ImageButton) view.findViewById(R.id.play_btn);

            final String stationN = station_name;

            final int stationId = (int) cursor.getLong(cursor.getColumnIndex(YarraContract.Status._ID));

            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startStationDetailsActivity(stationN, stationId);
                }
            });

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri stationUri = MainActivity.BASE_ACTIVITY_URI.buildUpon()
                            .appendPath(MainActivity.PATH_STATION)
                            .appendPath(Integer.toString(stationId))
                            .build();

                    mActivity.onFragmentInteraction(stationUri);
                    mCurrentSelection = position;
                }
            });

        }
    }

    private void startStationDetailsActivity(String station, int id) {
        Intent intent = new Intent(getActivity(), StationDetailsActivity.class);
        intent.putExtra(StationDetailsFragment.EXTRA_STATION_NAME, station);
        intent.putExtra(StationDetailsFragment.EXTRA_STATION_ID, id);
        startActivity(intent);
    }

}
