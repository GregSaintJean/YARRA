package com.ifightmonsters.yarra.ui.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
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

/**
 * Displays detailed information about the radio station that is currently being played
 */
public class StationDetailsFragment extends Fragment
        implements
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_STATION_ID
            = "com.ifightmonsters.yarra.ui.fragment.StationDetailsFragment.EXTRA_STATION_ID";

    private final int STATUS_WITH_STATUS_LOADER = 0;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(STATUS_WITH_STATUS_LOADER, null, this);
    }

    public static StationDetailsFragment newInstance(long station_id) {
        StationDetailsFragment fragment = new StationDetailsFragment();
        Bundle args = new Bundle();
        args.putLong(EXTRA_STATION_ID, station_id);
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
    private View mBorderOne, mBorderTwo, mNowPlayingLabel;

    private long mStationId;
    private String mDownloadUrl, mPreviewUrl, mRedditUrl;
    private Cursor mCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mStationId = getArguments().getLong(EXTRA_STATION_ID);
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
        mBorderOne = root.findViewById(R.id.border_one);
        mBorderTwo = root.findViewById(R.id.border_two);
        mNowPlayingLabel = root.findViewById(R.id.now_playing_label);
        mRedditUrlBtn.setOnClickListener(this);
        mPreviewUrlBtn.setOnClickListener(this);
        mDownloadUrlBtn.setOnClickListener(this);
        return root;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        showProgress(true);

        return new CursorLoader(getActivity(),
                YarraContract.Status.buildStatusUri(mStationId),
                YarraContract.Status.STATUS_WITH_SONG_PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        if (cursor != null && cursor.getCount() > 0) {

            if (cursorLoader.getId() == STATUS_WITH_STATUS_LOADER) {

                mCursor = cursor;
                mCursor.moveToFirst();

                String listeners = String.format(getString(R.string.listeners),
                        mCursor.getString(mCursor.getColumnIndex(YarraContract.Status.JOIN_COLUMN_LISTENERS)));

                String artist_name = String.format(getString(R.string.artist_name),
                        mCursor.getString(mCursor.getColumnIndex(YarraContract.Status.JOIN_COLUMN_ARTIST)));

                String song_name = String.format(getString(R.string.song_name),
                        mCursor.getString(mCursor.getColumnIndex(YarraContract.Status.JOIN_COLUMN_TITLE)));

                String score = String.format(getString(R.string.score),
                        mCursor.getString(mCursor.getColumnIndex(YarraContract.Status.JOIN_COLUMN_SCORE)));

                mStationName.setText(mCursor.getString(mCursor.getColumnIndex(YarraContract.Status.JOIN_COLUMN_PLAYLIST)));
                mArtistName.setText(artist_name);
                mListeners.setText(listeners);
                mScore.setText(score);
                mRedditor.setText(mCursor.getString(mCursor.getColumnIndex(YarraContract.Status.JOIN_COLUMN_REDDITOR)));
                mSongName.setText(song_name);

                String redditUrl = mCursor.getString(mCursor.getColumnIndex(YarraContract.Status.JOIN_COLUMN_REDDIT_URL));
                String downloadUrl = mCursor.getString(mCursor.getColumnIndex(YarraContract.Status.JOIN_COLUMN_DOWNLOAD_URL));
                String previewUrl = mCursor.getString(mCursor.getColumnIndex(YarraContract.Status.JOIN_COLUMN_PREVIEW_URL));

                if (!TextUtils.isEmpty(redditUrl)) {
                    mRedditUrl = redditUrl;
                }

                if (!TextUtils.isEmpty(downloadUrl)) {
                    mDownloadUrl = redditUrl;
                }

                if (!TextUtils.isEmpty(previewUrl)) {
                    mPreviewUrl = previewUrl;
                }

                showProgress(false);
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor.close();
        showProgress(true);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == mRedditUrlBtn.getId()) {
            openUrl(mRedditUrl);
        }

        if (v.getId() == mPreviewUrlBtn.getId()) {
            openUrl(mPreviewUrl);
        }

        if (v.getId() == mDownloadUrlBtn.getId()) {
            openUrl(mDownloadUrl);
        }

    }

    private void showProgress(boolean show) {

        if (show) {
            mProgress.setVisibility(View.VISIBLE);
            mListeners.setVisibility(View.GONE);
            mBorderOne.setVisibility(View.GONE);
            mBorderTwo.setVisibility(View.GONE);
            mNowPlayingLabel.setVisibility(View.GONE);
            mScore.setVisibility(View.GONE);
            mStationName.setVisibility(View.GONE);
            mSongName.setVisibility(View.GONE);
            mArtistName.setVisibility(View.GONE);
            mRedditor.setVisibility(View.GONE);
            mRedditUrlBtn.setVisibility(View.GONE);
            mPreviewUrlBtn.setVisibility(View.GONE);
            mDownloadUrlBtn.setVisibility(View.GONE);
        } else {
            mProgress.setVisibility(View.GONE);
            mListeners.setVisibility(View.VISIBLE);
            mBorderOne.setVisibility(View.VISIBLE);
            mBorderTwo.setVisibility(View.VISIBLE);
            mNowPlayingLabel.setVisibility(View.VISIBLE);
            mScore.setVisibility(View.VISIBLE);
            mStationName.setVisibility(View.VISIBLE);
            mSongName.setVisibility(View.VISIBLE);
            mArtistName.setVisibility(View.VISIBLE);
            mRedditor.setVisibility(View.VISIBLE);

            if (mDownloadUrl != null) {
                mDownloadUrlBtn.setVisibility(View.VISIBLE);
            }

            if (mPreviewUrl != null) {
                mPreviewUrlBtn.setVisibility(View.VISIBLE);
            }

            if (mRedditUrl != null) {
                mRedditUrlBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    /*
     *  Will attempt to open an app that can handle the url being passed
     *  @param url the url to open
     */
    private void openUrl(String url) {

        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
        //TODO tell the user that they don't have an app that can handle urls

    }

}
