package com.ifightmonsters.yarra.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.data.YarraContract;
import com.ifightmonsters.yarra.service.RadioService;
import com.ifightmonsters.yarra.ui.activity.MainActivity;
import com.ifightmonsters.yarra.utils.AndroidUtils;

/**
 * Fragment responsible for displaying a list of radio stations that the user can playback
 */
public class MainFragment extends Fragment
        implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    private static final String LOG = "MainFragment";

    private static final String EXTRA_CURRENT_SELECTION = "current_selection";

    /**
     * We only want the station id, playlist and number of listeners. The station id
     * we use to communicate with the activity. The playlist and the number of listeners, we display
     * to the user.
     */
    private static final String[] STATUS_PROJECTION = {
            YarraContract.Status.TABLE_NAME + "." + YarraContract.Status._ID,
            YarraContract.Status.COLUMN_PLAYLIST,
            YarraContract.Status.COLUMN_LISTENERS
    };
    private static final String LIST_SELECTION_KEY = "list_selection";
    private String statusSortOrder = YarraContract.Status._ID + " ASC";

    private int STATUS_LOADER = 0;

    private int mCurrentSelection;

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
                } else {
                    if (AndroidUtils.equalOrGreaterThanHoneycomb()) {
                        mGridView.setItemChecked(mCurrentSelection, false);
                    } else {
                        mGridView.setSelection(-1);
                    }
                }

                mCurrentSelection = -1;
            }

        }
    };

    private MainActivity mActivity;
    private ListView mListView;
    private GridView mGridView;
    private CursorAdapter mAdapter;
    private Cursor mCursor;
    private TextView mErrorTV;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    public MainFragment() {
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**
         * We want the fragment to remember the listview/gridview position. The gridview and listview position are
         * identical.
         */
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            if (savedInstanceState.containsKey(LIST_SELECTION_KEY)) {
                mCurrentSelection = savedInstanceState.getInt(LIST_SELECTION_KEY);
            }

        }

        getLoaderManager().initLoader(STATUS_LOADER, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocalMgr = LocalBroadcastManager.getInstance(getActivity());

        Bundle args = getArguments();

        if (args != null && !args.isEmpty()) {
            if (args.containsKey(EXTRA_CURRENT_SELECTION)) {
                mCurrentSelection = args.getInt(EXTRA_CURRENT_SELECTION);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_main, container, false);

        mErrorTV = (TextView) v.findViewById(R.id.error_tv);

        mAdapter = new StationCursorAdapter(getActivity(), null, false);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {


        if (mListView != null) {
            outState.putInt(LIST_SELECTION_KEY, mCurrentSelection);
        } else {
            outState.putInt(LIST_SELECTION_KEY, mCurrentSelection);
        }

        super.onSaveInstanceState(outState);
    }

    private void registerReceivers() {
        //We want unselect the listview/gridview selection when the station is killed.
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
    public void onResume() {
        super.onResume();
        attemptToRestoreListViews();
    }

    @SuppressWarnings("NewApi")
    private void attemptToRestoreListViews() {

        if (mListView != null) {
            mListView.setSelection(mCurrentSelection);
            mListView.setItemChecked(mCurrentSelection, true);
        } else {
            if (AndroidUtils.equalOrGreaterThanHoneycomb()) {
                mGridView.setItemChecked(mCurrentSelection, true);
            } else {
                mGridView.setSelection(mCurrentSelection);
            }
        }
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
        mCursor = cursor;

        if (mCursor != null && mCursor.getCount() > 0) {
            showErrorMessage(false);
            mAdapter.swapCursor(mCursor);
        } else {
            showErrorMessage(true);
            setErrorMessage(R.string.error_no_station_data);
        }


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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (mCursor.moveToPosition(position)) {
            //We communicate with the activity the database id of the radio station
            long statusId =
                    mCursor.getLong(mCursor.getColumnIndex(YarraContract.Status._ID));
            mActivity.onFragmentInteraction(YarraContract.Status.buildStatusUri(statusId));
            mCurrentSelection = position;
        }

    }

    /**
     * Adapter used in the listview/gridview to display information about the different radio stations
     */

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

            ((TextView) view.findViewById(R.id.station_name)).setText(String.format(getString(R.string.station), station_name));
            ((TextView) view.findViewById(R.id.listeners_tv)).setText(listeners);

        }
    }

    /**
     * Displays the appropriate error message. We have it take in a string resource so that in the future
     * if we want to add more error message, we just have to pass a string resource id.
     *
     * @param stringRes the string resource id of the message we wish to display
     */
    private void setErrorMessage(int stringRes) {
        mErrorTV.setText(stringRes);
    }

    /**
     * Determines whether or not an error message or the listview/gridview should be displayed on the fragment
     *
     * @param show whether or not an error message or a listview/gridview should be displayed
     */
    private void showErrorMessage(boolean show) {

        if (show) {

            if (mListView != null) {
                mListView.setVisibility(View.GONE);
            } else {
                mGridView.setVisibility(View.GONE);
            }

            mErrorTV.setVisibility(View.VISIBLE);

        } else {

            if (mListView != null) {
                mListView.setVisibility(View.VISIBLE);
            } else {
                mGridView.setVisibility(View.VISIBLE);
            }

            mErrorTV.setVisibility(View.GONE);

        }

    }

}
