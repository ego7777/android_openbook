package com.example.openbook.Data;


public class CartList {

    String menu_name;
    int menu_price;
    int menu_count;
    int viewType;

    public CartList (String name, int price, int count, int viewType){
        this.menu_name = name;
        this.menu_price = price;
        this.menu_count = count;
        this.viewType = viewType;
    }

    public CartList(String name, int count, int viewType){
        this.menu_name = name;
        this.menu_count =count;
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

    public int getMenu_count(){
        return  menu_count;
    }
    public void setMenu_count(int menu_count) {
        this.menu_count = menu_count;
    }


}
