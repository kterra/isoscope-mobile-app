package fgv.emap.kizzyterra.isoscope;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kizzyterra on 16/06/17.
 */

public class MapsFragment extends Fragment implements OnMapReadyCallback, MapDrawerCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private static final float MAP_MIN_ZOOM = 12.5f;
    private static final float MAP_MAX_ZOOM = 14.0f;
    private static final double TEN_MINUTES = 10.0;
    private static final double FIVE_MINUTES = 5.0;
    private static final int BICYCLING = 1000;
    private static final int DRIVING = 2000;
    private static final int WALKING = 3000;
    private static final int TRANSIT = 4000;
    private int MODE;

    private Double isochroneDuration = 0.0;
    private String TAG = "Maps";
    //private FirebaseManager fireManager;
    //private PolygonOptions rectOptions;
    //private Polygon region;
    //private ArrayList<LatLng> regionPoints;
    private Marker lastMarkerClicked;
    private LatLng lastPositionSelected;

    private CircleGrid grid;


    private Context mActivity;
    private SupportMapFragment supportMapFragment;
    private View rootView;
    private static final int ISOCHRONE_DURATION_REQUEST_CODE = 1;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // init
        //mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        // don't recreate fragment everytime ensure last map location/state are maintain
        if (supportMapFragment == null) {

            MODE = DRIVING;
            rootView = inflater.inflate(R.layout.fragment_map, container, false);

            supportMapFragment = SupportMapFragment.newInstance();
            supportMapFragment.getMapAsync(this);


        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        // R.id.map is a layout
        transaction.replace(R.id.map, supportMapFragment).commit();

        return rootView;

    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ISOCHRONE_DURATION_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                isochroneDuration = data.getDoubleExtra("duration", 1);
                Log.d(TAG, isochroneDuration.toString());

            }
        }


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

        grid = new CircleGrid(origin, isochroneDuration, MODE);
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
                    Log.d(TAG, "apirequester");
                    times.add(angleIndex, GoogleApiRequestsManager.getDirections(origin, coordinate, MODE, MapsFragment.this.getActivity()));
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
            Log.d(TAG, times.toString());
            // drawGrid(grid.getPointsByTime());
            drawIsochroneBySegment(grid.getIsochroneCell());



        }
    }
}
