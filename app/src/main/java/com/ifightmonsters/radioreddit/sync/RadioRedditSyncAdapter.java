package com.ifightmonsters.radioreddit.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ifightmonsters.radioreddit.R;
import com.ifightmonsters.radioreddit.data.RadioRedditContract;
import com.ifightmonsters.radioreddit.entities.Song;
import com.ifightmonsters.radioreddit.entities.Status;
import com.ifightmonsters.radioreddit.network.BaseResponse;
import com.ifightmonsters.radioreddit.network.RadioReddit;
import com.ifightmonsters.radioreddit.network.StatusResponse;
import com.ifightmonsters.radioreddit.network.TalkResponse;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Gregory on 10/5/2014.
 */
public class RadioRedditSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG = "RadioRedditSyncAdapter";


    public static final String BROADCAST_SYNC_BEGIN
            = "com.ifightmonsters.radioreddit.sync.RadioRedditSyncAdapter.broadcast.sync_begin";

    public static final String BROADCAST_SYNC_COMPLETED
            = "com.ifightmonsters.radioreddit.sync.RadioRedditSyncAdapter.broadcast.sync_completed";
    private LocalBroadcastManager mBroadMgr;

    public RadioRedditSyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
        mBroadMgr = LocalBroadcastManager.getInstance(context);
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        ContentResolver resolver = getContext().getContentResolver();

        LinkedList<BaseResponse> responses = new LinkedList<BaseResponse>();
        responses.add(RadioReddit.getMainStatus());
        responses.add(RadioReddit.getElectronicStatus());
        responses.add(RadioReddit.getIndieStatus());
        responses.add(RadioReddit.getHipHopStatus());
        responses.add(RadioReddit.getRockStatus());
        responses.add(RadioReddit.getMetalStatus());
        responses.add(RadioReddit.getRandomStatus());
        responses.add(RadioReddit.getTalkStatus());

        purgeDatabases(resolver);
        commitResponses(responses, resolver);
        Log.i(LOG, "sync completed");
        mBroadMgr.sendBroadcast(new Intent(BROADCAST_SYNC_COMPLETED));
    }

    private void purgeDatabases(ContentResolver resolver){
        resolver.delete(RadioRedditContract.Song.CONTENT_URI, null, null);
        resolver.delete(RadioRedditContract.Status.CONTENT_URI, null, null);
    }

    private void commitResponses(LinkedList<BaseResponse> responses, ContentResolver resolver){

        for(BaseResponse response : responses){

            if(response.getCode() !=  200){
                continue;
            }

            Status status;

            if(response instanceof TalkResponse){
                status = ((TalkResponse)response).getStatus();
            } else{
                status = ((StatusResponse)response).getStatus();
            }

            ContentValues statusValues = new ContentValues();
            statusValues.put(RadioRedditContract.Status.COLUMN_ONLINE, status.getOnline());
            statusValues.put(RadioRedditContract.Status.COLUMN_RELAY, status.getRelay());
            statusValues.put(RadioRedditContract.Status.COLUMN_LISTENERS, status.getListeners());
            statusValues.put(RadioRedditContract.Status.COLUMN_ALL_LISTENERS, status.getAll_listeners());
            statusValues.put(RadioRedditContract.Status.COLUMN_PLAYLIST, status.getPlaylist());

            Uri insertUri = resolver.insert(RadioRedditContract.Status.CONTENT_URI, statusValues);

            if(response instanceof StatusResponse){

                long statusId = ContentUris.parseId(insertUri);

                LinkedList<Song> songs = (LinkedList<Song>)status.getSongs();

                int size = songs.size();

                ContentValues[] values = new ContentValues[size];

                int count = 0;

                Iterator<Song> iter = songs.iterator();

                while(iter.hasNext()){
                    ContentValues songValue = new ContentValues();
                    Song song = iter.next();
                    songValue.put(RadioRedditContract.Song.COLUMN_STATUS_ID, statusId);
                    songValue.put(RadioRedditContract.Song.COLUMN_ALBUM, song.getAlbum());
                    songValue.put(RadioRedditContract.Song.COLUMN_ARTIST, song.getArtist());
                    songValue.put(RadioRedditContract.Song.COLUMN_DOWNLOAD_URL, song.getDownload_url());
                    songValue.put(RadioRedditContract.Song.COLUMN_GENRE, song.getGenre());
                    songValue.put(RadioRedditContract.Song.COLUMN_PREVIEW_URL, song.getPreview_url());
                    songValue.put(RadioRedditContract.Song.COLUMN_REDDIT_TITLE, song.getReddit_title());
                    songValue.put(RadioRedditContract.Song.COLUMN_REDDITOR, song.getRedditor());
                    songValue.put(RadioRedditContract.Song.COLUMN_SCORE, song.getScore());
                    songValue.put(RadioRedditContract.Song.COLUMN_TITLE, song.getTitle());
                    values[count++] = songValue;
                }

                resolver.bulkInsert(RadioRedditContract.Song.CONTENT_URI, values);
            }

        }

    }

    public static void syncImmediately(Context ctx){
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(ctx),
                ctx.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context ctx){
        AccountManager accountManager =
                (AccountManager)ctx.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
          ctx.getString(R.string.radio_reddit_account), ctx.getString(R.string.sync_account_type)
        );

        if( null == accountManager.getPassword(newAccount)){


            if(!accountManager.addAccountExplicitly(newAccount, "", null)){
                return null;
            }

        }
        return newAccount;
    }

}
