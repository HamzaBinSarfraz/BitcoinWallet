/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.o3.bitcoin.ui.dialogs;

/**
 *
 * @author tygac
 */
public class RequestPaymentHandler {
    String address;
    String description;
    String account;
    String expireTime;
    String amount;
    String status;
    String date;
    String id;
    
    public RequestPaymentHandler(){
    }

    public RequestPaymentHandler(String account, String address, String date, String description, String amount, String expireTime, String status, String id) {
        this.address = address;
        this.description = description;
        this.account = account;
        this.expireTime = expireTime;
        this.amount = amount;
        this.date = date;
        this.status = status;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    
    
    public String getDate()
    {
        return date;
    }
    
    public void setDatte(String date)
    {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }
    

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
}
