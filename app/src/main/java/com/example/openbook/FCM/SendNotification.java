package com.example.openbook.FCM;

import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.openbook.BuildConfig;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SuccessOrNot;
import com.example.openbook.retrofit.TokenDTO;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SendNotification {
    String TAG = "SendNotificationTAG";

    RetrofitService service;
    RetrofitManager retrofitManager = new RetrofitManager();
    Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);

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



    public void sendMenu(String menujArray) {

        service = retrofit.create(RetrofitService.class);

        Call<TokenDTO> call = service.requestFcmToken("admin");
        call.enqueue(new Callback<TokenDTO>() {
            @Override
            public void onResponse(Call<TokenDTO> call, Response<TokenDTO> response) {
                if(response.isSuccessful()){
                    switch (response.body().getResult()){
                        case "success" :
                            String token = response.body().getToken();
                            Log.d(TAG, "onResponse token: " + token);
                            Log.d(TAG, "onResponse menuArray: " + menujArray);
                            sendRequestFcm(token, menujArray);
                            break;

                        case "failed" :
                            Log.d(TAG, "onResponse get token failed");
                            break;

                    }
                }else{
                    Log.d(TAG, "onResponse get token is not successful");
                }
            }

            @Override
            public void onFailure(Call<TokenDTO> call, Throwable t) {

            }
        });



        mRootRef.child("admin").child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pushToken = (String) snapshot.getValue();

//                RequestBody formBody = new FormBody.Builder().
//                        add("to", pushToken).
//                        add("notification", menujArray).
//                        build();
//
//                Request httpRequest = new Request.Builder()
//                        .url("http://3.36.255.141/fcmPush.php")
//                        .addHeader("Authorization", "key=" + SERVER_KEY)
//                        .post(formBody)
//                        .build();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error);
            }
        });

    }

    public void usingTableUpdate(String tableName, String request, String paymentType) {

        service = retrofit.create(RetrofitService.class);

        Call<TokenDTO> call = service.requestFcmToken("admin");
        call.enqueue(new Callback<TokenDTO>() {
            @Override
            public void onResponse(Call<TokenDTO> call, Response<TokenDTO> response) {
                Log.d(TAG, "onResponse get token : " + response.body().getToken());
                if(response.isSuccessful()){
                    switch (response.body().getResult()){
                        case "success" :
                            String token = response.body().getToken();
                            Map<String, String> data = new HashMap<>();
                            data.put("request", request);
                            data.put("tableName", tableName);
                            data.put("paymentType", String.valueOf(paymentType));

                            sendRequestFcm(token, data.toString());
                            break;

                        case "failed" :
                            Log.d(TAG, "onResponse get token failed");
                            break;

                    }
                }else{
                    Log.d(TAG, "onResponse get token is not successful");
                }

            }

            @Override
            public void onFailure(Call<TokenDTO> call, Throwable t) {
                Log.d(TAG, "onFailure get token: " + t.getMessage());
            }
        });


//        mRootRef.child("admin").child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String pushToken = (String) snapshot.getValue();
//
//                JSONObject jsonObject = new JSONObject();
//
//                try {
//                    jsonObject.put("tableNumber", tableID);
//
//                    if (statement.equals("사용")) {
//                        Log.d(TAG, "사용 identifier: " + identifier);
//                        jsonObject.put("tableStatement", "선불 이용 좌석");
//                        jsonObject.put("tableIdentifier", identifier);
//
//
//                    } else if (statement.equals("종료")) {
//                        Log.d(TAG, "종료 identifier: " + identifier);
//                        jsonObject.put("tableStatement", "");
//                        jsonObject.put("tableIdentifier", identifier);
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//
//                RequestBody formBody = new FormBody.Builder().
//                        add("to", pushToken).
//                        add("notification", jsonObject.toString()).
//                        build();
//
//                Request httpRequest = new Request.Builder()
//                        .url("http://3.36.255.141/fcmPush.php")
//                        .addHeader("Authorization", "key=" + SERVER_KEY)
//                        .post(formBody)
//                        .build();
//
//                okHttpClient.newCall(httpRequest).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                        Log.d(TAG, "onFailure: " + e);
//                    }
//
//                    @Override
//                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                        Log.d(TAG, "body: " + response.body().string());
//                    }
//                });
//
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.d(TAG, "onCancelled: " + error);
//            }
//        });
    }

    public void sendRequestFcm(String token, String data){

        Call<SuccessOrNot> call = service.sendRequestFcm(BuildConfig.FCM_SERVER_KEY, token, data.toString());

        call.enqueue(new Callback<SuccessOrNot>() {
            @Override
            public void onResponse(Call<SuccessOrNot> call, Response<SuccessOrNot> response) {
                Log.d(TAG, "onResponse sendRequestFcm: " + response.body().getResult());
            }

            @Override
            public void onFailure(Call<SuccessOrNot> call, Throwable t) {
                Log.d(TAG, "onFailure sendRequestFcm: " + t.getMessage());
            }
        });
    }


}
