package com.ifightmonsters.yarra.ui.activity;

import android.net.Uri;

/**
 * Responsible for communication between fragments and activities by passing
 * the appropriate uri for the action the activity needs to do.
 */
public interface OnFragmentInteractionListener {

    /**
     * Called when communication needs to be done between fragment and activity.
     *
     * @param uri uri delivering the action the fragment is requesting to be done from the activity.
     */
    public void onFragmentInteraction(Uri uri);

}
