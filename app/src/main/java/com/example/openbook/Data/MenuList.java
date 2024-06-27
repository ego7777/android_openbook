package com.example.openbook.Data;

public class MenuList {
    String url;
    String menuName;
    int menuPrice;
    int menuType;


    public MenuList (String url, String name, int price, int menuType){
        this.url = url;
        this.menuName = name;
        this.menuPrice = price;
        this.menuType = menuType;  //1이면 mainMenu, 2면 drink, 3이면 sideMenu
    }


    public int getMenuPrice() {
        return menuPrice;
    }

    public void setMenuPrice(int menu_price) {
        this.menuPrice = menu_price;
    }

    public String getUrl() {
        return url;
    }

    public void setUri(String url) {
        this.url= url;
    }


    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menu_name) {
        this.menuName = menu_name;
    }


    public int getMenuType() {
        return menuType;
    }

    public void setMenuType(int menuType) {
        this.menuType = menuType;
    }


}
