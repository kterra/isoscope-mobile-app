package fgv.emap.kizzyterra.isoscope;

/**
 * Created by kizzyterra on 12/9/16.
 */

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class FirebaseManager {

    private static DatabaseReference mDatabase;
    private MapDrawerCallback drawer;
    private HashMap<String,LatLng> grid;
    private String TAG = "FirebaseManager";
    public InputStream inputStream;

    public FirebaseManager(MapDrawerCallback dw){
        drawer = dw;
        grid = new HashMap<>();

    }


    public void databaseTest(){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("tests").child("level1").child("level2").setValue("value");
    }

    public void insertData(Context context){

        inputStream = context.getResources().openRawResource(R.raw.distance_matrix_duration_result);

        BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String[] coordinates = new String[1];
        mDatabase = FirebaseDatabase.getInstance().getReference();

        try {
            if((line = buffer.readLine()) != null){

                String[] coords = line.split(",");
                coordinates = new String[coords.length-1];
                for (int i=1, j=0; i< coords.length & j<i; i =  i + 2, j++){
                    coordinates[j] = coords[i].replace("\"","") +","+ coords[i + 1].replace("\"","");
                    Log.d("debug",coordinates.toString());
                    mDatabase.child("coordinates").child(String.valueOf(j+1)).setValue(coordinates[j]);

                }

            }
            while ((line = buffer.readLine()) != null) {
                String[] columns = line.split(",");
                String coord = columns[0].replace("\"","")  +","+  columns[1].replace("\"","");
                int[] values = new int[columns.length-2];
                for (int i=2; i< columns.length;  i++){
                    values[i-2] = Integer.valueOf(columns[i]);
                }


                for (int i=0; i< values.length;  i++) {

                        int coordIndex = getIndex(coordinates, coord);
                        if (coordinates[i] != null && coordIndex > 0){
                            mDatabase.child(String.valueOf(coordIndex + 1)).child(String.valueOf(i+1)).setValue(values[i]);
                        }
                }

            }



        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: "+e);
            }
        }


    }

    public int getIndex(String[] coords, String coordinate ){

        for (int i=0; i< coords.length; i++){

            if (coords[i].equals(coordinate)){
                return i;
            }

        }
        return -1;
    }

    public void getGridPoints(){

        Log.d(TAG, "getPoints");

        final DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("coordinates");

        mDatabaseRef.orderByKey().addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "onChildAdded");
                String coords[] = String.valueOf(dataSnapshot.getValue()).split(",");

                LatLng coord = new LatLng(Double.valueOf(coords[0]), Double.valueOf(coords[1]));
                String index = String.valueOf(dataSnapshot.getKey());
                grid.put(index, coord);
                drawer.drawMarker(index, coord);


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}


            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}


            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });


    }


    public void filterRegionByTime(String startPoint, Integer timeLimit){

        if (Integer.valueOf(startPoint) < 103) {

            final DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference(startPoint);

            Log.d(TAG, startPoint + " - filterByRegion");

            mDatabaseRef.orderByValue().endAt(timeLimit).addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    Log.d(TAG, "onChildAdded - filterByRegion");
                    String index = String.valueOf(dataSnapshot.getKey());
                    String value = String.valueOf(dataSnapshot.getValue());
                    Log.d(TAG, index + " :" + value);

                    LatLng coord = getPointByIndex(index);

                    if (coord != null && Integer.valueOf(value) > 0 ) {
                        drawer.drawRegion(coord);
                    }


                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                }


                public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
                }


                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }

            });


        }
    }


    public LatLng getPointByIndex(String index){

        return grid.get(index);


    }

    public void removeAll(){


        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.setValue(null);
    }

}
