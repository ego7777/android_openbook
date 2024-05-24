package com.example.openbook.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SalesItemDTO {

    private String result;

    public String getResult() {
        return result;
    }
    @SerializedName("sales_items")
    private List<SalesItemData> salesItems;

    public List<SalesItemData> getSalesItems() {
        return salesItems;
    }

    public class SalesItemData {
        @SerializedName("menu_name")
        private String menuName;
        @SerializedName("image_url")
        private String imageUrl;
        @SerializedName("menu_category")
        private int menuCategory;

        public String getMenuName() {
            return menuName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public int getMenuCategory() {
            return menuCategory;
        }
    }
}
