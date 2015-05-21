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
import android.content.SyncResult;
import android.net.Uri;
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

import java.util.LinkedList;

/**
 * Sync adapter used to grab data from the Radio Reddit servers.
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

        //Broadcasts telling the app that it's in the middle of retrieving data from the server
        mBroadMgr.sendBroadcast(new Intent(BROADCAST_SYNC_BEGIN));

        /**
         * Each station has it's own endpoint for grabbing information.
         */
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
            //We don't want to do anything fancy here, out with the old in with the new.
            purgeDatabases(provider);
            commitResponses(responses, provider);
            MainApp app = (MainApp) getContext().getApplicationContext();
            app.setSyncTimestamp();
        } catch (RemoteException e) {
            Log.e(LOG, e.toString());
        }

        //Broadcast to the app that the sync is completed
        mBroadMgr.sendBroadcast(new Intent(BROADCAST_SYNC_COMPLETED));
    }

    /**
     * purges the databases of all data stored.
     *
     * @param provider provider used to communicate with the databases
     * @throws RemoteException
     */
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

            /*
             * TalkResponses are responses that contain json about the talk streaming station.
             * The json for the talk stream is structured a bit differently than the rest of the
             * streams.
             */
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

            /*
             * TalkResponses don't have a list of songs to import.
             */
            if (response instanceof StatusResponse) {

                long statusId = ContentUris.parseId(insertUri);

                LinkedList<Song> songs = (LinkedList<Song>) status.getSongs();

                if(songs != null){

                    int size = songs.size();

                    if(songs.size() > 0){
                        ContentValues[] values = new ContentValues[size];

                        int count = 0;

                        for(int i = 0; i < songs.size(); i++){
                            ContentValues songValue = new ContentValues();
                            Song song = songs.get(i);
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

        }

    }

    /**
     * Places an immediate request to sync data between the app and the servers
     *
     * @param ctx the context used to put in the sync request.
     */
    public static void syncImmediately(Context ctx) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(ctx),
                ctx.getString(R.string.content_authority), bundle);
    }

    /**
     * Sets up the sync adapter for syncing.
     *
     * @param ctx          context used for creating the sync request
     * @param syncInterval how often the sync should happen in seconds
     * @param flexTime     how much time between before and after the sync interval should the sync happen
     */
    public static void configurePeriodicSync(Context ctx, int syncInterval, int flexTime) {
        Account account = getSyncAccount(ctx);
        String authority = ctx.getString(R.string.content_authority);


        if (account == null) {
            Log.d(LOG, "account is null");
        } else {
            Log.d(LOG, "accout is not null");
        }

        Log.d(LOG, "sync internval: " + syncInterval);
        Log.d(LOG, "flexTime: " + flexTime);
        Log.d(LOG, "authority: " + authority);

        ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
    }

    public static void removePeriodicSync(Context ctx) {
        ContentResolver.removePeriodicSync(getSyncAccount(ctx), ctx.getString(R.string.content_authority), new Bundle());
    }

    /**
     * This method is used for setting up the app for syncing once the dummy account has been created
     *
     * @param newAccount
     * @param ctx
     */
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

    /**
     * Grab/create the dummy sync account for the sync adapter.
     *
     * @param ctx context used to create the dummy sync account
     * @return dummy sync account
     */
    private static Account getSyncAccount(Context ctx) {
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
