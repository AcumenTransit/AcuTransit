package com.example.johnchy.samplegui;

import com.google.android.gms.maps.model.LatLng;

public class Coordinates {
    private String Name;
    private LatLng coordinates;
    private String stop_code;
    private String stop_id;

    public Coordinates(){
        super();
    }
    public void setName(String name){
        this.Name = name;
    }
    public void setCoordinates(LatLng coordinates){
        this.coordinates = coordinates;
    }
    public void setStop_code(String stop_code){
        this.stop_code=stop_code;
    }
    public void setStop_id(String stop_id){
        this.stop_id = stop_id;
    }
    public String getName(){
        return Name;
    }
    public LatLng getCoordinates(){
        return coordinates;
    }
    public String getStop_code(){
        return stop_code;
    }
    public String getStop_id(){
        return stop_id;
    }

}
