package com.example.openbook.View;

public class MenuList {
    int redId;
    String menu_name;
    int menu_price;

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    int viewType;

    public MenuList (int redId, String name, int price, int viewType){
        this.redId = redId;
        this.menu_name = name;
        this.menu_price = price;
        this.viewType = viewType;
    }

    public int getMenu_price() {
        return menu_price;
    }

    public void setMenu_price(int menu_price) {
        this.menu_price = menu_price;
    }

    public int getRedId() {
        return redId;
    }

    public void setRedId(int redId) {
        this.redId = redId;
    }


    public String getMenu_name() {
        return menu_name;
    }

    public void setMenu_name(String menu_name) {
        this.menu_name = menu_name;
    }




}
