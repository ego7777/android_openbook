package com.example.openbook.FCM;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.openbook.BuildConfig;
import com.example.openbook.ResultCallback;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SuccessOrNot;

import com.google.gson.Gson;


import java.util.HashMap;
import java.util.Map;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SendNotification {
    String TAG = "SendNotificationTAG";
    RetrofitManager retrofitManager = new RetrofitManager();
    Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
    RetrofitService service = retrofit.create(RetrofitService.class);
    Gson gson = new Gson();;



    public void completePayment(String to, String request){
        Call<SuccessOrNot> call = service.sendRequestFcm(to, request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<SuccessOrNot> call, Response<SuccessOrNot> response) {
                if(response.isSuccessful()){
                    Log.d(TAG, "onResponse CompletePayment: " + response.body().getResult());
                }else{
                    Log.d(TAG, "CompletePayment is not successful ");
                }
            }
            @Override
            public void onFailure(Call<SuccessOrNot> call, Throwable t) {
                Log.d(TAG, "onFailure CompletePayment: " + t.getMessage());
            }
        });

    }


    public void sendMenu(Map<String, String> orderItems, ResultCallback callback) {
        Log.d(TAG, "sendMenu: " + gson.toJson(orderItems));
        Call<SuccessOrNot> call = service.sendRequestFcm("admin", gson.toJson(orderItems));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SuccessOrNot> call, @NonNull Response<SuccessOrNot> response) {
                Log.d(TAG, "onResponse sendMenu: " + response.body());
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse sendMenu :" + response.body().getResult());
                    callback.onResultReceived(response.body().getResult());

                } else {
                    Log.d(TAG, "onResponse sendMenu is not successful");
                    callback.onResultReceived("isNotSuccessful");
                }

            }

            @Override
            public void onFailure(@NonNull Call<SuccessOrNot> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure sendMenu: " + t.getMessage());
                callback.onResultReceived(t.getMessage());
            }
        });
    }

    public void usingTableUpdate(String to, String tableName, String request) {
        Map<String, String> data = new HashMap<>();
        data.put("request", request);
        data.put("tableName", tableName);
        Log.d(TAG, "usingTableUpdate data: " + gson.toJson(data));

        Call<SuccessOrNot> call = service.sendRequestFcm(to, gson.toJson(data));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SuccessOrNot> call, @NonNull Response<SuccessOrNot> response) {
                Log.d(TAG, "onResponse usingTableUpdate: " + response.body().getResult());
            }

            @Override
            public void onFailure(@NonNull Call<SuccessOrNot> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure usingTableUpdate: " + t.getMessage());
            }
        });
    }

    public void sendGift(String to, String from, String menuItem, String count){
        Log.d(TAG, "sendGift to: " + to);
        Map<String, String> data = new HashMap<>();
        data.put("tableName", from);
        data.put("request", "Gift");
        data.put("menuItem", menuItem);
        data.put("count", count);

        Log.d(TAG, "sendGift: " + gson.toJson(data));

        Call<SuccessOrNot> call = service.sendRequestFcm(to, gson.toJson(data));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SuccessOrNot> call,
                                   @NonNull Response<SuccessOrNot> response) {
                Log.d(TAG, "Send Gift :" + response.body().getResult());
            }

            @Override
            public void onFailure(@NonNull Call<SuccessOrNot> call,
                                  @NonNull Throwable t) {
                Log.d(TAG, "Send Gift onFailure :" + t.getMessage());
            }
        });
    }

    public void notifyIsGiftAccept(String to, String from, String menuItem , String count, boolean isAccept){

        Map<String, String> data = new HashMap<>();

        if(isAccept){
            data.put("tableName", from);
            data.put("request", "IsGiftAccept");
            data.put("menuItem", menuItem);
            data.put("count", count);
            data.put("isAccept", "true");
        }else{
            data.put("tableName", from);
            data.put("request", "IsGiftAccept");
            data.put("isAccept", "false");
        }


        Call<SuccessOrNot> call = service.sendRequestFcm(to, gson.toJson(data));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SuccessOrNot> call,
                                   @NonNull Response<SuccessOrNot> response) {
                Log.d(TAG, "Is Gift Accept:" + response.body().getResult());
            }

            @Override
            public void onFailure(@NonNull Call<SuccessOrNot> call,
                                  @NonNull Throwable t) {
                Log.d(TAG, "Is Gift Accept onFailure :" + t.getMessage());
            }
        });

    }

}
