package com.myapartment.model;

import java.util.Date;

public class DataIncome {

    private Date date;
    private String hno;
    private float amount;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getHno() {
        return hno;
    }

    public void setHno(String hno) {
        this.hno = hno;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

}
