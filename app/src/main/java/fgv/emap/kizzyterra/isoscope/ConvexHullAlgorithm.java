package fgv.emap.kizzyterra.isoscope;

/**
 * Created by kizzyterra on 12/10/16.
 */

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public interface ConvexHullAlgorithm
{
    ArrayList<LatLng> getConvexHull(ArrayList<LatLng> points);
}