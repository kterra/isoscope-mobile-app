package fgv.emap.kizzyterra.isoscope;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kizzyterra on 01/05/17.
 */

public class GoogleApiRequestsManager {

    public static final String API_KEY = "AIzaSyA5dG_IbvaxJYqNtDOiIFPq6J0PtdX1Fp8";
    public static final String DISTANCE_MATRIX_PREFIX = "https://maps.googleapis.com/maps/api/distancematrix/json";
    public static final String GEOCODE_PREFIX = "https://maps.googleapis.com/maps/api/geocode/json";
    public static final String DIRECTIONS_PREFIX = "https://maps.googleapis.com/maps/api/directions/json";

    private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String OK = "OK";
    private static final String VALUE = "value";
    private static final String TEXT = "text";
    private static final String DISTANCE = "distance";
    private static final String DURATION = "duration";


    public static Double getDirections(LatLng origin, LatLng dest, Context ctx){

        String travelMode = "driving";
        return googleDirectionsApiRequester(origin, dest, travelMode, ctx);

    }

    public static GenericUrl buildMatrixDistanceUrl(LatLng origin, LatLng[] destinations){
        GenericUrl matrixDistanceUrl = new GenericUrl(DISTANCE_MATRIX_PREFIX);
        matrixDistanceUrl.put("origins", String.valueOf(origin.latitude) + "," + String.valueOf(origin.longitude));
        String destination = "";
        for(LatLng dest : destinations){
            if (destination != ""){
                destination = destination + "|" + String.valueOf(dest.latitude) + "," + String.valueOf(dest.longitude);
            }else {
                destination = String.valueOf(dest.latitude) + "," + String.valueOf(dest.longitude);
            }

        }
        matrixDistanceUrl.put("destinations", destination);
        matrixDistanceUrl.put("key", API_KEY);

        return  matrixDistanceUrl;
    }

    public static GenericUrl buildDirectionsUrl(LatLng origin, LatLng destination, String travelMode){
        GenericUrl directionsUrl = new GenericUrl(DIRECTIONS_PREFIX);
        directionsUrl.put("origin", String.valueOf(origin.latitude) + "," + String.valueOf(origin.longitude));
        directionsUrl.put("destination", String.valueOf(destination.latitude) + "," + String.valueOf(destination.longitude));
        directionsUrl.put("mode", travelMode);
        directionsUrl.put("key", API_KEY);

        return  directionsUrl;
    }


    public static ArrayList googleMatrixDistanceApiRequester(LatLng origin, LatLng[] destinations, Context ctx){

        GenericUrl url = buildMatrixDistanceUrl(origin, destinations);
        try {
            HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) {
                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                }
            });



            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = request.execute();

            return parseMatrixDistanceJson(httpResponse.parseAsString(), destinations, ctx);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Double googleDirectionsApiRequester(LatLng origin, LatLng destination, String travelMode, Context ctx){

        GenericUrl url = buildDirectionsUrl(origin, destination, travelMode);
        try {
            HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) {
                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                }
            });

            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = request.execute();

            return parseDirectionsJson(httpResponse.parseAsString(), ctx);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ArrayList parseMatrixDistanceJson(String response, LatLng[] destinations, Context ctx){

        HashMap<LatLng, Double> durations = new HashMap<>(); // in minutes
        HashMap<LatLng, Double> distances = new HashMap<>(); // in kilometers

        try{
            JSONObject result = new JSONObject(response);
            // Log.d(TAG, result.toString());
            if (result.get("status").toString().equals("OK")){

                JSONArray destination_addresses = result.getJSONArray("destination_addresses");

                JSONArray rows = result.getJSONArray("rows");
                JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");
                for(int i=0; i< elements.length(); i++){
                    JSONObject element = elements.getJSONObject(i);
                    if (element.get("status").toString().equals("OK")){
                        String address = destination_addresses.get(i).toString();
                        LatLng coordinate = Utils.geocodeAddress(ctx, address);

                        JSONObject distance = element.getJSONObject("distance");
                        Double distanceValue = distance.getDouble("value");


                        JSONObject duration = element.getJSONObject("duration");
                        Double durationValue = duration.getDouble("value")/60.0;


                        if (coordinate != null){
                            distances.put(coordinate, distanceValue);
                            durations.put(coordinate, durationValue);
                        }

                    }
                }
            }

        }catch (JSONException je){
            je.printStackTrace();
        }

        ArrayList data = new ArrayList();
        data.add(durations);
        data.add(distances);
        return data;

    }

    public static Double parseDirectionsJson(String response, Context ctx){

        Double duration = null;

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(response);
            //Get the route object

            if(!json.getString("status").equals(OK)){
                new Exception("Route Exception");
            }

            JSONArray jsonRoutes = json.getJSONArray("routes");

            for (int i = 0; i < jsonRoutes.length(); i++) {


                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                //Get the bounds - northeast and southwest
                final JSONObject jsonBounds = jsonRoute.getJSONObject("bounds");
                final JSONObject jsonNortheast = jsonBounds.getJSONObject("northeast");
                final JSONObject jsonSouthwest = jsonBounds.getJSONObject("southwest");


                //Get the leg, only one leg as we don't support waypoints
                final JSONObject leg = jsonRoute.getJSONArray("legs").getJSONObject(0);
                //Get the steps for this leg
                final JSONArray steps = leg.getJSONArray("steps");
                //Number of steps for use in for loop
                final int numSteps = steps.length();
                //Set the name of this route using the start & end addresses
                String startAddress = leg.getString("start_address");
                String endAddress = leg.getString("end_address");
                //Get google's copyright notice (tos requirement)
                String copyRights = jsonRoute.getString("copyrights");

                //Get distance and time estimation
                String durationText = leg.getJSONObject(DURATION).getString(TEXT);
                duration = leg.getJSONObject(DURATION).getDouble(VALUE)/(double) 60;

                String distanceText = leg.getJSONObject(DISTANCE).getString(TEXT);
                Double distance = leg.getJSONObject(DISTANCE).getDouble(VALUE)/(double) 1000;

                //Get any warnings provided (tos requirement)
                if (!jsonRoute.getJSONArray("warnings").isNull(0)) {
                    String warnings = jsonRoute.getJSONArray("warnings").getString(0);
                }

                /* Loop through the steps, creating a segment for each one and
                 * decoding any polylines found as we go to add to the route object's
                 * map array. Using an explicit for loop because it is faster!
                 */
                for (int y = 0; y < numSteps; y++) {
                    //Get the individual step
                    final JSONObject step = steps.getJSONObject(y);
                    //Get the start position for this step and set it on the segment
                    final JSONObject start = step.getJSONObject("start_location");
                    final LatLng position = new LatLng(start.getDouble("lat"),
                            start.getDouble("lng"));

                    //Set the length of this segment in metres
                    final int length = step.getJSONObject(DISTANCE).getInt(VALUE);
                    distance += length;


                    //Strip html from google directions and set as turn instruction
                    String instruction = step.getString("html_instructions").replaceAll("<(.*?)*>", "");

//                    if(step.has("maneuver"))
//                        step.getString("maneuver");

                }

            }

        } catch (JSONException e) {
            new JSONException("JSONException. Msg: "+e.getMessage());
        }
        return duration;

    }




}
