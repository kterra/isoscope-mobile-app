package fgv.emap.kizzyterra.isoscope;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private String TAG = "Main";
    private static final int ISOCHRONE_DURATION_REQUEST_CODE = 1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_CARD_START = 2;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_CARD_END = 3;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_CARD_REFERENCE = 4;
    private static final double SIXTY_MINUTES = 60.0;
    private static final double THIRTY_MINUTES = 30.0;
    private static final double TWENTY_MINUTES = 20.0;
    private static final double FIFTEEN_MINUTES = 15.0;
    private static final double FIVE_MINUTES = 5.0;
    private static final int BICYCLING = 1000;
    private static final int DRIVING = 2000;
    private static final int WALKING = 3000;
    private static final int TRANSIT = 4000;
    private int MODE;
    private LocFragment lf;
    private TimeFragment tf;
    private RankFragment rf;
    public double isochroneDuration = 0.0;
    public String isochroneCenterAddress;
    public LatLng isochroneCenterCoordinate;
    public String isochroneEndAddress;
    public LatLng isochroneEndCoordinate;
    public boolean FIVE_MINUTES_SELECTED = false;
    public boolean FIFTEEN_MINUTES_SELECTED = false;
    public boolean TWENTY_MINUTES_SELECTED = false;
    public boolean THIRTY_MINUTES_SELECTED = false;
    public boolean SIXTY_MINUTES_SELECTED = false;

    public boolean WALKING_SELECTED = true;
    public boolean DRIVING_SELECTED = false;
    public boolean TRANSIT_SELECTED = false;

    private int LOC_FRAGMENT_ID =1;
    private int TIME_FRAGMENT_ID = 2;
    private int RANK_FRAGMENT_ID = 3;
    private String FRAGMENT_ID = "ID";
    private String MODE_VALUE = "MODE";
    private String TIME_VALUE = "TIME";
    private String CENTER_ADDRESS_VALUE = "CENTER_ADDRESS";
    private String CENTER_LAT_VALUE = "CENTER_LAT";
    private String CENTER_LONG_VALUE = "CENTER_LONG";
    private String END_ADDRESS_VALUE = "END_ADDRESS";
    private String END_COORDINATE_VALUE = "END_COORDINATE";


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//       Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//       setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(1);
        tabLayout.setupWithViewPager(mViewPager);

        MODE = WALKING;

//        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
//                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
//
//        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(Place place) {
//                // TODO: Get info about the selected place.
//                Log.i(TAG, "Place: " + place.getName());
//            }
//
//            @Override
//            public void onError(Status status) {
//                // TODO: Handle the error.
//                Log.i(TAG, "An error occurred: " + status);
//            }
//        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_plus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        Log.d(TAG, "OnActivityResult");
        for(Fragment frag : getSupportFragmentManager().getFragments()){
            frag.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    if(lf == null){
                        lf = new LocFragment();
                    }
                    return  lf;
                case 1:
                    if(tf == null){
                        tf = new TimeFragment();
                    }
                    return  tf;
                case 2:
                    if(rf == null){
                        rf = new RankFragment();
                    }
                    return  rf;
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "BY LOCATION";

                case 1:
                    return "BY TIME";
                case 2:
                    return "RANK";

            }
            return null;
        }

    }
    public void floatingActionButtonClicked(View fab){
        startActivityForResult(new Intent(this, FilterActivity.class), ISOCHRONE_DURATION_REQUEST_CODE);
    }

    public void onClickCardStart(View v) {

        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_CARD_START);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }

    }

    public void onClickCardEnd(View v) {

        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_CARD_END);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }

    }

    public void onClickCardReference(View v) {

        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_CARD_REFERENCE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }

    }

    public void onClickWalkingButton(View v){

    }

    public void onClickDrivingButton(View v){

    }
    public void onClickTransitButton(View v){

    }


    public void onClickTimeFragFiveMinutesButton(View v){

        Button five_minutes = (Button) v;
        if (!FIVE_MINUTES_SELECTED){
            five_minutes.setBackground(getDrawable(R.drawable.menu_button_selected));
            five_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_selected_text_color));
            FIVE_MINUTES_SELECTED = true;
            isochroneDuration = FIVE_MINUTES;

            if(FIFTEEN_MINUTES_SELECTED){
                Button fifteen_minutes = (Button) findViewById(R.id.fifteenminutes);
                fifteen_minutes.setBackground(getDrawable(R.drawable.menu_button));
                fifteen_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                FIFTEEN_MINUTES_SELECTED = false;
            }
            if(TWENTY_MINUTES_SELECTED){
                Button twenty_minutes = (Button) findViewById(R.id.twentyminutes);
                twenty_minutes.setBackground(getDrawable(R.drawable.menu_button));
                twenty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                TWENTY_MINUTES_SELECTED = false;
            }
            if(THIRTY_MINUTES_SELECTED){
                Button thirty_minutes = (Button) findViewById(R.id.thirtyminutes);
                thirty_minutes.setBackground(getDrawable(R.drawable.menu_button));
                thirty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                THIRTY_MINUTES_SELECTED = false;
            }
            if(SIXTY_MINUTES_SELECTED){
                Button sixty_minutes = (Button) findViewById(R.id.sixtyminutes);
                sixty_minutes.setBackground(getDrawable(R.drawable.menu_button));
                sixty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                SIXTY_MINUTES_SELECTED = false;
            }

        }else{
            five_minutes.setBackground(getDrawable(R.drawable.menu_button));
            five_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
            FIVE_MINUTES_SELECTED = false;
            isochroneDuration = 0;
        }

    }

    public void onClickTimeFragFifTeenMinutesButton(View v) {
        Button fifteen_minutes = (Button) v;
        if (!FIFTEEN_MINUTES_SELECTED) {
            fifteen_minutes.setBackground(getDrawable(R.drawable.menu_button_selected));
            fifteen_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_selected_text_color));
            FIFTEEN_MINUTES_SELECTED = true;
            isochroneDuration = FIFTEEN_MINUTES;

            if(FIVE_MINUTES_SELECTED){
                Button five_minutes = (Button) findViewById(R.id.fiveminutes);
                five_minutes.setBackground(getDrawable(R.drawable.menu_button));
                five_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                FIVE_MINUTES_SELECTED = false;
            }
            if (TWENTY_MINUTES_SELECTED) {
                Button twenty_minutes = (Button) findViewById(R.id.twentyminutes);
                twenty_minutes.setBackground(getDrawable(R.drawable.menu_button));
                twenty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                TWENTY_MINUTES_SELECTED = false;
            }
            if (THIRTY_MINUTES_SELECTED) {
                Button thirty_minutes = (Button) findViewById(R.id.thirtyminutes);
                thirty_minutes.setBackground(getDrawable(R.drawable.menu_button));
                thirty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                THIRTY_MINUTES_SELECTED = false;
            }
            if (SIXTY_MINUTES_SELECTED) {
                Button sixty_minutes = (Button) findViewById(R.id.sixtyminutes);
                sixty_minutes.setBackground(getDrawable(R.drawable.menu_button));
                sixty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                SIXTY_MINUTES_SELECTED = false;
            }

        }else{
            fifteen_minutes.setBackground(getDrawable(R.drawable.menu_button));
            fifteen_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
            FIFTEEN_MINUTES_SELECTED = false;
            isochroneDuration = 0;
        }
    }

    public void onClickTimeFragTwentyMinutesButton(View v){
        Button twenty_minutes = (Button) v;
        if (!TWENTY_MINUTES_SELECTED){
            twenty_minutes.setBackground(getDrawable(R.drawable.menu_button_selected));
            twenty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_selected_text_color));
            TWENTY_MINUTES_SELECTED = true;
            isochroneDuration = TWENTY_MINUTES;

            if(FIFTEEN_MINUTES_SELECTED){
                Button fifteen_minutes = (Button) findViewById(R.id.fifteenminutes);
                fifteen_minutes.setBackground(getDrawable(R.drawable.menu_button));
                fifteen_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                FIFTEEN_MINUTES_SELECTED = false;
            }
            if(FIVE_MINUTES_SELECTED){
                Button five_minutes = (Button) findViewById(R.id.fiveminutes);
                five_minutes.setBackground(getDrawable(R.drawable.menu_button));
                five_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                FIVE_MINUTES_SELECTED = false;
            }
            if(THIRTY_MINUTES_SELECTED){
                Button thirty_minutes = (Button) findViewById(R.id.thirtyminutes);
                thirty_minutes.setBackground(getDrawable(R.drawable.menu_button));
                thirty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                THIRTY_MINUTES_SELECTED = false;
            }
            if(SIXTY_MINUTES_SELECTED){
                Button sixty_minutes = (Button) findViewById(R.id.sixtyminutes);
                sixty_minutes.setBackground(getDrawable(R.drawable.menu_button));
                sixty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                SIXTY_MINUTES_SELECTED = false;
            }
    }else{
            twenty_minutes.setBackground(getDrawable(R.drawable.menu_button));
            twenty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
            TWENTY_MINUTES_SELECTED = false;
            isochroneDuration = 0;
        }

    }

    public void onClickTimeFragThirtyMinutesButton(View v){
        Button thirty_minutes = (Button) v;
        if (!THIRTY_MINUTES_SELECTED){
            thirty_minutes.setBackground(getDrawable(R.drawable.menu_button_selected));
            thirty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_selected_text_color));
            THIRTY_MINUTES_SELECTED = true;
            isochroneDuration =THIRTY_MINUTES;

            if(FIFTEEN_MINUTES_SELECTED){
                Button fifteen_minutes = (Button) findViewById(R.id.fifteenminutes);
                fifteen_minutes.setBackground(getDrawable(R.drawable.menu_button));
                fifteen_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                FIFTEEN_MINUTES_SELECTED = false;
            }
            if(TWENTY_MINUTES_SELECTED){
                Button twenty_minutes = (Button) findViewById(R.id.twentyminutes);
                twenty_minutes.setBackground(getDrawable(R.drawable.menu_button));
                twenty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                TWENTY_MINUTES_SELECTED = false;
            }
            if(FIVE_MINUTES_SELECTED){
                Button five_minutes = (Button) findViewById(R.id.fiveminutes);
                five_minutes.setBackground(getDrawable(R.drawable.menu_button));
                five_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                FIVE_MINUTES_SELECTED = false;
            }
            if(SIXTY_MINUTES_SELECTED){
                Button sixty_minutes = (Button) findViewById(R.id.sixtyminutes);
                sixty_minutes.setBackground(getDrawable(R.drawable.menu_button));
                sixty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                SIXTY_MINUTES_SELECTED = false;
            }
    }else{
            thirty_minutes.setBackground(getDrawable(R.drawable.menu_button));
            thirty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
            THIRTY_MINUTES_SELECTED = false;
            isochroneDuration = 0;
        }

    }

    public void onClickTimeFragSixtyMinutesButton(View v){
        Button sixty_minutes = (Button) v;
        if (!SIXTY_MINUTES_SELECTED){
            sixty_minutes.setBackground(getDrawable(R.drawable.menu_button_selected));
            sixty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_selected_text_color));
            SIXTY_MINUTES_SELECTED = true;
            isochroneDuration = SIXTY_MINUTES;

            if(FIFTEEN_MINUTES_SELECTED){
                Button fifteen_minutes = (Button) findViewById(R.id.fifteenminutes);
                fifteen_minutes.setBackground(getDrawable(R.drawable.menu_button));
                fifteen_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                FIFTEEN_MINUTES_SELECTED = false;
            }
            if(TWENTY_MINUTES_SELECTED){
                Button twenty_minutes = (Button) findViewById(R.id.twentyminutes);
                twenty_minutes.setBackground(getDrawable(R.drawable.menu_button));
                twenty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                TWENTY_MINUTES_SELECTED = false;
            }
            if(THIRTY_MINUTES_SELECTED){
                Button thirty_minutes = (Button) findViewById(R.id.thirtyminutes);
                thirty_minutes.setBackground(getDrawable(R.drawable.menu_button));
                thirty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                THIRTY_MINUTES_SELECTED = false;
            }
            if(FIVE_MINUTES_SELECTED){
                Button five_minutes = (Button) findViewById(R.id.fiveminutes);
                five_minutes.setBackground(getDrawable(R.drawable.menu_button));
                five_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
                FIVE_MINUTES_SELECTED = false;
            }
        }else{
            sixty_minutes.setBackground(getDrawable(R.drawable.menu_button));
            sixty_minutes.setTextColor(ContextCompat.getColor(this, R.color.menu_button_text_color));
            SIXTY_MINUTES_SELECTED = false;
            isochroneDuration = 0;
        }

    }


    public void onClickTimeFragButton(View v){
        if(isochroneCenterCoordinate == null) {
            Toast.makeText(this,"Selecione um ponto de referência!",Toast.LENGTH_LONG).show();
        }else{
            Integer hours;
            Integer minutes;
            Integer seconds;

            EditText editTextHour = (EditText) findViewById(R.id.hours_edit);
            EditText editTextMinute = (EditText) findViewById(R.id.minutes_edit);
            EditText editTextSecond = (EditText) findViewById(R.id.seconds_edit);

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
            }

            if (isochroneDuration == 0){
                Toast.makeText(this,"Selecione a duração!",Toast.LENGTH_LONG).show();
            }else{
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra(FRAGMENT_ID, TIME_FRAGMENT_ID);
                intent.putExtra(MODE_VALUE, MODE);
                intent.putExtra(TIME_VALUE, isochroneDuration);
                intent.putExtra(CENTER_ADDRESS_VALUE, isochroneCenterAddress);
                intent.putExtra(CENTER_LAT_VALUE, isochroneCenterCoordinate.latitude);
                intent.putExtra(CENTER_LONG_VALUE, isochroneCenterCoordinate.longitude);

                startActivity(intent);
            }

        }
    }




}
