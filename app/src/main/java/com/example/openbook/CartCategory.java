package com.example.openbook;

public enum CartCategory {

    SERVER(0), MENU(1), ADMIN(2);

    private int value;

    CartCategory(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
