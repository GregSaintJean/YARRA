package com.ifightmonsters.yarra.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * The service used for the sync adapter. Basic implementation based on what I learned at udacity.
 */
public class YarraSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static YarraSyncAdapter sYarraSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            sYarraSyncAdapter = new YarraSyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sYarraSyncAdapter.getSyncAdapterBinder();
    }

}
