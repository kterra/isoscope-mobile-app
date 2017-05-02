package fgv.emap.kizzyterra.isoscope;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by kizzyterra on 01/05/17.
 */

public class CircleGrid implements Grid {

    public static final int numberOfAngles = 24;
    public static final int numberOfRadii = 5;
    private ArrayList<ArrayList<Double>> timeData;
    public Double gridBaseTime;
    public LatLng gridCenter;

    ArrayList<ArrayList<LatLng>> points;
    ArrayList<Double> radii;
    ArrayList<Double> angles;

    public CircleGrid(LatLng point, double time){

        gridBaseTime = time;
        gridCenter = point;



//        if (/*testar se arquivo existe*/){
//            radii = Utils.listFromFile("radii.csv");
//            angles = Utils.listFromFile("angles.csv");
//            grid = Utils.matrixFromFile("grid.csv");
//        }else{
            buildGrid();
//        }


    }

    public void buildGrid(){

        points = new ArrayList<>();
        int delta_max = 2;
        int delta_min = 3;
        double max_radius = (gridBaseTime + delta_max)/12; //radius estimation based on speed of transit mode
        double min_radius = (gridBaseTime - delta_min)/12;  //radius estimation based on speed of transit mode
        double dRadius = (max_radius - min_radius)/(numberOfRadii - 1);

        for (int i =0; i<numberOfRadii; i++) {
            radii.add(i, min_radius + i * dRadius);
        }
        //list_to_file(self.radii, 'radii.csv')

        double dAngle = 360/numberOfAngles;
        for (int i =0; i < numberOfAngles; i++){
            angles.add(i, i*dAngle);
        }
        //list_to_file(self.angles, 'angles.csv')


        for (int radiusIndex = 0; radiusIndex < numberOfRadii; radiusIndex++){
            ArrayList<LatLng> row = new ArrayList<>();

            for (int angleIndex = 0; angleIndex < numberOfAngles; angleIndex++){
                row.add(Utils.haversine(gridCenter, angles.get(angleIndex), radii.get(radiusIndex)));
            }
           points.add(row);
        }
        //matrix_to_file(self.grid, 'grid.csv')

    }

    public void setTimeData(ArrayList<ArrayList<Double>> times){
        timeData = times;
    }


}
