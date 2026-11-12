package com.grocery.implementation.model;

public class CheckoutForm {
    // These fields match the names in your HTML form inputs
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zip;
    private String payment; // e.g., "COD" or "Card"

    // Getters and Setters must be added here for Spring to bind the form data
    // ...
    // Example:
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    // ... (rest of the fields)

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }
}