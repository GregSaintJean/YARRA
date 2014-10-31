package com.ifightmonsters.yarra.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Gregory on 10/31/2014.
 */
public final class NetworkUtils {

    public static boolean hasNetworkConnectivity(Context ctx){

        if(ctx == null){
            return false;
        }

        ConnectivityManager connMgr =
                (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    private NetworkUtils(){}

}
