package com.example.openbook;

import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;

import java.io.IOException;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TableQuantity {
    String TAG = "TableQuantityTAG";


    int tableQuantity;

    public void getTableQuantity(Callback<TableListDTO> callback)  {
        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        RetrofitService service = retrofit.create(RetrofitService.class);

        Call<TableListDTO> call = service.getTableList(BuildConfig.ADMIN_KEY);
        call.enqueue(callback);
    }


    public void setTableQuantity(int tableQuantity) {



    }
}
