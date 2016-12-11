package fgv.emap.kizzyterra.isoscope;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by kizzyterra on 12/10/16.
 */

public interface MapDrawerCallback {

    void drawMarker(String index, LatLng point);
    void drawRegion(LatLng point);

}
