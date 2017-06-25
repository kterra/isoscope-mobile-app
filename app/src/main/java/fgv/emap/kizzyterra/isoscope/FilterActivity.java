package fgv.emap.kizzyterra.isoscope;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

public class FilterActivity extends AppCompatActivity {

    private static final String TAG = "Filter";
    private Double isochroneDuration = 5.0;
    private static final int BICYCLING = 1000;
    private static final int DRIVING = 2000;
    private static final int WALKING = 3000;
    private static final int TRANSIT = 4000;
    private int MODE = WALKING;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    private LatLng lastPositionSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

//        Button filterButton = (Button) findViewById(R.id.gotomap);
//        filterButton.setOnClickListener(this);
    }

    public void onClickCard(View v) {

        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }

    }

    public void onClickButton(View v) {

        if(lastPositionSelected == null){
           Toast.makeText(this,"Selecione um ponto de referência!",Toast.LENGTH_LONG).show();
            Log.i(TAG, "null");

        }else {

            Integer hours;
            Integer minutes;
            Integer seconds;

            EditText editTextHour = (EditText) findViewById(R.id.editText2);
            EditText editTextMinute = (EditText) findViewById(R.id.editText3);
            EditText editTextSecond = (EditText) findViewById(R.id.editText4);

            try {
                hours = Integer.valueOf(editTextHour.getText().toString());
            } catch (NumberFormatException ne) {
                hours = 0;
            }

            try {
                minutes = Integer.valueOf(editTextMinute.getText().toString());
            } catch (NumberFormatException ne) {
                minutes = 0;
            }

            try {
                seconds = Integer.valueOf(editTextSecond.getText().toString());
            } catch (NumberFormatException ne) {
                seconds = 0;
            }

            Double isochroneDurationInserted;
            isochroneDurationInserted = hours * 60.0 + minutes + seconds / 60.0;

            if (isochroneDurationInserted > 0) {
                isochroneDuration = isochroneDurationInserted;
            } else {

            }

            Intent intent = new Intent();
            intent.putExtra("duration", isochroneDuration);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {


                Place place = PlaceAutocomplete.getPlace(this, data);
                CharSequence addressReturned = place.getAddress();
                TextView referencePoint = (TextView) findViewById(R.id.location_reference);
                referencePoint.setText(addressReturned);
                referencePoint.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                lastPositionSelected = place.getLatLng();



            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
