package com.ifightmonsters.yarra.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Gregory on 10/31/2014.
 */
public class RadioRedditAuthenticatorService extends Service {

    private RadioRedditAuthenticator mAuthenicator;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenicator = new RadioRedditAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenicator.getIBinder();
    }

}
