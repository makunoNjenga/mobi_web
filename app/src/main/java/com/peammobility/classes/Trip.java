package com.peammobility.classes;


public class Trip {
    LatLong origin, destination;
    double distance;
    boolean onboard, completed;
    int capacity, price, userID, driverID;
    String duration, cabType, originName, destinationName, status, phoneNumber, customerName, driverPhoneNumber, driverName, tripKey, date;

    public Trip() {
    }

    public Trip(LatLong origin, LatLong destination, double distance, boolean onboard, boolean completed, int capacity, int price, int userID, int driverID, String duration, String cabType, String originName, String destinationName, String status, String phoneNumber, String customerName, String driverPhoneNumber, String driverName, String tripKey, String date) {
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.onboard = onboard;
        this.completed = completed;
        this.capacity = capacity;
        this.price = price;
        this.userID = userID;
        this.driverID = driverID;
        this.duration = duration;
        this.cabType = cabType;
        this.originName = originName;
        this.destinationName = destinationName;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.customerName = customerName;
        this.driverPhoneNumber = driverPhoneNumber;
        this.driverName = driverName;
        this.tripKey = tripKey;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDriverID() {
        return driverID;
    }

    public void setDriverID(int driverID) {
        this.driverID = driverID;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getTripKey() {
        return tripKey;
    }

    public void setTripKey(String tripKey) {
        this.tripKey = tripKey;
    }

    public boolean isOnboard() {
        return onboard;
    }

    public void setOnboard(boolean onboard) {
        this.onboard = onboard;
    }

    public String getDriverPhoneNumber() {
        return driverPhoneNumber;
    }

    public void setDriverPhoneNumber(String driverPhoneNumber) {
        this.driverPhoneNumber = driverPhoneNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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
