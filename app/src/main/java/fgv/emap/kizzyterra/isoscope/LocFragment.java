package fgv.emap.kizzyterra.isoscope;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

/**
 * Created by kizzyterra on 16/06/17.
 */

public class LocFragment extends Fragment {

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_CARD_START = 2;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_CARD_END = 3;
    private static final String TAG = "LocFrag";
    private View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        rootView = inflater.inflate(R.layout.fragment_loc, container, false);

        return rootView;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_CARD_START) {
            if (resultCode == Activity.RESULT_OK) {


                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                CharSequence addressReturned = place.getAddress();
                TextView startReferencePoint = (TextView) rootView.findViewById(R.id.location_start);
                startReferencePoint.setText(addressReturned);
                startReferencePoint.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));



            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

        // Check which request we're responding to
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_CARD_END) {
            if (resultCode == Activity.RESULT_OK) {


                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                CharSequence addressReturned = place.getAddress();
                TextView endReferencePoint = (TextView) rootView.findViewById(R.id.location_end);
                endReferencePoint.setText(addressReturned);
                endReferencePoint.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));



            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

}
