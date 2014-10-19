package com.ifightmonsters.radioreddit.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ifightmonsters.radioreddit.R;
import com.ifightmonsters.radioreddit.ui.activity.MainActivity;

public class MainFragment extends Fragment implements AdapterView.OnItemClickListener{

    private static final String OUTPUT_TV_KEY = "output_tv";

    private MainActivity mActivity;
    private ListView mListView;
    private ImageView mErrorImage;
    private TextView mErrorText;

    public static MainFragment newInstance(){
        return new MainFragment();
    }

    public MainFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView)v.findViewById(R.id.list);
        mListView.setAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.stations, android.R.layout.simple_list_item_1));
        mErrorImage = (ImageView)v.findViewById(R.id.error_image);
        mErrorText = (TextView)v.findViewById(R.id.error_text);
        mListView.setOnItemClickListener(this);
        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        mActivity.onFragmentInteraction(
                MainActivity.BASE_ACTIVITY_URI
                        .buildUpon()
                .appendPath(MainActivity.PATH_STATION)
                .appendPath(String.valueOf(position))
                .build()
        );
    }
}
