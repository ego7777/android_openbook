package com.example.openbook.Category;

public enum MenuCategory {
    MAIN(0), SIDE(1), DRINK(2);
    private int value;

    MenuCategory(int value){
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
