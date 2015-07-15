package com.example.johnchy.samplegui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;


public class StartActivity extends Activity {
    private Handler ActivityHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Typeface routeFontface = Typeface.createFromAsset(getAssets(),"route.ttf");
        Typeface displayFontface = Typeface.createFromAsset(getAssets(), "display.TTF");
        TextView routeTextview = (TextView)findViewById(R.id.routetext);
        TextView displayTextview = (TextView)findViewById(R.id.displaytext);
        routeTextview.setTypeface(routeFontface);
        displayTextview.setTypeface(displayFontface);
        ActivityHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(StartActivity.this, ProgressActivity.class));
                overridePendingTransition(R.anim.left_right_animation, R.anim.right_left_animation);
                finish();
            }
        }, 3000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
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
        finish();
    }
}
