package com.example.johnchy.samplegui;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity {


    private GoogleMap map; // Might be null if Google Play services APK is not available.
    ArrayList<LinkedHashMap<String, LatLng>> markerPoints = new ArrayList<LinkedHashMap<String, LatLng>>();
    ArrayList<LatLng> shape = new ArrayList<LatLng>();
    private static final LatLng SAN_JOSE = new LatLng(37.3394, -121.89389);
    private final double degreesPerRadian = 180.0 / Math.PI;
    //private static final LatLng ROSE_GARDEN = new LatLng(37.3322,-121.9281);
    //private static final LatLng HOLLOW_PARK = new LatLng(37.3257, -121.8621);
    private ProgressDialog dialog;

    private String getShape = "";
    private String busNumber = "17";
    private String day = "Weekdays";
    private String direction = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #map} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    private class addMarkers extends AsyncTask<Void, Void, Void>{

        int stopCount = 0;
        int shapeCount = 0;
        Context context = getApplicationContext();
        SQLHelper markerDBHelper = new SQLHelper(context);

        @Override
        protected void onPreExecute(){
            dialog = new ProgressDialog(MapsActivity.this);
            dialog.setMessage("Downloading data, please wait...");
            dialog.show();
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    addMarkers.this.cancel(true);
                }
            });
        }
        @Override
        protected Void doInBackground(Void... params) {
            try{
                markerDBHelper.CreateDatabase();
            }catch(IOException e)
            {
                throw new Error("Unable to create database");
            }
            try{
                markerDBHelper.openDataBase();
                SQLiteDatabase db = markerDBHelper.getReadableDatabase();
                Cursor stopCursor = db.rawQuery("SELECT DISTINCT s.stop_name, s.stop_lat, s.stop_lon, shape_id " +
                        "FROM trips as t INNER JOIN stop_times as st " +
                        "ON st.trip_id = t.trip_id " +
                        "INNER JOIN stops as s ON s.stop_id = st.stop_id " +
                        "WHERE route_id = " + "\"" + busNumber + "\"" + " AND t.service_id = " + "\"" + day + "\"" + " AND t.direction_id = " +
                        "\"" + direction + "\"",null);
                stopCount = stopCursor.getCount();
                if(stopCount > 0)
                {
                    if(stopCursor.moveToFirst()){
                        getShape = stopCursor.getString(stopCursor.getColumnIndex("shape_id"));
                        for(int i = 0; i<stopCount; i++)
                        {
                            LinkedHashMap<String, LatLng> input = new LinkedHashMap<>();
                            LatLng coordinates = new LatLng(Double.parseDouble(stopCursor.getString(stopCursor.getColumnIndex("stop_lat"))), Double.parseDouble(stopCursor.getString(stopCursor.getColumnIndex("stop_lon"))));
                            input.put(stopCursor.getString(stopCursor.getColumnIndex("stop_name")), coordinates);
                            markerPoints.add(input);
                            stopCursor.moveToNext();
                        }
                    }
                }
               Cursor shapeCursor = db.rawQuery("SELECT shape_pt_lat, shape_pt_lon FROM shapes " +
                       "WHERE shape_id = \"" + getShape + "\" ORDER BY CAST(shape_pt_sequence AS INTEGER)", null);
                shapeCount = shapeCursor.getCount();
                if(shapeCount > 0){
                    if(shapeCursor.moveToFirst()){
                        for(int i = 0; i<shapeCount; i++){
                            LatLng path = new LatLng(
                                    Double.parseDouble(shapeCursor.getString(shapeCursor.getColumnIndex("shape_pt_lat"))),
                                    Double.parseDouble(shapeCursor.getString(shapeCursor.getColumnIndex("shape_pt_lon"))));
                            shape.add(path);
                            shapeCursor.moveToNext();
                        }
                    }
                }
            }catch(SQLException sqle){
                sqle.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v){
            dialog.dismiss();
            setUpMarkers(markerPoints);
            setBounds(markerPoints);
            createRoute(shape);
        }
    }

    private void setUpMarkers(ArrayList<LinkedHashMap<String, LatLng>> points){
        /*map.addMarker(new MarkerOptions().position(ROSE_GARDEN).title("Marker"));
        map.addMarker(new MarkerOptions().position(HOLLOW_PARK).title("Marker"));*/
        for (LinkedHashMap<String, LatLng> inputs : markerPoints){
            for(Map.Entry<String, LatLng> entry : inputs.entrySet()){
                map.addMarker(new MarkerOptions().position(entry.getValue()).title(entry.getKey()));
            }
        }
    }

    private void createRoute(ArrayList<LatLng> points){
        for (int i = 0; i < points.size() - 1; i++) {
            LatLng src = points.get(i);
            LatLng dest = points.get(i + 1);
            Polyline line = map.addPolyline(
                    new PolylineOptions().add(
                            new LatLng(src.latitude, src.longitude),
                            new LatLng(dest.latitude,dest.longitude)
                    ).width(9).color(Color.BLUE).geodesic(true)
            );
        }

    }

    private void setBounds(ArrayList<LinkedHashMap<String, LatLng>> points){
        int padding = 0;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LinkedHashMap<String, LatLng> inputs : markerPoints){
            for(Map.Entry<String, LatLng> entry : inputs.entrySet()){
                builder.include(entry.getValue());
            }
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 14F);
        map.animateCamera(cu);
    }




    /*
      This is where we can add markers or lines, add listeners or move the camera.
      <p/>
      This should only be called once and when we are sure that {@link #map} is not null.
     */

    private void setUpMap() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(SAN_JOSE)
                .zoom(13)
                .bearing(90)
                .tilt(30)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        /*markerPoints.add(ROSE_GARDEN);
        markerPoints.add(SAN_JOSE);
        markerPoints.add(HOLLOW_PARK);*/
        //markerPoints.put("ROSE GARDEN", ROSE_GARDEN);
        addMarkers MarkerSetforRoute = new addMarkers();
        MarkerSetforRoute.execute();
    }
}
