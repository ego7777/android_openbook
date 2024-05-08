package com.example.openbook.Data;


import com.example.openbook.PaymentCategory;

import java.io.Serializable;

public class MyData implements Serializable {
    String id;
    int tableFromDB;
    PaymentCategory paymentCategory;
    boolean isOrder;
    boolean usedTable;
    int identifier;
    boolean isFcmExist;

    public MyData(String id,
                  int tableFromDB,
                  PaymentCategory paymentCategory,
                  boolean isOrder,
                  boolean usedTable,
                  int identifier,
                  boolean isFcmExist){
        this.id = id;
        this.tableFromDB = tableFromDB;
        this.paymentCategory = paymentCategory;
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

    public PaymentCategory getPaymentCategory() {
        return paymentCategory;
    }

    public void setPaymentCategory(PaymentCategory paymentCategory) {
        this.paymentCategory = paymentCategory;
    }

    public boolean isOrder() {
        return isOrder;
    }

    public void setOrder(boolean order) {
        isOrder = order;
    }

    public void init(){
        paymentCategory = PaymentCategory.UNSELECTED;
        isOrder = false;
        usedTable = false;
        identifier = 0;
    }

}
