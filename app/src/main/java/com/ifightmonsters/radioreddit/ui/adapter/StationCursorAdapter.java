package com.ifightmonsters.radioreddit.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ifightmonsters.radioreddit.R;

public class StationCursorAdapter extends CursorAdapter {

    private CharSequence[] mStation;

    public StationCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mStation = context.getResources().getStringArray(R.array.stations);
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
        ((TextView)view.findViewById(R.id.station_name)).setText(mStation[cursor.getPosition()]);
    }
}
