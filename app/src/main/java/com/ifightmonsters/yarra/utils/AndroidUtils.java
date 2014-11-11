package com.ifightmonsters.yarra.utils;

import android.content.Context;
import android.os.Build;

import com.ifightmonsters.yarra.R;

/**
 * Assortment of methods used specifcally for Android to make things easier
 */
public final class AndroidUtils {

    public static boolean equalOrGreaterThanHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Checks whether or not the device can support a two-pane layout. This is based on what
     * is returned back from the resource value buckets.
     *
     * @param ctx the context used to do the check
     * @return whether or not the device supports a two-pane layout
     */
    public static final boolean isTwoPane(Context ctx) {
        return ctx.getResources().getBoolean(R.bool.is_two_pane);
    }

    private AndroidUtils() {
    }
}
