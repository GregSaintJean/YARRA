package com.ifightmonsters.yarra.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ifightmonsters.yarra.R;

/**
 * Created by Gregory on 10/31/2014.
 */
public class PlaceHolderFragment extends Fragment {

    public static PlaceHolderFragment newInstance(){
        return new PlaceHolderFragment();
    }

    public PlaceHolderFragment (){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

}
