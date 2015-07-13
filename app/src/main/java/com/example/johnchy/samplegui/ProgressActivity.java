package com.example.johnchy.samplegui;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProgressActivity extends ActionBarActivity {

    //private static final UUID BEACON_SERVICE = UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e");
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler btHandler = new Handler();
    private static final long SCAN_PERIOD = 5000;
    private boolean LEscan;
    private String ClosestBeacon;
    private ProgressDialog SetupDialog;
    private int routeCount = 0;
    private String route_name;
    private String bus_number;
    Intent MapData = new Intent(this, MapsActivity.class);

    ArrayList<String> BeaconsFound;
    ArrayList<String> BusFoundList;
    TextView[] Messages = new TextView[4];






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        BeaconsFound = new ArrayList<String>();
        BusFoundList = new ArrayList<String>();
        Messages[0] = (TextView) findViewById(R.id.beaconFindprogress);
        Messages[1] = (TextView) findViewById(R.id.gatheringDataprogress);
        Messages[2] = (TextView) findViewById(R.id.processingDataprogress);
        Messages[3] = (TextView) findViewById(R.id.progressDone);
        checkBluetooth();
        getNames Bus = new getNames();
        Bus.execute();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_progress, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class getNames extends AsyncTask<Void, Void, Void> {

        int stopCount = 0;
        int shapeCount = 0;
        Context context = getApplicationContext();
        SQLHelper markerDBHelper = new SQLHelper(context);

        @Override
        protected void onPreExecute(){
            TextView beaconMessage = Messages[0];
            beaconMessage.setText("Looking for beacons...Done!");
            stopAnimation(Messages, 0);
            TextView gatheringDataMessage = Messages[1];
            gatheringDataMessage.setVisibility(View.VISIBLE);
            startAnimation(Messages, 1);

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
                for(int i = 0; i<BeaconsFound.size(); i++){
                    Cursor nameCursor = db.rawQuery("SELECT DISTINCT s.stop_name, r.route_short_name,r.route_long_name " +
                            "FROM trips AS t INNER JOIN stop_times as st " +
                            "ON t.trip_id = st.trip_id " +
                            "INNER JOIN routes as r " +
                            "ON t.route_id = r.route_id " +
                            "INNER JOIN stops AS s " +
                            "ON s.stop_id = st.stop_id " +
                            "WHERE s.stop_id = \"" + BeaconsFound.get(i) + "\"", null);
                    routeCount = nameCursor.getCount();
                    if(routeCount > 0){
                        if(nameCursor.moveToFirst()){
                            for(int j = 0; j<routeCount; j++){
                                BusFoundList.add("Bus Number: " + nameCursor.getString(nameCursor.getColumnIndex("route_short_name")) + "\n"
                                        + "Route: " + nameCursor.getString(nameCursor.getColumnIndex("route_long_name")));
                                nameCursor.moveToNext();
                            }
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
            TextView gatheringDataMessage = Messages[1];
            TextView processingDataMessage = Messages[2];
            gatheringDataMessage.setText("Gathering Data...Done!");
            stopAnimation(Messages, 1);
            if(BusFoundList.size() > 0){
                processingDataMessage.setVisibility(View.VISIBLE);
                startAnimation(Messages,2);
                displayBusList();
            }
            else{
                processingDataMessage.setText("No Buses Found!");
                processingDataMessage.setVisibility(View.VISIBLE);
                TextView DoneMessage = Messages[3];
                DoneMessage.setVisibility(View.VISIBLE);
            }

        }
    }
    public void checkBluetooth() {
        TextView message = Messages[0];
        message.setVisibility(View.VISIBLE);
        startAnimation(Messages, 0);
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        if (btAdapter == null || !btAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            scanForBLE();
        }

    }
    public void scanForBLE()
    {
        scanLeDevice(true);
    }
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            btHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LEscan = false;
                    btAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            LEscan = true;
            btAdapter.startLeScan(mLeScanCallback);
        } else {
            LEscan = false;
            btAdapter.stopLeScan(mLeScanCallback);
        }
//	        ...
    }
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord)
                {
                    if (device.getType() == 2)
                    {
                        Log.d("bleScanner", device.toString() + " name: " + device.getName() + " type: " + device.getType() + " RSSI:" + rssi + " Address: " + device.getAddress());
                        String tempholder = device.getName();
                        if(!BeaconsFound.contains(tempholder)){
                            BeaconsFound.add(device.getName());
                        }

                        Log.d("Beacon Contains: ", Integer.toString(BeaconsFound.size()));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }
            };
    public void startAnimation(TextView MessageArray[], int position){
        TextView animateMessage = MessageArray[position];
        Animation anim = new AlphaAnimation(0.0f,1.0f);
        anim.setDuration(1000);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        animateMessage.startAnimation(anim);
    }
    public void stopAnimation(TextView MessageArray[], int position){
        TextView stopMessageanimation = MessageArray[position];
        stopMessageanimation.clearAnimation();
    }
    public void displayBusList(){
        TextView processingDataMessage = Messages[2];
        processingDataMessage.setText("Processing Data...Done!");
        stopAnimation(Messages,2);
        TextView DoneMessage = Messages[3];
        DoneMessage.setVisibility(View.VISIBLE);
    }
    public void onBackPressed(){
        finish();
    }
    public void sendMessagetoMap(View view){
        startActivity(MapData);
    }


}
