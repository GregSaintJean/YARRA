package com.ifightmonsters.radioreddit.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by Gregory on 10/5/2014.
 */
public class RadioRedditSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG = RadioRedditSyncAdapter.class.getSimpleName();

    public RadioRedditSyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

    }

}
