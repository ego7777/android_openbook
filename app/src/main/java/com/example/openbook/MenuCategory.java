package com.example.openbook;

public enum MenuCategory {
    MAIN(0), SIDE(1), DRINK(2);
    private int value;

    MenuCategory(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
