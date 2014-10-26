package com.ifightmonsters.radioreddit;

import android.app.Application;

import com.ifightmonsters.radioreddit.utils.ChronoUtils;

/**
 * Created by Gregory on 10/4/2014.
 */
public class MainApp extends Application {

    private static final long DEFAULT_SYNC_INTERVAL_IN_MINUTES = 20L;
    public static final long DEFAULT_SYNC_INTERVAL
            = DEFAULT_SYNC_INTERVAL_IN_MINUTES *
            ChronoUtils.SECONDS_PER_MINUTE *
            ChronoUtils.MILLISECONDS_PER_SECOND;

    @Override
    public void onCreate() {
        super.onCreate();
        /*ContentResolver resolver = getContentResolver();

        Account account =
                new Account(
                        getString(R.string.radio_reddit_account),
                        getString(R.string.sync_account_type));


        resolver.addPeriodicSync(account, getString(R.string.content_authority), null, DEFAULT_SYNC_INTERVAL);*/
    }

    public final boolean isFirstRun(){
        return false;
    }
}
