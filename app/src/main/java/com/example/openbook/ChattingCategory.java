package com.example.openbook;

public enum ChattingCategory {

    MY(0), OTHER(1);
    private int value;

    ChattingCategory(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
