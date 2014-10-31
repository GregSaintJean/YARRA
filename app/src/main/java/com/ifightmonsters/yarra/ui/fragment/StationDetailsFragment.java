package com.ifightmonsters.yarra.ui.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.ui.activity.MainActivity;

/**
 * Created by Gregory on 10/31/2014.
 */
public class StationDetailsFragment extends Fragment
        implements
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    private MainActivity mActivity;

    private final int STATUS_LOADER = 0;
    private final int SONG_LOADER = 1;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(STATUS_LOADER, null, this);
        getLoaderManager().initLoader(SONG_LOADER, null, this);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    public static StationDetailsFragment newInstance(){
        return new StationDetailsFragment();
    }

    public StationDetailsFragment(){

    }

    private ImageView mPosterContainer;
    private TextView mStationName, mListeners, mSongName, mArtistName, mScore, mRedditor;
    private ImageButton mRedditUrl, mDownloadUrl, mPreviewUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_station_details, container, false);

        mPosterContainer = (ImageView)root.findViewById(R.id.poster_container);
        mStationName = (TextView)root.findViewById(R.id.station_name_tv);
        mListeners = (TextView)root.findViewById(R.id.listener_tv);
        mScore = (TextView)root.findViewById(R.id.score_tv);
        mSongName = (TextView)root.findViewById(R.id.song_tv);
        mArtistName = (TextView)root.findViewById(R.id.artist_tv);
        mRedditor = (TextView)root.findViewById(R.id.redditor_tv);
        mRedditUrl = (ImageButton)root.findViewById(R.id.reddit_url);
        mPreviewUrl = (ImageButton)root.findViewById(R.id.preview_url);
        mDownloadUrl = (ImageButton) root.findViewById(R.id.download_url);
        mRedditUrl.setOnClickListener(this);
        mPreviewUrl.setOnClickListener(this);
        mDownloadUrl.setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == mRedditUrl.getId()){

        }

        if(v.getId() == mPreviewUrl.getId()){

        }

        if(v.getId() == mDownloadUrl.getId()){

        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

}
