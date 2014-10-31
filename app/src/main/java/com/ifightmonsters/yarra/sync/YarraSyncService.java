package com.ifightmonsters.yarra.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Gregory on 10/31/2014.
 */
public class YarraSyncService extends Service {

    private static final String LOG = "RadioRedditSyncService";

    private static final Object sSyncAdapterLock = new Object();
    private static YarraSyncAdapter sYarraSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d(LOG, "onCreate - " + LOG);
        synchronized (sSyncAdapterLock) {
            sYarraSyncAdapter = new YarraSyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sYarraSyncAdapter.getSyncAdapterBinder();
    }

}
