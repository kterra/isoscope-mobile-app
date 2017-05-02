package fgv.emap.kizzyterra.isoscope;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

/**
 * Created by kizzyterra on 01/05/17.
 */

public class Isochrone {


    private ArrayList<LatLng> points;
    private ArrayList<Double> timeData;
    public Double durarion;
    public LatLng origin;

//    ArrayList getIsochrone();
//    ArrayList getIsochroneCell();
//    void drawIsochrone();

    public void setPoints(ArrayList<LatLng> pts){
        points = pts;
    }

    public void setTimeData(ArrayList<Double> time){
        timeData = time;
    }

    public ArrayList getPoints(){
        return  points;
    }

    public ArrayList getTimeData(){
        return  timeData;
    }
}
