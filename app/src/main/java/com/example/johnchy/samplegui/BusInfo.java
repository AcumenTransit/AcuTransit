package com.example.johnchy.samplegui;

import android.os.Parcel;
import android.os.Parcelable;

public class BusInfo implements Parcelable{
    private String BusNumber;
    private String StopNumber;
    private String TripHeadsign;

    public BusInfo(){
        super();
    }

    public BusInfo(Parcel p){
        String[] data = new String[3];
        p.readStringArray(data);
        this.BusNumber = data[0];
        this.StopNumber = data[1];
        this.TripHeadsign = data[2];
    }

    public void setBusNumber(String BusNumber){
        this.BusNumber = BusNumber;
    }
    public void setStopNumber(String StopNumber){
        this.StopNumber = StopNumber;
    }
    public void setTripHeadsign(String TripHeadsign){
        this.TripHeadsign = TripHeadsign;
    }

    public String getBusNumber(){
        return BusNumber;
    }
    public String getStopNumber(){
        return StopNumber;
    }
    public String getTripHeadsign(){
        return TripHeadsign;
    }

    @Override
    public int describeContents(){
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeStringArray(new String[] {
                this.BusNumber,
                this.StopNumber,
                this.TripHeadsign
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
