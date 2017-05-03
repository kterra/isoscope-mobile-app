package fgv.emap.kizzyterra.isoscope;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.IndoorBuilding;
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

        radii = new ArrayList<>();
        angles = new ArrayList<>();

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

    public int booleanToInteger(boolean bool){
        if(bool)
            return 1;
        else
            return 0;
    }

    public int signsCode(boolean[] signs){
        int code = 0;
        int p = 0;
        for ( boolean s : signs){

            code = code + (int)Math.pow(2,p) * booleanToInteger(s);
            p = p + 1;
        }

        return code;
    }

    public boolean isInner(Double time){

        Double tolerance = 1.0;
        if (time < gridBaseTime + tolerance)
            return true;
        else
         return false;
    }

    public LatLng evaluatePointCut(int radiusIndex1, int angleIndex1, Double time1,
                                   int radiusIndex2, int angleIndex2, Double time2){

        Double delta1 = Math.abs(gridBaseTime - time1);
        Double delta2 = Math.abs(gridBaseTime - time2);
        Double ratio = delta1/Math.abs(delta1 + delta2);
        Double cutAngle = null;
        Double cutRadius = null;

        if (radiusIndex1 == radiusIndex2){
            Double angle1 = angles.get(angleIndex1);
            Double angle2 = angles.get(angleIndex2);

            if (Math.abs(angleIndex1 - angleIndex2) == numberOfAngles - 1) {
                if (angleIndex1 == 0)
                    angle1 = 360.0;
                if (angleIndex2 == 0)
                    angle2 = 360.0;
                cutAngle = angle1 + (angle2 - angle1) * ratio;
                cutRadius = radii.get(radiusIndex1);
            }

        }else{
            if(angleIndex1 == angleIndex2){
                cutAngle = angles.get(angleIndex1);
                cutRadius = radii.get(radiusIndex1) +  (radii.get(radiusIndex2) - radii.get(radiusIndex1))*ratio;

            }
        }

        return Utils.haversine(gridCenter, cutAngle, cutRadius);

    }

    public HashMap getIsochroneCell(){

        ArrayList<Tuple<Integer, Integer>> segments = new ArrayList<>();
        HashMap<LatLng, Integer> pmap = new HashMap();
        ArrayList<LatLng> isochrone = new ArrayList<>();

        for (int radiusIndex = 0; radiusIndex < numberOfRadii - 1; radiusIndex++){
            for (int angleIndex = 0; angleIndex < numberOfAngles; angleIndex++){
                int nextAngleIndex = (angleIndex+1)%numberOfAngles;

               //counter clock-wise
                Double a = timeData.get(radiusIndex).get(angleIndex);
                Double b = timeData.get(radiusIndex).get(nextAngleIndex);
                Double c = timeData.get(radiusIndex + 1).get(nextAngleIndex);
                Double d = timeData.get(radiusIndex + 1).get(angleIndex);


                boolean a_inner = isInner(a);
                boolean b_inner = isInner(b);
                boolean c_inner = isInner(c);
                boolean d_inner = isInner(d);

                boolean [] signs = new boolean[4];
                signs[0] = a_inner;
                signs[1] = b_inner;
                signs[2] = c_inner;
                signs[3] = d_inner;

                ArrayList<Integer> indices = new ArrayList<>();

                if (a_inner ^ b_inner) {
                    LatLng point = evaluatePointCut(radiusIndex, angleIndex, a,
                            radiusIndex, nextAngleIndex, b);

                    if (pmap.containsKey(point))
                        indices.add(pmap.get(point));
                    else{
                        int index = isochrone.size();
                        indices.add(index);
                        pmap.put(point, index);
                        isochrone.add(point);
                    }
                }
                if (b_inner ^ c_inner){

                    LatLng point = evaluatePointCut(radiusIndex, nextAngleIndex, b,
                            radiusIndex + 1, nextAngleIndex, c);

                    if (pmap.containsKey(point))
                        indices.add(pmap.get(point));
                    else{
                        int index = isochrone.size();
                        indices.add(index);
                        pmap.put(point, index);
                        isochrone.add(point);
                    }
                }

                if (c_inner ^ d_inner) {
                    LatLng point = evaluatePointCut(radiusIndex + 1, nextAngleIndex, c,
                            radiusIndex + 1, angleIndex, d);


                    if (pmap.containsKey(point))
                        indices.add(pmap.get(point));
                    else{
                        int index = isochrone.size();
                        indices.add(index);
                        pmap.put(point, index);
                        isochrone.add(point);
                    }
                }
                if (d_inner ^ a_inner){
                    LatLng point = evaluatePointCut(radiusIndex + 1, angleIndex, d,
                            radiusIndex, angleIndex, a);

                    if (pmap.containsKey(point))
                        indices.add(pmap.get(point));
                    else{
                        int index = isochrone.size();
                        indices.add(index);
                        pmap.put(point, index);
                        isochrone.add(point);
                    }
                }

                 // segments
                int code = signsCode(signs);

                if (code == 0 || code == 15){
                    continue;
                }else {
                    if(code == 5){
                        segments.add(new Tuple(indices.get(0), indices.get(3)));
                        segments.add(new Tuple(indices.get(1), indices.get(2)));
                    }else{
                        if(code == 10){
                            segments.add(new Tuple(indices.get(0), indices.get(1)));
                            segments.add(new Tuple(indices.get(2), indices.get(3)));
                        }else{
                            segments.add(new Tuple(indices.get(0), indices.get(1)));
                        }
                    }
                }

            }

        }
        HashMap<ArrayList<LatLng>, ArrayList<Tuple<Integer, Integer>>> data = new HashMap();
        data.put(isochrone, segments);
       // matrix_to_file(isochrone, 'isochrone.csv')
        return data;
    }

}

