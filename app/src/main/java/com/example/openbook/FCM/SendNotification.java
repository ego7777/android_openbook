package com.example.openbook.FCM;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendNotification {
    String TAG = "SendNotificationTAG";

    OkHttpClient okHttpClient = new OkHttpClient();

    String SERVER_KEY = "AAAAeTDfX_4:APA91bE0yKHvTCbVRoytfsstBn8XP9DzKdqnAFosy9HuClDrsMADaYASreReO5ra_YDdOPPiBpkE05GqaR0ULWupUbB_nNCUXftjsO7VVAVafdcGUK0Qn6HnQOBSV9BMriJshN1eQ9KF";
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

                    RequestBody formBody = new FormBody.Builder()
                            .add("to", pushToken)
                            .add("notification", notification.toString())
                            .build();

                    Request request = new Request.Builder()
                            .url("http://3.36.255.141/fcmPush.php")
                            .addHeader("Authorization", "key=" + SERVER_KEY)
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
                            Log.d(TAG, "onResponse: "  + responseBody);
                        }
                    });

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

                    RequestBody formBody = new FormBody.Builder().
                            add("to", pushToken).
                            add("notification", notification.toString()).
                            add("clickTable", clickTable).
                            build();

                    Request httpRequest = new Request.Builder()
                            .url("http://3.36.255.141/fcmPush.php")
                            .addHeader("Authorization", "key=" + SERVER_KEY)
                            .post(formBody)
                            .build();


                    okHttpClient.newCall(httpRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.d(TAG, "chatting onFailure: " + e);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            Log.d(TAG, "chatting body: " + response.body().string());
                        }
                    });


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

        mRootRef.child("admin").child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pushToken = (String) snapshot.getValue();

                RequestBody formBody = new FormBody.Builder().
                        add("to", pushToken).
                        add("notification", menujArray).
                        build();

                Request httpRequest = new Request.Builder()
                        .url("http://3.36.255.141/fcmPush.php")
                        .addHeader("Authorization", "key=" + SERVER_KEY)
                        .post(formBody)
                        .build();

                okHttpClient.newCall(httpRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.d(TAG, "menu onFailure: " + e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        Log.d(TAG, "menu body: " + response.body().string());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error);
            }
        });

    }

    public void usingTable(String tableID, String statement, int identifier) {

        mRootRef.child("admin").child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pushToken = (String) snapshot.getValue();

                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("tableNumber", tableID);

                    if (statement.equals("사용")) {
                        Log.d(TAG, "사용 identifier: " + identifier);
                        jsonObject.put("tableStatement", "선불 이용 좌석");
                        jsonObject.put("tableIdentifier", identifier);


                    } else if (statement.equals("종료")) {
                        Log.d(TAG, "종료 identifier: " + identifier);
                        jsonObject.put("tableStatement", "");
                        jsonObject.put("tableIdentifier", identifier);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                RequestBody formBody = new FormBody.Builder().
                        add("to", pushToken).
                        add("notification", jsonObject.toString()).
                        build();

                Request httpRequest = new Request.Builder()
                        .url("http://3.36.255.141/fcmPush.php")
                        .addHeader("Authorization", "key=" + SERVER_KEY)
                        .post(formBody)
                        .build();

                okHttpClient.newCall(httpRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.d(TAG, "onFailure: " + e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        Log.d(TAG, "body: " + response.body().string());
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error);
            }
        });
    }


}
