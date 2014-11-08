package com.ifightmonsters.yarra.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Gregory on 10/31/2014.
 */
public final class YarraContract {

    public static final String CONTENT_AUTHORITY = "com.ifightmonsters.yarra";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_STATUS = "status";
    public static final String PATH_SONG = "song";

    public static final class Status implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATUS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_STATUS;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_STATUS;

        public static final String TABLE_NAME = "status";

        public static final String COLUMN_ONLINE = "online";

        public static final String COLUMN_RELAY = "relay";

        public static final String COLUMN_LISTENERS = "listeners";

        public static final String COLUMN_ALL_LISTENERS = "all_listeners";

        public static final String COLUMN_PLAYLIST = "playlist";

        public static Uri buildStatusUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String JOIN_COLUMN_LISTENERS = TABLE_NAME + "." + COLUMN_LISTENERS;
        public static final String JOIN_COLUMN_PLAYLIST = TABLE_NAME + "." + COLUMN_PLAYLIST;
        public static final String JOIN_COLUMN_ALBUM = Song.TABLE_NAME + "." + Song.COLUMN_ALBUM;
        public static final String JOIN_COLUMN_ARTIST = Song.TABLE_NAME + "." + Song.COLUMN_ARTIST;
        public static final String JOIN_COLUMN_GENRE = Song.TABLE_NAME + "." + Song.COLUMN_GENRE;
        public static final String JOIN_COLUMN_REDDIT_TITLE = Song.TABLE_NAME + "." + Song.COLUMN_REDDIT_TITLE;
        public static final String JOIN_COLUMN_REDDIT_URL = Song.TABLE_NAME + "." + Song.COLUMN_REDDIT_URL;
        public static final String JOIN_COLUMN_DOWNLOAD_URL = Song.TABLE_NAME + "." + Song.COLUMN_DOWNLOAD_URL;
        public static final String JOIN_COLUMN_PREVIEW_URL = Song.TABLE_NAME + "." + Song.COLUMN_PREVIEW_URL;
        public static final String JOIN_COLUMN_SCORE = Song.TABLE_NAME + "." + Song.COLUMN_SCORE;
        public static final String JOIN_COLUMN_REDDITOR = Song.TABLE_NAME + "." + Song.COLUMN_REDDITOR;
        public static final String JOIN_COLUMN_TITLE = Song.TABLE_NAME + "." + Song.COLUMN_TITLE;

        public static final String[] STATUS_WITH_SONG_PROJECTION = {
                JOIN_COLUMN_LISTENERS,
                JOIN_COLUMN_PLAYLIST,
                JOIN_COLUMN_ALBUM,
                JOIN_COLUMN_ARTIST,
                JOIN_COLUMN_TITLE,
                JOIN_COLUMN_GENRE,
                JOIN_COLUMN_REDDIT_TITLE,
                JOIN_COLUMN_REDDIT_URL,
                JOIN_COLUMN_DOWNLOAD_URL,
                JOIN_COLUMN_PREVIEW_URL,
                JOIN_COLUMN_SCORE,
                JOIN_COLUMN_REDDITOR
        };

    }

    public static final class Song implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SONG).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_SONG;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_SONG;

        public static final String TABLE_NAME = "song";

        public static final String COLUMN_STATUS_ID = "status_id";

        public static final String COLUMN_REDDIT_ID = "reddit_id";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_ARTIST = "artist";

        public static final String COLUMN_ALBUM = "album";

        public static final String COLUMN_REDDITOR = "redditor";

        public static final String COLUMN_GENRE = "genre";

        public static final String COLUMN_SCORE = "score";

        public static final String COLUMN_REDDIT_TITLE = "reddit_title";

        public static final String COLUMN_REDDIT_URL = "reddit_url";

        public static final String COLUMN_PREVIEW_URL = "preview_url";

        public static final String COLUMN_DOWNLOAD_URL = "download_url";

        public static Uri buildSongUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

}
