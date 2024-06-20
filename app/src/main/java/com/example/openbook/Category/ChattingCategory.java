package com.example.openbook.Category;

public enum ChattingCategory {

    MINE(0), OTHERS(1);

    private int value;

    ChattingCategory(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
