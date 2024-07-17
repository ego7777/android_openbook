package com.example.openbook.Data;

import com.example.openbook.Category.MenuCategory;

public class MenuList {
    String url;
    String menuName;
    int menuPrice;
    int menuQuantity;
    int menuCategory;


    public MenuList (String url, String name, int price, int menuCategory){
        this.url = url;
        this.menuName = name;
        this.menuPrice = price;
        this.menuCategory = menuCategory;  //1이면 mainMenu, 2면 drink, 3이면 sideMenu
    }


    public int getMenuPrice() {
        return menuPrice;
    }

    public String getUrl() {
        return url;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menu_name) {
        this.menuName = menu_name;
    }

    public int getMenuCategory() {
        return menuCategory;
    }

}
