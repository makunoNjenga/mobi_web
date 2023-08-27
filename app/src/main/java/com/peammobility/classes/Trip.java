package com.peammobility.classes;


public class Trip {
    LatLong origin, destination;
    double distance;
    boolean completed;
    int capacity, price, userID;
    String duration, cabType, originName, destinationName, status;

    public Trip() {
    }

    public Trip(LatLong origin, LatLong destination, double distance, boolean completed, int capacity, int price, int userID, String duration, String cabType, String originName, String destinationName, String status) {
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.completed = completed;
        this.capacity = capacity;
        this.price = price;
        this.userID = userID;
        this.duration = duration;
        this.cabType = cabType;
        this.originName = originName;
        this.destinationName = destinationName;
        this.status = status;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LatLong getOrigin() {
        return origin;
    }

    public void setOrigin(LatLong origin) {
        this.origin = origin;
    }

    public LatLong getDestination() {
        return destination;
    }

    public void setDestination(LatLong destination) {
        this.destination = destination;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCabType() {
        return cabType;
    }

    public void setCabType(String cabType) {
        this.cabType = cabType;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }
}
