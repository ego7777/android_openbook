package com.example.openbook.Data;


import com.example.openbook.Category.TableCategory;

import java.io.Serializable;

public class TableList implements Serializable {

    int tableNumber;
    boolean isActive;
    String tableGender;
    String tableGuestNum;
    TableCategory category;
    String myTable;
    int isNotRead;


    public TableList(int tableNumber, TableCategory category, int isNotRead){
        this.tableNumber = tableNumber;
        this.category = category;
        this.isNotRead = isNotRead;

    }

    public TableList(String myTable, TableCategory category){
        this.myTable = myTable;
        this.category = category;
    }

    public void setIsNotRead(int isNotRead) {
        this.isNotRead = isNotRead;
    }

    public int getIsNotRead() {
        return isNotRead;
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
