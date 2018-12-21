package com.myapartment.model;

public class User {

    private String size;
    private String hno;
    private double amount;
    private String ownername;
    private long mobilenumber;
    private String email;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getHno() {
        return hno;
    }

    public void setHno(String hno) {
        this.hno = hno;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getOwnername() {
        return ownername;
    }

    public void setOwnername(String ownername) {
        this.ownername = ownername;
    }

    public long getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(long mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
