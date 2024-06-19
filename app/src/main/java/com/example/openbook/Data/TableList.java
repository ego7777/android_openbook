package com.example.openbook.Data;

import android.graphics.drawable.Drawable;


import com.example.openbook.TableCategory;

import java.io.Serializable;

public class TableList implements Serializable {

    int tableNumber;
    boolean isActive;
    String tableGender;
    String tableGuestNum;
    TableCategory category;

    String myTable;


    public TableList(int tableNumber, TableCategory category){
        this.tableNumber = tableNumber;
        this.category = category;
    }

    public TableList(String myTable, TableCategory category){
        this.myTable = myTable;
        this.category = category;
    }

    public String getMyTable() {
        return myTable;
    }

    public void setMyTable(String myTable) {
        this.myTable = myTable;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getTableGender() {
        return tableGender;
    }

    public void setTableGender(String tableGender) {
        this.tableGender = tableGender;
    }

    public String getTableGuestNum() {
        return tableGuestNum;
    }

    public void setTableGuestNum(String tableGuestNum) {
        this.tableGuestNum = tableGuestNum;
    }

    public TableCategory getCategory() {
        return category;
    }

    public void setCategory(TableCategory category) {
        this.category = category;
    }

}
