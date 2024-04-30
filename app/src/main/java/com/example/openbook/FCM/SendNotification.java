package com.example.openbook.FCM;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.openbook.BuildConfig;
import com.example.openbook.SendMenuCallback;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SuccessOrNot;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

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

    Gson gson = new Gson();

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();


    public void saveChatting(String tableName){
        mRootRef.child(tableName).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pushToken = (String) snapshot.getValue();

                try{
                    JSONObject notification = new JSONObject();

                    notification.put("tableName", tableName);
                    notification.put("action", "delete");

//                    RequestBody formBody = new FormBody.Builder()
//                            .add("to", pushToken)
//                            .add("notification", notification.toString())
//                            .build();
//
//                    Request request = new Request.Builder()
//                            .url("http://3.36.255.141/fcmPush.php")
//                            .addHeader("Authorization", "key=" + SERVER_KEY)
//                            .post(formBody)
//                            .build();
//
//                    okHttpClient.newCall(request).enqueue(new Callback() {
//                        @Override
//                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                            Log.d(TAG, "onFailure: " + e);
//                        }
//
//                        @Override
//                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                            String responseBody = response.body().string();
//                            Log.d(TAG, "onResponse: "  + responseBody);
//                        }
//                    });

                }catch (JSONException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void requestChatting(String clickTable, String get_id, String ticket, String message) {

        mRootRef.child(clickTable).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pushToken = (String) snapshot.getValue();

                try {

                    JSONObject notification = new JSONObject();

                    notification.put("title", get_id);
                    notification.put("body", get_id + message);
                    notification.put("clickTable", clickTable);
                    notification.put("ticket", ticket);

//                    RequestBody formBody = new FormBody.Builder().
//                            add("to", pushToken).
//                            add("notification", notification.toString()).
//                            add("clickTable", clickTable).
//                            build();
//
//                    Request httpRequest = new Request.Builder()
//                            .url("http://3.36.255.141/fcmPush.php")
//                            .addHeader("Authorization", "key=" + SERVER_KEY)
//                            .post(formBody)
//                            .build();
//
//
//                    okHttpClient.newCall(httpRequest).enqueue(new Callback() {
//                        @Override
//                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                            Log.d(TAG, "chatting onFailure: " + e);
//                        }
//
//                        @Override
//                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                            Log.d(TAG, "chatting body: " + response.body().string());
//                        }
//                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "onDataChange e: " + e);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error);
            }
        });

    }



    public void sendMenu(Map<String, String> orderItems, SendMenuCallback callback) {

        Call<SuccessOrNot> call = service.sendRequestFcm("admin", gson.toJson(orderItems));
        call.enqueue(new Callback<SuccessOrNot>() {
            @Override
            public void onResponse(Call<SuccessOrNot> call, Response<SuccessOrNot> response) {
                Log.d(TAG, "onResponse sendMenu: " + response.body());
                if(response.isSuccessful()){
                    Log.d(TAG, "onResponse sendMenu :" + response.body().getResult());
                    callback.onResultReceived(response.body().getResult());

                }else{
                    Log.d(TAG, "onResponse sendMenu is not successful");
                    callback.onResultReceived("isNotSuccessful");
                }

            }

            @Override
            public void onFailure(Call<SuccessOrNot> call, Throwable t) {
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
        call.enqueue(new Callback<SuccessOrNot>() {
            @Override
            public void onResponse(Call<SuccessOrNot> call, Response<SuccessOrNot> response) {
                Log.d(TAG, "onResponse usingTableUpdate: " + response.body().getResult());
            }

            @Override
            public void onFailure(Call<SuccessOrNot> call, Throwable t) {
                Log.d(TAG, "onFailure usingTableUpdate: " + t.getMessage());
            }
        });
    }

}
