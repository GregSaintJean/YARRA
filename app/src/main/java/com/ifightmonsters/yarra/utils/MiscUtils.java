package com.ifightmonsters.yarra.utils;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by Gregory on 10/31/2014.
 */
public final class MiscUtils {

    public static final boolean isHeadsetOn(Context ctx){

        if(ctx == null){
            return false;
        }

        AudioManager mgr = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);

        return mgr.isWiredHeadsetOn()
                || mgr.isBluetoothA2dpOn()
                || mgr.isBluetoothScoOn();
    }

    private MiscUtils(){}
}
