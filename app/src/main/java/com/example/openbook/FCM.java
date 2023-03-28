package com.example.openbook;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.openbook.Chatting.ChattingUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;


public class FCM extends FirebaseMessagingService{

    String TAG = "FCM";
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    String SERVER_KEY = "AAAAeTDfX_4:APA91bE0yKHvTCbVRoytfsstBn8XP9DzKdqnAFosy9HuClDrsMADaYASreReO5ra_YDdOPPiBpkE05GqaR0ULWupUbB_nNCUXftjsO7VVAVafdcGUK0Qn6HnQOBSV9BMriJshN1eQ9KF";
    public FCM() {
        super();

        Task<String> token = FirebaseMessaging.getInstance().getToken();
        token.addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful()){

                }
            }
        });
    }




    public void saveToken(String id, Task<String> task){
//        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, "mRootRef : " + mRootRef);
        DatabaseReference conditionRef = mRootRef.child(id);


        UserData userData = new UserData();
        userData.userID = id;
        userData.fcmToken = task.getResult();

        conditionRef.setValue(userData);
    }







    public void chattingRequest(String clickTable, String get_id){
//        Log.d(TAG, "chattingRequest : 여기까지는 넘어오니..?" + clickTable );
        mRootRef.child(clickTable).child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pushToken = (String) snapshot.getValue();

//                Log.d(TAG, "onDataChange: " + pushToken);

                try {
                    JSONObject message = new JSONObject();
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();

                    notification.put("title", get_id);
                    notification.put("body", get_id+"에서 채팅을 요청하였습니다.");
                    root.put("token", pushToken);
                    root.put("notification", notification);
                    message.put("message", root);

//                    Log.d(TAG, "request :" + message.toString());

                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody body = RequestBody.create(message.toString(),JSON);



                    RequestBody formBody = new FormBody.Builder().
                            add("message", message.toString()).build();

                    Request httpRequest = new Request.Builder()
                            .url("https://fcm.googleapis.com/fcm/send")
                            .header("Authorization", "key=" + SERVER_KEY)
                            .post(formBody)
                            .build();

//                    Request httpRequest = new Request.Builder()
//                            .url("https://fcm.googleapis.com/fcm/send")
//                            .addHeader("Authorization", "key=" + SERVER_KEY)
//                            .addHeader("Accept", "application/json")
//                            .addHeader("Content-type", "application/json")
//                            .post(formBody)
//                            .build();



                    Log.d(TAG, "httpRequest :\n" + httpRequest);

                    OkHttpClient okHttpClient = new OkHttpClient();

                    okHttpClient.newCall(httpRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.d(TAG, "onFailure: " + e);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            Log.d(TAG, "onResponse: \n" + response);
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    private RemoteViews getCustomDesign(String title, String message) {
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.noti_title, title);
        remoteViews.setTextViewText(R.id.noti_message, message);
        remoteViews.setImageViewResource(R.id.logo, R.drawable.heart);
        return remoteViews;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // 푸시메시지 수신시 할 작업을 작성

        Log.d(TAG, "From: " + message.getFrom());

        // Check if message contains a notification payload.
        if (message.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + message.getNotification().getBody());
            Log.d(TAG, "Message Notification Title : "  + message.getNotification().getTitle());
            showNotification(message.getNotification().getTitle(),
                    message.getNotification().getBody());
        }



//        if (message.getData().size() > 0) {
//            showNotification(message.getData().get("title"),
//                    message.getData().get("body"));
//        }

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotification(String title, String message){
        //팝업 터치시 이동할 액티비티를 지정합니다.


        Intent intent = new Intent(this, ChattingUI.class);
        //알림 채널 아이디 : 본인 하고싶으신대로...
        String channel_id = "openBook_fcm";
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setSmallIcon(R.drawable.heart)
                .setAutoCancel(true)
                .setOnlyAlertOnce(false)
                .setContentIntent(pendingIntent);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN){
            builder = builder.setContent(getCustomDesign(title, message));
        }else{
            builder = builder.setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.heart);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //알람 채널이 필요한 안드로이드 버전을 위한 코드

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, "OpenBook_FCM", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        //알람 표시
        notificationManager.notify(0, builder.build());


    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        //이 기기의 토큰이 바뀌었을 때 할 작업을 작성,
        // onNewToken에서는 서버로 변경된 키값을 전달
    }

    public void RequestChatting(){

    }
}
