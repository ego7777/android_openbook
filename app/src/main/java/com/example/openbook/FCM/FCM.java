package com.example.openbook.FCM;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.openbook.Activity.Admin;
import com.example.openbook.Activity.AdminPaymentBeforePopup;
import com.example.openbook.Activity.AdminPaymentAfterPopup;
import com.example.openbook.Activity.PaymentSelect;
import com.example.openbook.Activity.PopUpChatting;

import com.example.openbook.Chatting.DBHelper;
import com.example.openbook.Data.MyData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class FCM extends FirebaseMessagingService {

    String TAG = "FCM";
    String get_id;
    int requestCode;
    Handler mHandler = new Handler(Looper.getMainLooper());


    @Override
    protected Intent getStartCommandIntent(Intent originalIntent) {

        get_id = originalIntent.getStringExtra("get_id");

        Task<String> token = FirebaseMessaging.getInstance().getToken();
        token.addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: " + get_id);
                    saveToken(get_id, task.getResult());
                }
            }
        });

        return super.getStartCommandIntent(originalIntent);
    }


    public void saveToken(String id, String token) {
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference conditionRef = mRootRef.child(id);

        UserData userData = new UserData();
        userData.userID = id;
        userData.fcmToken = token;
        Log.d(TAG, "saveToken: " + token);

        conditionRef.setValue(userData);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // 푸시메시지 수신시 할 작업을 작성
        Log.d(TAG, "From: " + message.getData());

        // Check if message contains a notification payload.
        if (message.getData().size() > 0) {
            handleDataMessage(message.getData());
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void handleDataMessage(Map<String, String> data) {
        if (data.containsKey("title")) {
            Log.d(TAG, "handleDataMessage: " + data);

            // 채팅 요청 처리 메소드 호출
            handleChatRequest(data);
        } else if (data.containsKey("gender")) {

            String gender = data.get("gender");
            String guestNumber = data.get("guestNumber");
            String tableName = data.get("tableName");

            // admin page table 정보 업데이트 처리 메소드 호출
            handleAdminTableInformation(gender, guestNumber, tableName);
        } else if (data.containsKey("tableStatement")) {
            String tableStatement = data.get("tableStatement");
            String tableNumber = data.get("tableNumber");

            // admin page 선불좌석 표시 처리 메소드 호출
            handleAdminPaymentBefore(tableStatement, tableNumber);
        } else if (data.containsKey("menuName")) {
            String tableName = data.get("tableName");
            String menuName = data.get("menuName");
            String item = data.get("item");

            // admin page 메뉴 표시 처리 메소드 호출
            handleAdminTableMenu(tableName, menuName, item);
        } else if (data.containsKey("action")) {
            String tableName = data.get("tableName");

            saveChattingData(tableName);
        }
    }


    public void handleAdminTableMenu(String tableName, String menuSummary, String item) {
        int totalPrice = 0;

        try {
            JSONArray jsonArray = new JSONArray(item);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                totalPrice = totalPrice + jsonObject.getInt("price");

            }
            Log.d(TAG, "adminTableMenu totalPrice: " + totalPrice);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, AdminPaymentAfterPopup.class);
        intent.putExtra("menuSummary", menuSummary);
        intent.putExtra("totalPrice", totalPrice);
        intent.putExtra("totalMenuList", item);
        intent.putExtra("tableName", tableName);

        requestCode = (int) System.currentTimeMillis();

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }


    }

    public void handleAdminTableInformation(String gender, String guestNumber, String tableName) {
        Intent intent = new Intent(this, Admin.class);
        intent.putExtra("gender", gender);
        intent.putExtra("guestNumber", guestNumber);
        intent.putExtra("tableName", tableName);


        requestCode = (int) System.currentTimeMillis();

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

    }


    public void handleAdminPaymentBefore(String statement, String tableName) {
        Intent intent = new Intent(this, AdminPaymentBeforePopup.class);
        intent.putExtra("tableStatement", statement);
        intent.putExtra("tableName", tableName);
        Log.d(TAG, "tableStatement: " + statement);

        requestCode = (int) System.currentTimeMillis();

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void handleChatRequest(Map<String, String> data) {
        //popupActivity를 만들어서 띄우자

        //broadcast로 넘겨줘서
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("title", data.get("title"));
            jsonObject.put("body", data.get("body"));
            jsonObject.put("ticket", data.get("ticket"));
            jsonObject.put("clickTable", data.get("clickTable"));

            Intent intent = new Intent("chattingRequestArrived");
            intent.putExtra("fcmData", jsonObject.toString());
            LocalBroadcastManager.getInstance(FCM.this).sendBroadcast(intent);

        }catch (JSONException e){
            Log.d(TAG, "e: " + e);
        }

        //여기서 거절을 하면 앞으로 채팅 요청 못하게 만들어야해...!

//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//
//        try {
//            pendingIntent.send();
//
//        } catch (PendingIntent.CanceledException e) {
//            e.printStackTrace();
//        }

    }


    public void saveChattingData(String tableName) {
        int version = 2;

        DBHelper dbHelper = new DBHelper(FCM.this, version);
        version++;

        String chatData = dbHelper.chattingJson(tableName);
        Log.d(TAG, "saveChattingData chatData: " + chatData);

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("tableName", tableName)
                .add("chatData", chatData)
                .build();

        Request request = new Request.Builder()
                .url("http://3.36.255.141/SaveChattingData.php")
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

                //저장되면 삭제
                if(responseBody.equals("success")){
                    dbHelper.deleteTableData(tableName, "chattingTable", "sender");

                    //하고 PaymentSelect.java로 이동....!해야해...!
                    Intent intent = new Intent(FCM.this, PaymentSelect.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyData myData = new MyData(tableName, 0, null, false, false);
                    intent.putExtra("myData", myData);

                    startActivity(intent);
                }
            }
        });

    }


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "onNewToken: " + token);

        //이 기기의 토큰이 바뀌었을 때 할 작업을 작성,
        // onNewToken에서는 서버로 변경된 키값을 전달
    }


}
