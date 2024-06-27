package com.example.openbook;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.DecimalFormat;
import android.util.Log;
import android.util.Pair;

import com.example.openbook.Category.CartCategory;
import com.example.openbook.Category.MenuCategory;
import com.example.openbook.Data.CartList;
import com.example.openbook.Data.MyData;
import com.example.openbook.Data.OrderList;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ManageOrderItems {
    String TAG = "ManageOrderItemsTAG";
    Gson gson = new Gson();

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public String getMenuItem(String menuName, int menuQuantity, int menuPrice, int menuCategory){
        JsonObject menuItem = new JsonObject();
        menuItem.addProperty("menuName", menuName);
        menuItem.addProperty("menuQuantity", menuQuantity);
        menuItem.addProperty("menuPrice", menuPrice);
        menuItem.addProperty("menuCategory", menuCategory);

        JsonArray menuItems = new JsonArray();
        menuItems.add(menuItem);

        return gson.toJson(menuItems);
    }

    public void orderSharedPreference(Context context) {
        sharedPreferences = context.getSharedPreferences("CustomerData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String orderItems = sharedPreferences.getString("orderItems", null);
        Log.d(TAG, "orderSharedPreference: " + orderItems);

        if (orderItems != null && !orderItems.isEmpty()) {
            Type type = new TypeToken<ArrayList<CartList>>() {
            }.getType();
            ArrayList<CartList> orderLists = gson.fromJson(orderItems, type);

            boolean found = false;

            for (int j = 0; j < orderLists.size(); j++) {
                if (orderLists.get(j).getMenuName().equals("프로필 티켓")) {

                    int oldQuantity = orderLists.get(j).getMenuQuantity();
                    int newQuantity = oldQuantity + 1;
                    orderLists.get(j).setMenuQuantity(newQuantity);

                    int oldPrice = orderLists.get(j).getMenuPrice();
                    int newPrice = oldPrice + 2000;
                    orderLists.get(j).setMenuPrice(newPrice);
                    found = true;
                    break;
                }
            }

            if (!found) {
                orderLists.add(new CartList("프로필 티켓", 2000, 1, 2000,
                        MenuCategory.SIDE.getValue(), CartCategory.MENU));
            }

            editor.putString("orderItems", gson.toJson(orderLists));
            editor.commit();

        } else {
            ArrayList<CartList> orderList = new ArrayList<>();
            orderList.add(new CartList("프로필 티켓", 2000, 1, 2000,
                    MenuCategory.SIDE.getValue(), CartCategory.MENU));
            editor.putString("orderItems", gson.toJson(orderList));
            editor.commit();
        }

    }

    public Pair<ArrayList<OrderList>, String> getReceiptData(Context context, MyData myData) {

        sharedPreferences = context.getSharedPreferences("CustomerData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        ArrayList<OrderList> orderLists = new ArrayList<>();

        String sharedOrderList = sharedPreferences.getString("orderItems", null);
        Log.d(TAG, "showReceiptDialog orderList: " + sharedOrderList);
        int price = 0;

        if (sharedOrderList != null) {

            JsonArray jsonArray = gson.fromJson(sharedOrderList, JsonArray.class);

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                orderLists.add(new OrderList(myData.getPaymentCategory().getValue(),
                        myData.getId(),
                        jsonObject.get("menuName").getAsString(),
                        jsonObject.get("menuQuantity").getAsInt(),
                        jsonObject.get("menuPrice").getAsInt()));

                price = price + jsonObject.get("menuPrice").getAsInt();

            }

        } else {
            orderLists.add(new OrderList(myData.getPaymentCategory().getValue(),
                    myData.getId(),
                    "주문 내역이 없습니다."));
        }

        String totalPrice = addCommasToNumber(price);

        return Pair.create(orderLists, totalPrice);
    }

    public String addCommasToNumber(int price) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String totalPrice = decimalFormat.format(price) + "원";

        return totalPrice;
    }
}
