package com.example.openbook.Data;


public class OrderList {

    String tableName, menu;
    int quantity, price;

    String notice;

    int paymentType;


    public OrderList(int paymentType, String tableName, String menu, int quantity, int price){
        this.paymentType = paymentType;
        this.tableName = tableName;
        this.menu = menu;
        this.quantity = quantity;
        this.price = price;
    }

    public OrderList(int paymentType,String tableName, String notice){
        this.paymentType = paymentType;
        this.tableName = tableName;
        this.notice = notice;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public int getPaymentType(){
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
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
