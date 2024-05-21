package com.example.openbook.retrofit;

import java.util.List;

public class SalesItemDTO {

    private String result;

    private List<SalesItemData> salesItem;

    public class SalesItemData {
        private String menuName;
        private String imageUrl;
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
