package com.example.johnchy.samplegui;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class MapsActivity extends FragmentActivity {


    private GoogleMap map; // Might be null if Google Play services APK is not available.
    ArrayList<Coordinates> markerPoints = new ArrayList<>();
    ArrayList<ArrayList<LatLng>> routeShape = new ArrayList<>();
    ArrayList<String> getShape = new ArrayList<>();
    ArrayList<LatLng>RoutePositions = new ArrayList<>();
    HashMap <String, String> ArrayforETA = new HashMap<>();
    private static final LatLng SAN_JOSE = new LatLng(37.3394, -121.89389);


    private boolean backpressOnce = false;
    private boolean showInfoWindowOnce;
    private ProgressDialog dialog;
    private String routeName = "";
    private String stopNumber = "";
    private String shape_id = "";
    private String busNumber = "";
    private String directionId = "";
    private float zoom;

    LatLng position;
    Marker referenceMarker;
    final String PLACES_KEY = "AIzaSyCBUXlwlnQqYeNMizpaKt9Kyze5fZ2YEKM";
    final String ETA_KEY = "e6897481-a63c-4be2-8d72-fa3f8ddcfc54";
    private static String placesURL = "https://maps.googleapis.com/maps/api/place/search/json?location=";
    private static String photoURL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=";
    private static String etaURL = "http://api.511.org/transit/StopMonitoring?";
    private static String radius = "500";
    private static String sensor = "true";
    ArrayList<GooglePlace> venuesfound = new ArrayList<>();
    ArrayList<Marker> MarkerstoRemove = new ArrayList<>();
    HashMap<String, String> placesImages = new HashMap<>();

    public class InfoAdapter implements GoogleMap.InfoWindowAdapter {
        LayoutInflater inflater = null;
        private TextView textViewstopName;
        private TextView arrivalTime;
        private TextView etaTime;
        private TextView placeName;
        private TextView placeDescription;
        private ImageView placeImage;
        public InfoAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            if (marker != null) {
                if(placesImages.containsKey(marker.getId())) {
                        View v = inflater.inflate(R.layout.places_layout, null);
                        placeName = (TextView) v.findViewById(R.id.place_title);
                        placeDescription = (TextView) v.findViewById(R.id.place_snippet);
                        placeImage = (ImageView) v.findViewById(R.id.place_badge);
                        placeName.setText(marker.getTitle());
                        placeDescription.setText(marker.getSnippet());
                        if(showInfoWindowOnce){
                            showInfoWindowOnce = false;
                            Picasso picasso = Picasso.with(getApplicationContext());
                            picasso.invalidate(marker.getId());
                            picasso.load(photoURL + placesImages.get(marker.getId()) + "&key=" + PLACES_KEY)
                                    .placeholder(R.drawable.bus)
                                    .into(placeImage);
                        }
                        else{
                            showInfoWindowOnce = true;
                            Picasso picasso = Picasso.with(getApplicationContext());
                            picasso.invalidate(marker.getId());
                            picasso.load(photoURL + placesImages.get(marker.getId()) + "&key=" + PLACES_KEY)
                                    .placeholder(R.drawable.bus)
                                    .into(placeImage, new InfoWindowRefresher(marker));
                        }
                        return (v);
                }
                else{
                    View v = inflater.inflate(R.layout.businfo_layout, null);
                    String fullsnippet = marker.getSnippet();
                    String[] snippet1 = fullsnippet.split("_");
                    textViewstopName = (TextView) v.findViewById(R.id.businfo);
                    textViewstopName.setText(marker.getTitle());
                    arrivalTime = (TextView) v.findViewById(R.id.arrivalinfo);
                    arrivalTime.setText(snippet1[0]);
                    etaTime = (TextView) v.findViewById(R.id.eta_item);
                    etaTime.setText("ETA: " + snippet1[1]);
                    return (v);
                }
            }
            return null;
        }
        @Override
        public View getInfoContents(Marker marker) {
            return (null);
        }
    }
    private class InfoWindowRefresher implements Callback{
        private Marker markerToRefresh;
        private InfoWindowRefresher(Marker markerToRefresh){
            this.markerToRefresh = markerToRefresh;
        }
        @Override
        public void onSuccess() {
            markerToRefresh.showInfoWindow();
        }

        @Override
        public void onError() {}
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_maps);
        stopNumber = getIntent().getStringExtra("StopNumber");
        busNumber = getIntent().getStringExtra("busNumber");
        routeName = getIntent().getStringExtra("tripHeadsign");
        directionId = getIntent().getStringExtra("directionId");
        getPositions MarkerSetforRoute = new getPositions();
        MarkerSetforRoute.execute();
        setUpMapIfNeeded();
    }
    @Override
    protected void onResume() {
        super.onResume();
        this.backpressOnce = false;
        setUpMapIfNeeded();
    }
    @Override
    public void onBackPressed(){
        if(backpressOnce){
            super.onBackPressed();
            Intent DisplayData = new Intent(getApplicationContext(), ProgressActivity.class);
            DisplayData.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(DisplayData);
            overridePendingTransition(R.anim.left_right_animation, R.anim.right_left_animation);
            finish();
        }
        else{
            this.backpressOnce = true;
            for(Marker marker : MarkerstoRemove){
                marker.setVisible(false);
            }
            map.animateCamera(CameraUpdateFactory.zoomTo(zoom));
            Toast.makeText(this, "Click BACK again to search for beacons", Toast.LENGTH_LONG).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    backpressOnce = false;
                }
            }, 2000);
        }

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
    private void createRouteTitle(){
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.titlelayout);
        TextView routeTitle = (TextView) findViewById(R.id.routeInfotitle);
        routeTitle.setText(routeName);
        layout.setVisibility(View.VISIBLE);
    }
    private void SetupStopMarkers(){
        map.setInfoWindowAdapter(new InfoAdapter(getLayoutInflater()));
        addMarkersToMap(markerPoints);
    }
    private void addMarkersToMap(ArrayList<Coordinates> points){
        for(int i = 0; i<points.size(); i++){
            try{
                if(stopNumber.equals(points.get(i).getStop_id())){
                    Marker marker = map.addMarker(new MarkerOptions().position(points.get(i).getCoordinates())
                            .title(points.get(i).getName())
                            .snippet("calculating..." + "_" + "calculating...")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.stop_here)));
                    map.getUiSettings().setMapToolbarEnabled(false);
                    ArrayforETA.put(marker.getId(), points.get(i).getStop_code());
                }
                else{
                    Marker marker = map.addMarker(new MarkerOptions().position(points.get(i).getCoordinates())
                            .title(points.get(i).getName())
                            .snippet("calculating..." + "_" + "calculating...")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_pin)));
                    map.getUiSettings().setMapToolbarEnabled(false);
                    ArrayforETA.put(marker.getId(), points.get(i).getStop_code());
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    private void createfullRoute(ArrayList<ArrayList<LatLng>> allpoints){
        for(int i = 0; i< allpoints.size(); i++){
            RoutePositions = allpoints.get(i);
            createRoute(RoutePositions);
        }
    }
    private void createRoute(ArrayList<LatLng> points) {
        LatLngBounds.Builder stopBounds = new LatLngBounds.Builder();
        for (int i = 0; i < points.size() - 1; i++) {
            stopBounds.include(points.get(i));
            LatLng src = points.get(i);
            LatLng dest = points.get(i + 1);
            map.addPolyline(
                    new PolylineOptions().add(
                            new LatLng(src.latitude, src.longitude),
                            new LatLng(dest.latitude, dest.longitude)
                    ).width(9).color(Color.BLUE).geodesic(true)
            );
        }
        setBounds(stopBounds, 30);
    }
    private void setBounds(LatLngBounds.Builder bound, int padding){
        int width = getResources().getDisplayMetrics().widthPixels;
        LatLngBounds bounds = bound.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, width, padding);
        map.animateCamera(cu);
        zoom = map.getCameraPosition().zoom;
    }
    private void setUpMap() {
        map.getUiSettings().setCompassEnabled(false);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(SAN_JOSE)
                .zoom(13)
                .bearing(90)
                .tilt(30)
                .build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                position = marker.getPosition();
                marker.hideInfoWindow();
                placesRequest newPlaces = new placesRequest();
                newPlaces.execute();
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(ArrayforETA.containsKey(marker.getId())){
                    try {
                        referenceMarker = marker;
                        etaRequest newEta = new etaRequest();
                        newEta.execute(ArrayforETA.get(marker.getId()));
                        int zoom = (int)map.getCameraPosition().zoom;
                            final int dX = getResources().getDimensionPixelSize(R.dimen.map_dx);
                            final int dY = getResources().getDimensionPixelSize(R.dimen.map_dy);
                            final Projection projection = map.getProjection();
                            final Point markerPoint = projection.toScreenLocation(
                                    marker.getPosition()
                            );
                            markerPoint.offset(dX, dY);
                            final LatLng newLatLng = projection.fromScreenLocation(markerPoint);
                            map.animateCamera(CameraUpdateFactory.newLatLng(newLatLng));

                            marker.showInfoWindow();
                            return true;
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

    }
    private static ArrayList parsedataFound(final String response) {
        ArrayList<GooglePlace> temp = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("results")) {
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    GooglePlace point = new GooglePlace();
                    if (jsonArray.getJSONObject(i).has("name")) {
                        point.setName(jsonArray.getJSONObject(i).optString("name"));
                        point.setRating(jsonArray.getJSONObject(i).optString("rating", " "));
                        if (jsonArray.getJSONObject(i).has("opening_hours")) {
                            if (jsonArray.getJSONObject(i).getJSONObject("opening_hours").has("open_now")) {
                                if (jsonArray.getJSONObject(i).getJSONObject("opening_hours").getString("open_now").equals("true")) {
                                    point.setOpenNow("YES");
                                } else {
                                    point.setOpenNow("NO");
                                }
                            }
                        }
                        else{
                            point.setOpenNow("Not Known");
                        }
                        if(jsonArray.getJSONObject(i).has("types")) {
                            JSONArray typesArray = jsonArray.getJSONObject(i).getJSONArray("types");
                            for (int j = 0; j < typesArray.length(); j++) {
                                point.setCategory(typesArray.getString(j) + ", " + point.getCategory());
                            }
                        }
                        if(jsonArray.getJSONObject(i).has("icon")){
                            point.setIconURL(jsonArray.getJSONObject(i).optString("icon"));
                        }
                        if(jsonArray.getJSONObject(i).has("geometry")){
                            JSONObject position = jsonArray.getJSONObject(i).getJSONObject("geometry");
                            if(position.has("location")){
                                point.setLatitude(jsonArray.getJSONObject(i)
                                        .getJSONObject("geometry")
                                        .getJSONObject("location")
                                        .optString("lat"));
                                point.setLongtitude(jsonArray.getJSONObject(i)
                                        .getJSONObject("geometry")
                                        .getJSONObject("location")
                                        .optString("lng"));
                            }
                        }
                        if(jsonArray.getJSONObject(i).has("photos")){
                            point.setImageReference(jsonArray.getJSONObject(i).getJSONArray("photos")
                                    .getJSONObject(0).optString("photo_reference"));
                        }
                    }
                    temp.add(point);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList();
        }
        return temp;
    }
    private static String parseETAFound(final String response){
        String ETAresult = "";
        try{
            JSONObject jsonObject = new JSONObject(response);
            if(jsonObject.has("ServiceDelivery")){
                JSONObject jsonObject1 = jsonObject.getJSONObject("ServiceDelivery");
                if(jsonObject1.has("StopMonitoringDelivery")){
                    JSONObject jsonObject2 = jsonObject1.getJSONObject("StopMonitoringDelivery");
                    if(jsonObject2.has("MonitoredStopVisit")){
                        JSONArray jsonArray = jsonObject2.getJSONArray("MonitoredStopVisit");
                        for(int i = 0; i< jsonArray.length(); i++){
                            if(jsonArray.getJSONObject(i).has("MonitoredVehicleJourney")){
                                JSONObject jsonObject3 = jsonArray.getJSONObject(i).getJSONObject("MonitoredVehicleJourney");
                                if(jsonObject3.has("MonitoredCall")){
                                    JSONObject jsonObject4 = jsonObject3.getJSONObject("MonitoredCall");
                                    if(jsonObject4.has("AimedArrivalTime")){
                                        ETAresult = jsonObject4.getString("AimedArrivalTime");
                                    }
                                }
                            }
                        }
                    }
                }

            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return ETAresult;
    }
    private class getPositions extends AsyncTask<String, String, Void>{

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
                    getPositions.this.cancel(true);
                }
            });
        }
        @Override
        protected Void doInBackground(String... params) {
            try{
                markerDBHelper.CreateDatabase();
            }catch(IOException e)
            {
                throw new Error("Unable to create database");
            }
            try {
                markerDBHelper.openDataBase();
                SQLiteDatabase db = markerDBHelper.getReadableDatabase();
                Cursor stopCursor = db.rawQuery("SELECT DISTINCT s.stop_id, s.stop_code, s.stop_name, s.stop_lat, s.stop_lon, t.shape_id " +
                        "FROM trips as t INNER JOIN stop_times as st " +
                        "ON st.trip_id = t.trip_id " +
                        "INNER JOIN stops as s ON s.stop_id = st.stop_id " +
                        "WHERE t.route_id = " + "\"" + busNumber + "\"" +
                        " AND t.trip_headsign = " + "\"" + routeName + "\"" +
                        " AND t.service_id = " + "\"" + setDay() + "\"" +
                        " AND t.direction_id = " + "\"" + directionId + "\"", null);
                stopCount = stopCursor.getCount();
                if (stopCount > 0) {
                    if (stopCursor.moveToFirst()) {
                        for (int i = 0; i < stopCount; i++) {
                            shape_id = stopCursor.getString(stopCursor.getColumnIndex("shape_id"));
                            if (!getShape.contains(shape_id)) {
                                getShape.add(shape_id);
                            }
                            Coordinates newStop = new Coordinates();
                            LatLng coordinates = new LatLng(Double.parseDouble(stopCursor.getString(stopCursor.getColumnIndex("stop_lat"))),
                                    Double.parseDouble(stopCursor.getString(stopCursor.getColumnIndex("stop_lon"))));
                            newStop.setStop_id(stopCursor.getString(stopCursor.getColumnIndex("stop_id")));
                            newStop.setName(stopCursor.getString(stopCursor.getColumnIndex("stop_name")));
                            newStop.setCoordinates(coordinates);
                            newStop.setStop_code(stopCursor.getString(stopCursor.getColumnIndex("stop_code")));
                            markerPoints.add(newStop);
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
            SetupStopMarkers();
            createfullRoute(routeShape);
            createRouteTitle();
            dialog.dismiss();
        }
    }
    private class placesRequest extends AsyncTask<String, String, Void>{
        InputStream is = null;
        String result = "";

        @Override
        protected Void doInBackground(String... params) {
            try{
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(placesURL + position.latitude + "," + position.longitude
                        + "&radius=" + radius
                        + "&sensor=" + sensor
                        + "&key=" + PLACES_KEY);
                HttpResponse response = client.execute(get);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            }catch(Exception e){
                e.printStackTrace();
            }
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"),8);
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = reader.readLine())!=null){
                    sb.append(line+"\n");
                }
                is.close();
                result = sb.toString();
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v){
            if(result == null){}
            else{
                LatLngBounds.Builder places = new LatLngBounds.Builder();
                places.include(position);
                venuesfound = parsedataFound(result);
                for(int i = 0; i<venuesfound.size();i++){
                    try{
                        LatLng position = new LatLng(Double.parseDouble(venuesfound.get(i).getLatitude()),
                            Double.parseDouble(venuesfound.get(i).getLongtitude()));
                        places.include(position);
                        Marker marker = map.addMarker(new MarkerOptions().position(position)
                            .infoWindowAnchor(.5f, 4.0f)
                            .title(venuesfound.get(i).getName())
                            .snippet("\nOpen: " + venuesfound.get(i).getOpenNow()
                                    + "\n Category: " + venuesfound.get(i).getCategory()));
                        Bitmap bmpImg = Ion.with(getApplicationContext())
                                .load(venuesfound.get(i).getIconURL())
                                .asBitmap().get();
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmpImg));
                        placesImages.put(marker.getId(), venuesfound.get(i).getImageReference());
                        MarkerstoRemove.add(marker);
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                setBounds(places, 10);
                CameraUpdate newPosition = CameraUpdateFactory.newLatLngZoom(position, 15.5f);
                map.animateCamera(newPosition);
            }
        }
    }
    private class etaRequest extends AsyncTask<String, String, Void>{
        String result = "";
        @Override
        protected Void doInBackground(String... params) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(etaURL + "api_key="
                        + ETA_KEY
                        + "&agency=vta&stopCode="
                        + Integer.parseInt(params[0])
                        + "&format=json");
                get.addHeader("Accept-Encoding","gzip");
                try{
                    StringBuilder sb = new StringBuilder();
                    HttpResponse response = client.execute(get);
                    InputStream responseStream=response.getEntity().getContent();
                    Header header=response.getEntity().getContentEncoding();
                    String contentEncoding=header.getValue();
                    if (contentEncoding.contains("gzip"))   responseStream=new GZIPInputStream(responseStream);
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(responseStream));
                    String s = "";
                    while((s = buffer.readLine())!=null){
                        sb.append(s+"\n");
                    }
                    responseStream.close();
                    result = sb.toString();
                }catch(Exception e){
                    e.printStackTrace();
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void v){
            if(result == null){}
            else{
                String resultString = parseETAFound(result);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"){
                    public Date parse(String source,ParsePosition pos) {
                        return super.parse(source.replaceFirst(":(?=[0-9]{2}$)",""),pos);
                    }
                };
                try{
                    String ETAstring;
                    String ArrivalTime;
                    long secondsinMilli = 1000;
                    long minutesinMilli = secondsinMilli*60;
                    long hoursinMilli = minutesinMilli * 60;
                    Date formatted = df.parse(resultString);
                    formatted.setHours((formatted.getHours() - 8));
                    Calendar currentTime = Calendar.getInstance();
                    Date current = currentTime.getTime();
                    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    if(formatted.compareTo(current) < 0){
                        ArrivalTime = "Already Passed";
                        ETAstring = "No Info Available";
                    }
                    else{
                        long ETA = formatted.getTime() - current.getTime();
                        long elapsedHours = ETA / hoursinMilli;
                        ETA = ETA%hoursinMilli;
                        long elapsedMinutes = ETA / minutesinMilli;
                        ETA = ETA%minutesinMilli;
                        long elapsedseconds = ETA / secondsinMilli;
                        ArrivalTime = dateFormat.format(formatted);
                        ETAstring = elapsedHours +  "h " + elapsedMinutes + "m " + elapsedseconds + "s ";
                    }
                    referenceMarker.setSnippet(ArrivalTime + "_" + ETAstring);
                    referenceMarker.showInfoWindow();

                }catch(Exception e){
                    e.printStackTrace();
                    referenceMarker.setSnippet("No Info Available" + "_" + "No Info Available");
                    referenceMarker.showInfoWindow();
                }
        }
        }
    }

}
