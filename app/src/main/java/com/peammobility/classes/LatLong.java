package com.peammobility.classes;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class LatLong {
    Double latitude, longitude;

    public LatLong() {
    }

    public LatLong(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LatLng getLatLng() {
        return new LatLng(this.getLatitude(), this.getLongitude());
    }

    /**
     *
     */
    public String getLatLngString() {
        return this.getLatitude() + ":" + this.getLongitude();
    }
}
