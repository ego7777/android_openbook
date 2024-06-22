package com.example.openbook.FCM;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.openbook.Activity.Admin;

import com.example.openbook.BuildConfig;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SuccessOrNot;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;


import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class FCM extends FirebaseMessagingService {

    String TAG = "FcmTAG";
    int identifier;
    int requestCode;

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

        switch (data.get("request")){
            case "PayNow" :
            case "End" :
            case "PayLater":
                handleAdminPaymentType(data.get("request"), data.get("tableName"));
                break;

            case "CompletePayment":
                handleCompletePayment();
                break;

            case "Order" :
            case "GiftMenuOrder":
                handleAdminTableMenu(data);
                break;

            case "Gift" :
                handleGiftOtherTable
                        (data.get("tableName"),
                        data.get("menuItem"),
                        data.get("count"));
                break;

            case "IsGiftAccept" :
                handleIsGiftAccept
                        (data.get("tableName"),
                        Boolean.parseBoolean(data.get("isAccept")),
                                data.get("meuItem"));
                break;
        }

    }

    public void handleCompletePayment(){
        Intent intent = new Intent("CompletePayment");
        intent.putExtra("isCompletePayment", true);
        LocalBroadcastManager.getInstance(FCM.this).sendBroadcast(intent);
    }

    public void handleIsGiftAccept(String from, boolean isAccept, String menuItem){
        Log.d(TAG, "handleIsGiftAccept 호출");
        Intent intent = new Intent("isGiftAccept");
        intent.putExtra("from", from);

        if(isAccept){
            intent.putExtra("isAccept", true);
            intent.putExtra("menuItem", menuItem);
        }else{
            intent.putExtra("isAccept", false);
        }
        LocalBroadcastManager.getInstance(FCM.this).sendBroadcast(intent);
    }

    public void handleGiftOtherTable(String from, String menuItem, String count){
        Log.d(TAG, "handleGiftOtherTable 호출");
        Intent intent = new Intent("giftArrived");
        intent.putExtra("from", from);
        intent.putExtra("menuItem", menuItem);
        intent.putExtra("count", count);
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


    @RequiresApi(api = Build.VERSION_CODES.Q)
//    public void handleChatRequest(Map<String, String> data) {
//        //popupActivity를 만들어서 띄우자
//
//        //broadcast로 넘겨줘서
//        try{
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("title", data.get("title"));
//            jsonObject.put("body", data.get("body"));
//            jsonObject.put("ticket", data.get("ticket"));
//            jsonObject.put("clickTable", data.get("clickTable"));
//
//            Intent intent = new Intent("chattingRequestArrived");
//            intent.putExtra("fcmData", jsonObject.toString());
//            LocalBroadcastManager.getInstance(FCM.this).sendBroadcast(intent);
//
//        }catch (JSONException e){
//            Log.d(TAG, "e: " + e);
//        }
//
//        //여기서 거절을 하면 앞으로 채팅 요청 못하게 만들어야해...!
//
////        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
////
////        try {
////            pendingIntent.send();
////
////        } catch (PendingIntent.CanceledException e) {
////            e.printStackTrace();
////        }
//
//    }


//    public void saveChattingData(String tableName) {
//        int version = 2;
//
//        DBHelper dbHelper = new DBHelper(FCM.this, version);
//        version++;
//
//        String chatData = dbHelper.chattingJson(tableName);
//        Log.d(TAG, "saveChattingData chatData: " + chatData);
//
//        OkHttpClient okHttpClient = new OkHttpClient();
//
//        RequestBody formBody = new FormBody.Builder()
//                .add("tableName", tableName)
//                .add("chatData", chatData)
//                .build();
//
//        Request request = new Request.Builder()
//                .url("http://3.36.255.141/SaveChattingData.php")
//                .post(formBody)
//                .build();
//
//        okHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                Log.d(TAG, "onFailure: " + e);
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                String responseBody = response.body().string();
//                Log.d(TAG, "onResponse: " + responseBody);
//
//                //저장되면 삭제
//                if(responseBody.equals("success")){
//                    dbHelper.deleteTableData(tableName, "chattingTable", "sender");
//
//                    //하고 PaymentSelect.java로 이동....!해야해...!
//                    Intent intent = new Intent(FCM.this, PaymentSelect.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    MyData myData = new MyData(tableName, 0, null, false, false, 0);
//                    intent.putExtra("myData", myData);
//
//                    startActivity(intent);
//                }
//            }
//        });
//
//    }


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "onNewToken: " + token);

        //이 기기의 토큰이 바뀌었을 때 할 작업을 작성,
        // onNewToken에서는 서버로 변경된 키값을 전달
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }
}
