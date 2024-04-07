package com.example.openbook.Data;

import java.time.LocalDateTime;

public class AdminSalesList {

    String localDate;
    LocalDateTime localDateTime;
    int totalPrice;
    int intDate;


    public AdminSalesList(LocalDateTime localDateTime, int totalPrice){
        this.localDateTime = localDateTime;
        this.totalPrice = totalPrice;

    }

    public AdminSalesList(String localDate, int totalPrice){
        this.localDate = localDate;
        this.totalPrice = totalPrice;
    }

    public AdminSalesList(int intDate, int totalPrice){
        this.intDate = intDate;
        this.totalPrice = totalPrice;
    }

    public String getLocalDate() {
        return localDate;
    }

    public void setLocalDate(String localDate) {
        this.localDate = localDate;
    }

    public int getIntDate(){
        return intDate;
    }

    public void setIntDate(int intDate){
        this.intDate = intDate;
    }



    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }


}
