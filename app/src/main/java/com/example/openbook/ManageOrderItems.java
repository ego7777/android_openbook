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
import com.example.openbook.retrofit.MenuListDTO;
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

    public void updateProfileGift(Context context, String profileName, int price){
        sharedPreferences = context.getSharedPreferences("CustomerData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String orderedItems = sharedPreferences.getString("orderedItems", null);
        ArrayList<CartList> previousOrderList;

        if(orderedItems != null && !orderedItems.isEmpty()){
            Type type = new TypeToken<ArrayList<CartList>>() {
            }.getType();
            previousOrderList = gson.fromJson(orderedItems, type);
        }else{
            previousOrderList = new ArrayList<>();
        }

        previousOrderList.add(new CartList(profileName, price, 1, 2000, MenuCategory.SIDE.getValue(), CartCategory.MENU));

        editor.putString("orderedItems", gson.toJson(previousOrderList));
        editor.commit();
    }

    public void saveOrderedItems(Context context, ArrayList<CartList> cartLists) {
        Log.d(TAG, "saveOrderedItems 호출");
        sharedPreferences = context.getSharedPreferences("CustomerData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String orderedItems = sharedPreferences.getString("orderedItems", null);

        //shared에 저장된 내용이 있으면 기존값에 추가해서 저장
        if (orderedItems != null && !orderedItems.isEmpty()) {
            Type type = new TypeToken<ArrayList<CartList>>() {
            }.getType();
            ArrayList<CartList> orderLists = gson.fromJson(orderedItems, type);
            Log.d(TAG, "saveOrderedItems: ");

            boolean found = false;
            for (int i = 0; i < cartLists.size(); i++) {
                CartList cartItem = cartLists.get(i);

                for (int j = 0; j < orderLists.size(); j++) {
                    if (orderLists.get(j).getMenuName().equals(cartItem.getMenuName())) {

                        int oldQuantity = orderLists.get(j).getMenuQuantity();
                        int newQuantity = oldQuantity + cartItem.getMenuQuantity();
                        orderLists.get(j).setMenuQuantity(newQuantity);

                        int oldPrice = orderLists.get(j).getMenuPrice();
                        int newPrice = oldPrice + cartItem.getMenuPrice();
                        orderLists.get(j).setMenuPrice(newPrice);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    orderLists.add(new CartList(cartItem.getMenuName(),
                            cartItem.getMenuPrice(),
                            cartItem.getMenuQuantity(),
                            cartItem.getOriginalPrice(),
                            cartItem.getMenuCategory(),
                            cartItem.getCartCategory()));
                }
                found = false;
            }

            editor.putString("orderedItems", gson.toJson(orderLists));
            editor.commit();
        } else {
            editor.putString("orderedItems", gson.toJson(cartLists));
            editor.commit();
        }

    }


    public Pair<ArrayList<OrderList>, String> getReceiptData(Context context, MyData myData) {

        sharedPreferences = context.getSharedPreferences("CustomerData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        ArrayList<OrderList> orderLists = new ArrayList<>();

        String sharedOrderList = sharedPreferences.getString("orderedItems", null);
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

        String totalPrice = addCommasToNumber(price, 101);

        return Pair.create(orderLists, totalPrice);
    }

    public String addCommasToNumber(int price, int type) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");

        if(type == 0){
            String totalPrice = decimalFormat.format(price) + "원";
            return "총 금액 : " + totalPrice;
        }else if(price == 0){
            return "";
        }
        return null;
    }
}
