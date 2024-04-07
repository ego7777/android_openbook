package com.example.openbook.Data;


import com.google.gson.annotations.SerializedName;

import java.util.List;


public class MenuListDTO {
    String result;
    @SerializedName("items")
    private Items items;

    public String getResult() {
        return result;
    }

    public Items getItems() {
        return items;
    }


    public class Items{
        @SerializedName("item")
        private List<MenuItem> itemList;
        public List<MenuItem> getItemList() {
            return itemList;
        }

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
