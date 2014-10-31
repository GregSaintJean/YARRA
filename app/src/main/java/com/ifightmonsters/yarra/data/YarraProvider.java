package com.ifightmonsters.yarra.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Gregory on 10/31/2014.
 */
public class YarraProvider extends ContentProvider {

    private static final String LOG = "RadioRedditProvider";

    private static final int STATUS = 1;
    private static final int STATUS_ID = 2;
    private static final int SONG = 3;
    private static final int SONG_ID = 4;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private YarraDbHelper mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = YarraContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, YarraContract.PATH_STATUS, STATUS);
        matcher.addURI(authority, YarraContract.PATH_STATUS + "/#", STATUS_ID);

        matcher.addURI(authority, YarraContract.PATH_SONG, SONG);
        matcher.addURI(authority, YarraContract.PATH_SONG + "/#", SONG_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new YarraDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case STATUS:
                cursor = mOpenHelper.getReadableDatabase().query(
                        YarraContract.Status.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case STATUS_ID:
                cursor = mOpenHelper.getReadableDatabase().query(
                        YarraContract.Status.TABLE_NAME,
                        projection,
                        YarraContract.Status._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            case SONG:
                cursor = mOpenHelper.getReadableDatabase().query(
                        YarraContract.Song.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case SONG_ID:
                cursor = mOpenHelper.getReadableDatabase().query(
                        YarraContract.Song.TABLE_NAME,
                        projection,
                        YarraContract.Song._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {

            case STATUS:
                return YarraContract.Status.CONTENT_TYPE;
            case STATUS_ID:
                return YarraContract.Status.CONTENT_ITEM_TYPE;
            case SONG:
                return YarraContract.Song.CONTENT_TYPE;
            case SONG_ID:
                return YarraContract.Song.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case STATUS: {
                long _id = db.insert(YarraContract.Status.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = YarraContract.Status.buildStatusUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            }
            break;
            case SONG: {
                long _id = db.insert(YarraContract.Song.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = YarraContract.Song.buildSongUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {

            case STATUS:
                rowsDeleted = db.delete(
                        YarraContract.Status.TABLE_NAME, selection, selectionArgs
                );
                break;
            case SONG:
                rowsDeleted = db.delete(
                        YarraContract.Song.TABLE_NAME, selection, selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {

            case STATUS:
                rowsUpdated = db.update(
                        YarraContract.Status.TABLE_NAME, values, selection, selectionArgs
                );
                break;
            case SONG:
                rowsUpdated = db.update(
                        YarraContract.Song.TABLE_NAME, values, selection, selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {


        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case SONG:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(YarraContract.Song.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }

    }

}
