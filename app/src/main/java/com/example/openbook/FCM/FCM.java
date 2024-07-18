package com.example.openbook.FCM;


import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.openbook.BuildConfig;
import com.example.openbook.Data.CartList;
import com.example.openbook.ManageOrderItems;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SuccessOrNot;


import com.google.common.reflect.TypeToken;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class FCM extends FirebaseMessagingService {

    String TAG = "FcmTAG";

    RetrofitService service;
    Gson gson = new Gson();

    public void getToken(int identifier) {

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.d(TAG, "Fetching FCM Register token failed: " + task.getException());
                return;
            }
            String token = task.getResult();
            Log.d(TAG, "onComplete token: " + token);
            saveToken(identifier, token);

        });

    }


    public void saveToken(int identifier, String token) {

        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        service = retrofit.create(RetrofitService.class);

        Call<SuccessOrNot> call = service.saveFcmToken(identifier, token);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SuccessOrNot> call, @NonNull Response<SuccessOrNot> response) {
                Log.d(TAG, "onResponse save fcm token: " + response.body().getResult());
            }

            @Override
            public void onFailure(@NonNull Call<SuccessOrNot> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure save fcm token: " + t.getMessage());
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG, "fcm message: " + message.getData());

        if (message.getData().size() > 0) {
            handleDataMessage(message.getData());
        }

    }


    private void handleDataMessage(Map<String, String> data) {

        switch (data.get("request")) {
            case "PayNow":
            case "End":
            case "PayLater":
                String tableName = data.get("tableName");
                handleAdminPaymentType(data.get("request"), tableName);
                break;

            case "CompletePayment":
                handleCompletePayment(data.get("tid"));
                break;

            case "Order":
            case "GiftMenuOrder":
                handleAdminTableMenu(data);
                break;

            case "Gift":
                tableName = data.get("tableName");
                handleGiftOtherTable(tableName, data.get("menuItem"));
                break;

            case "IsGiftAccept":
                String menuItem = data.get("menuItem");
                boolean isAccept = Boolean.parseBoolean(data.get("isAccept"));
                String profileName = data.get("profile");
                tableName = data.get("tableName");

                handleIsGiftAccept(tableName, isAccept, menuItem, profileName);
                break;
        }

    }

    public void handleCompletePayment(String tid) {
        Intent intent = new Intent("CompletePayment");
        intent.putExtra("tid", tid);
        LocalBroadcastManager.getInstance(FCM.this).sendBroadcast(intent);
    }

    public void handleIsGiftAccept(String from, boolean isAccept, String menuItem, String profileName) {
        ManageOrderItems manageOrderItems;

        Intent intent = new Intent("isGiftAccept");
        intent.putExtra("from", from);

        if (isAccept) {
            intent.putExtra("isAccept", true);
            Type type = new TypeToken<ArrayList<CartList>>() {}.getType();
            ArrayList<CartList> orderedList = gson.fromJson(menuItem, type);

            manageOrderItems = new ManageOrderItems();
            manageOrderItems.saveOrderedItems(this, orderedList);

            if(profileName != null && !profileName.isBlank()){
                manageOrderItems.updateProfileGift(this, profileName, 2000);
            }

        } else {
            intent.putExtra("isAccept", false);
        }

        LocalBroadcastManager.getInstance(FCM.this).sendBroadcast(intent);
    }

    public void handleGiftOtherTable(String from, String menuItem) {
        Intent intent = new Intent("giftArrived");
        intent.putExtra("from", from);
        intent.putExtra("menuItem", menuItem);
        LocalBroadcastManager.getInstance(FCM.this).sendBroadcast(intent);
    }


    public void handleAdminTableMenu(Map<String, String> data) {
        Intent intent = new Intent("tableRequest");
        intent.putExtra("fcmData", gson.toJson(data));

        LocalBroadcastManager.getInstance(FCM.this).sendBroadcast(intent);
    }

    public void handleAdminPaymentType(String request, String tableName) {
        Intent intent = new Intent("tableRequest");
        Map<String, String> tableRequest = new HashMap<>();
        tableRequest.put("request", request);
        tableRequest.put("tableName", tableName);

        intent.putExtra("fcmData", gson.toJson(tableRequest));

        LocalBroadcastManager.getInstance(FCM.this).sendBroadcast(intent);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }
}
