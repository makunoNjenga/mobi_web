package com.peammobility.maps;

import com.google.android.gms.maps.model.LatLng;

public class TripRoute {
    LatLng pickUP, destination;
    String distanceText, durationText;
    Double distance, duration;
    public TripRoute(){
    }

    public TripRoute(LatLng pickUP, LatLng destination, Double distance, String distanceText, Double duration, String durationText) {
        this.pickUP = pickUP;
        this.destination = destination;
        this.distance = distance;
        this.distanceText = distanceText;
        this.duration = duration;
        this.durationText = durationText;
    }

    public String getDistanceText() {
        return distanceText;
    }

    public void setDistanceText(String distanceText) {
        this.distanceText = distanceText;
    }

    public String getDurationText() {
        return durationText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public LatLng getPickUP() {
        return pickUP;
    }

    public void setPickUP(LatLng pickUP) {
        this.pickUP = pickUP;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
