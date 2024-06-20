package com.example.openbook.Category;

public enum PaymentCategory {

    NOW(0), LATER(1), UNSELECTED(2);

    private int value;

    PaymentCategory(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
