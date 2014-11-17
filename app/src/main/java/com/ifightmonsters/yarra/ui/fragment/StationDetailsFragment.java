package com.ifightmonsters.yarra.ui.fragment;

import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final ArrayMap<String, Integer> mPosters;

    static {
        mPosters = new ArrayMap<String, Integer>();
        mPosters.put("main", 0);
        mPosters.put("electronic", 1);
        mPosters.put("indie", 2);
        mPosters.put("hiphop", 3);
        mPosters.put("rock", 4);
        mPosters.put("metal", 5);
        mPosters.put("random", 6);
        mPosters.put("talk", 7);
    }

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
    private TextView mSongName, mArtistName, mScore, mRedditor;
    private ImageButton mRedditUrlBtn, mDownloadUrlBtn;
    private ShareActionProvider mShareActionProvider;

    private long mStationId;
    private String mDownloadUrl, mRedditUrl, mRedditorUrl;
    private Cursor mCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null && !args.isEmpty()) {

            if (args.containsKey(EXTRA_STATION_ID)) {
                mStationId = args.getLong(EXTRA_STATION_ID);
            }

        }
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_station_details, container, false);
        mPosterContainer = (ImageView) root.findViewById(R.id.poster_container);
        mScore = (TextView) root.findViewById(R.id.score_tv);
        mSongName = (TextView) root.findViewById(R.id.song_tv);
        mArtistName = (TextView) root.findViewById(R.id.artist_tv);
        mRedditor = (TextView) root.findViewById(R.id.redditor_tv);
        mRedditUrlBtn = (ImageButton) root.findViewById(R.id.reddit_url);
        mDownloadUrlBtn = (ImageButton) root.findViewById(R.id.download_url);
        mRedditUrlBtn.setOnClickListener(this);
        mDownloadUrlBtn.setOnClickListener(this);
        mRedditor.setOnClickListener(this);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.station_details_menu, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);

        if (mDownloadUrl != null) {
            shareItem.setVisible(true);
            mShareActionProvider.setShareIntent(shareUrl(mDownloadUrl));
        } else {
            shareItem.setVisible(false);
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

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

                String artist_name = String.format(getString(R.string.artist_name),
                        mCursor.getString(mCursor.getColumnIndex(YarraContract.Song.COLUMN_ARTIST)));

                String song_name = String.format(getString(R.string.song_name),
                        mCursor.getString(mCursor.getColumnIndex(YarraContract.Song.COLUMN_TITLE)));

                String score = mCursor.getString(mCursor.getColumnIndex(YarraContract.Song.COLUMN_SCORE));

                if (TextUtils.isEmpty(score) || score.equals("null")) {
                    score = "0";
                }

                mRedditorUrl = mCursor.getString(mCursor.getColumnIndex(YarraContract.Song.COLUMN_REDDITOR));

                String redditor = String.format(getString(R.string.redditor), mRedditorUrl);

                String stationName = mCursor.getString(mCursor.getColumnIndex(YarraContract.Status.COLUMN_PLAYLIST));

                getActivity().setTitle(String.format(getString(R.string.stream), uppercaseFirstLetter(stationName)));

                postCorrectPoster(stationName);

                mArtistName.setText(artist_name);
                mScore.setText(score);
                mRedditor.setText(redditor);
                mSongName.setText(song_name);

                String redditUrl = mCursor.getString(mCursor.getColumnIndex(YarraContract.Song.COLUMN_REDDIT_URL));
                String downloadUrl = mCursor.getString(mCursor.getColumnIndex(YarraContract.Song.COLUMN_DOWNLOAD_URL));

                if (!TextUtils.isEmpty(redditUrl)) {
                    mRedditUrl = redditUrl;
                }

                if (!TextUtils.isEmpty(downloadUrl)) {
                    mDownloadUrl = downloadUrl;
                    mDownloadUrlBtn.setVisibility(View.VISIBLE);
                } else {
                    mDownloadUrlBtn.setVisibility(View.GONE);
                }

            }

        }

        ((ActionBarActivity) getActivity()).invalidateOptionsMenu();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == mRedditUrlBtn.getId()) {
            openUrl(mRedditUrl);
        }

        if (v.getId() == mDownloadUrlBtn.getId()) {
            openUrl(mDownloadUrl);
        }

        if (v.getId() == mRedditor.getId()) {
            openUrl(String.format(getString(R.string.reddit_user_profile_url), mRedditorUrl));
        }

    }

    private void postCorrectPoster(String stationName) {
        TypedArray posters = getActivity().getResources().obtainTypedArray(R.array.station_posters);
        mPosterContainer.setImageDrawable(posters.getDrawable(mPosters.get(stationName)));
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
        } else {
            Toast.makeText(getActivity(), getString(R.string.error_no_http_url_app), Toast.LENGTH_SHORT).show();
        }

    }

    private Intent shareUrl(String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    /**
     * Any string being passed to this method will have it's first letter uppercase.
     *
     * @param string the string to uppercase the first letter
     * @return the string with the first letter uppercased. If null is passed, null is returned. if a string with no characters is pased, no characters are returned. if a string with one letter is passed, a string containing the appropriate uppercased letter is returned
     */
    private String uppercaseFirstLetter(String string) {

        if (string == null) {
            return null;
        }

        if (string.length() <= 0) {
            return string;
        }

        if (string.length() == 1) {
            return "" + Character.toUpperCase(string.toCharArray()[0]);
        }

        return Character.toUpperCase(string.toCharArray()[0]) + string.substring(1);
    }
}
