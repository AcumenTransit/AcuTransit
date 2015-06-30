package com.example.johnchy.samplegui;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;


public class StartActivity extends ActionBarActivity {
    ImageView animatedtext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Typeface fontTypeface = Typeface.createFromAsset(getAssets(),"loaded.ttf");
        TextView routeTextview = (TextView)findViewById(R.id.routetext);
        TextView displayTextview = (TextView)findViewById(R.id.displaytext);
        TextView touchTextview = (TextView)findViewById(R.id.touchtext);
        routeTextview.setTypeface(fontTypeface);
        displayTextview.setTypeface(fontTypeface);
        touchTextview.setTypeface(touchTextview.getTypeface(),Typeface.BOLD);
        Animation anim = new AlphaAnimation(0.0f,1.0f);
        anim.setDuration(1000);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        touchTextview.startAnimation(anim);
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

    public void sendMessage(View view){
        startActivity(new Intent(this, MapsActivity.class));
    }

}
