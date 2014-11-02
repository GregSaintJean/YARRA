package com.ifightmonsters.yarra.ui.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.data.YarraContract;
import com.ifightmonsters.yarra.ui.activity.MainActivity;

/**
 * Created by Gregory on 10/31/2014.
 */
public class StationDetailsFragment extends Fragment
        implements
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_STATION_NAME
            = "com.ifightmonsters.yarra.ui.fragment.StationDetailsFragment.EXTRA_STATION_NAME";

    public static final String EXTRA_STATION_ID
            = "com.ifightmonsters.yarra.ui.fragment.StationDetailsFragment.EXTRA_STATION_ID";

    public static final String[] STATUS_PROJECTION = {
            YarraContract.Status.TABLE_NAME + "." + YarraContract.Status._ID,
            YarraContract.Status.COLUMN_LISTENERS,
            YarraContract.Status.COLUMN_PLAYLIST
    };

    public static final String[] SONG_PROJECTION = {
            YarraContract.Song.TABLE_NAME + "." + YarraContract.Song._ID,
            YarraContract.Song.COLUMN_ALBUM,
            YarraContract.Song.COLUMN_ARTIST,
            YarraContract.Song.COLUMN_GENRE,
            YarraContract.Song.COLUMN_REDDIT_TITLE,
            YarraContract.Song.COLUMN_REDDIT_URL,
            YarraContract.Song.COLUMN_DOWNLOAD_URL,
            YarraContract.Song.COLUMN_PREVIEW_URL,
            YarraContract.Song.COLUMN_SCORE
    };

    private final int STATUS_LOADER = 0;
    private final int SONG_LOADER = 1;

    private MainActivity mActivity;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(STATUS_LOADER, null, this);
        getLoaderManager().initLoader(SONG_LOADER, null, this);
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

    public static StationDetailsFragment newInstance(String station_name, int station_id) {
        StationDetailsFragment fragment = new StationDetailsFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_STATION_NAME, station_name);
        args.putInt(EXTRA_STATION_ID, station_id);
        fragment.setArguments(args);
        return fragment;
    }

    public StationDetailsFragment() {

    }

    private ImageView mPosterContainer;
    private TextView mStationName, mListeners, mSongName, mArtistName, mScore, mRedditor;
    private ImageButton mRedditUrlBtn, mDownloadUrlBtn;
    private Button mPreviewUrlBtn;
    private ProgressBar mProgress;

    private int mStationId;
    private String mDownloadUrl, mPreviewUrl, mRedditUrl, mStation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mStation = getArguments().getString(EXTRA_STATION_NAME);
        mStationId = getArguments().getInt(EXTRA_STATION_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_station_details, container, false);

        mPosterContainer = (ImageView) root.findViewById(R.id.poster_container);
        mStationName = (TextView) root.findViewById(R.id.station_name_tv);
        mListeners = (TextView) root.findViewById(R.id.listener_tv);
        mScore = (TextView) root.findViewById(R.id.score_tv);
        mSongName = (TextView) root.findViewById(R.id.song_tv);
        mArtistName = (TextView) root.findViewById(R.id.artist_tv);
        mRedditor = (TextView) root.findViewById(R.id.redditor_tv);
        mRedditUrlBtn = (ImageButton) root.findViewById(R.id.reddit_url);
        mPreviewUrlBtn = (Button) root.findViewById(R.id.preview_url);
        mDownloadUrlBtn = (ImageButton) root.findViewById(R.id.download_url);
        mProgress = (ProgressBar) root.findViewById(R.id.progress);
        mRedditUrlBtn.setOnClickListener(this);
        mPreviewUrlBtn.setOnClickListener(this);
        mDownloadUrlBtn.setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == mRedditUrlBtn.getId()) {

        }

        if (v.getId() == mPreviewUrlBtn.getId()) {

        }

        if (v.getId() == mDownloadUrlBtn.getId()) {

        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        if (i == STATUS_LOADER) {
            return new CursorLoader(getActivity(),
                    YarraContract.Status.CONTENT_URI,
                    STATUS_PROJECTION,
                    null,
                    null,
                    null);
        } else {
            return new CursorLoader(getActivity(),
                    YarraContract.Song.CONTENT_URI,
                    SONG_PROJECTION,
                    null,
                    null,
                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        if (cursor != null && cursor.getCount() > 0) {

            if (cursorLoader.getId() == STATUS_LOADER) {


            } else if (cursorLoader.getId() == SONG_LOADER) {


            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

}
