package com.ifightmonsters.yarra.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ifightmonsters.yarra.MainApp;
import com.ifightmonsters.yarra.R;
import com.ifightmonsters.yarra.data.YarraContract;
import com.ifightmonsters.yarra.entities.Song;
import com.ifightmonsters.yarra.entities.Status;
import com.ifightmonsters.yarra.network.BaseResponse;
import com.ifightmonsters.yarra.network.RadioReddit;
import com.ifightmonsters.yarra.network.StatusResponse;
import com.ifightmonsters.yarra.network.TalkResponse;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Gregory on 10/31/2014.
 */
public class YarraSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG = "RadioRedditSyncAdapter";

    public static final String BROADCAST_SYNC_BEGIN
            = "com.ifightmonsters.yarra.sync.RadioRedditSyncAdapter.broadcast.sync_begin";

    public static final String BROADCAST_SYNC_COMPLETED
            = "com.ifightmonsters.yarra.sync.RadioRedditSyncAdapter.broadcast.sync_completed";
    private LocalBroadcastManager mBroadMgr;

    public YarraSyncAdapter(Context context, boolean autoInitialize) {
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

        mBroadMgr.sendBroadcast(new Intent(BROADCAST_SYNC_BEGIN));

        LinkedList<BaseResponse> responses = new LinkedList<BaseResponse>();
        responses.add(RadioReddit.getMainStatus());
        responses.add(RadioReddit.getElectronicStatus());
        responses.add(RadioReddit.getIndieStatus());
        responses.add(RadioReddit.getHipHopStatus());
        responses.add(RadioReddit.getRockStatus());
        responses.add(RadioReddit.getMetalStatus());
        responses.add(RadioReddit.getRandomStatus());
        responses.add(RadioReddit.getTalkStatus());

        try {
            purgeDatabases(provider);
            commitResponses(responses, provider);
            MainApp app = (MainApp) getContext().getApplicationContext();
            app.setSyncTimestamp();
        } catch (RemoteException e) {
            Log.e(LOG, e.toString());
        }

        mBroadMgr.sendBroadcast(new Intent(BROADCAST_SYNC_COMPLETED));
    }

    private void purgeDatabases(ContentProviderClient provider) throws RemoteException {
        provider.delete(YarraContract.Song.CONTENT_URI, null, null);
        provider.delete(YarraContract.Status.CONTENT_URI, null, null);
    }

    private void commitResponses(LinkedList<BaseResponse> responses, ContentProviderClient provider)
            throws RemoteException {

        for (BaseResponse response : responses) {

            if (!response.isSuccessful()) {
                continue;
            }

            Status status;

            if (response instanceof TalkResponse) {
                status = ((TalkResponse) response).getStatus();
            } else {
                status = ((StatusResponse) response).getStatus();
            }

            ContentValues statusValues = new ContentValues();
            statusValues.put(YarraContract.Status.COLUMN_ONLINE, status.getOnline());
            statusValues.put(YarraContract.Status.COLUMN_RELAY, status.getRelay());
            statusValues.put(YarraContract.Status.COLUMN_LISTENERS, status.getListeners());
            statusValues.put(YarraContract.Status.COLUMN_ALL_LISTENERS, status.getAll_listeners());
            statusValues.put(YarraContract.Status.COLUMN_PLAYLIST, status.getPlaylist());

            Uri insertUri = provider.insert(YarraContract.Status.CONTENT_URI, statusValues);

            if (response instanceof StatusResponse) {

                long statusId = ContentUris.parseId(insertUri);

                LinkedList<Song> songs = (LinkedList<Song>) status.getSongs();

                int size = songs.size();

                ContentValues[] values = new ContentValues[size];

                int count = 0;

                Iterator<Song> iter = songs.iterator();

                while (iter.hasNext()) {
                    ContentValues songValue = new ContentValues();
                    Song song = iter.next();
                    songValue.put(YarraContract.Song.COLUMN_STATUS_ID, statusId);
                    songValue.put(YarraContract.Song.COLUMN_ALBUM, song.getAlbum());
                    songValue.put(YarraContract.Song.COLUMN_ARTIST, song.getArtist());
                    songValue.put(YarraContract.Song.COLUMN_DOWNLOAD_URL, song.getDownload_url());
                    songValue.put(YarraContract.Song.COLUMN_GENRE, song.getGenre());
                    songValue.put(YarraContract.Song.COLUMN_PREVIEW_URL, song.getPreview_url());
                    songValue.put(YarraContract.Song.COLUMN_REDDIT_TITLE, song.getReddit_title());
                    songValue.put(YarraContract.Song.COLUMN_REDDITOR, song.getRedditor());
                    songValue.put(YarraContract.Song.COLUMN_SCORE, song.getScore());
                    songValue.put(YarraContract.Song.COLUMN_TITLE, song.getTitle());
                    values[count++] = songValue;
                }

                provider.bulkInsert(YarraContract.Song.CONTENT_URI, values);
            }

        }

    }

    public static void syncImmediately(Context ctx) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(ctx),
                ctx.getString(R.string.content_authority), bundle);
    }

    public static void configurePeriodicSync(Context ctx, int syncInterval, int flexTime) {
        Account account = getSyncAccount(ctx);
        String authority = ctx.getString(R.string.content_authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime)
                    .setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    public static void removePeriodicSync(Context ctx) {
        ContentResolver.removePeriodicSync(getSyncAccount(ctx), ctx.getString(R.string.content_authority), new Bundle());
    }

    private static void onAccountCreated(Account newAccount, Context ctx) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        int syncInterval = Integer.valueOf(
                sharedPref.getString(
                        ctx.getString(R.string.pref_sync_interval),
                        Integer.toString(ctx.getResources().getInteger(R.integer.default_sync_interval))));

        configurePeriodicSync(
                ctx,
                syncInterval,
                ctx.getResources().getInteger(R.integer.default_flextime_interval));

        ContentResolver.setSyncAutomatically(
                newAccount,
                ctx.getString(R.string.content_authority),
                true);

        syncImmediately(ctx);
    }

    public static Account getSyncAccount(Context ctx) {
        AccountManager accountManager =
                (AccountManager) ctx.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                ctx.getString(R.string.radio_reddit_account), ctx.getString(R.string.sync_account_type)
        );

        if (null == accountManager.getPassword(newAccount)) {


            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            onAccountCreated(newAccount, ctx);
        }
        return newAccount;
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}
