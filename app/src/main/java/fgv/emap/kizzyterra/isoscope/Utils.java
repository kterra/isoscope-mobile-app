package fgv.emap.kizzyterra.isoscope;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by kizzyterra on 01/05/17.
 */

public class Utils {


    public static double RADIUS_OF_EARTH = 3963.1676;  // Radius of the Earth in miles
    //http://www.w3ii.com/pt/android/android_internal_storage.html
    //https://pt.stackoverflow.com/questions/48196/salvamento-de-dados-em-android

//    public static ArrayList listFromFile(String filename){
//
//
//    }

//    public static ArrayList matrixFromFile(String filename){
//
//    }
//
//    public static void listToFile(ArrayList sampleList, String fileName, Context ctx){
//        String commaSeparatedValues = "";
//
//        /** If the list is not null and the list size is not zero, do the processing**/
//        if (sampleList != null) {
//
//            /**Iterate through the list and append comma after each values**/
//            Iterator<String> iter = sampleList.iterator();
//            while (iter.hasNext()) {
//                commaSeparatedValues += iter.next() + ",";
//            }
//            /**Remove the last comma**/
//            if (commaSeparatedValues.endsWith(",")) {
//                commaSeparatedValues = commaSeparatedValues.substring(0,
//                        commaSeparatedValues.lastIndexOf(","));
//            }
//        }
//
//            try {
//
//                FileOutputStream outputStream = null;
//                try {
//                    outputStream = ctx.getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
//                    outputStream.write(commaSeparatedValues.getBytes());
//                    outputStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//
//    }

//    public static void matrixToFile(ArrayList matrix, String filename){
//
//    }

    public static double getUtility(double area, double time, double cost){

        double utility = -4.76 +0.147*area - 0.041*time - 2.24*cost;

        return  utility;
    }

    public static double estimatePolygonArea(ArrayList<LatLng> points)
    {
        double area = 0;
        try {

            int numberOfPoints = points.size();
            points.add(points.get(0));

            for (int i = 0; i < numberOfPoints -1; i++){
                LatLng p1 = points.get(i);
                LatLng p2 = points.get(i+1);

                area += (Math.toRadians(p2.longitude) - Math.toRadians(p1.longitude)) * (2 + Math.sin(Math.toRadians(p1.latitude)) + Math.sin(Math.toRadians(p2.latitude)));
            }

            area = area * 6378137 * 6378137 / 2;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Math.abs(area);
    }

//    public static double estimatePolygonArea(HashMap<ArrayList<LatLng>, ArrayList<Tuple>> data)
//    {
//        double area = 0;
//        try {
//            ArrayList<LatLng> isochrone = (ArrayList<LatLng>) data.keySet().toArray()[0];
//
//
//            ArrayList<Tuple> segments = data.get(isochrone);
//            for (Tuple tuple: segments){
//
//                LatLng p1 = isochrone.get((int)tuple.x);
//                LatLng p2 = isochrone.get((int)tuple.y);
//
//                area += (p2.longitude - p1.longitude) * (2 + Math.sin(Math.toRadians(p1.latitude)) + Math.sin(Math.toRadians(p2.latitude)));
//
//            }
//            area = area * 6378137 * 6378137 / 2;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return Math.abs(area);
//    }

    public static LatLng haversine(LatLng origin, double angle, double radius){

        double r = RADIUS_OF_EARTH;
        double bearing = Math.toRadians(angle); //Bearing in radians converted from angle in degrees
        double lat1 = Math.toRadians(origin.latitude);
        double lng1 = Math.toRadians(origin.longitude);
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(radius / r) + Math.cos(lat1) * Math.sin(radius / r) * Math.cos(bearing));
        double lng2 = lng1 + Math.atan2(Math.sin(bearing) * Math.sin(radius / r) * Math.cos(lat1), Math.cos(radius / r) - Math.sin(lat1) * Math.sin(lat2));
        lat2 = Math.toDegrees(lat2);
        lng2 = Math.toDegrees(lng2);

        return new LatLng(lat2, lng2);

    }

    public static LatLng geocodeAddress(Context ctx, String address){

        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        List<Address> coordinates;


        try{
            coordinates = geocoder.getFromLocationName(address, 1);

            return new LatLng( coordinates.get(0).getLatitude(), coordinates.get(0).getLongitude());
        }catch (IOException io){
            io.printStackTrace();
        }catch (IndexOutOfBoundsException iob){
            iob.printStackTrace();
        }

        return null;

    }

    public static Double getBearing (LatLng origin, LatLng destination){

        /**Calculate the bearing from origin to destination **/


        Double bearing = Math.atan2(Math.sin((destination.longitude - origin.longitude) * Math.PI / 180) * Math.cos(destination.latitude * Math.PI / 180),
                Math.cos(origin.latitude * Math.PI / 180) * Math.sin(destination.latitude * Math.PI / 180) -
                        Math.sin(origin.latitude * Math.PI / 180) * Math.cos(destination.latitude * Math.PI / 180) * Math.cos((destination.longitude - origin.longitude) * Math.PI / 180));
        bearing = bearing * 180 / Math.PI;
        bearing = (bearing + 360) % 360;
        return bearing;
    }

    public static Double sum(Double[] rad0, Double[] rad1){
        Double sum = 0.0;
        for (int i =0; i < rad0.length; i++){
            sum += rad0[i] - rad1[i];
        }
        return sum;
    }



}
