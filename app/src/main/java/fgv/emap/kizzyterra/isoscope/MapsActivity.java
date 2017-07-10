package fgv.emap.kizzyterra.isoscope;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


public class MapsActivity extends AppCompatActivity implements OnConnectionFailedListener, OnMapReadyCallback, MapDrawerCallback {

    private GoogleMap mMap;
    private static final float MAP_MIN_ZOOM = 12f;
    private static final float MAP_MAX_ZOOM = 14.0f;
    private static final double TEN_MINUTES = 10.0;
    private static final double FIVE_MINUTES = 5.0;
    private static final int BICYCLING = 1000;
    private static final int DRIVING = 2000;
    private static final int WALKING = 3000;
    private static final int TRANSIT = 4000;
    private int MODE;
    private static final int ISOCHRONE_DURATION_REQUEST_CODE = 1;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    private String TAG = "Maps";
    //private FirebaseManager fireManager;
    //private PolygonOptions rectOptions;
    //private Polygon region;
    //private ArrayList<LatLng> regionPoints;
    private Marker lastMarkerClicked;
    private LatLng lastPositionSelected;

    private CircleGrid grid;

    private GoogleApiClient mGoogleApiClient;
    private HashMap<ArrayList<LatLng>, ArrayList<Tuple>> isochroneData;
    private Double isochroneDuration = 0.0;
    private double isochroneArea;
    private String isochroneCenterAddress;
    private LatLng isochroneCenterCoordinate;
    private String isochroneEndAddress;
    private LatLng isochroneEndCoordinate;

    private int LOC_FRAGMENT_ID = 1 ;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_maps);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        Intent intent = getIntent();

        int fragment = intent.getIntExtra(FRAGMENT_ID, 0);

        MODE = intent.getIntExtra(MODE_VALUE, 0);
        Log.d(TAG, String.valueOf(MODE));
        isochroneDuration = intent.getDoubleExtra(TIME_VALUE, 0);
        Log.d(TAG, String.valueOf(isochroneDuration));
        isochroneCenterAddress = intent.getStringExtra(CENTER_ADDRESS_VALUE);
        Log.d(TAG, String.valueOf(isochroneCenterAddress));
        isochroneCenterCoordinate = new LatLng( intent.getDoubleExtra(CENTER_LAT_VALUE,0), intent.getDoubleExtra(CENTER_LONG_VALUE,0));
        Log.d(TAG, String.valueOf(isochroneCenterCoordinate));


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isochroneData != null){

                }

                //startActivityForResult(new Intent(MapsActivity.this, FilterActivity.class), ISOCHRONE_DURATION_REQUEST_CODE);
//
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

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setOnMarkerClickListener(this);
//        mMap.setOnMapLongClickListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(isochroneCenterCoordinate, MAP_MAX_ZOOM));
        drawMarker("100", isochroneCenterCoordinate);

        grid = new CircleGrid(isochroneCenterCoordinate, isochroneDuration, MODE);
        Log.d(TAG + "2", String.valueOf(isochroneCenterCoordinate));
        Log.d(TAG + "2", String.valueOf(isochroneDuration));
        Log.d(TAG+ "2", String.valueOf(MODE));
        new GetTimeDataForGrid().execute();



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
    public Marker drawMarker(String index, LatLng point) {

       return mMap.addMarker(new MarkerOptions().position(point).snippet(index));

    }

    @Override
    public void drawIsochroneBySegment(HashMap<ArrayList<LatLng>, ArrayList<Tuple>> data){

        try {
            ArrayList<LatLng> isochrone = (ArrayList<LatLng>) data.keySet().toArray()[0];

            ArrayList<Tuple> segments = data.get(isochrone);
            //PolygonOptions plo = new PolygonOptions().strokeColor(Color.BLUE).fillColor(Color.BLUE).strokeWidth(2);
            //plo.addAll(isochrone);
            for (Tuple tuple: segments){
                Log.d(TAG, Integer.toString((int)tuple.x));
                Log.d(TAG, Integer.toString((int)tuple.y));
                PolylineOptions rectOptions = new PolylineOptions()
                        .add(isochrone.get((int)tuple.x))
                        .add(isochrone.get((int)tuple.y))
                        .color(Color.RED)
                        .width(2);

               // plo.add(isochrone.get((int)tuple.x),isochrone.get((int)tuple.y));

                // Get back the mutable Polyline
                mMap.addPolyline(rectOptions);


            }
            //mMap.addPolygon(plo);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void drawGrid(ArrayList<ArrayList<LatLng>> grid){

        ArrayList<LatLng> inners = grid.get(0);
        for (LatLng pos: inners){
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        }

        ArrayList<LatLng> outers = grid.get(1);
        for (LatLng pos: outers){
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        }


    }

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
                    try {
                        Thread.currentThread();
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    LatLng coordinate = row.get(angleIndex);
                    Log.d(TAG, "apirequester");
                    times.add(angleIndex, GoogleApiRequestsManager.getDirections(origin, coordinate, MODE, MapsActivity.this));
                    Log.d(TAG, times.toString());
                }
                timeData.add(radiusIndex, times);

            }

          return timeData;

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(ArrayList<ArrayList<Double>> times) {

            grid.setTimeData(times);
            Log.d(TAG, times.toString());
           // drawGrid(grid.getPointsByTime());

            isochroneData = grid.getIsochroneSegments();
            Log.d(TAG, String.valueOf(grid.gridBaseTime));
            drawIsochroneBySegment(isochroneData);

            ArrayList<LatLng> isochrone = (ArrayList<LatLng>) isochroneData.keySet().toArray()[0];
            isochroneArea = SphericalUtil.computeArea(isochrone);
            Log.d(TAG, String.valueOf(isochroneArea));
//            Log.d(TAG, String.valueOf(SphericalUtil.computeArea(convexHull)));
//            Log.d(TAG, String.valueOf(SphericalUtil.computeArea(isochrone)));



        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}


