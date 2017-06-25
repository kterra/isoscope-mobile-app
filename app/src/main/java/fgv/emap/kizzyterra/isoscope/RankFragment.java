package fgv.emap.kizzyterra.isoscope;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by kizzyterra on 16/06/17.
 */

public class RankFragment extends Fragment {

    private static final String TAG = "RankFrag";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_rank, container, false);

        return rootView;
    }
}