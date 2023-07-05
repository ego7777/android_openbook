package com.example.openbook.Data;


import java.io.Serializable;

public class MyData implements Serializable {
    String id;
    int tableFromDB;
    String paymentStyle;
    boolean isOrder;

    public MyData(String id, int tableFromDB, String paymentStyle, boolean isOrder){
        this.id = id;
        this.tableFromDB = tableFromDB;
        this.paymentStyle = paymentStyle;
        this.isOrder = isOrder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTableFromDB() {
        return tableFromDB;
    }

    public void setTableFromDB(int tableFromDB) {
        this.tableFromDB = tableFromDB;
    }

    public String getPaymentStyle() {
        return paymentStyle;
    }

    public void setPaymentStyle(String paymentStyle) {
        this.paymentStyle = paymentStyle;
    }

    public boolean isOrder() {
        return isOrder;
    }

    public void setOrder(boolean order) {
        isOrder = order;
    }

}
