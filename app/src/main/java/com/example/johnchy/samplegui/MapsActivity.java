package com.example.johnchy.samplegui;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
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
    //private static final LatLng ROSE_GARDEN = new LatLng(37.3322,-121.9281);
    //private static final LatLng HOLLOW_PARK = new LatLng(37.3257, -121.8621);
    private ProgressDialog dialog;

    private String getShape = "";
    private String busNumber = "10";
    private String day = "Weekdays";
    private String direction = "0";
    private static final String dbCommandGET_STOPS = "";
    private static final String dbCommandGET_SHAPE = "SELECT DISTINCT shape_pt_lat";


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

        int recordCount = 0;
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
                        "WHERE route_id = " + "\"" + busNumber + "\"" + "AND t.service_id = " + "\"" + day + "\"" + "AND t.direction_id = " +
                        "\"" + direction + "\"",null);
                recordCount = stopCursor.getCount();
                if(recordCount > 0)
                {
                    if(stopCursor.moveToFirst()){
                        getShape = stopCursor.getString(stopCursor.getColumnIndex("shape_id"));
                        for(int i = 0; i<recordCount; i++)
                        {
                            LinkedHashMap<String, LatLng> input = new LinkedHashMap<>();
                            LatLng coordinates = new LatLng(Double.parseDouble(stopCursor.getString(stopCursor.getColumnIndex("stop_lat"))), Double.parseDouble(stopCursor.getString(stopCursor.getColumnIndex("stop_lon"))));
                            input.put(stopCursor.getString(stopCursor.getColumnIndex("stop_name")), coordinates);
                            markerPoints.add(input);
                            stopCursor.moveToNext();
                        }
                    }
                }
               // Cursor shapeCursor = db.rawQuery(dbCommandGET_SHAPE, null);
            }catch(SQLException sqle){
                sqle.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v){
            dialog.dismiss();
            setUpMarkers(markerPoints);
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



    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;


        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("url Error", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }

        /** A class to parse the Google Places in JSON format */
        private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

            // Parsing the data in non-ui thread
            @Override
            protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

                JSONObject jObject;
                List<List<HashMap<String, String>>> routes = null;

                try{
                    jObject = new JSONObject(jsonData[0]);
                    DirectionsJSONParser parser = new DirectionsJSONParser();

                    // Starts parsing data
                    routes = parser.parse(jObject);
                }catch(Exception e){
                    e.printStackTrace();
                }
                return routes;
            }

            // Executes in UI thread, after the parsing process
            @Override
            protected void onPostExecute(List<List<HashMap<String, String>>> result) {
                ArrayList<LatLng> points = null;
                PolylineOptions lineOptions = null;
                MarkerOptions markerOptions = new MarkerOptions();

                // Traversing through all the routes
                for(int i=0;i<result.size();i++){
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for(int j=0;j<path.size();j++){
                        HashMap<String,String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(Color.BLUE);

                }

                // Drawing polyline in the Google Map for the i-th route
                map.addPolyline(lineOptions);
            }
        }

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
