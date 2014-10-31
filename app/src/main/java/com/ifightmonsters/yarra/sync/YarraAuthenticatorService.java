package com.ifightmonsters.yarra.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Gregory on 10/31/2014.
 */
public class YarraAuthenticatorService extends Service {

    private YarraAuthenticator mAuthenicator;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenicator = new YarraAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenicator.getIBinder();
    }

}
