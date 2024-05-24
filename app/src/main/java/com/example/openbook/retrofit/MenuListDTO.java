package com.example.openbook.retrofit;


import android.view.MenuItem;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class MenuListDTO {
    String result;

    public String getResult() {
        return result;
    }

    @SerializedName("items")
    private List<MenuItem> itemList;

    public List<MenuItem> getItemList() {
        return itemList;
    }


    public class MenuItem {
        @SerializedName("menuName")
        String menuName;

        @SerializedName("menuPrice")
        int menuPrice;

        @SerializedName("menuCategory")
        int menuCategory;

        @SerializedName("imageURL")
        String imageURL;

        public String getMenuName() {
            return menuName;
        }

        public int getMenuCategory() {
            return menuCategory;
        }

        public int getMenuPrice() {
            return menuPrice;
        }

        public String getImageURL() {
            return imageURL;
        }
    }
}
