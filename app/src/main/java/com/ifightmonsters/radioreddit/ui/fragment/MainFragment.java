package com.ifightmonsters.radioreddit.ui.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ifightmonsters.radioreddit.R;
import com.ifightmonsters.radioreddit.data.RadioRedditContract;
import com.ifightmonsters.radioreddit.ui.activity.MainActivity;
import com.ifightmonsters.radioreddit.ui.adapter.StationCursorAdapter;

public class MainFragment extends Fragment
        implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener{

    private static final String OUTPUT_TV_KEY = "output_tv";

    private MainActivity mActivity;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private ImageView mErrorImage;
    private TextView mErrorText;

    private CursorAdapter mAdapter;

    private int SONG_LOADER = 0;

    private String songSortOrder = RadioRedditContract.Song.COLUMN_STATUS_ID + " DESC";

    private static final String[] SONG_COLUMNS = {

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

    public static MainFragment newInstance(){
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
        mActivity = (MainActivity)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.swipe);
        mSwipeRefreshLayout.setOnRefreshListener(mActivity);
        mListView = (ListView)v.findViewById(R.id.list);
        mAdapter = new StationCursorAdapter(getActivity(), null, false);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mErrorImage = (ImageView)v.findViewById(R.id.error_image);
        mErrorText = (TextView)v.findViewById(R.id.error_text);
        return v;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                RadioRedditContract.Song.CONTENT_URI,
                SONG_COLUMNS,
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Uri stationUri = MainActivity.BASE_ACTIVITY_URI.buildUpon()
                .appendPath(MainActivity.PATH_STATION)
                .appendPath(Integer.toString(position))
                .build();

        mActivity.onFragmentInteraction(stationUri);
    }
}
