package com.ifightmonsters.radioreddit.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ifightmonsters.radioreddit.R;
import com.ifightmonsters.radioreddit.data.RadioRedditDbHelper;
import com.ifightmonsters.radioreddit.ui.activity.MainActivity;

public class StationCursorAdapter extends CursorAdapter {

    private String[] mStation;
    private ListView mListView;

    public StationCursorAdapter(Context context, Cursor c, boolean autoRequery, ListView listView) {
        super(context, c, autoRequery);
        mStation = context.getResources().getStringArray(R.array.stations);
        mListView = listView;
    }

    public StationCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_station, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final int position = cursor.getPosition();

        final MainActivity activity = (MainActivity) context;

       String station_name = String.format(context.getString(R.string.station_name),
                mStation[cursor.getPosition()]);

        ((TextView)view.findViewById(R.id.station_name)).setText(station_name);

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

                activity.onFragmentInteraction(stationUri);
            }
        });

    }
}
