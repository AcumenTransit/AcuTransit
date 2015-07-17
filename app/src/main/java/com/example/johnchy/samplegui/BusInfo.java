package com.example.johnchy.samplegui;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Created by john.chy on 7/16/2015.
 */
public class BusInfo implements Parcelable{
    private String BusNumber;
    private String RouteName;

    public BusInfo(){
        super();
    }

    public BusInfo(Parcel p){
        String[] data = new String[2];
        p.readStringArray(data);
        this.BusNumber = data[0];
        this.RouteName = data[1];
    }

    public void setBusNumber(String BusNumber){
        this.BusNumber = BusNumber;
    }
    public void setRouteName(String RouteName){
        this.RouteName = RouteName;
    }

    public String getBusNumber(){
        return BusNumber;
    }
    public String getRouteName(){
        return RouteName;
    }

    @Override
    public int describeContents(){
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeStringArray(new String[] {
                this.BusNumber,
                this.RouteName
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public BusInfo createFromParcel(Parcel in) {
            return new BusInfo(in);
        }

        public BusInfo[] newArray(int size) {
            return new BusInfo[size];
        }
    };
}
