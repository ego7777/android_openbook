package com.example.openbook.Activity;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.openbook.Data.OrderList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderData {

    String TAG = "OrderDataTAG";
    String data;

    OkHttpClient okHttpClient = new OkHttpClient();

    public String getOrderData(String tableName, int tableIdentifier){

        RequestBody formBody = new FormBody.Builder().
                add("tableName", tableName).
                add("tableIdentifier", String.valueOf(tableIdentifier)).
                build();

        Request request = new Request.Builder()
                .url("http://3.36.255.141/OrderListLookUp.php")
                .post(formBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure: " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, "onResponse: " + responseBody);
                data = responseBody;
                Log.d(TAG, "data: " + data);
            }
        });

        return data;
    }

    public ArrayList setArrayList(String tableName, String data, ArrayList list){

        try{
            JSONArray jsonArray = new JSONArray(data);

            for(int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String menuName = jsonObject.getString("menu");
                int menuPrice = jsonObject.getInt("price");
                int menuQuantity = jsonObject.getInt("quantity");
                int totalPrice = jsonObject.getInt("totalPrice");

                list.add(new OrderList(1,tableName, menuName, menuPrice, menuQuantity));

            }

        }catch (JSONException e){
            Log.d(TAG, "setArrayList e: " + e);
        }

        return list;

    }
}
