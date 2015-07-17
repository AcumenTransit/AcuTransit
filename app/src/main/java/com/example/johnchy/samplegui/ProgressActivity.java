package com.example.johnchy.samplegui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;


public class ProgressActivity extends Activity {

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private int REQUEST_ENABLE_BT = 1;
    boolean LEscan;
    private Handler btHandler = new Handler();
    private static final long SCAN_PERIOD = 10000;
    private int routeCount = 0;
    ArrayList<String> BeaconsFound;
    ArrayList<BusInfo> BusInformation;
    TextView[] Messages = new TextView[4];
    public static String BUS_LIST_EXTRA = "BusList";

    getNames Bus;
    getBeacons beacons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        BeaconsFound = new ArrayList<>();
        BusInformation = new ArrayList<>();
        Messages[0] = (TextView) findViewById(R.id.beaconFindprogress);
        Messages[1] = (TextView) findViewById(R.id.gatheringDataprogress);
        Messages[2] = (TextView) findViewById(R.id.processingDataprogress);
        Messages[3] = (TextView) findViewById(R.id.progressDone);
        beacons = new getBeacons();
        beacons.execute();
        Bus = new getNames();
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
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        beacons.cancel(true);
        Bus.cancel(true);
        finish();
    }
    private class getBeacons extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute(){
            TextView message = Messages[0];
            message.setVisibility(View.VISIBLE);
            startAnimation(Messages, 0);
        }
        @Override
        protected Void doInBackground(Void... params) {
            checkBluetooth();
            return null;
        }
        @Override
        protected  void onPostExecute(Void v){
            TextView beaconMessage = Messages[0];
            beaconMessage.setText("Looking for beacons...Done!");
            stopAnimation(Messages, 0);
        }
    }
    public void checkBluetooth() {
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
        try {
            Thread.sleep(1000);
        }catch(Exception e){
            e.printStackTrace();
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
    private class getNames extends AsyncTask<Void, Void, Void> {
        Context context = getApplicationContext();
        SQLHelper markerDBHelper = new SQLHelper(context);
        @Override
        protected void onPreExecute(){
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
                    Cursor nameCursor = db.rawQuery("SELECT DISTINCT r.route_short_name,r.route_long_name " +
                            "FROM stop_times st INNER JOIN trips t " +
                            "ON t.trip_id = st.trip_id " +
                            "INNER JOIN routes r " +
                            "ON r.route_id = t.route_id " +
                            "WHERE st.stop_id = \"" + BeaconsFound.get(i) + "\"", null);
                    routeCount = nameCursor.getCount();
                    if(routeCount > 0){
                        if(nameCursor.moveToFirst()){
                            for(int j = 0; j<routeCount; j++){
                                BusInfo newBus = new BusInfo();
                                if(!BusInformation.contains(newBus)){
                                    BusInformation.add(newBus);
                                }
                                newBus.setBusNumber(nameCursor.getString(nameCursor.getColumnIndex("route_short_name")));
                                newBus.setRouteName(nameCursor.getString(nameCursor.getColumnIndex("route_long_name")));
                                /*String busnumber = nameCursor.getString(nameCursor.getColumnIndex("route_short_name"));
                                if(!BusNumbers.contains(busnumber)){
                                    BusNumbers.add(busnumber);
                                }*/
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
            if(BusInformation.size() > 0){
                processingDataMessage.setVisibility(View.VISIBLE);
                startAnimation(Messages, 2);
                displayMessage();
                sendMessagetoList();
            }
            else{
                processingDataMessage.setText("No Buses Found!");
                processingDataMessage.setVisibility(View.VISIBLE);
                TextView DoneMessage = Messages[3];
                DoneMessage.setVisibility(View.VISIBLE);
                try {
                    Thread.sleep(3000);
                }catch(Exception e){
                    e.printStackTrace();
                }
                sendMessagetoList();
            }
        }
    }
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
    public void displayMessage(){
        TextView processingDataMessage = Messages[2];
        processingDataMessage.setText("Processing Data...Done!");
        stopAnimation(Messages, 2);
        TextView DoneMessage = Messages[3];
        DoneMessage.setVisibility(View.VISIBLE);

    }
    public void sendMessagetoList(){
        Intent DisplayData = new Intent(getApplicationContext(), ListFoundItemsActivity.class);
        DisplayData.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //DisplayData.putStringArrayListExtra("BusList", BusFoundList);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BUS_LIST_EXTRA, (ArrayList<? extends Parcelable>) BusInformation);
        DisplayData.putExtras(bundle);
        startActivity(DisplayData);
        overridePendingTransition(R.anim.bottom_up_animation, R.anim.bottom_down_animation);
        finish();
    }


}
