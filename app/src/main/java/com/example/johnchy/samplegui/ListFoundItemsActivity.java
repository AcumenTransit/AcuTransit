package com.example.johnchy.samplegui;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class ListFoundItemsActivity extends Activity {

    private ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_buses);
        final ArrayList<String> foundItems =  getIntent().getStringArrayListExtra("BusList");
        final ArrayList<String> foundNumbers = getIntent().getStringArrayListExtra("BusNumbers");
        list = (ListView) findViewById(R.id.found_items_list);
        if(foundItems.size() > 0){
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                    R.layout.listrow, R.id.businfo, foundItems);
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
                String busToroute = foundNumbers.get(position);
                DisplayMap.putExtra("busNumber",busToroute);
                DisplayMap.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(DisplayMap);
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
