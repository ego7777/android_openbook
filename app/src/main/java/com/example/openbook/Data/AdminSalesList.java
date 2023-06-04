package com.example.openbook.Data;

import java.time.LocalDateTime;

public class AdminSalesList {

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

    LocalDateTime localDateTime;
    int totalPrice;

    public AdminSalesList(LocalDateTime localDateTime, int totalPrice){
        this.localDateTime = localDateTime;
        this.totalPrice = totalPrice;
    }


}
