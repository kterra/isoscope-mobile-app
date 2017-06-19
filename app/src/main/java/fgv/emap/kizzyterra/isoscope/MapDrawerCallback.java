package fgv.emap.kizzyterra.isoscope;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kizzyterra on 12/10/16.
 */

public interface MapDrawerCallback {

    Marker drawMarker(String index, LatLng point);
    void drawRegion(ArrayList<LatLng> region);
    void drawIsochroneBySegment(HashMap<ArrayList<LatLng>, ArrayList<Tuple>> data);
    void drawGrid(ArrayList<ArrayList<LatLng>> grid);

}
