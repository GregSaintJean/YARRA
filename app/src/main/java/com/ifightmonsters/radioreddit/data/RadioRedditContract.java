package com.ifightmonsters.radioreddit.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Gregory on 10/4/2014.
 */
public class RadioRedditContract {

    public static final String CONTENT_AUTHORITY = "com.ifightmonsters.radioreddit";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_STATUS = "status";
    public static final String PATH_SONG = "song";

    public static final class Status implements BaseColumns{

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

        public static Uri buildStatusUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    public static final class Song implements BaseColumns{

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

        public static final String COLUMN_DOWNLOAD_URL= "download_url";

        public static Uri buildSongUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

}
