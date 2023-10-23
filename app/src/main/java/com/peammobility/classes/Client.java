package com.peammobility.classes;

public class Client {
    String name, email, phoneNumber, createdAT;
    Boolean isActive;

    public Client() {
    }

    public Client(String name, String email, String phoneNumber, String createdAT, Boolean isActive) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAT = createdAT;
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCreatedAT() {
        return createdAT;
    }

    public void setCreatedAT(String createdAT) {
        this.createdAT = createdAT;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
