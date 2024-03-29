package com.example.openbook;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class MenuData {

    public String getMenuName() {
        return menuName;
    }

    public int getMenuPrice() {
        return menuPrice;
    }

    public int getMenuCategory() {
        return menuCategory;
    }

    public String getImageURL() {
        return imageURL;
    }

    @PrimaryKey
    int uid;

    @ColumnInfo(name ="menu_name")

    String menuName;
    @ColumnInfo(name ="menu_price")

    int menuPrice;
    @ColumnInfo(name ="menu_category")
    int menuCategory;
    @ColumnInfo(name ="image_url")
    String imageURL;

}
