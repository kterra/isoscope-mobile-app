package fgv.emap.kizzyterra.isoscope;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


public class MapsActivity extends AppCompatActivity implements OnConnectionFailedListener, OnMapReadyCallback, MapDrawerCallback, ConvexHullAlgorithm, OnMapLongClickListener, OnMarkerClickListener {

    private GoogleMap mMap;
    private static final float MAP_MIN_ZOOM = 10.5f;
    private static final float MAP_MAX_ZOOM = 14.0f;
    private static final double TEN_MINUTES = 10.0;
    private static final double FIVE_MINUTES = 5.0;
    private static final int ISOCHRONE_DURATION_REQUEST_CODE = 1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    private Double isochroneDuration = 0.0;
    private String TAG = "Maps";
    //private FirebaseManager fireManager;
    //private PolygonOptions rectOptions;
    //private Polygon region;
    //private ArrayList<LatLng> regionPoints;
    private Marker lastMarkerClicked;
    private LatLng lastPositionSelected;

    private CircleGrid grid;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
//       // toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivityForResult(new Intent(MapsActivity.this, FilterActivity.class), ISOCHRONE_DURATION_REQUEST_CODE);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //fireManager = new FirebaseManager(MapsActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ISOCHRONE_DURATION_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
               isochroneDuration = data.getDoubleExtra("duration", 1);
                Log.d(TAG, isochroneDuration.toString());

            }
        }

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {


                Place place = PlaceAutocomplete.getPlace(this, data);
                lastPositionSelected = place.getLatLng();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lastPositionSelected, MAP_MIN_ZOOM);
                mMap.moveCamera(cameraUpdate);

                Log.i(TAG, "Place: " + lastPositionSelected.toString());
                if(isochroneDuration == 0.0){
                    isochroneDuration = TEN_MINUTES;
                }
                new GetIsochroneTask().execute(lastPositionSelected.latitude, lastPositionSelected.longitude, isochroneDuration);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        // Assumes current activity is the searchable activity
//        MenuItem searchItem = menu.findItem(R.id.menu_search_view);
//        // Get the SearchView and set the searchable configuration
//        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
//        searchView.setSubmitButtonEnabled(true);
//
//        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
//            public boolean onQueryTextChange(String newText) {
//
//                return true;
//            }
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//        };
//        searchView.setOnQueryTextListener(queryTextListener);


        //super.onCreateOptionsMenu(menu, inflater);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);

        // Add a marker in Sydney and move the camera
        LatLng rio = new LatLng(-22.91541,-43.4258447);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rio, MAP_MIN_ZOOM));




        //fireManager.removeAll();
        //fireManager.insertData(MapsActivity.this);

        //Log.d(TAG, "called getPoints");
        //fireManager.getGridPoints();


//        GenericUrl directionsUrl = new GenericUrl("https://maps.googleapis.com/maps/api/directions/json");
//        directionsUrl.put("origin", "Chicago,IL");
//        directionsUrl.put("destination", "Los Angeles,CA");
//        directionsUrl.put("sensor",false);
//        directionsUrl.put("key","AIzaSyDgOs8KSdKtp8BU0cgH9oRlRVfuP07K7pM");
//        Log.d(TAG, "TESTE1");
//        //new GoogleApisRequesterTask().execute(url);
//
//        GenericUrl matrixDistanceUrl = new GenericUrl("https://maps.googleapis.com/maps/api/distancematrix/json");
//        matrixDistanceUrl.put("origins", "Seattle");
//        matrixDistanceUrl.put("destinations", "37.757815,-122.50764|48.4267596,-123.3934356");
//        matrixDistanceUrl.put("key","AIzaSyDZWHoI__d9kG7QinJKdPk9mIQf0bo_FbU");
//
//        new GoogleApisRequesterTask().execute(matrixDistanceUrl);


    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Log.d(TAG, "onMarkerClick");

        mMap.clear();

        lastMarkerClicked = marker;

        mMap.addMarker(new MarkerOptions().position(lastPositionSelected).snippet(lastMarkerClicked.getSnippet()));
        LatLng origin = marker.getPosition();

        if(isochroneDuration == 0.0){
            isochroneDuration = FIVE_MINUTES;
        }

        grid = new CircleGrid(origin, isochroneDuration);
        new GetTimeDataForGrid().execute();

//        String startPoint = String.valueOf(marker.getSnippet());
//        Integer timeLimit = 3000;

        //fireManager.filterRegionByTime(startPoint, timeLimit);

        return true;
    }

    @Override
    public void onMapLongClick (LatLng point){
        mMap.clear();
        Log.d(TAG,"LONGCLICK");
        lastMarkerClicked =  drawMarker("100", point);
        lastPositionSelected = lastMarkerClicked.getPosition();
      //  testCircle();
    }

    @Override
    public Marker drawMarker(String index, LatLng point) {

       return mMap.addMarker(new MarkerOptions().position(point).snippet(index));

    }

    @Override
    public void drawRegion(ArrayList<LatLng> regionPoints) {

        //mMap.clear();


        mMap.addMarker(new MarkerOptions().position(lastPositionSelected));


        PolygonOptions rectOptions = new PolygonOptions()
                .strokeWidth(2.0f)
                .fillColor(Color.argb(150, 102, 140, 255))
                .strokeColor(Color.argb(150, 102, 140, 255));
        //ArrayList<LatLng> convexHullPoints =  getConvexHull(regionPoints);
        for (LatLng p : regionPoints){
            rectOptions.add(p);
        }
        mMap.addPolygon(rectOptions);

    }


    @Override
    public ArrayList<LatLng> getConvexHull(ArrayList<LatLng> points) {

        ArrayList<LatLng> xSorted = (ArrayList<LatLng>) points.clone();
        Collections.sort(xSorted, new XCompare());

        int n = xSorted.size();

        LatLng[] lUpper = new LatLng[n];

        if(n > 1) {

            lUpper[0] = xSorted.get(0);
            lUpper[1] = xSorted.get(1);

            int lUpperSize = 2;

            for (int i = 2; i < n; i++) {
                lUpper[lUpperSize] = xSorted.get(i);
                lUpperSize++;

                while (lUpperSize > 2 && !rightTurn(lUpper[lUpperSize - 3], lUpper[lUpperSize - 2], lUpper[lUpperSize - 1])) {
                    // Remove the middle point of the three last
                    lUpper[lUpperSize - 2] = lUpper[lUpperSize - 1];
                    lUpperSize--;
                }
            }

            LatLng[] lLower = new LatLng[n];

            lLower[0] = xSorted.get(n - 1);
            lLower[1] = xSorted.get(n - 2);

            int lLowerSize = 2;

            for (int i = n - 3; i >= 0; i--) {
                lLower[lLowerSize] = xSorted.get(i);
                lLowerSize++;

                while (lLowerSize > 2 && !rightTurn(lLower[lLowerSize - 3], lLower[lLowerSize - 2], lLower[lLowerSize - 1])) {
                    // Remove the middle point of the three last
                    lLower[lLowerSize - 2] = lLower[lLowerSize - 1];
                    lLowerSize--;
                }
            }

            ArrayList<LatLng> result = new ArrayList<LatLng>();

            for (int i = 0; i < lUpperSize; i++) {
                result.add(lUpper[i]);
            }

            for (int i = 1; i < lLowerSize - 1; i++) {
                result.add(lLower[i]);
            }

            return result;
        }else{
            return points;
        }
    }

    private boolean rightTurn(LatLng a, LatLng b, LatLng c) {
        return (b.latitude - a.latitude) * (c.longitude - a.longitude) - (b.longitude - a.longitude) * (c.latitude - a.latitude) > 0;
    }

    private class XCompare implements Comparator<LatLng> {
        @Override
        public int compare(LatLng o1, LatLng o2) {
            return (new Float(o1.latitude)).compareTo(new Float(o2.latitude));
        }


    }


    public ArrayList<LatLng> sortPoints(LatLng origin, LatLng[] isochrone){

      /**  Put the isochrone points in a proper order **/


        ArrayList<Double> bearings = new ArrayList<>();
        ArrayList<LatLng> sortedPoints = new ArrayList<>();
        for(LatLng point : isochrone){
            bearings.add(Utils.getBearing(origin, point));
        }

//        Log.d(TAG, bearings.toString());

        HashMap <Double, LatLng> points = new HashMap<>(bearings.size());
        if (bearings.size() == isochrone.length) {
            for (int i = 0; i <bearings.size(); ++i) {
                points.put(bearings.get(i), isochrone[i]);
            }
        }

//        Log.d(TAG, points.toString());

        Map<Double, LatLng> treeMap = new TreeMap<>(points);
        for (Object v : treeMap.values()){
            LatLng value = (LatLng) v;
            sortedPoints.add(value);
        }

//        Log.d(TAG, sortedPoints.toString());
        return sortedPoints;
    }


    private class GetIsochroneTask extends AsyncTask<Double, Integer, ArrayList<LatLng>> {
        // Double : origin.latitude, origin.longitude, duration
        protected ArrayList<LatLng> doInBackground(Double... args) {

            LatLng origin = new LatLng(args[0], args[1]);
            Double duration = args[2];
            Double estimated_max_radius = duration*1333; // *84 for walking
            Log.d("tempo", duration.toString());
            Double radius_km = 0.1;
            Integer numberOfAngles = 40;
            LatLng[] isochrone = new LatLng[numberOfAngles];
            Double tolerance = 0.5;

            HashMap<LatLng, Double> data_durations = new HashMap();
            HashMap<LatLng, Double> data_distances = new HashMap();
            int MAX_LOOPS = 5;


            /*Make a radius list, one element for each angle,
          whose elements will update until the isochrone is found */

            Double [] rad1 = new Double [numberOfAngles];
            for (int i = 0; i< numberOfAngles; i++){
                rad1[i] = estimated_max_radius; // avarega walking speed: 84 m/min;
            }

            Double [] phi1 = new Double [numberOfAngles];
            for (int i = 0; i< numberOfAngles; i++){
                phi1[i] = i*(360/Double.valueOf(numberOfAngles));
            }

            LatLng [] data0 = new LatLng [numberOfAngles];
            for (int i = 0; i< numberOfAngles; i++){
                data0[i] = new LatLng(0.0,0.0);
            }

            Double [] rad0 = new Double [numberOfAngles];
            for (int i = 0; i< numberOfAngles; i++){
                rad0 [i] = 0.0;
            }

            Double [] rmin= new Double [numberOfAngles];
            for (int i = 0; i< numberOfAngles; i++){
                rmin[i] = 0.0;
            }

            Double [] rmax = new Double [numberOfAngles];
            for (int i = 0; i< numberOfAngles; i++){
                rmax[i] = 1.25*estimated_max_radius;
            }

            int loops = 0;
            while (loops < MAX_LOOPS){
                Log.d(TAG, String.valueOf(loops));
                Double [] rad2 = new Double [numberOfAngles];
                for (int i = 0; i< numberOfAngles; i++){
                    rad2 [i] = 0.0;
                }

                for (int i = 0; i< numberOfAngles; i++){
                    isochrone[i] = Utils.haversine(origin, phi1[i], rad1[i]);

                    try{
                        TimeUnit.SECONDS.sleep(1);
                    }catch (InterruptedException ie){
                        ie.printStackTrace();
                    }

                }
                ArrayList data = GoogleApiRequestsManager.googleMatrixDistanceApiRequester(origin, isochrone, MapsActivity.this);
                data_durations = (HashMap)data.get(0);
                data_distances = (HashMap)data.get(1);

                int i = 0;
                for (HashMap.Entry<LatLng, Double> entry : data_durations.entrySet()) {
                    LatLng curAddress = entry.getKey();
                    Double curDuration = entry.getValue();
//                    rad1[i] = data_distances.get(curAddress); 
                    if ((curDuration < (duration - tolerance))){
                        rad2[i] = (rmax[i] + rad1[i]) / 2;
                        rmax[i] = 1.25*rmax[i];
                        rmin[i] = rad1[i];

                    }else{
                        if ((curDuration > (duration + tolerance))) {
                            rad2[i] = (rmin[i] + rad1[i]) / 2;
                            rmax[i] = rad1[i];
                        }else{
                            rad2[i] = rad1[i];

                        }
                    }
                    data0[i] = curAddress;
                    i = i + 1;
                }

                for (int k = 0; k< numberOfAngles; k++){
                    rad0 [k] = rad1[k];
                    rad1[k] = rad2[k];
                }

                loops = loops + 1;
                Log.d(TAG, String.valueOf(Utils.sum(rad0, rad1)));

            }

            int j = 0;
            for (LatLng key: data_durations.keySet()){
                if (data_durations.get(key) <= duration + tolerance){
//                    Log.d(TAG, data.get(key).toString());
                    isochrone[j] = key;
                }

                j++;

            }
//            Log.d(TAG, new ArrayList<>(Arrays.asList(isochrone)).toString());
            return sortPoints(origin, isochrone);
            //return getConvexHull(new ArrayList<>(Arrays.asList(isochrone)));


        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(ArrayList<LatLng> isochronePoints) {

           drawRegion(isochronePoints);

        }
    }

    private class GetTimeDataForGrid extends AsyncTask<Void, Integer, ArrayList<ArrayList<Double>>> {
        // Double : origin.latitude, origin.longitude, duration
        protected ArrayList<ArrayList<Double>> doInBackground(Void... voidss) {


            LatLng origin = grid.gridCenter;
            Double duration = grid.gridBaseTime;
            int nRadii = grid.numberOfRadii;
            int nAngles = grid.numberOfAngles;

            ArrayList<ArrayList<Double>> timeData = new ArrayList<>();

            for (int radiusIndex = 0; radiusIndex < nRadii; radiusIndex++){
                ArrayList<LatLng> row = grid.points.get(radiusIndex);
                ArrayList<Double> times = new ArrayList<>();

                for (int angleIndex=0; angleIndex < nAngles; angleIndex++){
                    LatLng coordinate = row.get(angleIndex);
                    times.add(angleIndex, GoogleApiRequestsManager.getDirections(origin, coordinate, MapsActivity.this));
                    try {
                        Thread.currentThread();
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                timeData.add(radiusIndex, times);

            }

          return timeData;

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(ArrayList<ArrayList<Double>> times) {

            grid.setTimeData(times);

        }
    }


}


