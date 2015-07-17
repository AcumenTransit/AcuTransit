package com.example.johnchy.samplegui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class ListFoundItemsActivity extends Activity {
    ArrayList<String> BusFoundList;
    private ListView list;
    public static String BUS_LIST_EXTRA = "BusList";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_buses);
       // final ArrayList<String> foundItems =  getIntent().getStringArrayListExtra("BusList");
        BusFoundList = new ArrayList<>();
        final ArrayList<BusInfo> foundInfo = getIntent().getExtras().getParcelableArrayList(BUS_LIST_EXTRA);
        list = (ListView) findViewById(R.id.found_items_list);
        if(foundInfo.size() > 0){
            for(int i = 0; i<foundInfo.size();i++){
                BusFoundList.add("Bus Number: " + foundInfo.get(i).getBusNumber() + "\n"
                        + "Route: " + foundInfo.get(i).getRouteName());
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
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent DisplayMap = new Intent(getApplicationContext(), MapsActivity.class);
                DisplayMap.putExtra("busNumber",foundInfo.get(position).getBusNumber());
                DisplayMap.putExtra("routeName",foundInfo.get(position).getRouteName());
                DisplayMap.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(DisplayMap);
                finish();
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
