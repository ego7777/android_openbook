package com.example.openbook.Data;

import android.graphics.drawable.Drawable;



import java.io.Serializable;

public class TableList implements Serializable {

    int tableNum;
    Drawable tableColor;
    String myTable;
    String tableGender;
    String tableGuestNum;

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    int viewType;


    public TableList(int tableNum, Drawable tableColor, int viewType){
        this.tableNum = tableNum;
        this.tableColor = tableColor;
        this.viewType = viewType;
    }

    public TableList(String myTable, Drawable tableColor, int viewType){
        this.myTable = myTable;
        this.tableColor = tableColor;
        this.viewType = viewType;
    }




    public int getTableNum() {
        return tableNum;
    }

    public void setTableNum(int tableNum) {
        this.tableNum = tableNum;
    }

    public Drawable getTableColor(){
        return tableColor;
    }

    public void setTableColor(Drawable tableColor){
        this.tableColor = tableColor;
    }

    public String getMyTable() {
        return myTable;
    }

    public void setMyTable(String myTable) {
        this.myTable = myTable;
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

}
