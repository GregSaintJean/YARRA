package com.ifightmonsters.radioreddit.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Gregory on 10/5/2014.
 */
public class RadioRedditSyncService extends Service {

    private static final String LOG = "RadioRedditSyncService";

    private static final Object sSyncAdapterLock = new Object();
    private static RadioRedditSyncAdapter sRadioRedditSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d(LOG, "onCreate - " + LOG);
        synchronized (sSyncAdapterLock){
            sRadioRedditSyncAdapter = new RadioRedditSyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sRadioRedditSyncAdapter.getSyncAdapterBinder();
    }
}
