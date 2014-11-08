package com.ifightmonsters.yarra.ui.activity;

import android.net.Uri;

/**
 * Responsible for communication between fragments and activities by passing
 * the appropriate uri for the action the activity needs to do.
 */
public interface OnFragmentInteractionListener {

    public void onFragmentInteraction(Uri uri);

}
