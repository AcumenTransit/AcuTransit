package com.example.johnchy.samplegui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ListFoundItemsActivity extends Activity {
    ArrayList<String> BusFoundList;
    private ListView list;
    private String directionId = "";
    public static String BUS_LIST_EXTRA = "BusList";
    String directions[] ={"Northbound","Southbound","Westbound","Eastbound"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_buses);
        BusFoundList = new ArrayList<>();
        final ArrayList<BusInfo> foundInfo = getIntent().getExtras().getParcelableArrayList(BUS_LIST_EXTRA);
        list = (ListView) findViewById(R.id.found_items_list);
        if(foundInfo.size() > 0){
            for(int i = 0; i<foundInfo.size();i++){
                BusFoundList.add(foundInfo.get(i).getTripHeadsign());
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                    R.layout.listrow_layout, R.id.businfo, BusFoundList);
            list.setAdapter(arrayAdapter);
            list.setTextFilterEnabled(true);
            list.setVisibility(View.VISIBLE);
        }
        else{
            TextView noListFound = (TextView) findViewById(R.id.NoDevicesFoundMessage);
            TextView BusFoundMessage = (TextView) findViewById(R.id.busesfoundmessage);
            Button tryAgain = (Button) findViewById(R.id.tryagainbutton);
            Button quitbutton = (Button) findViewById(R.id.quitbutton);
            BusFoundMessage.setVisibility(View.INVISIBLE);
            noListFound.setVisibility(View.VISIBLE);
            tryAgain.setVisibility(View.VISIBLE);
            quitbutton.setVisibility(View.VISIBLE);
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int outerposition, long id) {


                final AlertDialog.Builder directionDialog = new AlertDialog.Builder(ListFoundItemsActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.direction_list_layout, null);
                directionDialog.setView(convertView);
                directionDialog.setTitle("Choose a direction:");
                ListView lv = (ListView) convertView.findViewById(R.id.direction_listview);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ListFoundItemsActivity.this, android.R.layout.simple_list_item_1, directions);
                lv.setAdapter(adapter);
                final AlertDialog dlg = directionDialog.show();
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (directions[position].equals("Northbound") || directions[position].equals("Westbound")) {
                            directionId = "0";
                        } else {
                            directionId = "1";
                        }
                        Intent DisplayMap = new Intent(getApplicationContext(), MapsActivity.class);
                        DisplayMap.putExtra("StopNumber", foundInfo.get(outerposition).getStopNumber());
                        DisplayMap.putExtra("busNumber", foundInfo.get(outerposition).getBusNumber());
                        DisplayMap.putExtra("tripHeadsign", foundInfo.get(outerposition).getTripHeadsign());
                        DisplayMap.putExtra("directionId", directionId);
                        DisplayMap.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(DisplayMap);
                        dlg.dismiss();
                        finish();
                    }
                });

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_buses, menu);
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
    public void onBackPressed(){
        finish();
    }

    public void searchforDevice(View view){
        startActivity(new Intent(ListFoundItemsActivity.this, ProgressActivity.class));
        overridePendingTransition(R.anim.left_right_animation, R.anim.right_left_animation);
        finish();
    }
    public void Quitnow(View view){
        finish();
    }
}
