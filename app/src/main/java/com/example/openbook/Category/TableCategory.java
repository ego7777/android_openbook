package com.example.openbook.Category;

public enum TableCategory {

    MY(0), OTHER(1), ACTIVE(2);
    private int value;

    TableCategory(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
