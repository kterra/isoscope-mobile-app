package fgv.emap.kizzyterra.isoscope;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, MapDrawerCallback, ConvexHullAlgorithm, OnMarkerClickListener {

    private GoogleMap mMap;
    private static final float MAP_MIN_ZOOM = 10.5f;
    private static final float MAP_MAX_ZOOM = 14.0f;
    private String TAG = "Maps";
    private FirebaseManager fireManager;
    private PolygonOptions rectOptions;
    private Polygon region;
    private ArrayList<LatLng> regionPoints;
    private Marker lastMarkerClicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fireManager = new FirebaseManager(MapsActivity.this);

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

        // Add a marker in Sydney and move the camera
        LatLng rio = new LatLng(-22.91541,-43.4258447);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rio, MAP_MIN_ZOOM));




        //fireManager.removeAll();
        //fireManager.insertData(MapsActivity.this);

        Log.d(TAG, "called getPoints");
        fireManager.getGridPoints();




    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Log.d(TAG, "onMarkerClick");

        mMap.clear();

        regionPoints = new ArrayList<>();
        lastMarkerClicked = marker;
        mMap.addMarker(new MarkerOptions().position(lastMarkerClicked.getPosition()).snippet(lastMarkerClicked.getSnippet()));

        String startPoint = String.valueOf(marker.getSnippet());
        Integer timeLimit = 3000;

        fireManager.filterRegionByTime(startPoint, timeLimit);

        return true;
    }


    @Override
    public void drawMarker(String index, LatLng point) {

        mMap.addMarker(new MarkerOptions().position(point).snippet(index));

    }

    @Override
    public void drawRegion(LatLng point) {

        mMap.clear();


        mMap.addMarker(new MarkerOptions().position(lastMarkerClicked.getPosition()).snippet(lastMarkerClicked.getSnippet()));

        rectOptions = new PolygonOptions()
                .strokeWidth(2.0f)
                .fillColor(Color.argb(150, 102, 140, 255))
                .strokeColor(Color.argb(150, 102, 140, 255));
        regionPoints.add(point);
        ArrayList<LatLng> convexHullPoints =  getConvexHull(regionPoints);
        for (LatLng p : convexHullPoints){
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
}
