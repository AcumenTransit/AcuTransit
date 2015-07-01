package com.example.johnchy.samplegui;


import android.app.ProgressDialog;
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
import java.util.List;

public class MapsActivity extends FragmentActivity {


    private GoogleMap map; // Might be null if Google Play services APK is not available.
    ArrayList<LatLng> markerPoints = new ArrayList<>();
    private static final LatLng SAN_JOSE = new LatLng(37.3394, -121.89389);
    private static final LatLng ROSE_GARDEN = new LatLng(37.3322,-121.9281);
    private static final LatLng HOLLOW_PARK = new LatLng(37.3257, -121.8621);

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

    private void addMarkers(){

    }
    private void setUpMarkers(ArrayList<LatLng> points){
        /*map.addMarker(new MarkerOptions().position(ROSE_GARDEN).title("Marker"));
        map.addMarker(new MarkerOptions().position(HOLLOW_PARK).title("Marker"));*/
       for (int i = 0; i<points.size(); i++)
        {
            map.addMarker(new MarkerOptions().position(points.get(i)).title("Marker"));
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
        setUpMarkers(markerPoints);
    }
}
