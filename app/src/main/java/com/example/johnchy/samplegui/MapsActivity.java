package com.example.johnchy.samplegui;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity {


    private GoogleMap map; // Might be null if Google Play services APK is not available.
    ArrayList<LinkedHashMap<String, LatLng>> markerPoints = new ArrayList<>();
    ArrayList<ArrayList<LatLng>> routeShape = new ArrayList<>();
    ArrayList<String> getShape = new ArrayList<>();
    ArrayList<LatLng>RoutePositions = new ArrayList<>();
    private static final LatLng SAN_JOSE = new LatLng(37.3394, -121.89389);
    private ProgressDialog dialog;
    private String shape_id = "";
    private String busNumber;
    private String direction = "0";
    public class InfoAdapter implements GoogleMap.InfoWindowAdapter {
        LayoutInflater inflater = null;
        private TextView textViewTitle;

        public InfoAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            View v = inflater.inflate(R.layout.businfo_layout, null);
            if (marker != null) {
                textViewTitle = (TextView) v.findViewById(R.id.businfo);
                textViewTitle.setText(marker.getTitle());
            }
            return (v);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return (null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_maps);
        busNumber = getIntent().getStringExtra("busNumber");
        addMarkers MarkerSetforRoute = new addMarkers();
        MarkerSetforRoute.execute();
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public String setDay(){
        Calendar c = Calendar.getInstance();
        int dayofweek = c.get(Calendar.DAY_OF_WEEK);
        if((dayofweek >= Calendar.MONDAY) && (dayofweek <= Calendar.FRIDAY)){
            return "Weekdays";
        }
        else if(dayofweek == Calendar.SATURDAY){
            return "Saturday";
        }
        else
        {
            return "Sunday";
        }
    }
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
            dialog.setMessage("Routing, please wait...");
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
            try {
                markerDBHelper.openDataBase();
                SQLiteDatabase db = markerDBHelper.getReadableDatabase();
                Cursor stopCursor = db.rawQuery("SELECT DISTINCT s.stop_name, s.stop_lat, s.stop_lon, t.shape_id " +
                        "FROM trips as t INNER JOIN stop_times as st " +
                        "ON st.trip_id = t.trip_id " +
                        "INNER JOIN stops as s ON s.stop_id = st.stop_id " +
                        "WHERE route_id = " + "\"" + busNumber + "\"" + " AND t.service_id = " + "\"" + setDay() + "\"" + " AND t.direction_id = " +
                        "\"" + direction + "\"", null);
                stopCount = stopCursor.getCount();
                if (stopCount > 0) {
                    if (stopCursor.moveToFirst()) {
                        for (int i = 0; i < stopCount; i++) {
                            shape_id = stopCursor.getString(stopCursor.getColumnIndex("shape_id"));
                            if (!getShape.contains(shape_id)) {
                                getShape.add(shape_id);
                            }
                            LinkedHashMap<String, LatLng> input = new LinkedHashMap<>();
                            LatLng coordinates = new LatLng(Double.parseDouble(stopCursor.getString(stopCursor.getColumnIndex("stop_lat"))),
                                    Double.parseDouble(stopCursor.getString(stopCursor.getColumnIndex("stop_lon"))));
                            input.put(stopCursor.getString(stopCursor.getColumnIndex("stop_name")), coordinates);
                            markerPoints.add(input);
                            stopCursor.moveToNext();
                        }
                    }
                }
                for (int i = 0; i < getShape.size(); i++) {
                    ArrayList<LatLng> points = new ArrayList<>();
                    Cursor shapeCursor = db.rawQuery("SELECT shape_pt_lat, shape_pt_lon FROM shapes " +
                            "WHERE shape_id = \"" + getShape.get(i) + "\" ORDER BY CAST(shape_pt_sequence AS INTEGER)", null);
                    shapeCount = shapeCursor.getCount();
                    if (shapeCount > 0) {
                        if (shapeCursor.moveToFirst()) {
                            for (int j = 0; j < shapeCount; j++) {
                                LatLng path = new LatLng(
                                        Double.parseDouble(shapeCursor.getString(shapeCursor.getColumnIndex("shape_pt_lat"))),
                                        Double.parseDouble(shapeCursor.getString(shapeCursor.getColumnIndex("shape_pt_lon"))));
                                points.add(path);
                                shapeCursor.moveToNext();
                            }
                        }
                    }
                    routeShape.add(points);
                }
            }catch(SQLException sqle){
                sqle.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v){
            createRouteTitle();
            setUpMarkers(markerPoints);
            setBounds(markerPoints);
            createfullRoute(routeShape);
            dialog.dismiss();
        }
    }

    private void createRouteTitle(){

    }
    private void setUpMarkers(ArrayList<LinkedHashMap<String, LatLng>> points){
        map.setInfoWindowAdapter(new InfoAdapter(getLayoutInflater()));
        for (LinkedHashMap<String, LatLng> inputs : points){
            for(final Map.Entry<String, LatLng> entry : inputs.entrySet()){
                map.addMarker(new MarkerOptions().position(entry.getValue()).title(entry.getKey())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.greenpin)));
            }
        }

    }
    private void createfullRoute(ArrayList<ArrayList<LatLng>> allpoints){
        for(int i = 0; i< allpoints.size(); i++){
            RoutePositions = allpoints.get(i);
            createRoute(RoutePositions);
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
        int padding = 30;
        int width = getResources().getDisplayMetrics().widthPixels;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LinkedHashMap<String, LatLng> inputs : markerPoints){
            for(Map.Entry<String, LatLng> entry : inputs.entrySet()){
                builder.include(entry.getValue());
            }
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width,width,padding);
        map.moveCamera(cu);
        map.animateCamera(cu);
    }
    public void onBackPressed(){
        Intent DisplayData = new Intent(getApplicationContext(), ProgressActivity.class);
        DisplayData.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(DisplayData);
        overridePendingTransition(R.anim.left_right_animation, R.anim.right_left_animation);
        finish();
    }
    private void setUpMap() {
        map.getUiSettings().setCompassEnabled(false);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(SAN_JOSE)
                .zoom(13)
                .bearing(90)
                .tilt(30)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

}
