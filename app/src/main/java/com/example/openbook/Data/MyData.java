package com.example.openbook.Data;


import java.io.Serializable;

public class MyData implements Serializable {
    String id;
    int tableFromDB;
    String paymentStyle;
    boolean isOrder;
    boolean usedTable;
    int identifier;

    boolean isFcmExist;

    public MyData(String id,
                  int tableFromDB,
                  String paymentStyle,
                  boolean isOrder,
                  boolean usedTable,
                  int identifier,
                  boolean isFcmExist){
        this.id = id;
        this.tableFromDB = tableFromDB;
        this.paymentStyle = paymentStyle;
        this.isOrder = isOrder;
        this.usedTable = usedTable;
        this.identifier = identifier;
        this.isFcmExist = isFcmExist;
    }

    public boolean isFcmExist() {
        return isFcmExist;
    }

    public void setFcmExist(boolean fcmExist) {
        isFcmExist = fcmExist;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public boolean isUsedTable() {
        return usedTable;
    }

    public void setUsedTable(boolean usedTable){
        this.usedTable = usedTable;
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
