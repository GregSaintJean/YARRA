package com.ifightmonsters.radioreddit.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Gregory on 10/4/2014.
 */
public class RadioRedditDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "radioreddit.db";

    public RadioRedditDbHelper(Context ctx){
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_STATUS_TABLE = "CREATE TABLE " + RadioRedditContract.Status.TABLE_NAME + " (" +
                RadioRedditContract.Status._ID + " INTEGER PRIMARY KEY, " +
                RadioRedditContract.Status.COLUMN_ONLINE + " TEXT NOT NULL, " +
                RadioRedditContract.Status.COLUMN_RELAY + " TEXT NOT NULL, " +
                RadioRedditContract.Status.COLUMN_LISTENERS + " TEXT NOT NULL, " +
                RadioRedditContract.Status.COLUMN_ALL_LISTENERS + "TEXT NOT NULL, " +
                RadioRedditContract.Status.COLUMN_PLAYLIST + " TEXT NOT NULL)";

        final String SQL_CREATE_SONG_TABLE = "CREATE TABLE " + RadioRedditContract.Song.TABLE_NAME + " (" +
                RadioRedditContract.Song._ID + " INTEGER PRIMARY KEY, " +
                RadioRedditContract.Song.COLUMN_REDDIT_ID + " TEXT, " +
                RadioRedditContract.Song.COLUMN_STATUS_ID + " INTEGER, " +
                RadioRedditContract.Song.COLUMN_TITLE + " TEXT, " +
                RadioRedditContract.Song.COLUMN_ARTIST + " TEXT, " +
                RadioRedditContract.Song.COLUMN_ALBUM + " TEXT, " +
                RadioRedditContract.Song.COLUMN_GENRE + " TEXT, " +
                RadioRedditContract.Song.COLUMN_SCORE + " TEXT, " +
                RadioRedditContract.Song.COLUMN_REDDIT_TITLE + " TEXT, " +
                RadioRedditContract.Song.COLUMN_REDDIT_URL + " TEXT, " +
                RadioRedditContract.Song.COLUMN_REDDITOR + " TEXT, " +
                RadioRedditContract.Song.COLUMN_DOWNLOAD_URL + " TEXT, " +
                RadioRedditContract.Song.COLUMN_PREVIEW_URL + " TEXT) " +
                "FOREIGN KEY (" + RadioRedditContract.Song.COLUMN_STATUS_ID + ") REFERENCES " +
                RadioRedditContract.Status.TABLE_NAME + " (" + RadioRedditContract.Status._ID + ")";

        db.execSQL(SQL_CREATE_STATUS_TABLE);
        db.execSQL(SQL_CREATE_SONG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RadioRedditContract.Status.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RadioRedditContract.Song.TABLE_NAME);
        onCreate(db);
    }
}
