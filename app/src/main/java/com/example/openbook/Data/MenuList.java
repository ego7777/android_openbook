package com.example.openbook.Data;

public class MenuList {
    int drawableId;
    String menu_name;
    int menu_price;

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    int viewType;

    public MenuList (int drawableId, String name, int price, int viewType){
        this.drawableId = drawableId;
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
        return drawableId;
    }

    public void setRedId(int drawableId) {
        this.drawableId = drawableId;
    }


    public String getMenu_name() {
        return menu_name;
    }

    public void setMenu_name(String menu_name) {
        this.menu_name = menu_name;
    }




}
