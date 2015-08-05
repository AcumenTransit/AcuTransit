package com.example.johnchy.samplegui;

public class GooglePlace {
    private String name;
    private String category;
    private String rating;
    private String open;
    private String iconURL;
    private String longtitude;
    private String latitude;
    private String imageReference;
    public GooglePlace() {
        this.name = "";
        this.rating = "";
        this.open = "";
        this.setCategory("");
        this.iconURL = "";
        this.imageReference ="";
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setRating(String rating) {
        this.rating = rating;
    }
    public String getRating() {
        return rating;
    }
    public void setOpenNow(String open) {
        this.open = open;
    }
    public String getOpenNow() {
        return open;
    }
    public void setIconURL(String iconURL){
        this.iconURL = iconURL;
    }
    public String getIconURL(){
        return iconURL;
    }
    public void setLongtitude(String longtitude){
        this.longtitude = longtitude;
    }
    public String getLongtitude(){
        return longtitude;
    }
    public void setLatitude(String latitude){
        this.latitude = latitude;
    }
    public String getLatitude(){
        return latitude;
    }
    public void setImageReference(String imageReference){
        this.imageReference = imageReference;
    }
    public String getImageReference(){
        return imageReference;
    }
}
