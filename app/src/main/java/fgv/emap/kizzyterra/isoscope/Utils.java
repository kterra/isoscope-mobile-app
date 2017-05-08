package fgv.emap.kizzyterra.isoscope;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by kizzyterra on 01/05/17.
 */

public class Utils {


    public static double RADIUS_OF_EARTH = 3963.1676;  // Radius of the Earth in miles

//    public static ArrayList listFromFile(String filename){
//
//
//    }
//
//    public static ArrayList matrixFromFile(String filename){
//
//    }
//
    public static void listToFile(ArrayList sampleList, String filename){
        String commaSeparatedValues = "";

        /** If the list is not null and the list size is not zero, do the processing**/
        if (sampleList != null) {

            /**Iterate through the list and append comma after each values**/
            Iterator<String> iter = sampleList.iterator();
            while (iter.hasNext()) {
                commaSeparatedValues += iter.next() + ",";
            }
            /**Remove the last comma**/
            if (commaSeparatedValues.endsWith(",")) {
                commaSeparatedValues = commaSeparatedValues.substring(0,
                        commaSeparatedValues.lastIndexOf(","));
            }
        }

            try {
                FileWriter fstream = new FileWriter(filename, false);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(commaSeparatedValues);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


    }

//    public static void matrixToFile(ArrayList matrix, String filename){
//
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
