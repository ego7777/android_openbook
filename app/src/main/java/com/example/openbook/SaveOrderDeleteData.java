package com.example.openbook;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SuccessOrNot;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class SaveOrderDeleteData {

    String TAG = "OrderSaveTAG";

    boolean success;
    RetrofitService service;
    RetrofitManager retrofitManager = new RetrofitManager();
    Retrofit retrofit;
    Gson gson;

    public void saveOrderDetailsOnServer(String tid,
                          String approvedAt,
                          int totalAmount,
                          int identifier,
                          int paymentMethodType,
                          String orderList) {

        retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        service = retrofit.create(RetrofitService.class);


        Call<SuccessOrNot> call = service.savePayment(tid,
                approvedAt, totalAmount, identifier, paymentMethodType, orderList);

        call.enqueue(new Callback<SuccessOrNot>() {
            @Override
            public void onResponse(@NonNull Call<SuccessOrNot> call, @NonNull Response<SuccessOrNot> response) {
                Log.d(TAG, "onResponse saveOrderDetailsOnServer: " + response);
                if(response.isSuccessful()){
                    Log.d(TAG, "onResponse saveOrderDetailsOnServer is not successful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessOrNot> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure saveOrderDetailsOnServer: " + t.getMessage());
            }
        });


    }


//    public void deleteServerData(String tableName){
//        //해당 테이블의 tableInfo를 지운다
//        OkHttpClient okHttpClient = new OkHttpClient();
//        Log.d(TAG, "deleteServerData: 1");
//
//        RequestBody formBody = new FormBody.Builder()
//                .add("tableName", tableName)
//                .build();
//
//        Request request = new Request.Builder()
//                .url("http://3.36.255.141/DeleteTableInfo.php")
//                .post(formBody)
//                .build();
//
//        okHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                Log.d(TAG, "Delete onFailure: " + e);
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                String responseBody = response.body().string();
//                Log.d(TAG, "Delete onResponse: " + responseBody);
//
//                if(responseBody.equals("")){
//
//                }
//
//                SendNotification sendNotification = new SendNotification();
//                sendNotification.saveChatting(tableName);
//
//
//            }
//        });
//
//    }
}
