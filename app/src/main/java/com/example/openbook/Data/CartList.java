package com.example.openbook.Data;


import com.example.openbook.Category.CartCategory;

public class CartList {

    String menuName;
    int menuPrice;
    int menuQuantity;
    CartCategory cartCategory;

    int originalPrice;

    public CartList(String name, int price, int quantity, int originalPrice, CartCategory cartCategory) {
        this.menuName = name;
        this.menuPrice = price;
        this.menuQuantity = quantity;
        this.originalPrice = originalPrice;
        this.cartCategory = cartCategory;
    }

    public CartList(String name, int quantity, CartCategory cartCategory) {
        this.menuName = name;
        this.menuQuantity = quantity;
        this.cartCategory = cartCategory;
    }


    public CartCategory getCartCategory() {
        return cartCategory;
    }

    public int getOriginalPrice() {
        return originalPrice;
    }

    public int getMenuPrice() {
        return menuPrice;
    }

    public void setMenuPrice(int menuPrice) {
        this.menuPrice = menuPrice;
    }


    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public int getMenuQuantity() {
        return menuQuantity;
    }

    public void setMenuQuantity(int menuQuantity) {
        this.menuQuantity = menuQuantity;
    }

}
