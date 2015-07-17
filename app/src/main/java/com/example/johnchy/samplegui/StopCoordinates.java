package com.example.johnchy.samplegui;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by john.chy on 7/17/2015.
 */
public class StopCoordinates {
    private String stopName;
    private LatLng stopCoordinates;

    public StopCoordinates(){
        super();
    }
    public void setStopName(String stopName){
        this.stopName=stopName;
    }
    public void setStopCoordinates(LatLng stopCoordinates){
        this.stopCoordinates=stopCoordinates;
    }
    public String getStopName(){
        return stopName;
    }
    public LatLng getStopCoordinates(){
        return stopCoordinates;
    }
}
