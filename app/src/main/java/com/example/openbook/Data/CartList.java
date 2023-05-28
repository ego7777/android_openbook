package com.example.openbook.Data;


public class CartList {

    String menu_name;
    int menu_price;
    int menu_quantity;
    int viewType;

    public CartList (String name, int price, int quantity, int viewType){
        this.menu_name = name;
        this.menu_price = price;
        this.menu_quantity = quantity;
        this.viewType = viewType;
    }

    public CartList(String name, int quantity, int viewType){
        this.menu_name = name;
        this.menu_quantity =quantity;
        this.viewType = viewType;
    }

    public int getViewType() {
        return viewType;
    }

    public int getMenu_price() {
        return menu_price;
    }

    public void setMenu_price(int menu_price) {
        this.menu_price = menu_price;
    }




    public String getMenu_name() {
        return menu_name;
    }

    public void setMenu_name(String menu_name) {
        this.menu_name = menu_name;
    }

    public int getMenu_quantity(){
        return  menu_quantity;
    }
    public void setMenu_quantity(int menu_quantity) {
        this.menu_quantity = menu_quantity;
    }


}
