package com.ifightmonsters.yarra.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class YarraDbHelper extends SQLiteOpenHelper {

    private static final String LOG = "RadioRedditDbHelper";

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "radioreddit.db";

    public YarraDbHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_STATUS_TABLE = "CREATE TABLE " + YarraContract.Status.TABLE_NAME + " (" +
                YarraContract.Status._ID + " INTEGER PRIMARY KEY, " +
                YarraContract.Status.COLUMN_ONLINE + " TEXT, " +
                YarraContract.Status.COLUMN_RELAY + " TEXT, " +
                YarraContract.Status.COLUMN_LISTENERS + " TEXT, " +
                YarraContract.Status.COLUMN_ALL_LISTENERS + " TEXT, " +
                YarraContract.Status.COLUMN_PLAYLIST + " TEXT)";

        final String SQL_CREATE_SONG_TABLE = "CREATE TABLE " + YarraContract.Song.TABLE_NAME + " (" +
                YarraContract.Song._ID + " INTEGER PRIMARY KEY, " +
                YarraContract.Song.COLUMN_REDDIT_ID + " TEXT, " +
                YarraContract.Song.COLUMN_STATUS_ID + " INTEGER, " +
                YarraContract.Song.COLUMN_TITLE + " TEXT, " +
                YarraContract.Song.COLUMN_ARTIST + " TEXT, " +
                YarraContract.Song.COLUMN_ALBUM + " TEXT, " +
                YarraContract.Song.COLUMN_GENRE + " TEXT, " +
                YarraContract.Song.COLUMN_SCORE + " TEXT, " +
                YarraContract.Song.COLUMN_REDDIT_TITLE + " TEXT, " +
                YarraContract.Song.COLUMN_REDDIT_URL + " TEXT, " +
                YarraContract.Song.COLUMN_REDDITOR + " TEXT, " +
                YarraContract.Song.COLUMN_DOWNLOAD_URL + " TEXT, " +
                YarraContract.Song.COLUMN_PREVIEW_URL + " TEXT, " +
                "FOREIGN KEY (" + YarraContract.Song.COLUMN_STATUS_ID + ") REFERENCES " +
                YarraContract.Status.TABLE_NAME + " (" + YarraContract.Status._ID + "))";

        db.execSQL(SQL_CREATE_STATUS_TABLE);
        db.execSQL(SQL_CREATE_SONG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + YarraContract.Status.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + YarraContract.Song.TABLE_NAME);
        onCreate(db);
    }

}
