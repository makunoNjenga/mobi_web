package com.peammobility.maps;

public class Place {
    String name, placeID;

    public Place() {
    }
    public Place(String name, String placeID) {
        this.name = name;
        this.placeID = placeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }
}
