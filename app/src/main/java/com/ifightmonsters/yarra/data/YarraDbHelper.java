package com.ifightmonsters.yarra.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Gregory on 10/31/2014.
 */
public class YarraDbHelper extends SQLiteOpenHelper {

    private static final String LOG = "RadioRedditDbHelper";

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "radioreddit.db";

    public static final int STATUS_COLUMN_ONLINE_ID = 1;
    public static final int STATUS_COLUMN_RELAY_ID = 2;
    public static final int STATUS_COLUMN_LISTENERS_ID = 3;
    public static final int STATUS_COLUMN_ALL_LISTENERS_ID = 4;
    public static final int STATUS_COLUMN_PLAYLIST_ID = 5;

    public static final int SONG_COLUMN_REDDITID_ID = 1;
    public static final int SONG_COLUMN_STATUSID_ID = 2;
    public static final int SONG_COLUMN_TITLE = 3;
    public static final int SONG_COLUMN_ARTIST = 4;
    public static final int SONG_COLUMN_ALBUM = 5;
    public static final int SONG_COLUMN_GENRE = 6;
    public static final int SONG_COLUMN_SCORE = 7;
    public static final int SONG_COLUMN_REDDITTITLE = 8;
    public static final int SONG_COLUMN_REDDITURL = 9;
    public static final int SONG_COLUMN_REDDITOR = 10;
    public static final int SONG_COLUMN_DOWNLOAD_URL_ID = 11;
    public static final int SONG_COLUMN_PREVIEW_URL_ID = 12;


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
