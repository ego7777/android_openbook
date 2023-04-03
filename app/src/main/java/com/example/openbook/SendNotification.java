package com.example.openbook;

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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendNotification {
    String TAG = "SendNotification";

    String SERVER_KEY = "AAAAeTDfX_4:APA91bE0yKHvTCbVRoytfsstBn8XP9DzKdqnAFosy9HuClDrsMADaYASreReO5ra_YDdOPPiBpkE05GqaR0ULWupUbB_nNCUXftjsO7VVAVafdcGUK0Qn6HnQOBSV9BMriJshN1eQ9KF";

    public void requestChatting(String clickTable, String get_id) {
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.child(clickTable).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pushToken = (String) snapshot.getValue();

//                Log.d(TAG, "pushToken: " + pushToken);


                try {
                    JSONObject message = new JSONObject();
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();

                    notification.put("title", get_id);
                    notification.put("body", get_id + "에서 채팅을 요청하였습니다.");
//                    root.put("to", pushToken);
//                    root.put("project_id", "520510988286");
                    root.put("notification", notification);
//                    message.put("message", root);

//                    Log.d(TAG, "request :" + root.toString());


                    RequestBody formBody = new FormBody.Builder().
                            add("to",pushToken).
                            add("notification", notification.toString()).
                            build();

                    Request httpRequest = new Request.Builder()
                            .url("http://3.36.255.141/fcmPush.php")
                            .addHeader("Authorization", "key=" + SERVER_KEY)
                            .post(formBody)
                            .build();


                    OkHttpClient okHttpClient = new OkHttpClient();


                    okHttpClient.newCall(httpRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.d(TAG, "onFailure: " + e);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            Log.d(TAG, "response: \n" + response);

                            Log.d(TAG, "body: " + response.body().string());
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


}
