package com.example.openbook.Data;


import java.io.Serializable;

public class OrderList {

    String tableName, menu;
    int quantity, price;

    String statement;

    int viewType; // 0->in/out, 1->menu

    public OrderList(int viewType, String tableName, String menu, int quantity, int price){
        this.viewType = viewType;
        this.tableName = tableName;
        this.menu = menu;
        this.quantity = quantity;
        this.price = price;
    }

    public OrderList(int viewType,String tableName, String statement){
        this.viewType = viewType;
        this.tableName = tableName;
        this.statement = statement;
    }

    public int getViewType(){
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getStatement(){
        return statement;
    }

    public void setStatement(String statement){
        this.statement = statement;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }





}
